package com.prettybyte.hexagons;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

import java.util.ArrayList;

import static java.lang.Math.*;

/**
 * A Hexagon is the building block of the grid.
 */
public class Hexagon extends Polygon {

    final GridPosition position;
    private HexagonMap map;
    private boolean isVisualObstacle;
    private boolean isBlockingPath;
    int aStarGscore, aStarFscore;      // Variables for the A* pathfinding algorithm.
    Hexagon aStarCameFrom;
    private int graphicsXoffset;
    private int graphicsYoffset;

    /**
     * The position of the Hexagon is specified with axial coordinates
     *
     * @param q the Q coordinate
     * @param r the R coordinate
     */
    public Hexagon(int q, int r) {
        this.position = new GridPosition(q, r);
    }

    void init() {
        this.setStroke(Color.BLACK);
        for (double p : calculatePolygonPoints()) {
            this.getPoints().add(p);
        }
    }

    /**
     * @return axial Q-value
     */
    public int getQ() {
        return position.q;
    }

    /**
     * @return axial R-value
     */
    public int getR() {
        return position.r;
    }

    /**
     * This affects the field of view calculations. If true, the hexagons behind
     * this hexagon cannot be seen (but this hexagon can still be seen).
     */
    public void setIsVisualObstacle(boolean b) {
        isVisualObstacle = b;
    }

    /**
     * This affects the field of view calculations.
     *
     * @return If true, the hexagons behind this hexagon cannot be seen (but
     * this hexagon can still be seen).
     */
    public boolean isVisualObstacle() {
        return isVisualObstacle;
    }

    /**
     * This affects the pathfinding calculations. If true, the algorithm will
     * try to find a path around this Hexagon.
     * If you want to have more control over this, you can supply your own class implementing IPathInfoSupplier to the pathfinding method.
     */
    public void setIsBlockingPath(boolean b) {
        isBlockingPath = b;
    }

    /**
     * This affects the pathfinding calculations
     *
     * @return true if this is an obstacle that blocks the path
     */
    public boolean isBlockingPath() {
        return isBlockingPath;
    }

    // --------------------- Graphics --------------------------------------------
    private double[] calculatePolygonPoints() {
        checkMap();
        int graphicsHeight = map.hexagonSize * 2;
        double graphicsWidth = sqrt(3) / 2 * graphicsHeight;
        graphicsXoffset = (int) (graphicsWidth * (double) position.q + 0.5 * graphicsWidth * (double) position.r);
        graphicsYoffset = (int) (3.0 / 4.0 * graphicsHeight * position.r);
        graphicsXoffset = graphicsXoffset + map.graphicsXpadding;
        graphicsYoffset = graphicsYoffset + map.graphicsYpadding;

        double polyPoints[] = new double[12];
        double angle;
        for (int i = 0; i < 6; i++) {
            angle = 2 * PI / 6 * (i + 0.5);
            polyPoints[(i * 2)] = (graphicsXoffset + map.hexagonSize * cos(angle));
            polyPoints[(i * 2 + 1)] = (graphicsYoffset + map.hexagonSize * sin(angle));
        }
        return polyPoints;
    }

    public int getGraphicsXoffset() {
        if (graphicsXoffset == 0) {
            calculatePolygonPoints();
        }
        return graphicsXoffset;
    }

    public int getGraphicsYoffset() {
        if (graphicsYoffset == 0) {
            calculatePolygonPoints();
        }
        return graphicsYoffset;
    }

    /**
     * This method is the safe way to change the background color since it makes
     * sure that the change is made on the JavaFX Application thread.
     *
     * @param c the color
     */
    public void setBackgroundColor(Color c) {
        Platform.runLater(new UIupdater(this, c));
    }

    @Override
    public String toString() {
        return "Hexagon q:" + position.q + " r:" + position.r;
    }

    public HexagonMap.Direction getDirectionTo(Hexagon target) {
        return position.getDirectionTo(target.position);
    }

    /**
     * Returns all Hexagons that are located a certain distance from here
     */
    public ArrayList<Hexagon> getHexagonsOnRingEdge(int radius) {
        checkMap();
        return Calculations.getHexagonsOnRingEdge(this, radius, map);
    }

