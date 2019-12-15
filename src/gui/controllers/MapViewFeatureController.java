package gui.controllers;

import editor.Editor;
import helpers.UpdateListener;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.QuadCurve;
import javafx.scene.text.Text;
import map.*;

/**
 * Map View-feature controller. This is the main controller class for the Map View-feature.
 *
 * @author Henrik Nilsson
 */
public class MapViewFeatureController implements UpdateListener<Map> {
    private static final double NODE_DOT_SIZE = 15;
    private static final double HANDLE_DOT_SIZE = 10;

    @FXML private Pane mapViewFeature;

    @FXML private Group mapViewNodeLayer;
    @FXML private Group mapViewPathLayer;
    @FXML private Group mapViewJunctionLayer;

    @FXML private Group mapViewNodeMarks;
    @FXML private Group mapViewPathMarks;
    @FXML private Group mapViewJunctionMarks;

    @FXML private Group mapViewNodeHandles;
    @FXML private Group mapViewPathHandles;
    @FXML private Group mapViewJunctionHandles;

    @FXML private Group mapViewConnectHandles;

    private Node markedNode;
    private Connection markedPath;
    private Junction markedJunction;

    public void initialize() {
        Editor.getInstance().map.subscribe(this);

        Editor.getInstance().markedNode.subscribe(o -> {
            Map map = Editor.getInstance().map.get();

            if (markedNode != null) {
                Circle oldMark = (Circle) mapViewNodeMarks.lookup("#" + markedNode.getIndex(map));
                oldMark.getStyleClass().remove("marked");
            }

            if (o != null) {
                Circle mark = (Circle) mapViewNodeMarks.lookup("#" + o.getIndex(map));
                mark.getStyleClass().add("marked");
            }

            markedNode = o;
        });

        Editor.getInstance().markedPath.subscribe(o -> {
            Map map = Editor.getInstance().map.get();

            if (markedPath != null) {
                Node oldN1 = markedPath.getNodes().getKey();
                Node oldN2 = markedPath.getNodes().getValue();

                QuadCurve oldMark = (QuadCurve) mapViewPathMarks.lookup("#" + oldN1.getIndex(map) + "-" + oldN2.getIndex(map));
                if (oldMark == null)
                    oldMark = (QuadCurve) mapViewPathMarks.lookup("#" + oldN2.getIndex(map) + "-" + oldN1.getIndex(map));

                oldMark.getStyleClass().remove("marked");
            }

            if (o != null) {
                Node n1 = o.getNodes().getKey();
                Node n2 = o.getNodes().getValue();

                QuadCurve mark = (QuadCurve) mapViewPathMarks.lookup("#" + n1.getIndex(map) + "-" + n2.getIndex(map));
                if (mark == null){
                    System.out.println("WAS NULL");
                    mark = (QuadCurve) mapViewPathMarks.lookup("#" + n2.getIndex(map) + "-" + n1.getIndex(map));
                }

                mark.getStyleClass().add("marked");
            }

            markedPath = o;
        });

        Editor.getInstance().markedJunction.subscribe(o -> {
            Map map = Editor.getInstance().map.get();

            if (markedJunction != null) {
                Node oldN1 = markedJunction.getBottomNode();
                Node oldN2 = markedJunction.getRightNode();
                Node oldN3 = markedJunction.getLeftNode();

                QuadCurve oldMark1 = (QuadCurve) mapViewJunctionMarks.lookup("#" + oldN1.getIndex(map) + "-" + oldN2.getIndex(map));
                QuadCurve oldMark2 = (QuadCurve) mapViewJunctionMarks.lookup("#" + oldN2.getIndex(map) + "-" + oldN3.getIndex(map));
                QuadCurve oldMark3 = (QuadCurve) mapViewJunctionMarks.lookup("#" + oldN1.getIndex(map) + "-" + oldN3.getIndex(map));

                oldMark1.getStyleClass().remove("marked");
                oldMark2.getStyleClass().remove("marked");
                oldMark3.getStyleClass().remove("marked");
            }

            if (o != null) {
                Node n1 = o.getBottomNode();
                Node n2 = o.getRightNode();
                Node n3 = o.getLeftNode();

                QuadCurve mark1 = (QuadCurve) mapViewJunctionMarks.lookup("#" + n1.getIndex(map) + "-" + n2.getIndex(map));
                QuadCurve mark2 = (QuadCurve) mapViewJunctionMarks.lookup("#" + n2.getIndex(map) + "-" + n3.getIndex(map));
                QuadCurve mark3 = (QuadCurve) mapViewJunctionMarks.lookup("#" + n1.getIndex(map) + "-" + n3.getIndex(map));

                mark1.getStyleClass().add("marked");
                mark2.getStyleClass().add("marked");
                mark3.getStyleClass().add("marked");
            }

            markedJunction = o;
        });
    }

