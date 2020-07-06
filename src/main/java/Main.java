import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.DataKey;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
    public static final DataKey<Integer> NODE_ID = new DataKey<>(){ };
    public static List<File> filesToParse = new ArrayList<>();
    public static Map<String, CompilationUnit> units = new HashMap<>();
    public static ArrayList<Graph> graphs = new ArrayList<>();
    
    static boolean checkNodeToStartDFS(Node node)
    {
        Node parent = node.getParentNodeForChildren();
        return (node instanceof BlockStmt || parent instanceof ForStmt || parent instanceof ForEachStmt
                || parent instanceof IfStmt || parent instanceof WhileStmt);
    }

    static void parseArgs(String[] args) {
        if (args.length != 1) {
            System.out.println("You need to specify only path to directory!");
            System.exit(0);
        } else {
            File folder = new File(args[0]);
            if (folder.isDirectory()) {
                File[] listOfFiles = folder.listFiles();
                if (listOfFiles != null) {
                    for (File file : listOfFiles) {
                        if (file.isFile() && file.getName().
                                substring(file.getName().
                                        lastIndexOf('.') + 1).equals("java"))
                            filesToParse.add(file);
                    }
                }
            } else {
                filesToParse.add(folder);
                //cause it's not really a folder, it's a file
            }
        }
    }

    public static void main(String[] args) {
        long timeInMillisStart = System.currentTimeMillis();
        parseArgs(args);
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
            entry.getValue().stream().filter(Main::checkNodeToStartDFS)
                    .forEach(node -> graphs.add(new Graph(node, entry.getKey())));
            graphs.add(new Graph(entry.getValue(), entry.getKey(), true));
        }
        for (Graph graph : graphs)
            graph.export(graph.fromWhere.getName()+" Node "+graph.root.getData(Main.NODE_ID));
        long timeInMillisEnd = System.currentTimeMillis();
        System.out.println("Execution time: ~" + (timeInMillisEnd - timeInMillisStart) + "ms");
    }
}