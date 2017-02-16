/*
 * Copyright (C) 2014 Linus Törnkrantz <linus@blom.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.prettybyte.hexagons;

import javafx.scene.paint.Color;
import static java.lang.Math.PI;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;
import javafx.application.Platform;
import javafx.scene.shape.Polygon;

import java.util.ArrayList;

/**
 * A Hexagon is the building block of the grid.
 */
public class Hexagon extends Polygon {

    final GridPosition position;
    private Map map;
    private boolean isVisualObstacle;
    private boolean isBlockingPath;
    int aStarGscore, aStarFscore;      // Variables for the A* pathfinding algorithm.
    Hexagon aStarCameFrom;
    private int graphicsHeight;
    private double graphicsWidth;
    private int graphicsXoffset;
    private int graphicsYoffset;

    /**
     * @param q
     * @param r
     */
    public Hexagon(int q, int r) {
        this.position = new GridPosition(q, r);
    }

    /**
     * @param q
     * @param r
     * @map map
     */
    public Hexagon(int q, int r, Map map) {
        this.position = new GridPosition(q, r);
        this.map = map;
        init();
    }

    private void init() {
        this.setStroke(Color.BLACK);
        for (double p : calculatePolygonPoints()) {
            this.getPoints().add(p);
        }
    }

    /**
     *
     * @return axial Q-value
     */
    public int getQ() {
        return position.q;
    }

    /**
     *
     * @return axial R-value
     */
    public int getR() {
        return position.r;
    }

    /**
     * @return the axial coordinates of the Hexagon
     */
//    public GridPosition getPosition() {
  //      return position;
    //}

    /**
     * This affects the field of view calculations. If true, the hexagons behind
     * this hexagon cannot be seen (but this hexagon can still be seen).
     *
     * @param b
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
     *
     * @param b
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
        graphicsHeight = map.graphicsSize * 2;
        graphicsWidth = sqrt(3) / 2 * graphicsHeight;
        graphicsXoffset = (int) (graphicsWidth * (double) position.q + 0.5 * graphicsWidth * (double) position.r);
        graphicsYoffset = (int) (3.0 / 4.0 * graphicsHeight * position.r);
        graphicsXoffset = graphicsXoffset + map.graphicsXpadding;
        graphicsYoffset = graphicsYoffset + map.graphicsYpadding;

        double polyPoints[] = new double[12];
        double angle;
        for (int i = 0; i < 6; i++) {
            angle = 2 * PI / 6 * (i + 0.5);
            polyPoints[(i * 2)] = (graphicsXoffset + map.graphicsSize * cos(angle));
            polyPoints[(i * 2 + 1)] = (graphicsYoffset + map.graphicsSize * sin(angle));
        }
        return polyPoints;
    }

    public int getGraphicsXoffset() {
        return graphicsXoffset;
    }

    public int getGraphicsYoffset() {
        return graphicsYoffset;
    }

    public static double getGraphicsHexagonWidth(int hexagonSize) {
        return sqrt(3) / 2 * hexagonSize * 2;
    }

    public static int getGraphicsHexagonHeight(int hexagonSize) {
        return hexagonSize * 2;
    }

    public static double getGraphicsHorizontalDistanceBetweenHexagons(int hexagonSize) {
        return getGraphicsHexagonWidth(hexagonSize);
    }

    public static double getGraphicsverticalDistanceBetweenHexagons(int hexagonSize) {
        return (3.0/4.0 * hexagonSize * 2.0);
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

    public int getGraphicsDistanceTo(Hexagon destination) {
        int deltaX = this.getGraphicsXoffset() - destination.getGraphicsXoffset();
        int deltaY = this.getGraphicsYoffset() - destination.getGraphicsYoffset();
        return (int) Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
    }

    @Override
    public String toString() {
        return "Hexagon q:"+position.q +" r:"+position.r;
    }

    public Map.Direction getDirectionTo(Hexagon target) {
        return position.getDirectionTo(target.position);
    }

    public ArrayList<Hexagon> getPositionsOnCircleEdge(int radius) {
        ArrayList<Hexagon> result = new ArrayList<>();
        for (GridPosition position : position.getPositionsOnCircleEdge(radius)) {
            try {
                Hexagon hexagon = map.getHexagon(position);
                result.add(hexagon);
            } catch (NoHexagonException e) {
            }
        }
        return result;
    }

    public ArrayList<Hexagon> getPositionsInCircleArea(int radius) {
        ArrayList<Hexagon> result = new ArrayList<>();
        for (GridPosition position : position.getPositionsInCircleArea(radius)) {
            try {
                Hexagon hexagon = map.getHexagon(position);
                result.add(hexagon);
            } catch (NoHexagonException e) {
            }
        }
        return result;
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

    void setMap(Map map) {
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