    private static void printAllNodes(Group group) {
        for (javafx.scene.Node n : group.getChildren()) {
            System.out.println(n.getId());
        }
    }

    /**
     * Update the shown map.
     * @param data New Map data.
     */
    public void update(Map data) {
        markedPath = null;
        markedJunction = null;
        markedNode = null;

        redraw(data);
    }

    /**
     * Draw the given Map to the view.
     * @param map Map data
     */
    private void redraw(Map map) {
        mapViewNodeLayer.getChildren().clear();
        mapViewPathLayer.getChildren().clear();
        mapViewJunctionLayer.getChildren().clear();

        mapViewNodeMarks.getChildren().clear();
        mapViewPathMarks.getChildren().clear();
        mapViewJunctionMarks.getChildren().clear();

        mapViewNodeHandles.getChildren().clear();
        mapViewPathHandles.getChildren().clear();
        mapViewJunctionHandles.getChildren().clear();

        mapViewConnectHandles.getChildren().clear();

        double width = mapViewFeature.getWidth();
        double height = mapViewFeature.getHeight();

        Position mapCenterMass = calculateCenterMass(map);
        double scaleFactor = calculateScaleFactor(map, width, height, mapCenterMass);

        for (Node n : map.getNodes()) {
            // Add node dot
            Position startPos = repositionPoint(n.getPosition(), width, height, mapCenterMass, scaleFactor);

            // Add node
            Circle fxNodeDot = new Circle(startPos.x, startPos.y, NODE_DOT_SIZE);
            fxNodeDot.getStyleClass().add("mapNodeDot");
            fxNodeDot.idProperty().setValue(String.valueOf(n.getIndex(map)));
            mapViewNodeLayer.getChildren().add(fxNodeDot);

            // Add node mark
            Circle fxNodeMarkDot = new Circle(startPos.x, startPos.y, NODE_DOT_SIZE);
            fxNodeMarkDot.getStyleClass().addAll("mapNodeDot", "marker");
            fxNodeMarkDot.idProperty().setValue(String.valueOf(n.getIndex(map)));
            mapViewNodeMarks.getChildren().add(fxNodeMarkDot);

            // Add node handle
            Circle fxNodeHandleDot = new Circle(startPos.x, startPos.y, HANDLE_DOT_SIZE);
            fxNodeHandleDot.getStyleClass().addAll("mapNodeDot", "mapHandle");
            fxNodeHandleDot.idProperty().setValue(String.valueOf(n.getIndex(map)));
            mapViewNodeHandles.getChildren().add(fxNodeHandleDot);

            // Add connect handles
            if (n.getNeighbors().size() < 2) {
                // Add connect handle
                Circle fxConnectHandleDot = new Circle(startPos.x, startPos.y, HANDLE_DOT_SIZE);
                fxConnectHandleDot.getStyleClass().addAll("mapNodeDot", "mapHandle");
                fxConnectHandleDot.idProperty().setValue(String.valueOf(n.getIndex(map)));
                mapViewConnectHandles.getChildren().add(fxConnectHandleDot);
            }

            for (Connection c : n.getNeighbors()) {
                Position midPos = repositionPoint(c.getMidPoint(), width, height, mapCenterMass, scaleFactor);
                Position endPos = repositionPoint(c.getConnectingNode(n).getPosition(), width, height, mapCenterMass, scaleFactor);

                // Add path
                QuadCurve fxPathLine = new QuadCurve(startPos.x, startPos.y, midPos.x, midPos.y, endPos.x, endPos.y);
                fxPathLine.getStyleClass().add("mapPathLine");
                fxPathLine.idProperty().setValue(n.getIndex(map) + "-" + c.getConnectingNode(n).getIndex(map));
                mapViewPathLayer.getChildren().add(fxPathLine);

                // Add path mark
                QuadCurve fxPathMarkLine = new QuadCurve(startPos.x, startPos.y, midPos.x, midPos.y, endPos.x, endPos.y);
                fxPathMarkLine.getStyleClass().addAll("mapPathLine", "marker");
                fxPathMarkLine.idProperty().setValue(n.getIndex(map) + "-" + c.getConnectingNode(n).getIndex(map));
                mapViewPathMarks.getChildren().add(fxPathMarkLine);

                // Add distance text
                Text distanceText = new Text(String.valueOf(c.getDistance()));
                distanceText.getStyleClass().addAll("distanceText", "darkText");
                double halfWidth = distanceText.getLayoutBounds().getWidth() / 2;
                double halfHeight = distanceText.getLayoutBounds().getHeight() / 2;
                distanceText.setX(midPos.x - halfWidth * 1.5);
                distanceText.setY(midPos.y - halfHeight * 1.5);
                mapViewPathLayer.getChildren().add(distanceText);

                // Add path handle
                Circle fxPathHandleDot = new Circle(midPos.x, midPos.y, HANDLE_DOT_SIZE);
                fxPathHandleDot.getStyleClass().addAll("mapNodeDot", "mapHandle");
                fxPathHandleDot.idProperty().setValue(n.getIndex(map) + "-" + c.getConnectingNode(n).getIndex(map));
                mapViewPathHandles.getChildren().add(fxPathHandleDot);
            }
        }

        for (Junction junc : map.getJunctions()) {
            // Get positions
            Position posMid = repositionPoint(junc.getPosition(), width, height, mapCenterMass, scaleFactor);
            Position pos1 = repositionPoint(junc.getBottomNode().getPosition(), width, height, mapCenterMass, scaleFactor);
            Position pos2 = repositionPoint(junc.getRightNode().getPosition(), width, height, mapCenterMass, scaleFactor);
            Position pos3 = repositionPoint(junc.getLeftNode().getPosition(), width, height, mapCenterMass, scaleFactor);

            // Add junction handle
            Circle fxJunctionHandleDot = new Circle(posMid.x, posMid.y, HANDLE_DOT_SIZE);
            fxJunctionHandleDot.getStyleClass().addAll("mapNodeDot", "mapHandle");
            fxJunctionHandleDot.idProperty().setValue(String.valueOf(map.getIndex(junc)));
            mapViewJunctionHandles.getChildren().add(fxJunctionHandleDot);

            // Add nodes
            Circle fxNodeDot1 = new Circle(pos1.x, pos1.y, NODE_DOT_SIZE);
            Circle fxNodeDot2 = new Circle(pos2.x, pos2.y, NODE_DOT_SIZE);
            Circle fxNodeDot3 = new Circle(pos3.x, pos3.y, NODE_DOT_SIZE);

            fxNodeDot1.getStyleClass().add("mapNodeDot");
            fxNodeDot2.getStyleClass().add("mapNodeDot");
            fxNodeDot3.getStyleClass().add("mapNodeDot");

            mapViewNodeLayer.getChildren().add(fxNodeDot1);
            mapViewNodeLayer.getChildren().add(fxNodeDot2);
            mapViewNodeLayer.getChildren().add(fxNodeDot3);

            // Add node marks
            Circle fxNodeMarkDot1 = new Circle(pos1.x, pos1.y, NODE_DOT_SIZE);
            Circle fxNodeMarkDot2 = new Circle(pos2.x, pos2.y, NODE_DOT_SIZE);
            Circle fxNodeMarkDot3 = new Circle(pos3.x, pos3.y, NODE_DOT_SIZE);

            fxNodeMarkDot1.getStyleClass().addAll("mapNodeDot", "marker");
            fxNodeMarkDot2.getStyleClass().addAll("mapNodeDot", "marker");
            fxNodeMarkDot3.getStyleClass().addAll("mapNodeDot", "marker");

            fxNodeMarkDot1.setId(String.valueOf(junc.getBottomNode().getIndex(map)));
            fxNodeMarkDot2.setId(String.valueOf(junc.getRightNode().getIndex(map)));
            fxNodeMarkDot3.setId(String.valueOf(junc.getLeftNode().getIndex(map)));

            mapViewNodeMarks.getChildren().add(fxNodeMarkDot1);
            mapViewNodeMarks.getChildren().add(fxNodeMarkDot2);
            mapViewNodeMarks.getChildren().add(fxNodeMarkDot3);

            // Add Paths
            QuadCurve fxPathLine1 = new QuadCurve(pos1.x, pos1.y, posMid.x, posMid.y, pos2.x, pos2.y);
            QuadCurve fxPathLine2 = new QuadCurve(pos2.x, pos2.y, posMid.x, posMid.y, pos3.x, pos3.y);
            QuadCurve fxPathLine3 = new QuadCurve(pos3.x, pos3.y, posMid.x, posMid.y, pos1.x, pos1.y);

            fxPathLine1.getStyleClass().add("mapPathLine");
            fxPathLine2.getStyleClass().add("mapPathLine");
            fxPathLine3.getStyleClass().add("mapPathLine");

            mapViewPathLayer.getChildren().add(fxPathLine1);
            mapViewPathLayer.getChildren().add(fxPathLine2);
            mapViewPathLayer.getChildren().add(fxPathLine3);

            // Add path marks
            QuadCurve fxPathMarkLine1 = new QuadCurve(pos1.x, pos1.y, posMid.x, posMid.y, pos2.x, pos2.y);
            QuadCurve fxPathMarkLine2 = new QuadCurve(pos2.x, pos2.y, posMid.x, posMid.y, pos3.x, pos3.y);
            QuadCurve fxPathMarkLine3 = new QuadCurve(pos3.x, pos3.y, posMid.x, posMid.y, pos1.x, pos1.y);

            fxPathMarkLine1.getStyleClass().addAll("mapPathLine", "marker");
            fxPathMarkLine2.getStyleClass().addAll("mapPathLine", "marker");
            fxPathMarkLine3.getStyleClass().addAll("mapPathLine", "marker");

            fxPathMarkLine1.setId(junc.getBottomNode().getIndex(map) + "-" + junc.getRightNode().getIndex(map));
            fxPathMarkLine2.setId(junc.getRightNode().getIndex(map) + "-" + junc.getLeftNode().getIndex(map));
            fxPathMarkLine3.setId(junc.getBottomNode().getIndex(map) + "-" + junc.getLeftNode().getIndex(map));

            mapViewJunctionMarks.getChildren().add(fxPathMarkLine1);
            mapViewJunctionMarks.getChildren().add(fxPathMarkLine2);
            mapViewJunctionMarks.getChildren().add(fxPathMarkLine3);

            for (Connection c : junc.getExternalNeighbors()) {
                Node n1 = c.getNodes().getKey();
                Node n2 = c.getNodes().getValue();

                Position startPos = repositionPoint(n1.getPosition(), width, height, mapCenterMass, scaleFactor);
                Position midPos = repositionPoint(c.getMidPoint(), width, height, mapCenterMass, scaleFactor);
                Position endPos = repositionPoint(n2.getPosition(), width, height, mapCenterMass, scaleFactor);

                // Add path
                QuadCurve fxPathLine = new QuadCurve(startPos.x, startPos.y, midPos.x, midPos.y, endPos.x, endPos.y);
                fxPathLine.getStyleClass().add("mapPathLine");
                mapViewPathLayer.getChildren().add(fxPathLine);

                // Add path mark
                QuadCurve fxPathMarkLine = new QuadCurve(startPos.x, startPos.y, midPos.x, midPos.y, endPos.x, endPos.y);
                fxPathMarkLine.getStyleClass().addAll("mapPathLine", "marker");
                fxPathMarkLine.idProperty().setValue(n1.getIndex(map) + "-" + n2.getIndex(map));
                mapViewPathMarks.getChildren().add(fxPathMarkLine);

                // Add distance text
                Text distanceText = new Text(String.valueOf(c.getDistance()));
                distanceText.getStyleClass().addAll("distanceText", "darkText");
                double halfWidth = distanceText.getLayoutBounds().getWidth() / 2;
                double halfHeight = distanceText.getLayoutBounds().getHeight() / 2;
                distanceText.setX(midPos.x - halfWidth * 1.5);
                distanceText.setY(midPos.y - halfHeight * 1.5);
                mapViewPathLayer.getChildren().add(distanceText);

                // Add path handle
                Circle fxPathHandleDot = new Circle(midPos.x, midPos.y, HANDLE_DOT_SIZE);
                fxPathHandleDot.getStyleClass().addAll("mapNodeDot", "mapHandle");
                fxPathHandleDot.idProperty().setValue(n1.getIndex(map) + "-" + n2.getIndex(map));
                mapViewPathHandles.getChildren().add(fxPathHandleDot);
            }

            // Add connect handles
            if (junc.getBottomNode().getNeighbors().size() < 3) {
                System.out.println("Adding handle for bottom");
                Circle fxConnectHandleDot = new Circle(pos1.x, pos1.y, HANDLE_DOT_SIZE);
                fxConnectHandleDot.getStyleClass().addAll("mapNodeDot", "mapHandle");
                fxConnectHandleDot.idProperty().setValue(String.valueOf(junc.getBottomNode().getIndex(map)));
                mapViewConnectHandles.getChildren().add(fxConnectHandleDot);
            }

            if (junc.getRightNode().getNeighbors().size() < 3) {
                Circle fxConnectHandleDot = new Circle(pos2.x, pos2.y, HANDLE_DOT_SIZE);
                fxConnectHandleDot.getStyleClass().addAll("mapNodeDot", "mapHandle");
                fxConnectHandleDot.idProperty().setValue(String.valueOf(junc.getRightNode().getIndex(map)));
                mapViewConnectHandles.getChildren().add(fxConnectHandleDot);
            }

            if (junc.getLeftNode().getNeighbors().size() < 3) {
                Circle fxConnectHandleDot = new Circle(pos3.x, pos3.y, HANDLE_DOT_SIZE);
                fxConnectHandleDot.getStyleClass().addAll("mapNodeDot", "mapHandle");
                fxConnectHandleDot.idProperty().setValue(String.valueOf(junc.getLeftNode().getIndex(map)));
                mapViewConnectHandles.getChildren().add(fxConnectHandleDot);
            }

        }
    }

