import at.unisalzburg.dbresearch.apted.distance.APTED;
import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.DataKey;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.*;

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
    public static List<File> filesToParse = new ArrayList<>();
    public static Map<String, CompilationUnit> units = new HashMap<>();
    public static ArrayList<Graph> graphs = new ArrayList<>();
    public static APTED<Cost, NodeData> apted = new APTED<>(new Cost());

    static boolean dontCollide(File fromWhere1, Range r1, File fromWhere2, Range r2)
    {
        boolean flag = false;
        if (!(fromWhere1.getAbsolutePath()).equals(fromWhere2.getAbsolutePath()))
            flag = true;
        else
            flag = Math.max(r1.begin.line, r2.begin.line)>Math.min(r1.end.line, r2.end.line);
        return (flag && fromWhere1.getAbsolutePath().compareTo(fromWhere2.getAbsolutePath())<=0
                && r1.begin.line<=r2.begin.line);
    }

    static boolean checkNodeToMakeGraph(Node node) {
        Node parent = node.getParentNodeForChildren();
        return (node instanceof BlockStmt || parent instanceof ForStmt || parent instanceof ForEachStmt
                || parent instanceof IfStmt || parent instanceof WhileStmt);
    }

    static void parseArgs(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("You need to specify only path to directory!");
            System.exit(0);
        } else {
            File file = new File(args[0]);
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
                    if (distance > 0.0f && distance <= 98.0f) {
                        flag = true;
                        System.out.printf("found copy: %s lines %s->%s, and %s lines %s->%s \n",
                                fromWhere.getAbsolutePath(), node.getRange().get().begin.line,
                                node.getRange().get().end.line, graph.fromWhere.getAbsolutePath(),
                                graph.root.getRange().get().begin.line, graph.root.getRange().get().end.line);
                        MethodCallExpr expr = new MethodCallExpr();
                        expr.setName("func3");
                        if (node instanceof BlockStmt) {
                            BlockStmt stmt = new BlockStmt();
                            stmt.addStatement(expr);
                            node.replace(stmt);
                        }
                        else
                        {
                            node.replace(expr);
                        }
                    }
                }
            }
        }
        if (!flag)
            for (Node child : node.getChildNodes())
            {
                dfs(child, fromWhere);
            }
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
        for (File file : filesToParse)
            try {
                units.put(file.getAbsolutePath(), StaticJavaParser.parse(file));
            } catch (Exception e) {
                System.out.println("Could not parse file: " + file.getAbsolutePath());
                e.printStackTrace();
                System.exit(0);
            }
        AtomicReference<Integer> counter = new AtomicReference<>(0);
        for (Map.Entry<String, CompilationUnit> entry : units.entrySet())
            entry.getValue().walk(node -> node.setData(Main.NODE_ID, counter.getAndSet(counter.get() + 1)));
        for (Map.Entry<String, CompilationUnit> entry : units.entrySet()) {
            entry.getValue().stream().filter(Main::checkNodeToMakeGraph)
                    .forEach(node -> graphs.add(new Graph(node, entry.getKey())));
        }
        for (Graph graph : graphs)
        {
            graph.export(graph.getPublicName());
        }
        for (Map.Entry<String, CompilationUnit> entry : units.entrySet())
            dfs(entry.getValue(), new File(entry.getKey()));
        for (Map.Entry<String, CompilationUnit> entry : units.entrySet())
            System.out.println(entry.getValue().toString());
        long timeInMillisEnd = System.currentTimeMillis();
        System.out.println("Execution time: ~" + (timeInMillisEnd - timeInMillisStart) + "ms");
        System.out.println("Memory usage: ~" +
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576 + " MB");
    }
}