import at.unisalzburg.dbresearch.apted.distance.APTED;
import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.DataKey;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
    public static final DataKey<Integer> NODE_ID = new DataKey<>() {
    };
    public static final DataKey<Boolean> IS_VARIABLE = new DataKey<>() {
    };
    public static List<File> filesToParse = new ArrayList<>();
    public static Map<String, CompilationUnit> units = new HashMap<>();
    public static ArrayList<Graph> graphs = new ArrayList<>();
    public static APTED<Cost, NodeData> apted = new APTED<>(new Cost());
    public static AtomicReference<Integer> nodeIDCounter = new AtomicReference<>(0);
    public static int counter=0;
    public static TypeSolver typeSolver = null;
    public static JavaParserFacade facade = null;
    static boolean dontCollide(File fromWhere1, Range r1, File fromWhere2, Range r2)
    {
        //if (!fromWhere1.getAbsolutePath().equals(fromWhere2.getAbsolutePath()) && Math.min(r1.getLineCount(), r2.getLineCount())<)
        //    return false;
        boolean flag;
        if (!(fromWhere1.getAbsolutePath()).equals(fromWhere2.getAbsolutePath()))
            flag = (fromWhere1.getAbsolutePath().compareTo(fromWhere2.getAbsolutePath())<0);
        else
            flag = (Math.max(r1.begin.line, r2.begin.line)>Math.min(r1.end.line, r2.end.line) && r1.begin.line<=r2.begin.line);
        return flag;
    }
    static boolean checkDistance(float distance, int l1, int l2)
    {
        return distance<=Math.min(l1, l2)*5;
    }
    static boolean checkNodeToMakeGraph(Node node) {
        Node parent = node.getParentNodeForChildren();
        return (node instanceof BlockStmt || parent instanceof ForStmt || parent instanceof ForEachStmt
                || parent instanceof IfStmt || parent instanceof WhileStmt || node instanceof ClassOrInterfaceDeclaration);
    }

    static void parseArgs(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("You need to specify only path to directory!");
            System.exit(0);
        } else {
            File file = new File(args[0]);
            CombinedTypeSolver solver = new CombinedTypeSolver();
            solver.add(new ReflectionTypeSolver());
            solver.add(new JavaParserTypeSolver(file));
            typeSolver = solver;
            System.out.println(typeSolver.toString());
            Files.walk(Paths.get(file.getAbsolutePath()))
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        File pFile = path.toFile();
                        return pFile.getName().
                                substring(pFile.getName().
                                        lastIndexOf('.') + 1).equals("java");
                    })
                    .forEach(path -> filesToParse.add(path.toFile()));
        }
    }

    static void dfs(Node node, File fromWhere)
    {
        boolean flag = false;
        if (checkNodeToMakeGraph(node))
        {
            Graph g = new Graph(node, fromWhere.getAbsolutePath());
            for (Graph graph : graphs) {
                if (node.getRange().isPresent() && graph.root.getRange().isPresent()
                        && dontCollide(fromWhere, node.getRange().get(), graph.fromWhere,
                        graph.root.getRange().get())) {
                    float distance = apted.computeEditDistance(g.algoRoot, graph.algoRoot);
                    if (checkDistance(distance, graph.root.getRange().get().getLineCount(),
                            node.getRange().get().getLineCount())) {
                        System.out.printf("found copy: %s lines %s->%s, and %s lines %s->%s %s\n",
                                fromWhere.getAbsolutePath(), node.getRange().get().begin.line,
                                node.getRange().get().end.line, graph.fromWhere.getAbsolutePath(),
                                graph.root.getRange().get().begin.line, graph.root.getRange().get().end.line, counter);
                        g.export(counter+" 1");
                        graph.export(counter+" 2");
                        counter++;
                        flag = true;
                        DSU.unite(node, graph.root);
                    }
                }
            }
        }
        if (!flag)
            for (Node child : node.getChildNodes())
                dfs(child, fromWhere);
    }


    public static void main(String[] args) {
        Runtime.getRuntime().gc();
        long timeInMillisStart = System.currentTimeMillis();
        try {
            parseArgs(args);
        } catch (IOException e) {
            System.out.println("couldn't parse folder/file..\n");
            e.printStackTrace();
            System.exit(0);
        }
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
        facade = JavaParserFacade.get(typeSolver);
        for (File file : filesToParse)
            try {
                units.put(file.getAbsolutePath(), StaticJavaParser.parse(file));
            } catch (Exception e) {
                System.out.println("Could not parse file: " + file.getAbsolutePath());
                e.printStackTrace();
                System.exit(0);
            }
        for (Map.Entry<String, CompilationUnit> entry : units.entrySet())
            entry.getValue().walk(node -> node.setData(Main.NODE_ID, nodeIDCounter.getAndSet(nodeIDCounter.get() + 1)));
        for (Map.Entry<String, CompilationUnit> entry : units.entrySet()) {
            entry.getValue().stream().filter(Main::checkNodeToMakeGraph)
                    .forEach(node -> graphs.add(new Graph(node, entry.getKey())));
        }
        for (Map.Entry<String, CompilationUnit> entry : units.entrySet())
            dfs(entry.getValue(), new File(entry.getKey()));
        List<List<Node>> subsets = DSU.getAllSubsets();
        Uniter.unite(subsets.get(0));
        long timeInMillisEnd = System.currentTimeMillis();
        System.out.println("Execution time: ~" + (timeInMillisEnd - timeInMillisStart) + "ms");
        System.out.println("Memory usage: ~" +
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576 + " MB");
    }
}