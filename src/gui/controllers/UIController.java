package gui.controllers;

import editor.Editor;
import editor.Tools;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class UIController {

    @FXML private Scene uiScene;
    @FXML private VBox toolBar;
    @FXML private Pane mapViewFeature;

    public void initialize() {
        // Make Map View fit all available space
        mapViewFeature.prefWidthProperty().bind(uiScene.widthProperty().subtract(toolBar.widthProperty()));
        mapViewFeature.prefHeightProperty().bind(uiScene.heightProperty());

        // Make scroll pane wide enough


        // Add tools
        for (Tools t : Tools.values()) {
            Button b = new Button();
            b.addEventHandler(MouseEvent.MOUSE_CLICKED, UIController::handleToolBarButtonClick);
            b.setGraphic(t.getIcon());
            b.setId(t.name());
            b.getStyleClass().add("toolButton");
            toolBar.getChildren().add(b);
        }
    }

    private static void handleToolBarButtonClick(MouseEvent e) {
        Tools tool = Tools.valueOf(((Button)e.getSource()).getId());
        Editor.getInstance().activeTool.update(tool);
    }
}
