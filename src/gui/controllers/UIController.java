package gui.controllers;

import editor.Editor;
import editor.Tools;
import helpers.UpdateListener;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.StringConverter;
import map.Connection;
import map.Direction;
import map.Junction;
import map.Position;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class UIController implements UpdateListener<Tools> {

    @FXML private Scene uiScene;
    @FXML private VBox toolBar;
    @FXML private Pane mapViewFeature;

    @FXML private TextInputDialog pathDistanceDialog;

    @FXML private Circle dragGhost;

    private Tools currentTool;
    private AtomicBoolean dragging;

    public UIController() {
        currentTool = Tools.NONE;
        dragging = new AtomicBoolean(false);
    }

    public void initialize() {

        // Remove header from input dialog
        pathDistanceDialog.setHeaderText(null);
        pathDistanceDialog.setGraphic(null);

        // Add input formatter to input dialog
        Pattern validEditingState = Pattern.compile("^\\d+$|");

        UnaryOperator<TextFormatter.Change> filter = c -> {
            String text = c.getControlNewText();
            if (validEditingState.matcher(text).matches()) {
                return c;
            } else {
                return null;
            }
        };

        StringConverter<Integer> converter = new StringConverter<Integer>() {
            @Override
            public Integer fromString(String s) {
                if (s.isEmpty() || "-".equals(s) || ".".equals(s) || "-.".equals(s)) {
                    return 0;
                } else {
                    return Integer.valueOf(s);
                }
            }

            @Override
            public String toString(Integer d) {
                return d.toString();
            }
        };

        pathDistanceDialog.getEditor().setTextFormatter(new TextFormatter<Integer>(converter, 0, filter));

        // Init drag ghost node
        dragGhost = (Circle) mapViewFeature.lookup("#dragGhost");

        // Make Map View fit all available space
        mapViewFeature.prefWidthProperty().bind(uiScene.widthProperty().subtract(toolBar.widthProperty()));
        mapViewFeature.prefHeightProperty().bind(uiScene.heightProperty());

        // Add tools
        for (Tools t : Tools.values()) {
            Button b = new Button();
            b.addEventHandler(MouseEvent.MOUSE_CLICKED, UIController::handleToolBarButtonClick);
            b.setGraphic(t.getIcon());
            b.setId(t.name());
            b.getStyleClass().add("toolButton");
            toolBar.getChildren().add(b);
        }

        deactivateHandles(Tools.MOVE_NODE);
        deactivateHandles(Tools.MOVE_PATH_CENTER);
        deactivateHandles(Tools.MOVE_JUNCTION);
        deactivateHandles(Tools.ADD_PATH);

        Editor.getInstance().activeTool.subscribe(this);
        uiScene.addEventHandler(MouseEvent.MOUSE_CLICKED, this::handleClickEvent);

        Editor.getInstance().map.subscribe(data -> setHandleEventHandlers());
    }

    private void setHandleEventHandlers() {
        Group nodeHandles = getHandleGroup(Tools.MOVE_NODE);
        Group pathHandles = getHandleGroup(Tools.MOVE_PATH_CENTER);
        Group junctionHandles = getHandleGroup(Tools.MOVE_JUNCTION);

        for (Node n : nodeHandles.getChildren()) {
            n.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleHandlePress);
            n.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleHandleRelease);
            n.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleHandleDrag);
        }
        for (Node n : pathHandles.getChildren()) {
            n.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleHandlePress);
            n.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleHandleRelease);
            n.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleHandleDrag);
        }
        for (Node n : junctionHandles.getChildren()) {
            n.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleHandlePress);
            n.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleHandleRelease);
            n.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleHandleDrag);
        }
    }

    private void deactivateHandles(Tools active) {
        Group handleGroup = getHandleGroup(active);

        if (handleGroup == null)
            return;

        for(Node n : handleGroup.getChildren()) {
            n.setDisable(true);
        }
        handleGroup.setVisible(false);
        handleGroup.toBack();
    }

    private void activateHandles(Tools active) {
        Group handleGroup = getHandleGroup(active);

        if (handleGroup == null){
            return;
        }

        for(Node n : handleGroup.getChildren()) {
            n.setDisable(false);
        }

        handleGroup.setVisible(true);
        handleGroup.toFront();
    }

    private void handleHandlePress(MouseEvent mouseEvent) {
        Node handle = (Node) mouseEvent.getSource();
        Tools tool = Editor.getInstance().activeTool.get();

        if (tool.getType() == Tools.ToolType.NODE) {
            int id = Integer.parseInt(handle.getId());
            map.Node node = Editor.getInstance().map.get().getNode(id);

            handleNodeToolPress(mouseEvent, handle, tool, node);
        }
        else if (tool.getType() == Tools.ToolType.PATH) {
            String[] bits = handle.getId().split(":");

            int id1 = Integer.parseInt(bits[0]);
            int id2 = Integer.parseInt(bits[1]);

            map.Node node1 = Editor.getInstance().map.get().getNode(id1);
            map.Node node2 = Editor.getInstance().map.get().getNode(id2);
            map.Connection con = Editor.getInstance().map.get().getConnection(node1, node2);

            handlePathToolPress(mouseEvent, handle, tool, node1, node2, con);
        }
        else if (tool.getType() == Tools.ToolType.JUNCITON) {
            int id = Integer.parseInt(handle.getId());
            map.Junction junc = Editor.getInstance().map.get().getJunction(id);

            handleJunctionToolPress(mouseEvent, handle, tool, junc);
        }
        else if (tool.getType() == Tools.ToolType.CONNECT) {
            int id = Integer.parseInt(handle.getId());
            map.Node node = Editor.getInstance().map.get().getNode(id);

            handleConnectToolPress(mouseEvent, handle, tool, node);
        }
    }

    private void handleConnectToolPress(MouseEvent mouseEvent, Node handle, Tools tool, map.Node node) {
        if (tool == Tools.ADD_PATH) {
            map.Node mark = Editor.getInstance().markedNode.get();

            if (mark == null) {
                Editor.getInstance().markedNode.update(node);
            }
            else {
                Connection newCon = new Connection(mark, node, Direction.STRAIGHT, 0, true);
                node.addNeighbor(newCon);
                mark.addNeighbor(newCon);
                Editor.getInstance().markedNode.update(null);
                Editor.getInstance().map.noteChange();
            }
        }
    }

    private void handleJunctionToolPress(MouseEvent mouseEvent, Node handle, Tools tool, Junction junc) {
        if (tool == Tools.MOVE_JUNCTION) {
            dragging.set(true);
            Editor.getInstance().markedJunction.update(junc);
        }
        else if (tool == Tools.REMOVE_JUNCTION) {
            Editor.getInstance().map.get().removeJunction(junc);
            Editor.getInstance().map.noteChange();
        }
    }

    private void handlePathToolPress(MouseEvent mouseEvent, Node handle, Tools tool, map.Node node1, map.Node node2, Connection con) {
        if (tool == Tools.MOVE_PATH_CENTER) {
            dragging.set(true);
            Editor.getInstance().markedPath.update(con);
        }
        else if (tool == Tools.REMOVE_PATH) {
            node1.removeNeighbor(con);
            node2.removeNeighbor(con);
            Editor.getInstance().map.noteChange();
        }
        else if (tool == Tools.DISTANCE_PATH) {
            Optional<String> res = pathDistanceDialog.showAndWait();

            if (res.isPresent()) {
                int distance = (Integer) pathDistanceDialog.getEditor().getTextFormatter().getValue();
                con.setDistance(distance);
                Editor.getInstance().map.noteChange();
            }
        }
        else if (tool == Tools.SPLIT_PATH) {
            node1.removeNeighbor(con);
            node2.removeNeighbor(con);

            map.Node newNode = new map.Node(con.getMidPoint());

            Connection con1 = new Connection(node1, newNode, Direction.STRAIGHT, con.getDistance() / 2, true);
            Connection con2 = new Connection(node2, newNode, Direction.STRAIGHT, con.getDistance() / 2, true);

            node1.addNeighbor(con1);
            node2.addNeighbor(con2);

            newNode.addNeighbor(con1);
            newNode.addNeighbor(con2);

            Editor.getInstance().map.get().addNode(newNode);
            Editor.getInstance().map.noteChange();
        }
    }

    private void handleNodeToolPress(MouseEvent mouseEvent, Node handle, Tools tool, map.Node node) {
        if (tool == Tools.MOVE_NODE) {
            dragging.set(true);
            Editor.getInstance().markedNode.update(node);
        }
        else if (tool == Tools.REMOVE_NODE) {
            Editor.getInstance().map.get().removeNode(node);
            Editor.getInstance().map.noteChange();
        }

    }

    private void handleHandleRelease(MouseEvent mouseEvent) {
        Node handle = (Node) mouseEvent.getSource();
        Tools tool = Editor.getInstance().activeTool.get();

        if (dragging.get()) {
            dragGhost.setVisible(false);

            Position newPos = getMapPosition(mouseEvent);

            if (tool == Tools.MOVE_NODE) {
                Editor.getInstance().markedNode.get().setPosition(newPos);
            }
            else if (tool == Tools.MOVE_PATH_CENTER) {
                Editor.getInstance().markedPath.get().setMidPoint(newPos);
            }
            else if (tool == Tools.MOVE_JUNCTION) {
                Editor.getInstance().markedJunction.get().setPos(newPos);
            }

            Editor.getInstance().map.noteChange();

            dragging.set(false);
        }
    }

    private void handleHandleDrag(MouseEvent mouseEvent) {
        dragGhost.setCenterX(mouseEvent.getX());
        dragGhost.setCenterY(mouseEvent.getY());

        dragGhost.setVisible(true);
    }


    public void handleClickEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() != MouseButton.PRIMARY)
            return;

        Position nodePos = getMapPosition(mouseEvent);

        switch (Editor.getInstance().activeTool.get()) {
            case ADD_NODE:
                Editor.getInstance().map.get().addNode(new map.Node(nodePos));
                Editor.getInstance().map.noteChange();
                break;
            case ADD_JUNCTION:
                Editor.getInstance().map.get().addJunction(new Junction(nodePos));
                Editor.getInstance().map.noteChange();
                break;
            default:
                return;
        }


    }

    private Position getMapPosition(MouseEvent mouseEvent) {
        Position mid = MapViewFeatureController.calculateCenterMass(Editor.getInstance().map.get());
        double scale = MapViewFeatureController.calculateScaleFactor(
                Editor.getInstance().map.get(),
                mapViewFeature.getWidth(),
                mapViewFeature.getHeight(),
                mid);

        Position clickPosition = new Position(mouseEvent.getX(), mouseEvent.getY());
        clickPosition.subtract(new Position(mapViewFeature.getLayoutX(), mapViewFeature.getLayoutY()));

        Position nodePos = MapViewFeatureController.unpositionPoint(
                clickPosition,
                mapViewFeature.getWidth(),
                mapViewFeature.getHeight(),
                mid,
                scale);

        System.out.println(nodePos);

        return nodePos;
    }

    private static void handleToolBarButtonClick(MouseEvent e) {
        Tools tool = Tools.valueOf(((Button)e.getSource()).getId());
        Editor.getInstance().activeTool.update(tool);
    }

    private Group getHandleGroup(Tools tool) {
        Group handleGroup = null;
        switch (tool) {
            case REMOVE_NODE:
            case MOVE_NODE:
                handleGroup = (Group) mapViewFeature.lookup("#mapViewNodeHandles");
                break;
            case SPLIT_PATH:
            case DISTANCE_PATH:
            case REMOVE_PATH:
            case MOVE_PATH_CENTER:
                handleGroup = (Group) mapViewFeature.lookup("#mapViewPathHandles");
                break;
            case ROTATE_JUNCTION:
            case REMOVE_JUNCTION:
            case MOVE_JUNCTION:
                handleGroup = (Group) mapViewFeature.lookup("#mapViewJunctionHandles");
                break;
            case ADD_PATH:
                handleGroup = (Group) mapViewFeature.lookup("#mapViewConnectHandles");
                break;
        }
        return handleGroup;
    }

    @Override
    public void update(Tools data) {
        deactivateHandles(currentTool);
        activateHandles(data);
        currentTool = data;
    }
}
