package map;

public class SplitNode {
    private Node node;
    private Node sibling;
    private Integer junctionID;
    private boolean isSplit;


    public SplitNode(Node node) {
        this.node = node;
        this.sibling = node;
        this.junctionID = null;
        this.isSplit = false;
    }

    public SplitNode(Node node, int junctionID) {
        this.node = node;
        this.sibling = node;
        this.junctionID = junctionID;
        this.isSplit = false;
    }

    private SplitNode(Node node, int junctionID, Node sibling) {
        this.node = node;
        this.sibling = sibling;
        this.junctionID = junctionID;
        this.isSplit = false;
    }

    public SplitNode split() {
        if (isSplit)
            throw new RuntimeException("Tried to resplit node");

        SplitNode splt = new SplitNode(new Node(new Position(this.node.getPosition())), this.junctionID, this.node);
        this.sibling = splt.getNode();

        this.isSplit = true;
        splt.isSplit = true;

        return splt;
    }

    public Node getNode() {
        return node;
    }

    public Node getSibling() {
        return sibling;
    }

    public Integer getJunctionID() {
        return junctionID;
    }

    public void setJunctionID(Integer junctionID) {
        this.junctionID = junctionID;
    }

    @Override
    public String toString() {
        return "SplitNode{" +
                "node=" + node +
                ", sibling=" + sibling +
                ", junctionID=" + junctionID +
                ", isSplit=" + isSplit +
                '}';
    }
}
