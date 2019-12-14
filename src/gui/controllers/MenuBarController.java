package gui.controllers;

import editor.Editor;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Pair;
import map.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class MenuBarController {

    @FXML private Menu fileMenu;
    @FXML private FileChooser fileChooser;

    public void initialize() {
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Map files", "*.map"));
        fileChooser.getExtensionFilters().add(new ExtensionFilter("All files", "*"));
    }

    public void handleExportClick(ActionEvent actionEvent) {
        // Choose file
        File file = fileChooser.showSaveDialog(null);

        if (file == null)
            return;

        // Ask acceptance
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Exporting the map deletes your unsaved work. Continue?", ButtonType.YES, ButtonType.CANCEL);
        alert.showAndWait();

        if (alert.getResult() != ButtonType.YES)
            return;

        Map currentMap = Editor.getInstance().map.get();

        List<SplitNode> splitNodes = new ArrayList<>();

        for (Junction j : currentMap.getJunctions()) {
            SplitNode node1 = new SplitNode(j.getBottomNode(), currentMap.getIndex(j));
            SplitNode node2 = new SplitNode(j.getLeftNode(), currentMap.getIndex(j));
            SplitNode node3 = new SplitNode(j.getRightNode(), currentMap.getIndex(j));

            splitNodes.add(node1);
            splitNodes.add(node2);
            splitNodes.add(node3);
        }

        for (Node n : currentMap.getNodes()) {
            splitNodes.add(new SplitNode(n));
        }

        List<SplitNode> finalNodes = new ArrayList<>(splitNodes);

        // Split nodes
        for (SplitNode a : splitNodes) {
            SplitNode b = a.split();

            finalNodes.add(b);

            for (SplitNode neighbor : getSplitNeighbor(finalNodes, a.getNode())) {
                if (neighbor.getJunctionID() == null || !neighbor.getJunctionID().equals(a.getJunctionID())) {
                    Connection con = a.getNode().getNeighbor(neighbor.getNode());
                    Connection newCon = new Connection(b.getNode(), neighbor.getNode(), con.getDirection(), con.getDistance(), con.isStopable());

                    a.getNode().removeNeighbor(con);
                    b.getNode().addNeighbor(newCon);

                    if (neighbor.getJunctionID() == null) {
                        neighbor.setJunctionID(a.getJunctionID());
                    }
                }
                else {
                    Connection con = neighbor.getSibling().getNeighbor(a.getNode());
                    Connection newCon = new Connection(b.getNode(), neighbor.getSibling(), con.getDirection(), con.getDistance(), con.isStopable());

                    neighbor.getSibling().removeNeighbor(con);
                    neighbor.getSibling().addNeighbor(newCon);
                }
            }
        }

        List<Pair<Node, Connection>> removeCons = new ArrayList<>();
        List<Pair<Node, Connection>> addCons = new ArrayList<>();

        // Apply correction
        for (SplitNode sn : finalNodes) {
            if (sn.getNode().getNeighbors().isEmpty()) {
                for (Connection sibNeighborCon : sn.getSibling().getNeighbors()) {
                    Node sibNeighbor = sibNeighborCon.getConnectingNode(sn.getSibling());
                    if (sibNeighbor.getNeighbors().isEmpty()) {
                        Node sibNeighborSib = getSplitNode(finalNodes, sibNeighbor).getSibling();

                        Connection oldCon = null;
                        for (Connection con : sibNeighborSib.getNeighbors()) {
                            if (con.getDistance() != 0 && con.getConnectingNode(sibNeighborSib) == sn.getNode()) {
                                oldCon = con;
                            }
                        }

                        Connection newCon = new Connection(sn.getNode(), sibNeighborSib, oldCon.getDirection().reverse(), oldCon.getDistance(), oldCon.isStopable());

                        removeCons.add(new Pair<>(sibNeighborSib, oldCon));
                        addCons.add(new Pair<>(sn.getNode(), newCon));
                    }
                }
            }
        }

        for (Pair<Node, Connection> nodeConPair : removeCons) {
            nodeConPair.getKey().removeNeighbor(nodeConPair.getValue());
        }

        for (Pair<Node, Connection> nodeConPair : addCons) {
            nodeConPair.getKey().addNeighbor(nodeConPair.getValue());
        }

        Map exportMap = new Map();
        for (SplitNode sn : finalNodes) {
            exportMap.addNode(sn.getNode());
        }

        FileOutputStream fileStream;
        try {
            fileStream = new FileOutputStream(file);
            fileStream.write(exportMap.toExport());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void printGraph(List<SplitNode> finalNodes)
    {
        for (SplitNode sn : finalNodes) {
            System.out.print("Node " + sn.getNode().id + " at position " + sn.getNode().getPosition() + " has neighbors: ");
            for (Connection n : sn.getNode().getNeighbors())
            {
                System.out.print(n.getConnectingNode(sn.getNode()).id);
                System.out.print(", ");
            }
            System.out.println();
        }
        System.out.println();
    }

    private static SplitNode getSplitNode(List<SplitNode> splitNodes, Node n) {
        for (SplitNode sn : splitNodes) {
            if (sn.getNode() == n) {
                return sn;
            }
        }
        return null;
    }

    private static List<SplitNode> getSplitNeighbor(List<SplitNode> splitNodes, Node n) {
        List<SplitNode> neighbors = new ArrayList<>();

        for (Connection c : n.getNeighbors()) {
            for (SplitNode sn : splitNodes) {
                if (sn.getNode() == c.getConnectingNode(n))
                    neighbors.add(sn);
            }
        }

        return neighbors;
    }

    public void handleSaveClick(ActionEvent actionEvent) {
        // Choose file
        File file = fileChooser.showSaveDialog(null);

        if (file == null)
            return;

        FileOutputStream fileStream;
        try {
            fileStream = new FileOutputStream(file);

            byte[] bytes = Editor.getInstance().map.get().toExport();

            fileStream.write(bytes);
            fileStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleLoadClick(ActionEvent actionEvent) {
        // Choose file
        File file = fileChooser.showSaveDialog(null);

        if (file == null)
            return;

        // Ask acceptance
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Loading map deletes your unsaved work. Continue?", ButtonType.YES, ButtonType.CANCEL);
        alert.showAndWait();

        if (alert.getResult() != ButtonType.YES)
            return;

        try {
            byte[] readBytes = Files.readAllBytes(file.toPath());

            Editor.getInstance().map.update(new Map(readBytes));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
