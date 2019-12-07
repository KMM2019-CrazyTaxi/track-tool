package gui.controllers;

import editor.Editor;
import editor.Tools;
import helpers.UpdateListener;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import map.Position;

public class UIController implements UpdateListener<Tools> {

    @FXML private Scene uiScene;
    @FXML private VBox toolBar;
    @FXML private Pane mapViewFeature;

    private Tools currentTool;

    public UIController() {
        currentTool = Tools.NONE;
    }

    public void initialize() {
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

        Editor.getInstance().activeTool.subscribe(this);
        uiScene.addEventHandler(MouseEvent.MOUSE_CLICKED, this::handleClickEvent);

        Editor.getInstance().map.subscribe(data -> setHandleEventHandlers());
    }

    private void setHandleEventHandlers() {
        Group nodeHandles = getHandleGroup(Tools.MOVE_NODE);
        Group pathHandles = getHandleGroup(Tools.MOVE_PATH_CENTER);
        Group junctionHandles = getHandleGroup(Tools.MOVE_JUNCTION);

        for (Node n : nodeHandles.getChildren()) {
            n.addEventHandler(MouseEvent.MOUSE_PRESSED, UIController::handleHandlePress);
        }
        for (Node n : pathHandles.getChildren()) {
            n.addEventHandler(MouseEvent.MOUSE_PRESSED, UIController::handleHandlePress);
        }
        for (Node n : junctionHandles.getChildren()) {
            n.addEventHandler(MouseEvent.MOUSE_PRESSED, UIController::handleHandlePress);
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

    private static void handleHandlePress(MouseEvent mouseEvent) {
        Node handle = (Node) mouseEvent.getSource();
        Tools tool = Editor.getInstance().activeTool.get();

        if (tool.getType() == Tools.ToolType.NODE) {

            int id = Integer.parseInt(handle.getId());
            map.Node node = Editor.getInstance().map.get().getNode(id);

            if (tool == Tools.MOVE_NODE) {
                mouseEvent.setDragDetect(true);
                Editor.getInstance().markedNode.update(node);

            }
            else if (tool == Tools.REMOVE_NODE) {
                Editor.getInstance().map.get().removeNode(node);
                Editor.getInstance().map.noteChange();
            }
        }

        else if (tool.getType() == Tools.ToolType.PATH) {
            String[] bits = handle.getId().split(":");

            int id1 = Integer.parseInt(bits[0]);
            int id2 = Integer.parseInt(bits[1]);

            map.Node node1 = Editor.getInstance().map.get().getNode(id1);
            map.Node node2 = Editor.getInstance().map.get().getNode(id2);
            map.Connection con = Editor.getInstance().map.get().getConnection(node1, node2);

            if (tool == Tools.MOVE_PATH_CENTER) {
                mouseEvent.setDragDetect(true);
                Editor.getInstance().markedPath.update(con);

            }
            else if (tool == Tools.REMOVE_PATH) {
                node1.removeNeighbor(con);
                node2.removeNeighbor(con);
                Editor.getInstance().map.noteChange();
            }
        }
    }

    private static void handleHandleRelease(MouseEvent mouseEvent) {
        mouseEvent.setDragDetect(false);
    }


    public void handleClickEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() != MouseButton.PRIMARY)
            return;

        Position mid = MapViewFeatureController.calculateCenterMass(Editor.getInstance().map.get());
        double scale = MapViewFeatureController.calculateScaleFactor(Editor.getInstance().map.get(),
                mapViewFeature.getWidth(),
                mapViewFeature.getHeight(),
                mid);
        Position nodePos = MapViewFeatureController.unpositionPoint(new Position(mouseEvent.getX(), mouseEvent.getY()),
                mapViewFeature.getWidth(),
                mapViewFeature.getHeight(),
                mid,
                scale);

        switch (Editor.getInstance().activeTool.get()) {
            case ADD_NODE:
                Editor.getInstance().map.get().addNode(new map.Node(nodePos));
                Editor.getInstance().map.noteChange();
                System.out.println("Added node at " + nodePos);
                break;
            case ADD_PATH:
                break;
            case ADD_JUNCTION:
                break;
            default:
                return;
        }


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
            case REMOVE_PATH:
            case MOVE_PATH_CENTER:
                handleGroup = (Group) mapViewFeature.lookup("#mapViewPathHandles");
                break;
            case REMOVE_JUNCTION:
            case MOVE_JUNCTION:
                handleGroup = (Group) mapViewFeature.lookup("#mapViewJunctionHandles");
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
