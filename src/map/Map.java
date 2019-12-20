package map;

import helpers.DataConversionHelper;

import java.util.ArrayList;
import java.util.List;

public class Map {
    private List<Node> nodes;
    private List<Junction> junctions;

    public Map() {
        nodes = new ArrayList<>();
        junctions = new ArrayList<>();
    }

    public Map(byte[] bytes) {
        this();

        int numberOfJunctions = DataConversionHelper.byteArrayToUnsignedInt(bytes, 0, 2);
        int numberOfNodes = DataConversionHelper.byteArrayToUnsignedInt(bytes, 2, 2);
        int numberOfConnections = DataConversionHelper.byteArrayToUnsignedInt(bytes, 4, 2);

        int offset = 6;
        
        for (int i = 0; i < numberOfJunctions; i++) {

            double x = DataConversionHelper.byteArrayToDouble(bytes, offset);
            offset += 8;
            
            double y = DataConversionHelper.byteArrayToDouble(bytes, offset);
            offset += 8;
            
            double rot = DataConversionHelper.byteArrayToDouble(bytes, offset);
            offset += 8;

            Position pos = new Position(x, y);
            Junction j = new Junction(pos, rot);
            addJunction(j);
        }
        
        for (int i = 0; i < numberOfNodes; i++) {
            double x = DataConversionHelper.byteArrayToDouble(bytes, offset);
            offset += 8;
            
            double y = DataConversionHelper.byteArrayToDouble(bytes, offset);
            offset += 8;

            Position pos = new Position(x, y);
            Node n = new Node(pos);
            addNode(n);
        }
        
        for (int i = 0; i < numberOfConnections; i++) {
            int fromIndex = DataConversionHelper.byteArrayToUnsignedInt(bytes, offset, 1);
            offset += 1;
            
            int toIndex = DataConversionHelper.byteArrayToUnsignedInt(bytes, offset, 1);
            offset += 1;

            int distance = DataConversionHelper.byteArrayToUnsignedInt(bytes, offset, 2);
            offset += 2;

            Direction direction = Direction.fromByte(bytes[offset]);
            offset += 1;

            double x = DataConversionHelper.byteArrayToDouble(bytes, offset);
            offset += 8;

            double y = DataConversionHelper.byteArrayToDouble(bytes, offset);
            offset += 8;

            Node fromNode = getNode(fromIndex);
            Node toNode = getNode(toIndex);

            Connection c = getConnection(fromNode, toNode);

            if (c == null) {
                c = new Connection(getNode(fromIndex), getNode(toIndex), direction, distance, new Position(x, y));
                fromNode.addNeighbor(c);
                toNode.addNeighbor(c);
            }
        }
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
        if (index > junctions.size())
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

    public void addAll(List<Node> nodes) {
        this.nodes.addAll(nodes);
    }

    public byte[] toExport() {
        byte[] bytes = new byte[this.exportableByteSize()];
        byte[] juncSizeBytes = DataConversionHelper.intToByteArray(junctions.size(), 2);
        byte[] nodeSizeBytes = DataConversionHelper.intToByteArray(nodes.size(), 2);

        bytes[0] = juncSizeBytes[0];
        bytes[1] = juncSizeBytes[1];

        bytes[2] = nodeSizeBytes[0];
        bytes[3] = nodeSizeBytes[1];

        int offset = 6;

        for (Junction j : junctions) {
            int juncSize = j.exportableByteSize();
            System.arraycopy(j.toExport(), 0, bytes, offset, juncSize);
            offset += juncSize;
        }

        for (Node n : nodes) {
            int nodeSize = n.exportableByteSize();
            System.arraycopy(n.toExport(), 0, bytes, offset, nodeSize);
            offset += nodeSize;
        }

        int connections = 0;

        for (Junction j : junctions) {
            for (Connection c : j.getBottomNode().getNeighbors()) {
                int conSize = c.exportableByteSize();

                System.arraycopy(c.toExport(this, j.getBottomNode()), 0, bytes, offset, conSize);
                offset += conSize;
                connections++;
            }

            for (Connection c : j.getRightNode().getNeighbors()) {
                int conSize = c.exportableByteSize();
                System.arraycopy(c.toExport(this, j.getRightNode()), 0, bytes, offset, conSize);
                offset += conSize;
                connections++;
            }

            for (Connection c : j.getLeftNode().getNeighbors()) {
                int conSize = c.exportableByteSize();
                System.arraycopy(c.toExport(this, j.getLeftNode()), 0, bytes, offset, conSize);
                offset += conSize;
                connections++;
            }
        }

        for (Node n : nodes) {
            for (Connection c : n.getNeighbors()) {
                int conSize = c.exportableByteSize();
                System.arraycopy(c.toExport(this, n), 0, bytes, offset, conSize);
                offset += conSize;
                connections++;
            }
        }

        byte[] connectionSizeBytes = DataConversionHelper.intToByteArray(connections, 2);

        bytes[4] = connectionSizeBytes[0];
        bytes[5] = connectionSizeBytes[1];

        return bytes;
    }

    public int exportableByteSize() {
        int sum = 6;
        for (Junction j : junctions) {
            sum += j.exportableByteSize();
        }
        for (Node n : nodes) {
            sum += n.exportableByteSize();
        }

        for (Junction j : junctions) {
            for (Connection c : j.getBottomNode().getNeighbors()) {
                sum += c.exportableByteSize();
            }

            for (Connection c : j.getRightNode().getNeighbors()) {
                sum += c.exportableByteSize();
            }

            for (Connection c : j.getLeftNode().getNeighbors()) {
                sum += c.exportableByteSize();
            }
        }

        for (Node n : nodes) {
            for (Connection c : n.getNeighbors()) {
                sum += c.exportableByteSize();
            }
        }

        return sum;
    }
}
