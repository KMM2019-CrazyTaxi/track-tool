package gui.controllers;

import editor.Editor;
import editor.Tools;
import helpers.UpdateListener;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class UIController implements UpdateListener<Tools> {

    @FXML private Scene uiScene;
    @FXML private VBox toolBar;
    @FXML private Pane mapViewFeature;

    private Tools.ToolType currentTool;

    public UIController() {
        currentTool = Tools.ToolType.NONE;
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

        deactivateHandles(Tools.ToolType.NODE);
        deactivateHandles(Tools.ToolType.PATH);
        deactivateHandles(Tools.ToolType.JUNCTION);

        Editor.getInstance().activeTool.subscribe(this);
    }

    private void deactivateHandles(Tools.ToolType handles) {
        Group handleGroup = null;
        switch (handles) {
            case NODE:
                handleGroup = (Group) mapViewFeature.lookup("#mapViewNodeHandles");
                break;
            case PATH:
                handleGroup = (Group) mapViewFeature.lookup("#mapViewPathHandles");
                break;
            case JUNCTION:
                handleGroup = (Group) mapViewFeature.lookup("#mapViewJunctionHandles");
                break;
        }

        if (handleGroup == null)
            return;

        for(Node n : handleGroup.getChildren()) {
            n.setDisable(true);
        }
        handleGroup.setVisible(false);
        handleGroup.toBack();
    }

    private void activateHandles(Tools.ToolType handles) {
        Group handleGroup = null;
        switch (handles) {
            case NODE:
                handleGroup = (Group) mapViewFeature.lookup("#mapViewNodeHandles");
                break;
            case PATH:
                handleGroup = (Group) mapViewFeature.lookup("#mapViewPathHandles");
                break;
            case JUNCTION:
                handleGroup = (Group) mapViewFeature.lookup("#mapViewJunctionHandles");
                break;
        }

        if (handleGroup == null){
            System.out.println("Null group");
            return;
        }

        System.out.println("Children: " + handleGroup.getChildren().size());

        for(Node n : handleGroup.getChildren()) {
            n.setDisable(false);
        }

        handleGroup.setVisible(true);
        handleGroup.toFront();
    }


    private static void handleToolBarButtonClick(MouseEvent e) {
        Tools tool = Tools.valueOf(((Button)e.getSource()).getId());
        Editor.getInstance().activeTool.update(tool);
    }

    @Override
    public void update(Tools data) {
        deactivateHandles(currentTool);
        activateHandles(data.getType());
        currentTool = data.getType();
    }
}
