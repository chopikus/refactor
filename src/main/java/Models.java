import at.unisalzburg.dbresearch.apted.costmodel.CostModel;
import at.unisalzburg.dbresearch.apted.node.Node;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.utils.Pair;
import org.apache.commons.math3.analysis.UnivariateFunction;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

class Cost implements CostModel<NodeData> {
    private float delCost = 100.0f;
    private float insCost = 100.0f;
    private float renCost = 0.0f;

    public float del(Node<NodeData> n) {
        //if (n.getNodeData().isStmt())
        //    return 3*delCost;
        return delCost;
    }
    public float ins(Node<NodeData> n) {
        //if (n.getNodeData().isStmt())
        //    return 3*insCost;
        return insCost;
    }
    public float ren(Node<NodeData> n1, Node<NodeData> n2) {
        return (n1.getNodeData().getLabel().equals(n2.getNodeData().getLabel())) ? 0.0f : renCost;
    }
}


class NodeData
{
    private Integer label=0;
    public NodeData(Integer label)
    {
        this.label = label;
    }
    public Integer getLabel()
    {
        return label;
    }
    public void setLabel(Integer a){this.label = a;}
}

class Piece
{
    com.github.javaparser.ast.Node node;
    int hash=0;
    boolean isReplaced = false;

    Piece(com.github.javaparser.ast.Node node) {
        this.node = node;
        this.hash = hashPiece();
    }

    static boolean checkBranching(com.github.javaparser.ast.Node node)
    {
        return (node instanceof ForStmt || node instanceof WhileStmt || node instanceof ForEachStmt || node instanceof DoStmt || node instanceof IfStmt || node instanceof TryStmt);
    }

    static boolean isMultipleCondition(com.github.javaparser.ast.Node expression)
    {
        if (!(expression instanceof BinaryExpr))
            return false;
        BinaryExpr.Operator operator = ((BinaryExpr) expression).getOperator();
        return operator.compareTo(BinaryExpr.Operator.AND)==0 || operator.compareTo(BinaryExpr.Operator.OR)==0;
    }

    static boolean checkPiece(com.github.javaparser.ast.Node node) {
        if (isMultipleCondition(node))
            return false;
        if (node instanceof Expression &&
                node.getParentNode().isPresent() && isMultipleCondition(node.getParentNode().get()))
            return true;
        return (node instanceof ExpressionStmt ||
                (node instanceof Expression && node.getParentNode().isPresent()
                        && checkBranching(node.getParentNode().get())));
    }

    public int hashCode()
    {
        return hash;
    }

    private int hashPiece()
    {
        final String[] s = {""};
        node.walk(com.github.javaparser.ast.Node.TreeTraversal.PREORDER, node1 -> {
            s[0]+=node1.getMetaModel().getTypeNameGenerified();
            if (node1 instanceof BinaryExpr)
                s[0]+=((BinaryExpr) node1).getOperator();
            if (node1 instanceof UnaryExpr)
                s[0]+=((UnaryExpr) node1).getOperator();
            if (node1 instanceof LiteralExpr)
                s[0]+=((LiteralExpr) node1).calculateResolvedType();
        });
        return s[0].hashCode();
    }
}

class Block
{
    List<Piece> list;
    BlockStmt root;

    Block(BlockStmt stmt)
    {
        this.root = stmt;
        this.list = new ArrayList<>();
        stmt.walk(com.github.javaparser.ast.Node.TreeTraversal.PREORDER, node -> {
           if (Piece.checkPiece(node))
               this.list.add(new Piece(node));
        });
    }

    Node<NodeData> algoGraph(int l, int r)
    {
        assert(l>=0 && l<list.size() && r>=l && r<=list.size());
        Queue<Pair<com.github.javaparser.ast.Node, Integer> > queue = new ArrayDeque<>();
        Node<NodeData> algoNode = new Node<>(new NodeData(root.getData(Main.NODE_ID)));
        for (int i=l; i<r; i++)
        {
            com.github.javaparser.ast.Node pieceNode = list.get(i).node;
            Integer hash = list.get(i).hash;
            queue.add(new Pair<>(pieceNode, hash));
        }
        Map<Integer, List<Integer>> edges = new TreeMap<>();
        Map<Integer, Integer> hashByNodeId = new HashMap<>();
        Map<Integer, Boolean> used = new TreeMap<>();
        while (!queue.isEmpty())
        {
            Pair<com.github.javaparser.ast.Node, Integer> p = queue.poll();
            hashByNodeId.put(p.a.getData(Main.NODE_ID), p.b);
            AtomicBoolean broke = new AtomicBoolean(false);
            used.put(p.a.getData(Main.NODE_ID), true);
            p.a.walk(com.github.javaparser.ast.Node.TreeTraversal.PARENTS, parent->{
                if (parent instanceof Statement && !broke.get())
                {
                    if (!parent.getData(Main.NODE_ID).equals(root.getData(Main.NODE_ID))
                            && !used.getOrDefault(parent.getData(Main.NODE_ID), false)) {
                        queue.add(new Pair<>(parent, parent.getMetaModel().getTypeNameGenerified().hashCode()));
                        used.put(parent.getData(Main.NODE_ID), true);
                    }
                    List<Integer> list = edges.getOrDefault(parent.getData(Main.NODE_ID), new ArrayList<>());
                    list.add(p.a.getData(Main.NODE_ID));
                    edges.put(parent.getData(Main.NODE_ID), list);
                    broke.set(true);
                }
            });
        }
        System.out.println(edges);
        Queue<Node<NodeData>> algoQ = new ArrayDeque<>();
        algoQ.add(algoNode);
        while (!algoQ.isEmpty())
        {
            Node<NodeData> q1 = algoQ.poll();
            for (Integer q2 : edges.getOrDefault(q1.getNodeData().getLabel(), new ArrayList<>()))
            {
                Node<NodeData> nd = new Node<NodeData>(new NodeData(q2));
                q1.addChild(nd);
                algoQ.add(nd);
            }
            q1.getNodeData().setLabel(hashByNodeId.getOrDefault(q1.getNodeData().getLabel(), -1));
        }
        return algoNode;
    }
}

class SimilarityFunction implements UnivariateFunction
{

    public static float dist(int b1, int i1, int b2, int i2, int l)
    {
        assert(b1>=0 && b1<Main.blocks.size() && b2>=0 && b2<Main.blocks.size());
        assert(i1>=0 && i1<Main.blocks.get(b1).list.size() && i2>=0 && i2<Main.blocks.get(b2).list.size());
        int j1 = Math.min(i1+l, Main.blocks.size());
        int j2 = Math.min(i2+l, Main.blocks.size());
        Node<NodeData> graph1 = Main.blocks.get(b1).algoGraph(i1, j1);
        Node<NodeData> graph2 = Main.blocks.get(b1).algoGraph(i2, j2);
        return Main.apted.computeEditDistance(graph1, graph2);
    }

    @Override
    public double value(double x) {
        long x1 = Math.round(x);
        List<Node<NodeData>> graphs = new ArrayList<>();
        for (Pair<Integer, Integer> p : Main.piecesToCompare)
        {
            graphs.add(Main.blocks.get(p.a).algoGraph(p.b, Math.toIntExact(p.b + x1)));
        }
        if (graphs.size()==0)
            return 0;
        float res = 0;
        for (int i=1; i<graphs.size(); i++)
        {
            float distance = Main.apted.computeEditDistance(graphs.get(0), graphs.get(1));
            if (distance<=Main.threshold)
            {
                res+=(Main.threshold-distance)/Main.threshold;
            }
        }
        res*=x;
        return res;
    }
}