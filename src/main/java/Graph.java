import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.resolution.types.ResolvedTypeVariable;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.SymbolReference;
import com.github.javaparser.utils.Pair;
import javassist.compiler.ast.Variable;

import javax.xml.crypto.NodeSetData;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class Graph {
    ArrayList<Pair<Node, Node>> edges = new ArrayList<>();
    at.unisalzburg.dbresearch.apted.node.Node<NodeData> algoRoot;
    Node root;
    File fromWhere;
    Boolean traverseEverything;
    Integer leaves=0, nodes=0;

    static String getName(Node node) {
        String result = node.toString();
        if (result.startsWith("{\n") && result.endsWith("\n}")) {
            result = result.substring(2);
            if (result.length()>=2)
                result = result.substring(0, result.length() - 2);
        }
        String id="";
        if (node instanceof Name || node instanceof NameExpr || node instanceof LiteralExpr || node instanceof SimpleName) {
            return id+" "+result.replace('"', '\'');
        }
        //return "Type: "+node.getMetaModel().getTypeName()+" "+result.replace('"', '\'');
        return id+" "+node.getMetaModel().getTypeName();
    }

    boolean checkNode(Node node)
    {
        if (traverseEverything)
            return true;
        if (node instanceof BlockStmt && node.getChildNodes().size()==0)
            return false;
        if (node instanceof ClassOrInterfaceDeclaration)
            return false;
        return true;//!(node instanceof SimpleName);
    }

    at.unisalzburg.dbresearch.apted.node.Node<NodeData> dfsAST(Node node) {
        NodeData algoNodeData = new NodeData(node.getMetaModel().getTypeName());
        at.unisalzburg.dbresearch.apted.node.Node<NodeData> algoNode
                = new at.unisalzburg.dbresearch.apted.node.Node<>(algoNodeData);
        nodes++;
        boolean flag = false;
        for (Node child : node.getChildNodes()) {
            if (checkNode(child)) {
                edges.add(new Pair<>(node, child));
                flag = true;
                algoNode.addChild(dfsAST(child));
            }
        }
        if (!flag)
            leaves++;
        return algoNode;
    }

    public Graph(Node root, String fromWherePath, boolean... traverseEverything)
    {
        this.root = root;
        this.fromWhere = new File(fromWherePath);
        this.traverseEverything = traverseEverything.length >= 1 && traverseEverything[0];
        this.algoRoot = dfsAST(root);
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
            dotExport.append(String.format("node%s [label=\"%s\"];\n", entry.getKey().toString(), entry.getValue()));
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

    String getPublicName()
    {
        return fromWhere.getName() + " Node " + root.getData(Main.NODE_ID);
    }
}
