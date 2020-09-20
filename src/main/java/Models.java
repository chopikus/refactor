import at.unisalzburg.dbresearch.apted.costmodel.CostModel;
import at.unisalzburg.dbresearch.apted.node.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private String label="";
    private boolean stmt = false;
    public NodeData(String label, Boolean stmt)
    {
        this.label = label;
        this.stmt = stmt;
    }
    public String getLabel()
    {
        return label;
    }
    public boolean isStmt(){return stmt;};
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
    BlockStmt stmt;
    Block(BlockStmt stmt)
    {
        this.stmt = stmt;
        this.list = new ArrayList<>();
        stmt.walk(com.github.javaparser.ast.Node.TreeTraversal.PREORDER, node -> {
           if (Piece.checkPiece(node))
               this.list.add(new Piece(node));
        });
    }
}