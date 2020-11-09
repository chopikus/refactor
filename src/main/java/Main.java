import at.unisalzburg.dbresearch.apted.distance.APTED;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.DataKey;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.Pair;
import flanagan.math.Maximisation;
import picocli.CommandLine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


@CommandLine.Command(version = "Refactoring tool v.0.1", header = "%nRefactoring tool%n",
        description = "Helps to remove duplicates from your code.%n", mixinStandardHelpOptions = true)
public class Main implements Runnable{

    public static final DataKey<Integer> NODE_ID = new DataKey<>() {
    };
    public static final DataKey<String> SUBDIRECTORY = new DataKey<>() {
    };
    public static final DataKey<String> FILE_NAME = new DataKey<>() {
    };
    public static List<File> filesToParse = new ArrayList<>();
    public static Map<String, CompilationUnit> units = new HashMap<>();
    public static AtomicReference<Integer> nodeIDCounter = new AtomicReference<>(0);
    public static TypeSolver typeSolver = null;
    public static JavaParserFacade facade = null;
    public static List<Block> blocks = new ArrayList<>();
    public static long timeInMillisStart = -1;
    public static Integer minimumSegmentPieceCount = 3;
    public static List<NavigableSet<Integer>> badPieces = new ArrayList<>();
    public static APTED<Cost, NodeData> apted = new APTED<>(new Cost());
    public static List<Pair<Integer, Integer>> blockPieceIndexesToCompare = new ArrayList<>();
    public static List<List<List<Pair<Integer, Integer>>>> duplicatedSegments = new ArrayList<>();
    public static float duplicateLines = 0;
    public static float allLines = 0;

    @CommandLine.Parameters(paramLabel = "<file>", description = "what's the shit i need to parse?")
    public static File execFile;

    @CommandLine.Option(names = { "-t", "--threshold" }, defaultValue = "3f", description = "Threshold")
    public static float threshold = 3f;

    @CommandLine.Option(names = { "-p", "--path" }, defaultValue = "out", description = "Where to put the result?")
    public static String outputFolder = "";

    @CommandLine.Option(names = { "--max-do-parameter-count" }, defaultValue = "5", description = "Maximum amount of \"doXXX\" in new functions made by tool")
    public static int maxDoParametersCount = 5;

    @CommandLine.Option(names = { "--big-clone-eval"}, defaultValue = "false", description = "Print data to csv in \"BigCloneEval\" format")
    public static boolean bigCloneEval = false;
    static void countMemoryAndTime() {
        long timeInMillisEnd = System.currentTimeMillis();
        System.out.println("Execution time: ~" + (timeInMillisEnd - timeInMillisStart) + "ms");
        System.out.println("Memory usage: ~" +
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576 + " MB");
    }

    static void parseArgFile(File file) throws IOException {
        CombinedTypeSolver solver = new CombinedTypeSolver();
        solver.add(new ReflectionTypeSolver());
        if (file.isDirectory())
            solver.add(new JavaParserTypeSolver(file));
        typeSolver = solver;
        Files.walk(Paths.get(file.getAbsolutePath()))
                .filter(Files::isRegularFile)
                .filter(path -> {
                    File pFile = path.toFile();
                    return pFile.getName().
                            substring(pFile.getName().
                                    lastIndexOf('.') + 1).equals("java");
                })
                .forEach(path -> filesToParse.add(path.toFile()));
        for (File fileToParse : filesToParse)
            try {
                units.put(fileToParse.getAbsolutePath(), StaticJavaParser.parse(fileToParse));
            } catch (Exception e) {
                System.out.println("Could not parse file: " + fileToParse.getAbsolutePath());
                e.printStackTrace();
                System.exit(0);
            }
    }

    private static void setup() {
        timeInMillisStart = System.currentTimeMillis();
        try {
            parseArgFile(execFile);
        } catch (IOException e) {
            System.out.println("couldn't parse folder/file..\n");
            e.printStackTrace();
            System.exit(0);
        }
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        for (Map.Entry<String, CompilationUnit> entry : units.entrySet()) {
            File file = new File(entry.getKey());
            String subDirectory = Paths.get(execFile.getAbsolutePath()).relativize(Paths.get(file.getParentFile().getAbsolutePath())).toString();
            String name = file.getName();
            entry.getValue().walk(node -> {
                node.setData(Main.NODE_ID, nodeIDCounter.getAndSet(nodeIDCounter.get() + 1));
                node.setData(Main.SUBDIRECTORY, subDirectory);
                node.setData(Main.FILE_NAME, name);
            });
            symbolSolver.inject(entry.getValue());
            allLines+=entry.getValue().getRange().get().getLineCount();
        }
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
        facade = JavaParserFacade.get(typeSolver);
    }