    /**
     * Reposition the given viewport point to correspond to the map coordinate.
     * @param p Viewport position
     * @param width Width of view
     * @param height Height of view
     * @param offset Center mass offset from (0,0)
     * @param scale Scale factor
     * @return New repositioned position
     */
    public static Position unpositionPoint(Position p, double width, double height, Position offset, double scale) {
        Position newPos = new Position(p);

        newPos.subtract(new Position(width / 2, height / 2));
        newPos.divide(scale);
        newPos.add(offset);

        return newPos;
    }

    /**
     * Reposition the given point to correspond to the viewport.
     * @param p Base position
     * @param width Width of view
     * @param height Height of view
     * @param offset Center mass offset from (0,0)
     * @param scale Scale factor
     * @return New repositioned position
     */
    public static Position repositionPoint(Position p, double width, double height, Position offset, double scale) {
        Position newPos = new Position(p);

        newPos.subtract(offset);
        newPos.multiply(scale);
        newPos.add(new Position(width / 2, height / 2));

        return newPos;
    }

    /**
     * Calculate a scale factor to fit the given Map to the view.
     * @param map Map data
     * @param width View width
     * @param height View height
     * @param mid Map center mass
     * @return Rescale factor to fit the view
     */
    public static double calculateScaleFactor(Map map, double width, double height, Position mid) {
        return Math.min(calculateScaleFactorX(map, width, mid.x), calculateScaleFactorY(map, height, mid.y));
    }

