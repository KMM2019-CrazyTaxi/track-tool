import editor.Editor;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import map.*;

public class CTTool extends Application {
    public static void main(String[] args) {
        launch();
    }

    public void start(Stage stage) throws Exception {
        Stage root = FXMLLoader.load(getClass().getResource("gui/fxml/UI.fxml"));
        root.show();

        // Map test
        Map map = new Map();
        Node node0 = new Node(new Position(0, 0));
        Node node1 = new Node(new Position(0, 200));
        Node node2 = new Node(new Position(300, 0));
        Node node3 = new Node(new Position(300, 200));

        Junction junc1 = new Junction(new Position(150, 0));
        Junction junc2 = new Junction(new Position(150, 200));

        junc2.rotatePos(Math.PI);

        Connection con1 = new Connection(node0, node1, Direction.STRAIGHT, 100, false, new Position(-100, 100));
        Connection con2 = new Connection(node0, junc1.getLeftNode(), Direction.STRAIGHT, 100, false);
        Connection con3 = new Connection(node1, junc2.getRightNode(), Direction.STRAIGHT, 100, false);
        Connection con4 = new Connection(node2, node3, Direction.STRAIGHT, 100, false, new Position(400, 100));
        Connection con5 = new Connection(node2, junc1.getRightNode(), Direction.STRAIGHT, 100, false);
        Connection con6 = new Connection(node3, junc2.getLeftNode(), Direction.STRAIGHT, 100, false);
        Connection con7 = new Connection(junc1.getBottomNode(), junc2.getBottomNode(), Direction.STRAIGHT, 100, false);

        node0.addAllNeighbors(con1, con2);
        node1.addAllNeighbors(con1, con3);
        node2.addAllNeighbors(con4, con5);
        node3.addAllNeighbors(con4, con6);

        junc1.getRightNode().addNeighbor(con5);
        junc1.getLeftNode().addNeighbor(con2);
        junc1.getBottomNode().addNeighbor(con7);

        junc2.getRightNode().addNeighbor(con3);
        junc2.getLeftNode().addNeighbor(con6);
        junc2.getBottomNode().addNeighbor(con7);

        map.addNode(node0);
        map.addNode(node1);
        map.addNode(node2);
        map.addNode(node3);

        map.addJunction(junc1);
        map.addJunction(junc2);

        Editor.getInstance().map.update(map);
    }
}
