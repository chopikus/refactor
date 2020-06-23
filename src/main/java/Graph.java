import com.github.javaparser.ast.Node;
import com.github.javaparser.utils.Pair;

import java.util.ArrayList;

public class Graph {
    ArrayList<Pair<Node, Node>> edges = new ArrayList<>();
    String uniqueID="";
    void addEdge(Node from, Node to)
    {
        edges.add(new Pair<>(from, to));
    }
    void export()
    {
        for (Pair<Node, Node> edge : edges)
            DotExporter.addEdge(uniqueID, edge.a, edge.b);
    }
    public Graph(String uniqueID) {
        this.uniqueID = uniqueID;
    }
    ///TODO compare with other graph
}