    /**
     * Calculate a scale factor to fit the width of given Map to the view.
     * @param map Map data
     * @param width View width
     * @param mid Map center mass
     * @return Rescale factor to fit the width of view
     */
    public static double calculateScaleFactorX(Map map, double width, double mid) {
        double min = mid;
        double max = mid;

        // Sum all positions
        for (Node n : map.getNodes()) {
            Position nodePos = n.getPosition();

            if (min > nodePos.x)
                min = nodePos.x;

            if (max < nodePos.x)
                max = nodePos.x;

            for (Connection c : n.getNeighbors()) {
                Position midPos = c.getMidPoint();

                if (min > midPos.x)
                    min = midPos.x;

                if (max < midPos.x)
                    max = midPos.x;
            }
        }

        for (Junction j : map.getJunctions()) {

            Position juncPos = j.getPosition();

            if (min > juncPos.x)
                min = juncPos.x;

            if (max < juncPos.x)
                max = juncPos.x;
        }

        Double factor = width / (2 * (max - min));
        factor = factor.isInfinite() || factor.isNaN() ? 1 : factor;

        return factor;
    }

    /**
     * Calculate a scale factor to fit the height of given Map to the view.
     * @param map Map data
     * @param height View height
     * @param mid Map center mass
     * @return Rescale factor to fit the width of view
     */
    public static double calculateScaleFactorY(Map map, double height, double mid) {
        double min = mid;
        double max = mid;

        // Sum all positions
        for (Node n : map.getNodes()) {
            Position nodePos = n.getPosition();

            if (min > nodePos.y)
                min = nodePos.y;

            if (max < nodePos.y)
                max = nodePos.y;

            for (Connection c : n.getNeighbors()) {
                Position midPos = c.getMidPoint();

                if (min > midPos.y)
                    min = midPos.y;

                if (max < midPos.y)
                    max = midPos.y;
            }
        }

        for (Junction j : map.getJunctions()) {

            Position juncPos = j.getPosition();

            if (min > juncPos.y)
                min = juncPos.y;

            if (max < juncPos.y)
                max = juncPos.y;
        }


        Double factor = height / (2 * (max - min));
        factor = factor.isInfinite() || factor.isNaN() ? 1 : factor;

        return factor;
    }

    /**
     * Calculate the center mass of the given map. This takes into account the node and center point positions.
     * @param map Map data
     * @return Position of map center mass
     */
    public static Position calculateCenterMass(Map map) {
        Position offset = new Position();
        int numberOfPositions = 0;

        // Sum all positions
        for (Node n : map.getNodes()) {
            offset.add(n.getPosition());
            numberOfPositions++;

            for (Connection c : n.getNeighbors()) {
                offset.add(c.getMidPoint());
                numberOfPositions++;
            }
        }

        for (Junction j : map.getJunctions()) {
            offset.add(j.getPosition());
            numberOfPositions++;
        }

        // Divide by number of positions to get center of mass
        offset.divide(numberOfPositions);

        if (((Double)offset.x).isNaN() ||((Double)offset.x).isInfinite() || ((Double)offset.y).isNaN() || ((Double)offset.y).isInfinite())
            return new Position(0, 0);

        return offset;
    }
}
