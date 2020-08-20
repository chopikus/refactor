import at.unisalzburg.dbresearch.apted.costmodel.CostModel;
import at.unisalzburg.dbresearch.apted.node.Node;

class Cost implements CostModel<NodeData> {
    private float delCost = 1.0f;
    private float insCost = 1.0f;
    private float renCost = 1.0f;

    public float del(Node<NodeData> n) {
        if (n.getNodeData().isExprStmt())
            return 3*delCost;
        return delCost;
    }
    public float ins(Node<NodeData> n) {
        if (n.getNodeData().isExprStmt())
            return 3*insCost;
        return insCost;
    }
    public float ren(Node<NodeData> n1, Node<NodeData> n2) {
        return (n1.getNodeData().getLabel().equals(n2.getNodeData().getLabel())) ? 0.0f : renCost;
    }

    public void setDelCost(float f)
    {
        this.delCost = f;
    }

    public void setInsCost(float f)
    {
        this.insCost = f;
    }

    public void setRenCost(float f)
    {
        this.renCost = f;
    }
}


class NodeData
{
    private String label="";
    private boolean exprStmt = false;
    public NodeData(String label, Boolean expr)
    {
        this.label = label;
        this.exprStmt = expr;
    }
    public String getLabel()
    {
        return label;
    }
    public boolean isExprStmt(){return exprStmt;};
}