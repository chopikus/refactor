import at.unisalzburg.dbresearch.apted.distance.APTED;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.DataKey;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.Pair;
import flanagan.math.Maximisation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


public class Main {
    enum Bullshit{
        ONE, TWO, THREE
    }

    public static final DataKey<Integer> NODE_ID = new DataKey<>() {
    };
    public static List<File> filesToParse = new ArrayList<>();
    public static Map<String, CompilationUnit> units = new HashMap<>();
    public static AtomicReference<Integer> nodeIDCounter = new AtomicReference<>(0);
    public static File execFile;
    public static int lineCount = 0;
    public static TypeSolver typeSolver = null;
    public static JavaParserFacade facade = null;
    public static List<Block> blocks = new ArrayList<>();
    public static long timeInMillisStart = -1;
    public static boolean hashLiteralTypes = true;
    public static float threshold = 3f;
    public static Integer minimumSegmentPieceCount = 7;
    public static List<NavigableSet<Integer>> piecesToReplace = new ArrayList<>();
    public static APTED<Cost, NodeData> apted = new APTED<>(new Cost());
    public static List<Pair<Integer, Integer>> blockPieceIndexesToCompare = new ArrayList<>();
    public static List<List<List<Pair<Integer, Integer>>>> duplicatedSegments = new ArrayList<>();
    static void countMemoryAndTime() {
        long timeInMillisEnd = System.currentTimeMillis();
        System.out.println("Execution time: ~" + (timeInMillisEnd - timeInMillisStart) + "ms");
        System.out.println("Memory usage: ~" +
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576 + " MB");
    }

    static void parseArgsToUnits(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("You need to specify only path to directory!");
            System.exit(0);
        } else {
            File file = new File(args[0]);
            execFile = file;
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
    }

    private static void setup(String[] args) {
        timeInMillisStart = System.currentTimeMillis();
        try {
            parseArgsToUnits(args);
        } catch (IOException e) {
            System.out.println("couldn't parse folder/file..\n");
            e.printStackTrace();
            System.exit(0);
        }
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        for (Map.Entry<String, CompilationUnit> entry : units.entrySet()) {
            entry.getValue().walk(node -> node.setData(Main.NODE_ID, nodeIDCounter.getAndSet(nodeIDCounter.get() + 1)));
            lineCount += entry.getValue().getRange().get().getLineCount();
            symbolSolver.inject(entry.getValue());
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
        //some bollocks for Nelder Mead algo
        for (int i = 0; i < blocks.size(); i++) piecesToReplace.add(new TreeSet<>());
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
        int replacedPieces = 0;
        int func = 0;
        for (List<Pair<Integer, Integer>> blockPieceList : piecesByHashList) {
            blockPieceList.sort(new Utils.PotentialLengthComparator());
            Set<Integer> blockSet = new TreeSet<>();
            blockPieceIndexesToCompare.clear();
            for (Pair<Integer, Integer> blockPieceIndexes : blockPieceList) {
                int pairBlockIndex = blockPieceIndexes.a;
                int pairPieceIndex = blockPieceIndexes.b;
                if (piecesToReplace.get(pairBlockIndex).contains(pairPieceIndex))
                    continue;
                if (!blockSet.contains(pairBlockIndex)) {
                    blockSet.add(pairBlockIndex);
                    blockPieceIndexesToCompare.add(blockPieceIndexes);
                }
            }
            int funcMaxConstraint = Integer.MIN_VALUE;
            for (Pair<Integer, Integer> blockPiece : blockPieceIndexesToCompare) {
                Integer nearestReplacePiece = piecesToReplace.get(blockPiece.a).ceiling(blockPiece.b);
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
            if (blocksToReplacePieces.size() <= 1)
                continue;
            func++;
            duplicatedSegments.add(new ArrayList<>());
            for (Pair<Integer, Integer> blockPiece : blockPieceIndexesToCompare) {
                if (blocksToReplacePieces.contains(blockPiece.a)) {
                    Utils.getLast(duplicatedSegments).add(new ArrayList<>());
                    for (Integer piece = blockPiece.b; piece < Math.min(blocks.get(blockPiece.a).list.size(),
                            blockPiece.b + lenMaxRes); piece++) {
                        Utils.getLast(Utils.getLast(duplicatedSegments)).add(new Pair<>(blockPiece.a, piece));
                        piecesToReplace.get(blockPiece.a).add(piece);
                        replacedPieces++;
                    }
                }
            }
        }
        int all = 0;
        for (int i = 0; i < blocks.size(); i++)
            all += blocks.get(i).list.size();
    }

    public static void main(String[] args) {
        setup(args);
        for (CompilationUnit cu : units.values()) {
            cu.findAll(BlockStmt.class).forEach(blockStmt -> {
                if (blockStmt.getParentNode().isPresent() && Piece.checkBranching(blockStmt.getParentNode().get()))
                    return;
                blocks.add(new Block(blockStmt));
            });
        }
        findCopiedPieces();
        Uniter.makeMethod(duplicatedSegments);
        countMemoryAndTime();
    }
}