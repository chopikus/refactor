import at.unisalzburg.dbresearch.apted.costmodel.CostModel;
import at.unisalzburg.dbresearch.apted.node.Node;

class Cost implements CostModel<NodeData> {
    private final float delCost = 5.0f;
    private final float insCost = 5.0f;
    private final float renCost = 1.0f;

    public float del(Node<NodeData> n) {
        return delCost;
    }
    public float ins(Node<NodeData> n) {
        return insCost;
    }
    public float ren(Node<NodeData> n1, Node<NodeData> n2) {
        return (n1.getNodeData().getLabel().equals(n2.getNodeData().getLabel())) ? 0.0f : renCost;
    }
}

class NodeData
{
    private String label="";
    public NodeData(String label)
    {
        this.label = label;
    }
    public String getLabel()
    {
        return label;
    }
}