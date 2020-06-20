import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.utils.Pair;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

public class DotExporter {

    private static final HashMap<Pair<String, Node>, Integer> nodeIds = new HashMap<>();
    private static final HashMap<String, String> fileContent = new HashMap<>();
    private static int counter=0;

    private static String getName(Node node) {
        String result = node.getMetaModel().getTypeNameGenerified();
        if (node instanceof SimpleName) {
            SimpleName simpleName = (SimpleName) node;
            result = simpleName.asString();
        }
        if (node instanceof NameExpr)
        {
            NameExpr nameExpr = (NameExpr) node;
            result = nameExpr.getName().asString();
        }
        else if (node instanceof BooleanLiteralExpr)
        {
            BooleanLiteralExpr literalExpr = (BooleanLiteralExpr) node;
            result = ""+literalExpr.getValue();
        }
        else if (node instanceof IntegerLiteralExpr)
        {
            IntegerLiteralExpr literalExpr = (IntegerLiteralExpr) node;
            result = ""+literalExpr.getValue();
        }
        else if (node instanceof StringLiteralExpr)
        {
            StringLiteralExpr literalExpr = (StringLiteralExpr) node;
            result = ""+literalExpr.getValue();
        }
        else if (node instanceof DoubleLiteralExpr) {
            DoubleLiteralExpr literalExpr = (DoubleLiteralExpr) node;
            result = ""+literalExpr.getValue();
        }
        else if (node instanceof Type)
        {
            Type primitiveType = (Type) node;
            result = primitiveType.asString();
        }
        else if (node instanceof BinaryExpr)
        {
            BinaryExpr binaryExpr = (BinaryExpr) node;
            result = binaryExpr.toString();
        }
        else if (node instanceof VariableDeclarator)
        {
            VariableDeclarator declarator = (VariableDeclarator) node;
            return "decl:"+declarator.getName().asString();
        }
        return result.replace('"', '\'');
    }


    static void addEdge(String whichGraph, Node from, Node to)
    {
        if (!fileContent.containsKey(whichGraph))
            fileContent.put(whichGraph, "");
        Pair<String, Node> pairFrom = new Pair<>(whichGraph, from);
        Pair<String, Node> pairTo = new Pair<>(whichGraph, to);
        if (!nodeIds.containsKey(pairFrom)) {
            nodeIds.put(pairFrom, counter);
            fileContent.put(whichGraph,
                    fileContent.get(whichGraph)+String.format("node%s [label=\"%s\"];\n", nodeIds.get(pairFrom),
                            getName(from)));
            counter++;
        }
        if (!nodeIds.containsKey(pairTo)) {
            nodeIds.put(pairTo, counter);
            fileContent.put(whichGraph,
                    fileContent.get(whichGraph)+String.format("node%s [label=\"%s\"];\n", nodeIds.get(pairTo),
                            getName(to)));
            counter++;
        }
        fileContent.put(whichGraph, fileContent.get(whichGraph)+String.format("node%s -> node%s;\n",
                nodeIds.get(pairFrom), nodeIds.get(pairTo)));
    }

    static void export()
    {
        for (String fileName : fileContent.keySet())
        {
            try {
                File printFile = new File("out/" + fileName + ".dot");
                if (!printFile.exists() && !printFile.getParentFile().mkdirs() && !printFile.createNewFile())
                    throw new Exception();
                FileWriter fileWriter = new FileWriter(printFile, false);
                fileWriter.write(String.format("digraph %s {\n%s}", fileName.split("\\.")[0],
                        fileContent.get(fileName)));
                fileWriter.close();
            }
            catch (Exception e)
            {
                System.out.println("Could not export to DOT: "+fileName);
                e.printStackTrace();
                System.exit(0);
            }
        }
    }
}
