package editor;

import gui.controllers.UIController;
import helpers.UpdateWrapper;
import map.Connection;
import map.Map;
import map.Node;

public class Editor {
    private static Editor instance = new Editor();

    public UpdateWrapper<Map> map;
    public UpdateWrapper<Tools> activeTool;
    public UpdateWrapper<Node> markedNode;
    public UpdateWrapper<Connection> markedPath;

    private Editor() {
        map = new UpdateWrapper<>(new Map());
        activeTool = new UpdateWrapper<>(Tools.NONE);
    }

    public static Editor getInstance() {
        return instance;
    }
}
