package main;

import helpers.UpdateWrapper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import map.Map;

public class CTPanel extends Application {
    private static CTPanel instance = new CTPanel();

    private Map internalMap;
    public UpdateWrapper<Map> map;

    public CTPanel() {
        internalMap = new Map();
        map = new UpdateWrapper<>(internalMap);
    }

    public void start(Stage stage) throws Exception {
        Stage root = FXMLLoader.load(getClass().getResource("gui/fxml/UI.fxml"));
        root.show();
    }

    public static CTPanel getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        launch();
    }
}