    static void findCopiedPieces() {
        double[] start = {0};
        double[] step = new double[start.length];
        Arrays.fill(step, 100);
        double ftol = 0.0001;
        int maxIterations = 5000;
        Maximisation maximize = new Maximisation();
        SimilarityFunction function = new SimilarityFunction();
        for (int blockIndex=0; blockIndex<Main.blocks.size(); blockIndex++)
            function.preProcessUsagesAfterPossibleSegment(blockIndex);
        // above some bollocks for Nelder Mead algo
        badPieces.clear();
        for (int i = 0; i < blocks.size(); i++) badPieces.add(new TreeSet<>());
        for (int i = 0; i < blocks.size(); i++)
            for (int j = 0; j < blocks.get(i).list.size(); j++)
                if (blocks.get(i).list.get(j).dependentOnOtherBlocks)
                    badPieces.get(i).add(j);
        Map<Integer, List<Pair<Integer, Integer>>> piecesByHash = new TreeMap<>();
        int blockIndex = 0;
        for (Block block : blocks) {
            int pieceIndex = 0;
            for (Piece piece : block.list) {
                List<Pair<Integer, Integer>> list = piecesByHash.getOrDefault(piece.hash, new ArrayList<>());
                list.add(new Pair<>(blockIndex, pieceIndex));
                piecesByHash.put(piece.hash, list);
                pieceIndex++;
            }
            blockIndex++;
        }
        List<List<Pair<Integer, Integer>>> piecesByHashList = new ArrayList<>(piecesByHash.values());
        piecesByHashList.sort(new Utils.ListBlockIndexComparator());
        for (List<Pair<Integer, Integer>> blockPieceList : piecesByHashList) {
            Set<Integer> blockSet = new TreeSet<>();
            blockPieceIndexesToCompare.clear();
            for (Pair<Integer, Integer> blockPieceIndexes : blockPieceList) {
                int pairBlockIndex = blockPieceIndexes.a;
                int pairPieceIndex = blockPieceIndexes.b;
                if (badPieces.get(pairBlockIndex).contains(pairPieceIndex))
                    continue;
                if (!blockSet.contains(pairBlockIndex)) {
                    blockSet.add(pairBlockIndex);
                    blockPieceIndexesToCompare.add(blockPieceIndexes);
                }
            }
            int funcMaxConstraint = Integer.MIN_VALUE;
            for (Pair<Integer, Integer> blockPiece : blockPieceIndexesToCompare) {
                Integer nearestReplacePiece = badPieces.get(blockPiece.a).ceiling(blockPiece.b);
                if (nearestReplacePiece == null)
                    nearestReplacePiece = blocks.get(blockPiece.a).list.size();
                funcMaxConstraint = Math.max(funcMaxConstraint, nearestReplacePiece - blockPiece.b);
            }
            if (funcMaxConstraint < minimumSegmentPieceCount)
                continue;
            maximize.removeConstraints();
            maximize.addConstraint(0, -1, minimumSegmentPieceCount);
            maximize.addConstraint(0, 1, funcMaxConstraint);
            maximize.nelderMead(function, start, step, ftol, maxIterations);
            double argWithMaxRes = maximize.getParamValues()[0];
            long lenMaxRes = Math.round(argWithMaxRes);
            Set<Integer> blocksToReplacePieces = function.getBlocksToReplacePieces(lenMaxRes);
            if (blocksToReplacePieces.size() <= 1 && maximize.getMaximum()>0)
                continue;
            duplicatedSegments.add(new ArrayList<>());
            for (Pair<Integer, Integer> blockPiece : blockPieceIndexesToCompare) {
                if (blocksToReplacePieces.contains(blockPiece.a)) {
                    Utils.getLast(duplicatedSegments).add(new ArrayList<>());
                    for (Integer piece = blockPiece.b; piece < Math.min(blocks.get(blockPiece.a).list.size(),
                            blockPiece.b + lenMaxRes); piece++) {
                        Utils.getLast(Utils.getLast(duplicatedSegments)).add(new Pair<>(blockPiece.a, piece));
                        badPieces.get(blockPiece.a).add(piece);
                    }
                }
            }
        }
    }



    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        setup();
        System.out.println("Parsed code.");
        countMemoryAndTime();
        System.out.println();
        for (CompilationUnit cu : units.values()) {
            cu.findAll(BlockStmt.class).forEach(blockStmt -> {
                if (blockStmt.getParentNode().isPresent() && Piece.checkBranching(blockStmt.getParentNode().get()))
                    return;
                Block block = new Block(blockStmt);
                if (block.list.size()!=0)
                    blocks.add(block);
            });
        }
        findCopiedPieces();
        System.out.println("Found copied pieces");
        countMemoryAndTime();
        System.out.println();
        try {
            if (new File(outputFolder).exists() && !Utils.isDirEmpty(Path.of(outputFolder)))
            {
                System.out.println("Folder is not empty. Please remove all files from it.");
                System.exit(0);
            }
        } catch (IOException e) {
            System.out.println("couldn't check whether folder is empty!!!");
            System.exit(0);
            e.printStackTrace();
        }
        if (!bigCloneEval)
            Utils.writeToFile(Uniter.makeMethod(duplicatedSegments).toString(), new File(outputFolder+File.separator+"/Copied.java"));
        else {
            Uniter.exportBigEval(duplicatedSegments, new File(outputFolder + File.separator + "/out.csv"));
            countMemoryAndTime();
            return;
        }
        System.out.println("wrote method to Copied.java");
        countMemoryAndTime();
        System.out.println();
        Path execPath = Paths.get(execFile.getAbsolutePath());
        for (Map.Entry<String, CompilationUnit> pathUnit : units.entrySet()) {
            Path filePath = Paths.get(pathUnit.getKey());
            String rel = execPath.relativize(filePath).toString();
            if (rel.equals(""))
                rel = filePath.getFileName().toString();
            Utils.writeToFile(pathUnit.getValue().toString(), new File(outputFolder + File.separator + rel));
        }
        countMemoryAndTime();
        System.out.printf("%s lines are duplicate. That is about %s%% of all code", duplicateLines, (int)((duplicateLines/allLines)*10000)/100.0f);
    }
}