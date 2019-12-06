package editor;

import javafx.scene.image.Image;

public enum Tools {
    ADD_NODE,
    MOVE_NODE,
    REMOVE_NODE,

    ADD_PATH,
    REMOVE_PATH,
    MOVE_PATH_CENTER,

    ADD_JUNCTION,
    REMOVE_JUCTION,
    MOVE_JUNCTION,

    NONE;

    public Image getIcon() {
        return Tools.getIcon(this);
    }

    public static Image getIcon(Tools t) {
        if (t == Tools.NONE)
            return null;
        return new Image("resources/" + t.name() + ".png");
    }
}
