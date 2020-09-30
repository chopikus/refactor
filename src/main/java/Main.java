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
    public static float threshold = 0.1f;
    public static List<NavigableSet<Integer>> piecesToReplace = new ArrayList<>();
    public static APTED<Cost, NodeData> apted = new APTED<>(new Cost());
    public static List<Pair<Integer, Integer>> blockPieceIndexesToCompare = new ArrayList<>();
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
            lineCount+=entry.getValue().getRange().get().getLineCount();
            symbolSolver.inject(entry.getValue());
        }
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
        facade = JavaParserFacade.get(typeSolver);
    }

    static void findCopiedPieces()  {
        double[] start = {0};double[] step = new double[start.length];Arrays.fill(step, 100);double ftol = 0.0001;
        int maxIterations = 5000; Maximisation maximize = new Maximisation();
        SimilarityFunction function = new SimilarityFunction();
        //some bollocks for Nelder Mead algo
        for (int i=0; i<blocks.size(); i++) piecesToReplace.add(new TreeSet<>());
        Map<Integer, List<Pair<Integer, Integer>>> piecesByHash = new TreeMap<>();
        int blockIndex=0;
        for (Block block : blocks) {
            int pieceIndex=0;
            for (Piece piece : block.list) {
                List<Pair<Integer, Integer>> list = piecesByHash.getOrDefault(piece.hash, new ArrayList<>());
                list.add(new Pair<>(blockIndex, pieceIndex));
                piecesByHash.put(piece.hash, list);
                pieceIndex++;
            }
            blockIndex++;
        }
        for (Map.Entry<Integer, List<Pair<Integer, Integer>>> entry : piecesByHash.entrySet()) {
            Set<Integer> set = new TreeSet<>();
            blockPieceIndexesToCompare.clear();
            for (Pair<Integer, Integer> blockPieceIndexes : entry.getValue()) {
                int pairBlockIndex = blockPieceIndexes.a;
                int pairPieceIndex = blockPieceIndexes.b;
                if (piecesToReplace.get(pairBlockIndex).contains(pairPieceIndex))
                    continue;
                if (!set.contains(pairBlockIndex)) {
                    set.add(pairBlockIndex);
                    blockPieceIndexesToCompare.add(blockPieceIndexes);
                }
            }
            int funcConstraint = Integer.MAX_VALUE;
            for (Pair<Integer, Integer> blockPiece : blockPieceIndexesToCompare) {
                int nearestReplacePiece = blocks.get(blockPiece.a).list.size();
                try {
                    nearestReplacePiece = piecesToReplace.get(blockPiece.a).ceiling(blockPiece.b); }
                catch (Exception ignored){}
                funcConstraint = Math.min(funcConstraint, nearestReplacePiece-blockPiece.b);
            }
            maximize.removeConstraints();
            maximize.addConstraint(0, -1, 1);
            maximize.addConstraint(0, 1, funcConstraint);
            maximize.nelderMead(function, start, step, ftol, maxIterations);
            double argWithMaxRes = maximize.getParamValues()[0];
            long lenMaxRes = Math.round(argWithMaxRes);
            Set<Integer> blocksToReplacePieces = function.getBlocksToReplacePieces(lenMaxRes);
            for (Pair<Integer, Integer> blockPiece : blockPieceIndexesToCompare)
                for (Integer piece = blockPiece.b; piece<blockPiece.b+lenMaxRes; piece++)
                    if (blocksToReplacePieces.contains(blockPiece.a)){
                        piecesToReplace.get(blockPiece.a).add(piece);
                        System.out.println("REPLACING PIECE "+blocks.get(blockPiece.a).list.get(piece).node);
                    }
            System.out.println();
        }
        for (int i=0; i<blocks.size(); i++)
        {
            System.out.println("BLOCK "+i+"===========");
            for (int j=0; j<blocks.get(i).list.size(); j++)
                System.out.println(blocks.get(i).list.get(j).node+" "+ piecesToReplace.get(i).contains(j));
        }
    }

    public static void main(String[] args) {
        setup(args);
        for (CompilationUnit cu : units.values())
            cu.findAll(BlockStmt.class).forEach(blockStmt -> blocks.add(new Block(blockStmt)));
        findCopiedPieces();
        countMemoryAndTime();
    }
}