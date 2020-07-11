import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.DataKey;
import com.github.javaparser.ast.Node;
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

    public static int log2( int bits ) // returns 0 for bits=0
    {
        int log = 0;
        if( ( bits & 0xffff0000 ) != 0 ) { bits >>>= 16; log = 16; }
        if( bits >= 256 ) { bits >>>= 8; log += 8; }
        if( bits >= 16  ) { bits >>>= 4; log += 4; }
        if( bits >= 4   ) { bits >>>= 2; log += 2; }
        return log + ( bits >>> 1 );
    }

    static boolean checkNodeToStartDFS(Node node) {
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
            entry.getValue().stream().filter(Main::checkNodeToStartDFS)
                    .forEach(node -> graphs.add(new Graph(node, entry.getKey())));
        }
        int sum = 0;
        for (Graph graph : graphs) {
            int sq = graph.leaves*graph.leaves;
            sum += sq*log2(sq);
        }
        System.out.println(sum);
        /*for (Graph graph : graphs)
            graph.export(graph.fromWhere.getName() + " Node " + graph.root.getData(Main.NODE_ID));*/
        System.out.println(graphs.size() + " graphs");
        long timeInMillisEnd = System.currentTimeMillis();
        System.out.println("Execution time: ~" + (timeInMillisEnd - timeInMillisStart) + "ms");
        System.out.println("Memory usage: ~"+(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1048576+" MB");
    }
}