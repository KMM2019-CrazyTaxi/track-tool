import editor.Editor;
import editor.Tools;
import helpers.UpdateWrapper;
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

        Editor.getInstance().activeTool.subscribe(System.out::println);

        // Map test
        Map map = new Map();
        Node node0 = new Node(new Position(100, 100));
        Node node1 = new Node(new Position(100, 300));
        Node node2 = new Node(new Position(300, 100));
        Node node3 = new Node(new Position(300, 300));

        Connection con0 = new Connection(node0, node1, Direction.STRAIGHT, 100, false, new Position(50, 200));
        Connection con1 = new Connection(node1, node3, Direction.STRAIGHT, 100, false, new Position(200, 300));
        Connection con2 = new Connection(node3, node2, Direction.STRAIGHT, 100, false, new Position(350, 200));
        Connection con3 = new Connection(node0, node2, Direction.STRAIGHT, 100, false, new Position(200, 100));

        node0.addAllNeighbors(con0, con3);
        node1.addAllNeighbors(con0, con1);
        node3.addAllNeighbors(con1, con2);
        node2.addAllNeighbors(con2, con3);

        map.addNode(node0);
        map.addNode(node1);
        map.addNode(node2);
        map.addNode(node3);

        Editor.getInstance().map.update(map);
    }
}
