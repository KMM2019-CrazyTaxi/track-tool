package editor;

import helpers.UpdateWrapper;
import map.Map;

public class Editor {
    private static Editor instance = new Editor();

    public UpdateWrapper<Map> map;
    public UpdateWrapper<Tools> activeTool;

    private Editor() {
        map = new UpdateWrapper<>(new Map());
        activeTool = new UpdateWrapper<>(Tools.NONE);
    }

    public static Editor getInstance() {
        return instance;
    }
}
