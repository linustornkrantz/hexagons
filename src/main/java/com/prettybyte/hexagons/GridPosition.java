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

import java.io.Serializable;
import static java.lang.Math.abs;
import static java.lang.Math.round;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stores coordinates and has functions for grid calculations, e.g. getLine, ring
 * and distance. These calculations do not depend on how you have placed the
 * Hexagons on the Map. The axial coordinate system is used.
 */
class GridPosition implements Cloneable, Serializable {

    /**
     * The Axial Q coordinate
     */
    int q;

    /**
     * The Axial R coordinate
     */
    int r;

    /**
     * @param q the axial Q coordinate
     * @param r the axial R coordinate
     */
    GridPosition(int q, int r) {
        this.q = q;
        this.r = r;
    }

    /**
     * Finds the adjacent position in the specified direction from this position
     *
     * @param direction
     * @return the adjacent position
     */
    public GridPosition getNeighborPosition(Map.Direction direction) {
        int i = getNumberFromDirection(direction);
        int[][] neighbors = new int[][]{
            {0, -1}, {+1, -1}, {+1, 0}, {0, +1}, {-1, +1}, {-1, 0}
        };
        int[] d = neighbors[i];
        return new GridPosition(q + d[0], r + d[1]);
    }

    /**
     * Finds all positions that are on the edge of a circle in which this position is the
     * center. 
     * If radius is 0, an array with only this GridPosition will be returned.
     *
     * @param radius
     * @return
     */
    ArrayList<GridPosition> getPositionsOnCircleEdge(int radius) {
        ArrayList<GridPosition> result = new ArrayList<>();
        if (radius == 0) {
            result.add(this);
            return result;
        } else {
            GridPosition h = this;
            for (int i = 0; i < radius; i++) {
                h = h.getNeighborPosition(Map.Direction.SOUTHWEST);
            }
            int counter = 0;
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < radius; j++) {
                    result.add(h.clone());
                    counter++;
                    h = h.getNeighborPosition(getDirectionFromNumber(i));
                }
            }
            return result;
        }
    }

    ArrayList<GridPosition> getPositionsInCircleArea(int radius) {
        ArrayList<GridPosition> result = new ArrayList<>();
        for (int i = 0; i <= radius; i++) {
            ArrayList<GridPosition> positions = getPositionsOnCircleEdge(i);
            result.addAll(positions);
        }
        return result;
    }

    private static int getNumberFromDirection(Map.Direction direction) {
        switch (direction) {
            case NORTHWEST: return 0;
            case NORTHEAST: return 1;
            case EAST: return 2;
            case SOUTHEAST: return 3;
            case SOUTHWEST: return 4;
            case WEST: return 5;
        }
        throw new RuntimeException();
    }

    static Map.Direction getDirectionFromNumber(int i) {
        switch (i) {
            case 0: return Map.Direction.NORTHWEST;
            case 1: return Map.Direction.NORTHEAST;
            case 2: return Map.Direction.EAST;
            case 3: return Map.Direction.SOUTHEAST;
            case 4: return Map.Direction.SOUTHWEST;
            case 5: return Map.Direction.WEST;
        }
        throw new RuntimeException();
    }


    String getCoordinates() {
        String s = (Integer.toString(q) + ", " + Integer.toString(r));
            return s;
        }

        /**
         * Finds the position that best matches given non-integer coordinates
         *
         * @param q
         * @param r
         * @return
         */
    public static GridPosition hexRound(double q, double r) {
        double cubeX = q;
        double cubeY = r;
        double cubeZ = -cubeX - cubeY;

        long rx = round(cubeX);
        long ry = round(cubeY);
        long rz = round(cubeZ);

        double x_diff = abs(rx - cubeX);
        double y_diff = abs(ry - cubeY);
        double z_diff = abs(rz - cubeZ);

        if (x_diff > y_diff && x_diff > z_diff) {
            rx = -ry - rz;
        } else if (y_diff > z_diff) {
            ry = -rx - rz;
        } else {
            rz = -rx - ry;
        }
        return new GridPosition((int) rx, (int) ry);
    }

    @Override
    protected GridPosition clone() {
        try {
            return (GridPosition) super.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(GridPosition.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Two positons are equal if they have the same q and r
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj.getClass().equals(this.getClass())) {
            GridPosition gridPositionObj = (GridPosition) obj;
            if (gridPositionObj.q == this.q && gridPositionObj.r == this.r) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.q;
        hash = 97 * hash + this.r;
        return hash;
    }

    /**
     *
     * @param otherPosition
     * @return true if the positions are adjacent
     */
    public boolean isAdjacent(GridPosition otherPosition) {
        GridPosition neighbor;
        for (int i = 0; i < 6; i++) {
            neighbor = getNeighborPosition(getDirectionFromNumber(i));
            if (otherPosition.equals(neighbor)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param otherPosition
     * @return the direction
     */
    public Map.Direction getDirectionTo(GridPosition otherPosition) {
        if (this.equals(otherPosition)) {
            throw new IllegalArgumentException("Other position ("+otherPosition.toString()+") cannot be same as this ("+toString()+")");
        }
        GridPosition firstStepInLine = line(otherPosition).get(1);

        for (int i = 0; i < 6; i++) {
            if (getNeighborPosition(getDirectionFromNumber(i)).equals(firstStepInLine)) {
                return getDirectionFromNumber(i);
            }
        }
        throw new RuntimeException();
    }

    /**
     * Finds all GridPositions that are on a getLine between this and the given
     * position (the array includes this and the destination positions)
     *
     * @param destination
     * @return an array positions
     */
    public ArrayList<GridPosition> line(GridPosition destination) {
        ArrayList<GridPosition> result = new ArrayList<>();
        GridPosition p;
        double q_calculated, r_calculated;
        double n = getDistance(this, destination);
        for (int i = 0; i < n; i++) {
            double j = i;
            q_calculated = ((double) this.q * (1.0 - j / n) + (double) destination.q * j / n);
            r_calculated = ((double) this.r * (1.0 - j / n) + (double) destination.r * j / n);
            p = GridPosition.hexRound(q_calculated, r_calculated);
            result.add(p);

        }
        result.add(destination);
        return result;
    }

    /**
     * Calculates the grid distance between two positions
     *
     * @param a the start position
     * @param b the destination position
     * @return the distance (number of hexagons)
     */
    public static int getDistance(GridPosition a, GridPosition b) {
        return ((abs(a.q - b.q) + abs(a.r - b.r) + abs(a.q + a.r - b.q - b.r)) / 2);
    }

    public int getDistance(GridPosition target) {
        return getDistance(this, target);
    }

    @Override
    public String toString() {
        return "GridPosition q=" + q + ", r=" + r;
    }
}
