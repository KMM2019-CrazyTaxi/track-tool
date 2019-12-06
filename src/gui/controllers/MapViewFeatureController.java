package gui.controllers;

import editor.Editor;
import helpers.UpdateListener;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.QuadCurve;
import map.Connection;
import map.Map;
import map.Node;
import map.Position;

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

    @FXML private Group mapViewNodeHandles;
    @FXML private Group mapViewPathHandles;
    @FXML private Group mapViewJunctionHandles;

    public void initialize() {
        Editor.getInstance().map.subscribe(this);
    }

    /**
     * Update the shown map.
     * @param data New Map data.
     */
    public void update(Map data) {
        redraw(data);
    }

    /**
     * Draw the given Map to the view.
     * @param map Map data
     */
    private void redraw(Map map) {
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

            if (n.getNeighbors().size() <= 2) {
                // Add node handle
                Circle fxNodeHandleDot = new Circle(startPos.x, startPos.y, HANDLE_DOT_SIZE);
                fxNodeHandleDot.getStyleClass().addAll("mapNodeDot", "mapHandle");
                fxNodeHandleDot.idProperty().setValue(String.valueOf(n.getIndex(map)));
                mapViewNodeHandles.getChildren().add(fxNodeHandleDot);


                for (Connection c : n.getNeighbors()) {
                    Position midPos = repositionPoint(c.getMidPoint(), width, height, mapCenterMass, scaleFactor);
                    Position endPos = repositionPoint(c.getConnectingNode().getPosition(), width, height, mapCenterMass, scaleFactor);

                    // Add path
                    QuadCurve fxPathLine = new QuadCurve(startPos.x, startPos.y, midPos.x, midPos.y, endPos.x, endPos.y);
                    fxPathLine.getStyleClass().add("mapPathLine");
                    fxPathLine.idProperty().setValue("mapPathLine" + n.getIndex(map) + ":" + c.getConnectingNode().getIndex(map));
                    mapViewPathLayer.getChildren().add(fxPathLine);

                    // Add path handle

                    Circle fxPathHandleDot = new Circle(midPos.x, midPos.y, HANDLE_DOT_SIZE);
                    fxPathHandleDot.getStyleClass().addAll("mapNodeDot", "mapHandle");
                    fxPathHandleDot.idProperty().setValue(n.getIndex(map) + ":" + c.getConnectingNode().getIndex(map));
                    mapViewPathHandles.getChildren().add(fxPathHandleDot);
                }
            }
            else {
                // Add junction handle
                Node junc1 = n;
                Node junc2 = n.getNeighbors().get(0).getConnectingNode();
                Node junc3 = n.getNeighbors().get(1).getConnectingNode();

                Position juncPos = new Position(startPos);
                juncPos.add(junc2.getPosition());
                juncPos.add(junc3.getPosition());
                juncPos.divide(3);

                Circle fxJunctionHandleDot = new Circle(juncPos.x, juncPos.y, HANDLE_DOT_SIZE);
                fxJunctionHandleDot.getStyleClass().addAll("mapNodeDot", "mapHandle");
                fxJunctionHandleDot.idProperty().setValue(junc1.getIndex(map) + ":" + junc2.getIndex(map) + ":" + junc3.getIndex(map));
                mapViewJunctionHandles.getChildren().add(fxJunctionHandleDot);
            }

        }
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
    private static Position repositionPoint(Position p, double width, double height, Position offset, double scale) {
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
    private static double calculateScaleFactor(Map map, double width, double height, Position mid) {
        return Math.min(calculateScaleFactorX(map, width, mid.x), calculateScaleFactorY(map, height, mid.y));
    }

    /**
     * Calculate a scale factor to fit the width of given Map to the view.
     * @param map Map data
     * @param width View width
     * @param mid Map center mass
     * @return Rescale factor to fit the width of view
     */
    private static double calculateScaleFactorX(Map map, double width, double mid) {
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

        return width / (2 * (max - min));
    }

    /**
     * Calculate a scale factor to fit the height of given Map to the view.
     * @param map Map data
     * @param height View height
     * @param mid Map center mass
     * @return Rescale factor to fit the width of view
     */
    private static double calculateScaleFactorY(Map map, double height, double mid) {
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

        return height / (2 * (max - min));
    }

    /**
     * Calculate the center mass of the given map. This takes into account the node and center point positions.
     * @param map Map data
     * @return Position of map center mass
     */
    private static Position calculateCenterMass(Map map) {
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

        // Divide by number of positions to get center of mass
        offset.divide(numberOfPositions);

        return offset;
    }
}
