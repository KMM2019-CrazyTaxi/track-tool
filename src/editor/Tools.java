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

    public enum ToolType {
        NONE,
        NODE,
        PATH,
        JUNCITON;
    }

    public ToolType getType() {
        return Tools.getType(this);
    }

    private static ToolType getType(Tools t) {
        switch (t) {
            case NONE:
                return ToolType.NONE;

            case ADD_NODE:
            case REMOVE_NODE:
            case MOVE_NODE:
                return ToolType.NODE;

            case ADD_PATH:
            case REMOVE_PATH:
            case MOVE_PATH_CENTER:
                return ToolType.PATH;

            case ADD_JUNCTION:
            case REMOVE_JUNCTION:
            case MOVE_JUNCTION:
                return ToolType.JUNCITON;

            default:
                throw new IllegalStateException("Unexpected value: " + t);
        }
    }
}
