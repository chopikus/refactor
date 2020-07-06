import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.utils.Pair;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class Graph {
    ArrayList<Pair<Node, Node>> edges = new ArrayList<>();
    Node root;
    File fromWhere;
    Boolean traverseEverything;
    static String getName(Node node) {
        String result = node.toString();
        if (result.startsWith("{\n") && result.endsWith("\n}")) {
            result = result.substring(2);
            result = result.substring(0, result.length() - 2);
        }
        return "ID: "+node.getData(Main.NODE_ID)+" "+result.replace('"', '\'');
    }

    boolean checkNode(Node node)
    {
        if (traverseEverything)
            return true;
        if (node instanceof BlockStmt && node.getChildNodes().size()==0)
            return false;
        if (node instanceof ClassOrInterfaceDeclaration)
            return false;
        return !(node instanceof SimpleName);
    }

    void dfsAST(Node node) {
        for (Node child : node.getChildNodes()) {
            if (checkNode(child)) {
                edges.add(new Pair<>(node, child));
                dfsAST(child);
            }
        }
    }

    public Graph(Node root, String fromWherePath, boolean... traverseEverything)
    {
        this.root = root;
        this.fromWhere = new File(fromWherePath);
        this.traverseEverything = traverseEverything.length >= 1 && traverseEverything[0];
        dfsAST(root);
    }

    void export(String name)
    {
        StringBuilder dotExport = new StringBuilder();
        TreeMap<Integer, String> nodes = new TreeMap<>();
        for (Pair<Node, Node> edge : edges)
        {
            nodes.put(edge.a.getData(Main.NODE_ID), getName(edge.a));
            nodes.put(edge.b.getData(Main.NODE_ID), getName(edge.b));
            dotExport.append(String.format("node%s -> node%s;\n", edge.a.getData(Main.NODE_ID),
                    edge.b.getData(Main.NODE_ID)));
        }
        for (Map.Entry<Integer, String> entry : nodes.entrySet())
        {
            dotExport.append(String.format("node%s [label=\"%s\"];\n", entry.getKey().toString(), entry.getValue()));
        }
        try {
            File printFile = new File("out/" + name + ".dot");
            if (!printFile.exists() && !printFile.getParentFile().mkdirs() && !printFile.createNewFile())
                throw new Exception();
            FileWriter fileWriter = new FileWriter(printFile, false);
            fileWriter.write(String.format("digraph %s {\n%s}", name.replaceAll("[ ,.\"']", ""),
                    dotExport.toString()));
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
