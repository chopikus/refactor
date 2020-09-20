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
    public static float threshold = 5f;
    public static List<NavigableSet<Integer>> replacedPieces = new ArrayList<>();
    public static APTED<Cost, NodeData> apted = new APTED<>(new Cost());
    public static List<Pair<Integer, Integer>> piecesToCompare = new ArrayList<>();
    static void countMemoryAndTime()
    {
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
    private static void setup(String[] args)
    {
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


    public static void main(String[] args) {
        setup(args);
        for (CompilationUnit cu : units.values())
            cu.findAll(BlockStmt.class).forEach(blockStmt -> blocks.add(new Block(blockStmt)));
        for (int i=0; i<blocks.size(); i++)
            replacedPieces.add(new TreeSet<>());
        Map<Integer, List<Pair<Integer, Integer>>> pieceMap = new TreeMap<>();
        int b=0;
        for (Block block : blocks)
        {
            int i=0;
            for (Piece piece : block.list)
            {
                List<Pair<Integer, Integer>> list = pieceMap.getOrDefault(piece.hash, new ArrayList<>());
                list.add(new Pair<>(b, i));
                pieceMap.put(piece.hash, list);
                i++;
            }
            b++;
        }
        for (Map.Entry<Integer, List<Pair<Integer, Integer>>> entry : pieceMap.entrySet())
        {
            Set<Integer> set = new TreeSet<>();
            List<Pair<Integer, Integer>> ll = new ArrayList<>();
            for (Pair<Integer, Integer> p : entry.getValue())
            {
                if (replacedPieces.get(p.a).contains(p.b))
                    continue;
                if (!set.contains(p.a))
                {
                    set.add(p.a);
                    ll.add(p);
                }
            }
            piecesToCompare = ll;
            int maxL = Integer.MAX_VALUE;
            for (Pair<Integer, Integer> p : piecesToCompare)
            {
                int r = blocks.get(p.a).list.size();
                if (replacedPieces.get(p.a).size()>0)
                    r = replacedPieces.get(p.a).ceiling(p.b);
                maxL = Math.min(maxL, r-p.b);
            }
            Maximisation max = new Maximisation();
            SimilarityFunction function = new SimilarityFunction();
            double[] start = {0};
            double[] step = new double[start.length];
            Arrays.fill(step, 100);
            double ftol = 0.0001;
            int maxIter = 5000;
            max.addConstraint(0, -1, 0);
            max.addConstraint(0, 1, maxL);
            max.nelderMead(function, start, step, ftol, maxIter);
        }
        countMemoryAndTime();
    }
}