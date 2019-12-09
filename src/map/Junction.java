package map;

import java.util.ArrayList;
import java.util.List;

public class Junction {
    private final static double DISTANCE_OFFSET = 50;

    private Node bottomNode;
    private Node rightNode;
    private Node leftNode;

    private Position pos;

    public Junction(Position pos) {
        this.pos = pos;

        Position posBottom = new Position(pos);
        Position posRight = new Position(pos);
        Position posLeft = new Position(pos);

        posBottom.add(new Position(0, DISTANCE_OFFSET));
        posRight.add(new Position(DISTANCE_OFFSET, 0));
        posLeft.subtract(new Position(DISTANCE_OFFSET, 0));

        bottomNode = new Node(posBottom);
        rightNode = new Node(posRight);
        leftNode = new Node(posLeft);

        Connection botToRight = new Connection(bottomNode, rightNode, Direction.RIGHT, 0, false, pos);
        Connection rightToBot = new Connection(bottomNode, rightNode, Direction.LEFT, 0, false, pos);

        Connection rightToLeft = new Connection(leftNode, rightNode, Direction.STRAIGHT, 0, false, pos);
        Connection leftToRight = new Connection(leftNode, rightNode, Direction.STRAIGHT, 0, false, pos);

        Connection leftToBot = new Connection(leftNode, bottomNode, Direction.RIGHT, 0, false, pos);
        Connection botToLeft = new Connection(leftNode, bottomNode, Direction.RIGHT, 0, false, pos);

        bottomNode.addNeighbor(botToLeft);
        bottomNode.addNeighbor(botToRight);

        rightNode.addNeighbor(rightToBot);
        rightNode.addNeighbor(rightToLeft);

        leftNode.addNeighbor(leftToBot);
        leftNode.addNeighbor(leftToRight);
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

    public Position getPos() {
        return pos;
    }

    public void setPos(Position pos) {
        this.pos = pos;

        Position posBottom = new Position(pos);
        Position posRight = new Position(pos);
        Position posLeft = new Position(pos);

        posBottom.add(new Position(0, DISTANCE_OFFSET));
        posRight.add(new Position(DISTANCE_OFFSET, 0));
        posLeft.subtract(new Position(DISTANCE_OFFSET, 0));


        bottomNode.setPosition(posBottom);
        leftNode.setPosition(posLeft);
        rightNode.setPosition(posRight);

        for (Connection c : getInternalNeighbors()) {
            c.setMidPoint(pos);
        }
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
}
