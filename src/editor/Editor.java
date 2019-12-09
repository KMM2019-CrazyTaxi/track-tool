package editor;

import gui.controllers.UIController;
import helpers.UpdateWrapper;
import map.Connection;
import map.Junction;
import map.Map;
import map.Node;

public class Editor {
    private static Editor instance = new Editor();

    public UpdateWrapper<Map> map;
    public UpdateWrapper<Tools> activeTool;
    public UpdateWrapper<Node> markedNode;
    public UpdateWrapper<Connection> markedPath;
    public UpdateWrapper<Junction> markedJunction;

    private Editor() {
        map = new UpdateWrapper<>(new Map());
        activeTool = new UpdateWrapper<>(Tools.NONE);

        markedNode = new UpdateWrapper<>();
        markedPath = new UpdateWrapper<>();
        markedJunction = new UpdateWrapper<>();
    }

    public static Editor getInstance() {
        return instance;
    }
}
