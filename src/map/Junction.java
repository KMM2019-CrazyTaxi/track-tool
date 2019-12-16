package map;

import helpers.DataConversionHelper;

import java.util.ArrayList;
import java.util.List;

public class Junction {
    private final static double DISTANCE_OFFSET = 50;

    private Node bottomNode;
    private Node rightNode;
    private Node leftNode;

    private Position pos;
    private double rotation;

    public Junction(Position pos) {
        this.rotation = 0;

        bottomNode = new Node(new Position());
        rightNode = new Node(new Position());
        leftNode = new Node(new Position());

        Connection botToRight = new Connection(bottomNode, rightNode, Direction.RIGHT, 0, pos);
        Connection rightToBot = new Connection(bottomNode, rightNode, Direction.LEFT, 0, pos);

        Connection rightToLeft = new Connection(leftNode, rightNode, Direction.STRAIGHT, 0, pos);
        Connection leftToRight = new Connection(leftNode, rightNode, Direction.STRAIGHT, 0, pos);

        Connection leftToBot = new Connection(leftNode, bottomNode, Direction.RIGHT, 0, pos);
        Connection botToLeft = new Connection(leftNode, bottomNode, Direction.LEFT, 0, pos);

        bottomNode.addNeighbor(botToLeft);
        bottomNode.addNeighbor(botToRight);

        rightNode.addNeighbor(rightToBot);
        rightNode.addNeighbor(rightToLeft);

        leftNode.addNeighbor(leftToBot);
        leftNode.addNeighbor(leftToRight);

        setPos(pos);
    }

    public Junction(Position pos, double rotation) {
        this(pos);
        this.rotation = rotation;
        setPos(pos);
    }

    public Node getBottomNode() {
        return bottomNode;
    }

    public void setBottomNode(Node bottomNode) {
        this.bottomNode = bottomNode;
    }

    public Node getRightNode() {
        return rightNode;
    }

    public void setRightNode(Node rightNode) {
        this.rightNode = rightNode;
    }

    public Node getLeftNode() {
        return leftNode;
    }

    public void setLeftNode(Node leftNode) {
        this.leftNode = leftNode;
    }

    public Position getPosition() {
        return pos;
    }

    public void setPos(Position pos) {
        this.pos = pos;

        Position posBottom = new Position(pos);
        Position posRight = new Position(pos);
        Position posLeft = new Position(pos);

        Position offset = new Position(0, DISTANCE_OFFSET);
        offset.rotate(rotation);

        posBottom.add(offset);

        offset.rotate(Math.PI / 2);
        posRight.subtract(offset);
        posLeft.add(offset);

        bottomNode.setPosition(posBottom);
        leftNode.setPosition(posLeft);
        rightNode.setPosition(posRight);

        for (Connection c : getInternalNeighbors()) {
            c.setMidPoint(pos);
        }
    }

    public void rotatePos(double rotation) {
        this.rotation = rotation;
        setPos(pos);
    }

    private List<Connection> getInternalNeighbors() {
        List<Connection> cons = new ArrayList<>();

        for (Connection c : bottomNode.getNeighbors()) {
            if (c.getConnectingNode(bottomNode) == rightNode || c.getConnectingNode(bottomNode) == leftNode)
                cons.add(c);
        }

        for (Connection c : rightNode.getNeighbors()) {
            if (c.getConnectingNode(rightNode) == bottomNode || c.getConnectingNode(rightNode) == leftNode)
                cons.add(c);
        }

        for (Connection c : leftNode.getNeighbors()) {
            if (c.getConnectingNode(leftNode) == rightNode || c.getConnectingNode(leftNode) == bottomNode)
                cons.add(c);
        }

        return cons;
    }

    public List<Connection> getExternalNeighbors() {
        List<Connection> cons = new ArrayList<>();

        for (Connection c : bottomNode.getNeighbors()) {
            if (c.getConnectingNode(bottomNode) != rightNode && c.getConnectingNode(bottomNode) != leftNode)
                cons.add(c);
        }

        for (Connection c : rightNode.getNeighbors()) {
            if (c.getConnectingNode(rightNode) != bottomNode && c.getConnectingNode(rightNode) != leftNode)
                cons.add(c);
        }

        for (Connection c : leftNode.getNeighbors()) {
            if (c.getConnectingNode(leftNode) != rightNode && c.getConnectingNode(leftNode) != bottomNode)
                cons.add(c);
        }

        return cons;
    }

    public int getIndex(Node n) {
        if (n == bottomNode)
            return 0;
        if (n == rightNode)
            return 1;
        if (n == leftNode)
            return 2;
        return -1;
    }

    public Node getNode(int index) {
        switch (index) {
            case 0:
                return bottomNode;
            case 1:
                return rightNode;
            case 2:
                return leftNode;
            default:
                throw new IllegalArgumentException("Given index is out of range (" + index + ").");
        }
    }

    public int exportableByteSize() {
        return 3 * 8;
    }

    public byte[] toExport() {
        byte[] bytes = new byte[this.exportableByteSize()];

        byte[] posXBytes = DataConversionHelper.doubleToByteArray(pos.x);
        byte[] posYBytes = DataConversionHelper.doubleToByteArray(pos.y);
        byte[] rotationBytes = DataConversionHelper.doubleToByteArray(rotation);

        System.arraycopy(posXBytes, 0, bytes, 0, 8);
        System.arraycopy(posYBytes, 0, bytes, 8, 8);
        System.arraycopy(rotationBytes, 0, bytes, 16, 8);

        return bytes;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
        setPos(this.pos);
    }
}
