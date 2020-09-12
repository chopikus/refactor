import at.unisalzburg.dbresearch.apted.distance.APTED;
import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.DataKey;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;


public class Main {
    public static final DataKey<Integer> NODE_ID = new DataKey<>() {
    };
    public static final DataKey<Boolean> IS_VARIABLE = new DataKey<>() {
    };
    public static final DataKey<Boolean> IS_DEPTH_ONE = new DataKey<>() {
    };
    public static List<File> filesToParse = new ArrayList<>();
    public static Map<String, CompilationUnit> units = new HashMap<>();
    public static ArrayList<Graph> graphs = new ArrayList<>();
    public static AtomicReference<Integer> nodeIDCounter = new AtomicReference<>(0);
    public static File execFile;
    public static int lineCount = 0;
    public static TypeSolver typeSolver = null;
    public static JavaParserFacade facade = null;
    public static CountDownLatch doneSignal;
    public static int availableThreads = Runtime.getRuntime().availableProcessors();
    public static long timeInMillisStart = -1;

    static void countMemoryAndTime()
    {
        long timeInMillisEnd = System.currentTimeMillis();
        System.out.println("Execution time: ~" + (timeInMillisEnd - timeInMillisStart) + "ms");
        System.out.println("Memory usage: ~" +
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576 + " MB");
    }
    static void exportUnits()
    {
        for (Map.Entry<String, CompilationUnit> entry : units.entrySet())
        {
            try {
                File file = new File(entry.getKey());
                Path execPath = Paths.get(execFile.getAbsolutePath());
                Path filePath = Paths.get(file.getAbsolutePath());
                File targetFile = new File("out"+File.separator+execPath.relativize(filePath).toString());
                if (!targetFile.getParentFile().exists() && !targetFile.getParentFile().mkdirs())
                    throw new IllegalAccessException();
                FileWriter writer = new FileWriter(targetFile);
                writer.write(entry.getValue().toString());
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
        try {
            parseArgsToUnits(args);
        } catch (IOException e) {
            System.out.println("couldn't parse folder/file..\n");
            e.printStackTrace();
            System.exit(0);
        }
        timeInMillisStart = System.currentTimeMillis();
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        for (Map.Entry<String, CompilationUnit> entry : units.entrySet()) {
            entry.getValue().walk(node -> node.setData(Main.NODE_ID, nodeIDCounter.getAndSet(nodeIDCounter.get() + 1)));
            lineCount+=entry.getValue().getRange().get().getLineCount();
            symbolSolver.inject(entry.getValue());
        }
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
        facade = JavaParserFacade.get(typeSolver);
    }

    static boolean dontCollide(File fromWhere1, Range r1, File fromWhere2, Range r2)
    {
        if (Math.min(r1.getLineCount(), r2.getLineCount())<4)
            return false;
        if (Math.min(r1.getLineCount(), r2.getLineCount())<10 && Main.lineCount>200)
            return false;
        boolean flag;
        if (!(fromWhere1.getAbsolutePath()).equals(fromWhere2.getAbsolutePath()))
            flag = (fromWhere1.getAbsolutePath().compareTo(fromWhere2.getAbsolutePath())<0);
        else
            flag = (Math.max(r1.begin.line, r2.begin.line)>Math.min(r1.end.line, r2.end.line) && r1.begin.line<=r2.begin.line);
        return flag;
    }
    static boolean checkDistance(float distance, int l1, int l2)
    {
        return (Math.min(l1, l2)+distance/5*2)*1.4f<(l1+l2);
    }
    static boolean checkNodeToMakeGraph(Node node) {
        Node parent = node.getParentNodeForChildren();
        return ((node instanceof BlockStmt || parent instanceof ForStmt || parent instanceof ForEachStmt
                || parent instanceof IfStmt || parent instanceof WhileStmt) && node.getRange().get().getLineCount()>3);
    }

    static void multiThreadedCompare(List<Pair<Graph, Graph>> compareList)
    {
        doneSignal = new CountDownLatch(availableThreads);
        for (int i=0; i<availableThreads; i++)
        {
            APTED<Cost, NodeData> apted = new APTED<>(new Cost());
            int finalI = i;
            (new Thread(){
                public void run()
                {
                    for (int j = finalI; j<compareList.size(); j+=availableThreads)
                    {
                        Graph graph1 = compareList.get(j).a;
                        Graph graph2 = compareList.get(j).b;
                        float distance = apted.computeEditDistance(graph1.algoRoot, graph2.algoRoot);
                        if (checkDistance(distance, graph1.root.getRange().get().getLineCount(),
                                graph2.root.getRange().get().getLineCount())) {
                            synchronized (this) {
                                DSU.unite(graph1.root, graph2.root);
                            }
                        }
                    }
                    doneSignal.countDown();
                }
            }).start();
        }
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        setup(args);
        for (Map.Entry<String, CompilationUnit> entry : units.entrySet()) {
            entry.getValue().stream().filter(Main::checkNodeToMakeGraph)
                    .forEach(node -> graphs.add(new Graph(node, entry.getKey())));
        }
        List<Pair<Graph, Graph>> compareList = new ArrayList<>();
        for (Graph graph : graphs)
            for (Graph graph1 : graphs)
                if (dontCollide(graph.fromWhere,
                        graph.root.getRange().get(), graph1.fromWhere, graph1.root.getRange().get()))
                    compareList.add(new Pair<>(graph, graph1));
        multiThreadedCompare(compareList);
        List<List<Node>> subsets = DSU.getAllSubsets();
        for (int i=0; i<subsets.size(); i++)
            Uniter.uniteCode(subsets.get(i), i + 1);
        Uniter.exportClass();
        exportUnits();
        countMemoryAndTime();
    }
}