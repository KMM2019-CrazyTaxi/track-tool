package gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.Scene;
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
    }
}
