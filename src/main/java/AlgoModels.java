import at.unisalzburg.dbresearch.apted.costmodel.CostModel;
import at.unisalzburg.dbresearch.apted.node.Node;

class Cost implements CostModel<NodeData> {
    private float delCost = 3.0f;
    private float insCost = 3.0f;
    private float renCost = 2.0f;

    public float del(Node<NodeData> n) {
        return delCost;
    }
    public float ins(Node<NodeData> n) {
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
    public NodeData(String label)
    {
        this.label = label;
    }
    public String getLabel()
    {
        return label;
    }
}