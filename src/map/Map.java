package map;

import helpers.DataConversionHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Map {
    private List<Node> nodes;
    private List<Junction> junctions;

    public Map() {
        nodes = new ArrayList<>();
        junctions = new ArrayList<>();
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void addJunction(Junction junc) {
        junctions.add(junc);
    }

    public List<Junction> getJunctions() {
        return junctions;
    }

    public int getIndex(Node n) {
        int index = nodes.indexOf(n);
        if (index == -1) {
            for (Junction j : junctions) {
                index = j.getIndex(n);
                if (index != -1)
                    return nodes.size() + getIndex(j) * 3 + index;
            }
            throw new IllegalArgumentException("Given Node not found in map.");
        }

        return index;
    }

    public int getIndex(Junction j) {
        int index = junctions.indexOf(j);
        if (index == -1)
            throw new IllegalArgumentException("Given Junction not found in map.");
        return index;
    }

    public int byteSize() {
        int sum = 2;
        for (Node n : nodes) {
            sum += n.byteSize();
        }
        return sum;
    }

    public byte[] toBytes() {
        byte[] bytes = new byte[this.byteSize()];
        byte[] sizeBytes = DataConversionHelper.intToByteArray(nodes.size(), 2);;

        bytes[0] = sizeBytes[0];
        bytes[1] = sizeBytes[1];

        int offset = 2;
        for (Node n : nodes) {
            int nodeSize = n.byteSize();
            System.arraycopy(n.toBytes(this), 0, bytes, offset, nodeSize);
            offset += nodeSize;
        }

        return bytes;
    }

    public Node getNode(int index) {
        if (index > (nodes.size() + junctions.size() * 3))
            throw new IllegalArgumentException("Given index is out of range (" + index + ").");

        if (index < nodes.size())
            return nodes.get(index);

        int div = (index - nodes.size()) / 3;
        int rest = (index - nodes.size()) % 3;

        return getJunction(div).getNode(rest);
    }

    public void removeNode(Node node) {
        for (Connection c : node.getNeighbors()) {
            Node n = c.getConnectingNode(node);
            n.removeNeighbor(c);
        }
        nodes.remove(node);
    }

    public Connection getConnection(Node node1, Node node2) {
        for (Connection c : node1.getNeighbors()) {
            if (c.getConnectingNode(node1) == node2) {
                return c;
            }
        }
        return null;
    }

    public Junction getJunction(int index) {
        if (index > nodes.size())
            throw new IllegalArgumentException("Given index is out of range (" + index + ").");
        return junctions.get(index);
    }

    public void removeJunction(Junction junc) {
        Node bottomNode = junc.getBottomNode();
        Node rightNode = junc.getRightNode();
        Node leftNode = junc.getLeftNode();

        for (Connection c : bottomNode.getNeighbors()) {
            if (c.getConnectingNode(bottomNode) != rightNode && c.getConnectingNode(bottomNode) != leftNode)
                c.getConnectingNode(bottomNode).removeNeighbor(c);
        }

        for (Connection c : rightNode.getNeighbors()) {
            if (c.getConnectingNode(rightNode) != bottomNode && c.getConnectingNode(rightNode) != leftNode)
                c.getConnectingNode(rightNode).removeNeighbor(c);
        }

        for (Connection c : leftNode.getNeighbors()) {
            if (c.getConnectingNode(leftNode) != rightNode && c.getConnectingNode(leftNode) != bottomNode)
                c.getConnectingNode(leftNode).removeNeighbor(c);
        }

        junctions.remove(junc);
    }
}