    /**
     * Returns all Hexagons that are located within a certain distance from here
     */
    public ArrayList<Hexagon> getHexagonsInRingArea(int radius) {
        checkMap();
        return Calculations.getHexagonsInRingArea(this, radius, map);
    }

    private void checkMap() {
        if (map == null) {
            throw new RuntimeException("Hexagon must be added to a HexagonMap before this operation. See addHexahon()");
        }
    }

    /**
     * Finds the neighbour of this Hexagon
     *
     * @param direction
     * @return neighbour
     * @throws NoHexagonFoundException
     */
    public Hexagon getNeighbour(HexagonMap.Direction direction) throws NoHexagonFoundException {
        checkMap();
        GridPosition neighborPosition = position.getNeighborPosition(direction);
        return map.getHexagon(neighborPosition);
    }

    /**
     * Finds all neighbors of this Hexagon
     */
    public ArrayList<Hexagon> getNeighbours() {
        ArrayList<Hexagon> result = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            try {
                Hexagon neighbour = getNeighbour(GridPosition.getDirectionFromNumber(i));
                result.add(neighbour);
            } catch (NoHexagonFoundException ex) {
            }
        }
        return result;
    }

    /**
     * Finds the cheapest path from start to the goal. The A* algorithm is used.
     *
     * @param destination      the target Hexagon
     * @param pathInfoSupplier a class implementing the IPathInfoSupplier interface. This can be used to add inpassable hexagons and customize the movement costs.
     * @return an array of Hexagons, sorted so that the first step comes first.
     * @throws NoPathFoundException if there exists no path between start and the goal
     */
    public ArrayList<Hexagon> getPathTo(Hexagon destination, IPathInfoSupplier pathInfoSupplier) throws NoPathFoundException {
        checkMap();
        return Calculations.getPathBetween(this, destination, pathInfoSupplier);
    }

    /**
     * Finds the cheapest path from here to the destination. The A* algorithm is used.
     * This method uses the method isBlockingPath() in Hexagon and the movement cost between neighboring hexagons is always 1.
     *
     * @param destination the target Hexagon
     * @return an array of Hexagons, sorted so that the first step comes first.
     * @throws NoPathFoundException if there exists no path between start and the
     *                              goal
     */
    public ArrayList<Hexagon> getPathTo(Hexagon destination) throws NoPathFoundException {
        checkMap();
        return Calculations.getPathBetween(this, destination, new HexagonMap.DefaultPathInfoSupplier());
    }

    /**
     * Finds all Hexagons that are on a line between this and destination
     */
    public ArrayList<Hexagon> getLine(Hexagon origin, Hexagon destination) {
        checkMap();
        return Calculations.getLine(origin.position, destination.position, map);
    }

    /**
     * Calculates all Hexagons that are visible from this Hexagon. The
     * line of sight can be blocked by Hexagons that has isVisualObstacle ==
     * true. NOTE: Accuracy is not guaranteed!
     *
     * @param visibleRange a limit of how long distance can be seen assuming
     *                     there are no obstacles
     * @return an array of Hexagons that are visible
     */
    public ArrayList<Hexagon> getVisibleHexes(int visibleRange) {
        checkMap();
        return Calculations.getVisibleHexes(this, visibleRange, map);
    }

    public int getDistance(Hexagon target) {
        return position.getDistance(target.position);
    }

    /**
     * Two Hexagons are equal if they have the same q and r
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (!obj.getClass().equals(this.getClass())) {
            return false;
        }
        Hexagon hexagonObj = (Hexagon) obj;
        return (hexagonObj.getQ() == this.getQ() && hexagonObj.getR() == this.getR());
    }

    /**
     * This gives the Hexagon access a HexagonMap without actually adding it to the HexagonMap. It can be useful e.g. if you want
     * to make some calculations before creating another Hexagon.
     */
    void setMap(HexagonMap map) {
        this.map = map;
        init();
    }

    class UIupdater implements Runnable {
        private final Hexagon h;
        private final Color c;

        UIupdater(Hexagon h, Color c) {
            this.h = h;
            this.c = c;
        }

        @Override
        public void run() {
            h.setFill(c);
        }
    }
}
