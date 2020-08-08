import com.github.javaparser.ast.Node;
import com.github.javaparser.utils.Pair;

import java.util.*;

public class DSU {
    static HashMap<Integer, Node> parent = new HashMap<>();
    static HashMap<Integer, Node> nodeByID = new HashMap<>();

    static Node get(Node node)
    {
        nodeByID.put(node.getData(Main.NODE_ID), node);
        int id = node.getData(Main.NODE_ID);
        if (!parent.containsKey(id)) {
            parent.put(node.getData(Main.NODE_ID), node);
            return node;
        }
        if (parent.get(id).getData(Main.NODE_ID).equals(id))
            return node;
        Node res = get(parent.get(id));
        parent.put(node.getData(Main.NODE_ID), res);
        return res;
    }

    static void unite(Node node1, Node node2)
    {
        nodeByID.put(node1.getData(Main.NODE_ID), node1);
        nodeByID.put(node2.getData(Main.NODE_ID), node2);
        node1 = get(node1);
        node2 = get(node2);
        if (!Objects.equals(node1.getData(Main.NODE_ID), node2.getData(Main.NODE_ID)))
        {
            parent.put(node1.getData(Main.NODE_ID), node2);
        }
    }

    static void reset()
    {
        parent.clear();
        nodeByID.clear();
    }

    static List<List<Node>> getAllSubsets()
    {
        HashMap<Integer, ArrayList<Node>> res = new HashMap<>();
        for (Map.Entry<Integer, Node> entry : parent.entrySet())
        {
            Node child = nodeByID.get(entry.getKey());
            Integer parID = get(child).getData(Main.NODE_ID);
            ArrayList<Node> old = res.get(parID);
            if (old==null)
                old = new ArrayList<>();
            old.add(child);
            res.put(parID, old);
        }
        List<List<Node> > ret = new ArrayList<>();
        for (Map.Entry<Integer, ArrayList<Node> > entry : res.entrySet())
        {
            ret.add(entry.getValue());
        }
        return ret;
    }
}
