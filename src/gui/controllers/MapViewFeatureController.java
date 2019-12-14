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

    @FXML private Group mapViewNodeHandles;
    @FXML private Group mapViewPathHandles;
    @FXML private Group mapViewJunctionHandles;

    @FXML private Group mapViewConnectHandles;

    public void initialize() {
        Editor.getInstance().map.subscribe(this);
    }

    /**
     * Update the shown map.
     * @param data New Map data.
     */
    public void update(Map data) {
        redraw(data);
        System.out.println("Redrew");
        System.out.println(mapViewConnectHandles.getChildren().size() + " connect handles");
        System.out.println(Editor.getInstance().map.get().getJunctions().size() + " junctions");
    }

    /**
     * Draw the given Map to the view.
     * @param map Map data
     */
    private void redraw(Map map) {
        mapViewNodeLayer.getChildren().clear();
        mapViewPathLayer.getChildren().clear();
        mapViewJunctionLayer.getChildren().clear();

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
                fxPathLine.idProperty().setValue("mapPathLine" + n.getIndex(map) + ":" + c.getConnectingNode(n).getIndex(map));
                mapViewPathLayer.getChildren().add(fxPathLine);

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
                fxPathHandleDot.idProperty().setValue(n.getIndex(map) + ":" + c.getConnectingNode(n).getIndex(map));
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
                fxPathHandleDot.idProperty().setValue(n1.getIndex(map) + ":" + n2.getIndex(map));
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

        System.out.println("facX: " + factor);

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

        System.out.println("facY: " + factor);

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
