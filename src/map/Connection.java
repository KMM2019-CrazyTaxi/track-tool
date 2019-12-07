package map;

import helpers.DataConversionHelper;

public class Connection {
    // Byte size as per definition in connection protocol
    private final static int BYTE_SIZE = 5;

    private Node node1;
    private Node node2;
    private Direction direction;
    private int distance;
    private boolean stopable;

    private Position midPoint;

    public Connection(Node node1, Node node2, Direction direction, int distance, boolean stopable, Position midPoint) {
        this.node1 = node1;
        this.node2 = node2;
        this.direction = direction;
        this.distance = distance;
        this.stopable = stopable;
        this.midPoint = midPoint;
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
}
