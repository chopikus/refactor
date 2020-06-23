import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.DataKey;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {
    public static final DataKey<Integer> NODE_ID = new DataKey<>(){ };
    static List<File> filesToParse = new ArrayList<>();
    static ArrayList<Node> nodeList = new ArrayList<>();
    static List<Boolean> used = new ArrayList<>();
    static String currentFileNameParsing = "";
    static HashMap<File, CompilationUnit> roots = new HashMap<>();

    static void dfsASTPrepare(Node node) {
        node.setData(NODE_ID, nodeList.size());
        nodeList.add(node);
        for (Node child : node.getChildNodes()) {
            dfsASTPrepare(child);
        }
    }

    static boolean dfsAST(Node node) {
        if (used.get(node.getData(NODE_ID)))
            return false;
        used.set(node.getData(NODE_ID), true);
        if (node instanceof ClassOrInterfaceDeclaration)
            return false;
        if (node instanceof SimpleName)
            return false;
        for (Node child : node.getChildNodes()) {
            if (dfsAST(child))
                DotExporter.addEdge(currentFileNameParsing, node, child);
        }
        return true;
    }

    static void makeAST() {
        for (File sourceFile : filesToParse) {
            try {
                currentFileNameParsing = sourceFile.getName();
                nodeList.clear();
                used.clear();
                dfsASTPrepare(roots.get(sourceFile));
                for (int i = 0; i < nodeList.size(); i++) used.add(false);
                for (int i = 1; i < nodeList.size(); i++)
                    if (!used.get(i)) {
                        Node node = nodeList.get(i);
                        Node parent = node.getParentNodeForChildren();
                        if (node instanceof BlockStmt || parent instanceof ForStmt || parent instanceof ForEachStmt
                                || parent instanceof IfStmt || parent instanceof WhileStmt) dfsAST(nodeList.get(i));
                    }
                int counter = 0;
                for (int i = 0; i < nodeList.size(); i++)
                    if (used.get(i))
                        counter++;
                System.out.println(String.format("Nodes used in graph %s: %s/%s, %s %%",
                        sourceFile.getName().split("\\.")[0],
                        counter,
                        nodeList.size(),
                        Math.round(counter / (double) nodeList.size() * 100)));
            } catch (Exception e) {
                System.out.println("Could not parse this file: " + sourceFile.getName());
                e.printStackTrace();
                System.exit(0);
            }
        }
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
        for (File file : filesToParse) {
            try {
                roots.put(file, StaticJavaParser.parse(file));
                /*Metrica metrica = new Metrica(roots.get(file));
                System.out.println(String.format("Amount of supposed bugs in %s: %s. Time spent on coding: %s seconds",
                        file.getName(), metrica.getBugs(), metrica.getCodingTime()));*/
            } catch (FileNotFoundException e) {
                System.out.println("Not found file: " + file.getAbsolutePath());
                e.printStackTrace();
                System.exit(0);
            }
        }
        makeAST();
        DotExporter.export();
        long timeInMillisEnd = System.currentTimeMillis();
        System.out.println("Execution time: ~" + (timeInMillisEnd - timeInMillisStart) + "ms");
    }
}