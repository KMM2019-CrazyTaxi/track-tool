import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class CTPanel extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) throws Exception {
        Stage root = FXMLLoader.load(getClass().getResource("gui/fxml/UI.fxml"));
        root.show();
    }
}
