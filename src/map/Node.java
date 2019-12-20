package map;

import helpers.DataConversionHelper;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private static int created = 0;

    private List<Connection> neighbors;

    private Position position;
    public int id;

    public Node(Position position) {
        this.position = position;
        this.id = created++;
        neighbors = new ArrayList<>();
    }

    public Node(Position position, List<Connection> neighbors) {
        this.neighbors = neighbors;
        this.position = position;
    }

    public Node(Node node) {
        this.position = new Position(node.position);

        this.neighbors = new ArrayList<>();

        for (Connection c : node.getNeighbors()) {
            this.neighbors.add(new Connection(c));
        }
    }

    public void addNeighbor(Connection neighbor) {
        neighbors.add(neighbor);
    }

    public void addAllNeighbors(Connection... neighbors) {
        for (Connection c : neighbors) {
            addNeighbor(c);
        }
    }

    public void removeNeighbor(Connection c) {
        this.neighbors.remove(c);
    }

    public List<Connection> getNeighbors() {
        return neighbors;
    }

    public int getIndex(Map map) {
        return map.getIndex(this);
    }

    public int byteSize() {
        return 1 + Connection.BYTE_SIZE * neighbors.size();
    }

    public Position getPosition() {
        return position;
    }

    public byte[] toBytes(Map map) {
        byte[] bytes = new byte[this.byteSize()];

        bytes[0] = DataConversionHelper.intToByteArray(neighbors.size(), 1)[0];

        int offset = 1;
        for (Connection c : neighbors) {
            int connectionSize = c.byteSize();
            System.arraycopy(c.toBytes(map, this), 0, bytes, offset, connectionSize);
            offset += connectionSize;
        }

        return bytes;
    }

    public void setPosition(Position newPos) {
        this.position = newPos;
    }

    public void removeNeighbor(Node node) {
        List<Connection> removedCons = new ArrayList<>();

        for (Connection c : neighbors) {
            if (c.getConnectingNode(this) == node)
                removedCons.add(c);
        }

        for (Connection c : removedCons) {
            this.removeNeighbor(c);
        }
    }

    public Connection getNeighbor(Node node) {
        for (Connection c : neighbors) {
            if (c.getConnectingNode(this) == node)
                return c;
        }
        return null;
    }

    @Override
    public String toString() {
        return "Node{" +
                ", id=" + id +
                "neighbors=" + neighbors +
                ", position=" + position +
                '}';
    }

    public int exportableByteSize() {
        return 2 * 8;
    }

    public byte[] toExport() {
        byte[] bytes = new byte[this.exportableByteSize()];

        byte[] posXBytes = DataConversionHelper.doubleToByteArray(position.x);
        byte[] posYBytes = DataConversionHelper.doubleToByteArray(position.y);

        System.arraycopy(posXBytes, 0, bytes, 0, 8);
        System.arraycopy(posYBytes, 0, bytes, 8, 8);

        return bytes;
    }
}
