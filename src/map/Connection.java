package map;

import helpers.DataConversionHelper;
import javafx.util.Pair;

public class Connection {
    // Byte size as per definition in connection protocol
    public final static int BYTE_SIZE = 5;

    public static final int EXPORT_SIZE = 6 + 2 * 8;

    private Node node1;
    private Node node2;
    private Direction direction;
    private int distance;
    private boolean stopable;

    private Position midPoint;

    public Connection(Node node1, Node node2, Direction direction, int distance, boolean stopable) {
        this.node1 = node1;
        this.node2 = node2;
        this.direction = direction;
        this.distance = distance;
        this.stopable = stopable;

        // Average the positions of connecting nodes
        Position avg = new Position(node1.getPosition());
        avg.add(node2.getPosition());
        avg.divide(2);

        this.midPoint = avg;
    }

    public Connection(Node node1, Node node2, Direction direction, int distance, boolean stopable, Position midPoint) {
        this.node1 = node1;
        this.node2 = node2;
        this.direction = direction;
        this.distance = distance;
        this.stopable = stopable;
        this.midPoint = midPoint;
    }

    public Connection(Connection c) {
        this.node1 = c.node1;
        this.node2 = c.node2;
        this.direction = c.direction;
        this.distance = c.distance;
        this.stopable = c.stopable;
        this.midPoint = c.midPoint;
    }

    public int byteSize() {
        return BYTE_SIZE;
    }

    public Node getConnectingNode(Node self) {
        if (self == node1)
            return node2;
        return node1;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getDistance() {
        return distance;
    }

    public boolean isStopable() {
        return stopable;
    }

    public Position getMidPoint() {
        return midPoint;
    }

    public byte[] toBytes(Map map, Node self) {
        byte[] bytes = new byte[this.byteSize()];

        bytes[0] = DataConversionHelper.intToByteArray(getConnectingNode(self).getIndex(map), 1)[0];
        bytes[1] = DataConversionHelper.intToByteArray(distance, 2)[0];
        bytes[2] = DataConversionHelper.intToByteArray(distance, 2)[1];
        bytes[3] = (byte) (stopable ? 1 : 0);
        bytes[4] = direction.code();

        return bytes;
    }

    public void setMidPoint(Position newPos) {
        this.midPoint = newPos;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public Pair<Node, Node> getNodes() {
        return new Pair<>(node1, node2);
    }

    public byte[] toExport(Map map, Node self) {
        byte[] bytes = new byte[this.exportableByteSize()];

        bytes[0] = DataConversionHelper.intToByteArray(self.getIndex(map), 1)[0];
        bytes[1] = DataConversionHelper.intToByteArray(getConnectingNode(self).getIndex(map), 1)[0];
        bytes[2] = DataConversionHelper.intToByteArray(distance, 2)[0];
        bytes[3] = DataConversionHelper.intToByteArray(distance, 2)[1];
        bytes[4] = (byte) (stopable ? 1 : 0);
        bytes[5] = direction.code();

        byte[] posXBytes = DataConversionHelper.doubleToByteArray(midPoint.x);
        byte[] posYBytes = DataConversionHelper.doubleToByteArray(midPoint.y);

        System.arraycopy(posXBytes, 0, bytes, 6, 8);
        System.arraycopy(posYBytes, 0, bytes, 14, 8);

        return bytes;
    }

    public int exportableByteSize() {
        return EXPORT_SIZE;
    }
}
