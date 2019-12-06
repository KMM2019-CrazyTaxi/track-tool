package editor;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public enum Tools {
    NONE,

    ADD_NODE,
    REMOVE_NODE,
    MOVE_NODE,

    ADD_PATH,
    REMOVE_PATH,
    MOVE_PATH_CENTER,

    ADD_JUNCTION,
    REMOVE_JUNCTION,
    MOVE_JUNCTION;

    private final static double ICON_SIZE = 40;

    public ImageView getIcon() {
        return Tools.getIcon(this);
    }

    public static ImageView getIcon(Tools t) {
        String path = "resources/" + t.name() + ".png";

        ImageView im = new ImageView(new Image(path, ICON_SIZE, ICON_SIZE, true, true));

        return im;
    }
}
