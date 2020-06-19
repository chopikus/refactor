import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    static List<File> filesToParse = new ArrayList<>();
    static FileWriter fileWriter;
    static int dfsCounter=0;

    static String getDotName(Node node)
    {
        if (node instanceof SimpleName)
        {
            SimpleName simpleName = (SimpleName) node;
            System.out.println(simpleName.getId());
            return simpleName.asString();
        }
        return node.getMetaModel().getTypeNameGenerified();
    }
    static void dfsAST(Node node) throws IOException {
        dfsCounter++;
        int nodeCounter = dfsCounter;
        fileWriter.write(String.format("node%s [label=\"%s\"]", nodeCounter, getDotName(node)));
        fileWriter.write(";\n");
        for (Node child : node.getChildNodes())
        {
            if (child instanceof ImportDeclaration || child instanceof Modifier)
                continue;
            if (node instanceof MethodDeclaration && (!(child instanceof SimpleName) && !(child instanceof BlockStmt)))
                continue;
            fileWriter.write(String.format("node%s -> node%s", nodeCounter, dfsCounter+1));
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
                CompilationUnit cu = StaticJavaParser.parse(sourceFile);
                fileWriter.write("digraph G {\n");
                dfsAST(cu);
                fileWriter.write("}");
                fileWriter.close();
                System.out.println(dfsCounter);
            } catch (Exception e) {
                System.out.println("Could not parse this file: "+sourceFile.getAbsolutePath());
                System.exit(0);
                e.printStackTrace();
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
        parseArgs(args);
        makeAST();
    }
}