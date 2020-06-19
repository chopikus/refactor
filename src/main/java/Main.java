import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.SimpleName;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {
    static List<File> filesToParse = new ArrayList<>();
    static FileWriter fileWriter;
    static HashMap<Node, Integer> nodeMap = new HashMap<>();
    static ArrayList<Node> nodeList = new ArrayList<>();
    static List<Boolean> used = new ArrayList<>();
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

    static void dfsAST(Node node) throws IOException {
        fileWriter.write(String.format("node%s [label=\"%s\"]", nodeMap.get(node), getGraphName(node)));
        fileWriter.write(";\n");
        used.set(nodeMap.get(node), true);
        for (Node child : node.getChildNodes())
        {
            if (used.get(nodeMap.get(child)))
                continue;
            fileWriter.write(String.format("node%s -> node%s", nodeMap.get(node), nodeMap.get(child)));
            fileWriter.write(";\n");
            dfsAST(child);
        }
    }

    static void makeAST()
    {
        for (File sourceFile : filesToParse)
        {
            try {
                File printFile = new File("temp/"+sourceFile.getName()+".dot");
                if (!printFile.exists() && !printFile.getParentFile().mkdirs() && !printFile.createNewFile())
                    throw new Exception();
                fileWriter = new FileWriter(printFile, false);
                fileWriter.write("digraph G {\n");
                CompilationUnit root = StaticJavaParser.parse(sourceFile);
                nodeMap.clear();
                nodeList.clear();
                dfsASTPrepare(root);
                used.clear();
                for (int i=0; i<nodeList.size(); i++)
                    used.add(false);
                for (int i=1; i<nodeList.size(); i++)
                {
                    if (!used.get(i))
                    {
                        String parentClass = nodeList.get(i).getParentNodeForChildren()
                                .getMetaModel().getTypeNameGenerified();
                        String nodeClass = nodeList.get(i).getMetaModel().getTypeNameGenerified();
                        if (nodeClass.equals("BlockStmt") | parentClass.equals("ForStmt") ||
                                parentClass.equals("ForeachStmt") || parentClass.equals("IfStmt") ||
                                parentClass.equals("WhileStmt"))
                        {
                            dfsAST(nodeList.get(i));
                        }
                    }
                }
                int counter=0;
                for (int i=0; i<nodeList.size(); i++)
                    if (used.get(i))
                        counter++;
                System.out.println("nodes used in graph: "+counter+"/"+nodeList.size());
                fileWriter.write("}");
                fileWriter.close();
            } catch (Exception e) {
                System.out.println("Could not parse this file: "+sourceFile.getAbsolutePath());
                e.printStackTrace();
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