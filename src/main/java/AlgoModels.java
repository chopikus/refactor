import at.unisalzburg.dbresearch.apted.costmodel.CostModel;
import at.unisalzburg.dbresearch.apted.node.Node;

class Cost implements CostModel<NodeData> {
    private float delCost = 3.0f;
    private float insCost = 3.0f;
    private float renCost = 2.0f;

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