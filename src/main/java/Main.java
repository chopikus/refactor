import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.*;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {
    static List<File> filesToParse = new ArrayList<>();
    static FileWriter fileWriter;
    static HashMap<Node, Integer> nodeMap = new HashMap<>();
    static ArrayList<Node> nodeList = new ArrayList<>();
    static List<Boolean> used = new ArrayList<>();
    static HashMap<String, String> dotExport = new HashMap<>();
    static String currentFileNameParsing = "";

    static String getGraphName(Node node) {
        if (node instanceof SimpleName) {
            SimpleName simpleName = (SimpleName) node;
            return simpleName.asString();
        }
        return node.getMetaModel().getTypeNameGenerified();
    }

    static void dfsASTPrepare(Node node) {
        nodeMap.put(node, nodeList.size());
        nodeList.add(node);
        for (Node child : node.getChildNodes())
        {
            dfsASTPrepare(child);
        }
    }

    static void dfsAST(Node node) {
        used.set(nodeMap.get(node), true);
        if (node instanceof BlockStmt && node.getChildNodes().size()==0)
            return;
        if (node instanceof ClassOrInterfaceDeclaration)
            return;
        dotExport.put(currentFileNameParsing, dotExport.get(currentFileNameParsing)+
                String.format("node%s [label=\"%s\"];\n", nodeMap.get(node), getGraphName(node)));
        for (Node child : node.getChildNodes())
        {
            if (used.get(nodeMap.get(child)))
                continue;
            dotExport.put(currentFileNameParsing, dotExport.get(currentFileNameParsing)+
                    String.format("node%s -> node%s;\n", nodeMap.get(node), nodeMap.get(child)));
            dfsAST(child);
        }
    }

    static void makeAST()
    {
        for (File sourceFile : filesToParse)
        {
            try {
                CompilationUnit root = StaticJavaParser.parse(sourceFile);
                currentFileNameParsing = sourceFile.getName();
                dotExport.put(currentFileNameParsing,
                        String.format("digraph %s {", sourceFile.getName().split("\\.")[0]));
                nodeMap.clear();
                nodeList.clear();
                dfsASTPrepare(root);
                used.clear();
                for (int i=0; i<nodeList.size(); i++) used.add(false);
                for (int i=1; i<nodeList.size(); i++)
                    if (!used.get(i))
                    {
                        Node node = nodeList.get(i);
                        Node parent = node.getParentNodeForChildren();
                        if (node instanceof BlockStmt || parent instanceof ForStmt || parent instanceof ForEachStmt
                                || parent instanceof IfStmt || parent instanceof WhileStmt) dfsAST(nodeList.get(i));
                    }
                int counter=0;
                for (int i=0; i<nodeList.size(); i++)
                    if (used.get(i))
                        counter++;
                System.out.println(String.format("Nodes used in graph %s: %s/%s, %s %%",
                        sourceFile.getName().split("\\.")[0],
                        counter,
                        nodeList.size(),
                        Math.round(counter/(double)nodeList.size()*100)));
                dotExport.put(currentFileNameParsing, dotExport.get(currentFileNameParsing)+"}");
            }
            catch (Exception e) {
                System.out.println("Could not parse this file: "+sourceFile.getName());
                e.printStackTrace();
                System.exit(0);
            }
        }
        for (String key : dotExport.keySet())
        {
            try {
                File printFile = new File("out/" + key + ".dot");
                if (!printFile.exists() && !printFile.getParentFile().mkdirs() && !printFile.createNewFile())
                    throw new Exception();
                fileWriter = new FileWriter(printFile, false);
                fileWriter.write(dotExport.get(key));
                fileWriter.close();
            }
            catch (Exception e)
            {
                System.out.println("Could not export .dot file: " + key.split("\\.")[0]);
                System.exit(0);
            }
        }
    }
    static void parseArgs(String[] args)
    {
        if (args.length!=1)
        {
            System.out.println("You need to specify only path to directory!");
            System.exit(0);
        }
        else
        {
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
            }
            else
            {
                filesToParse.add(folder);
                //cause it's not really a folder, it's a file
            }
        }
    }
    public static void main(String[] args) {
        long timeInMillisStart = System.currentTimeMillis();
        parseArgs(args);
        makeAST();
        long timeInMillisEnd = System.currentTimeMillis();
        System.out.println("Execution time: ~" + (timeInMillisEnd-timeInMillisStart) + "ms");
    }
}