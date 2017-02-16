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
package com.prettybyte.hexagonz;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * Holds all the hexes.
 * Pathfinding, line-of-sight and other useful functions.
 */
public class Map {

    public enum Direction {NORTHWEST, NORTHEAST, EAST, SOUTHEAST, SOUTHWEST, WEST}

    private HashMap<GridPosition, Hexagon> hexagons = new HashMap<>();

    private static Map instance = null;

    public Map() {
    }

    /**
     * Map is a Singleton class
     *
     * @return the instance
     */
    public static Map getInstance() {
        if (instance == null) {
            instance = new Map();
        }
        return instance;
    }

    /**
     * Retrieves the Hexagon at the specified position
     *
     * @param position the position
     * @return the Hexagon
     * @throws NoHexagonException if there is no Hexagon at the specified position
     */
    public Hexagon getHexagon(GridPosition position) throws NoHexagonException {
        Hexagon result = hexagons.get(position);
        if (result == null) {
            throw new NoHexagonException("The Hexagon does not exist");
        }
        return result;
    }

    Hexagon getHexagonByCube(int x, int y, int z) throws NoHexagonException {
        return getHexagon(new GridPosition(x, z));
    }

    /**
     * Adds a Hexagon to the map
     *
     * @param hexagon the Hexagon that should be added to the map
     */
    public void addHexagon(Hexagon hexagon) {
        hexagons.put(hexagon.getPosition(), hexagon);
    }

    /**
     *
     * @return all Hexagons that has been added to the map
     */
    public Collection<Hexagon> getAllHexagons() {
        return hexagons.values();
    }

    /**
     * Finds the neighbour of the given Hexagon
     *
     * @param hexagon
     * @param direction
     * Hexagon class
     * @return
     * @throws com.prettybyte.hexagonz.NoHexagonException
     */
    public Hexagon getNeighbour(Hexagon hexagon, Direction direction) throws NoHexagonException {
        GridPosition neighborPosition = hexagon.getPosition().getNeighborPosition(direction);
        return getHexagon(neighborPosition);
    }

    /**
     * Finds all neighbors of the given Hexagon
     *
     * @param hexagon
     * @return
     */
    public Hexagon[] getNeighbours(Hexagon hexagon) {
        Hexagon[] tempResult = new Hexagon[6];
        Hexagon neighbour;
        int numberOfNeighbours = 0;
        for (int i = 0; i < 6; i++) {
            try {
                neighbour = getNeighbour(hexagon, GridPosition.getDirectionFromNumber(i));
                tempResult[numberOfNeighbours] = neighbour;
                numberOfNeighbours++;
            } catch (NoHexagonException ex) {
            }
        }
        Hexagon[] result = new Hexagon[numberOfNeighbours];
        System.arraycopy(tempResult, 0, result, 0, numberOfNeighbours);
        return result;
    }

    /**
     * Finds the cheapest path from start to the goal. The A* algorithm is used.
     * This method uses the method isBlockingPath() in Hexagon and the movement cost between neighboring hexagons is always 1.
     *
     * @param start the Hexagon that you are starting from
     * @param destination the target Hexagon
     * @param pathInfoSupplier a class implementing the IPathInfoSupplier interface
     * @return an array of Hexagons, sorted so that the first step comes first.
     * @throws NoPathException if there exists no path between start and the goal
     */
    public ArrayList<Hexagon> getPathBetween(Hexagon start, Hexagon destination, IPathInfoSupplier pathInfoSupplier) throws NoPathException {
        ArrayList<Hexagon> closedSet = new ArrayList<>();    // The set of nodes already evaluated
        ArrayList<Hexagon> openSet = new ArrayList<>();   // The set of tentative nodes to be evaluated, initially containing the start node
        openSet.add(start);
        start.aStarGscore = 0;
        start.aStarFscore = start.aStarGscore + GridPosition.getDistance(start.getPosition(), destination.getPosition());

        Hexagon currentHexagon;
        int tentative_g_score;
        while (openSet.size() > 0) {
            currentHexagon = findHexagonWithLowestFscore(openSet);
            if (currentHexagon.getPosition().equals(destination.getPosition())) {
                return reconstruct_path(start, destination);
            }
            openSet.remove(currentHexagon);
            closedSet.add(currentHexagon);

            for (Hexagon neighbour : getNeighbours(currentHexagon)) {
                if ((!pathInfoSupplier.isBlockingPath(neighbour)) || neighbour.equals(destination)) {
                    if (!closedSet.contains(neighbour)) {
                        tentative_g_score = currentHexagon.aStarGscore + pathInfoSupplier.getMovementCost(currentHexagon, neighbour);

                        if (!openSet.contains(neighbour) || tentative_g_score < neighbour.aStarGscore) {
                            neighbour.aStarCameFrom = currentHexagon;
                            neighbour.aStarGscore = tentative_g_score;
                            neighbour.aStarFscore = neighbour.aStarGscore + GridPosition.getDistance(neighbour.getPosition(), destination.getPosition());

                            /*
                            TODO: Vill få den att generera path som är mer som line() så att de inte rör sig kantigt på kartan. Nedanstående funkar sådär:
                            neighbour.aStarFscore = neighbour.aStarGscore + neighbour.getGraphicsDistanceTo(destination);

                            Ett sätt kunde vara att undersöka om man kan identifiera hex där path går runt ett hörn (har de unika g-värden?), dvs en ruta som definitivt ska besökas och sedan mäta det grafiska avståndet till dem som f-värde.
                             */
                            if (!openSet.contains(neighbour)) {
                                openSet.add(neighbour);
                            }
                        }
                    }
                }
            }
        }
        throw new NoPathException("Can't find any path to the goal Hexagon");
    }

    /**
     * Finds the cheapest path from start to the goal. The A* algorithm is used.
     * This method uses the method isBlockingPath() in Hexagon and the movement cost between neighboring hexagons is always 1.
     *
     * @param start the Hexagon that you are starting from
     * @param destination the target Hexagon
     * @return an array of Hexagons, sorted so that the first step comes first.
     * @throws NoPathException if there exists no path between start and the
     * goal
     */
    public ArrayList<Hexagon> getPathBetween(Hexagon start, Hexagon destination) throws NoPathException {
        return getPathBetween(start, destination, new DefaultPathInfoSupplier());
    }

    private static class DefaultPathInfoSupplier implements IPathInfoSupplier {
        @Override
        public boolean isBlockingPath(Hexagon hexagon) {
            return hexagon.isBlockingPath();
        }

        @Override
        public int getMovementCost(Hexagon from, Hexagon to) {
            return 1;
        }
    }

    private Hexagon findHexagonWithLowestFscore(ArrayList<Hexagon> openSet) {
        Hexagon hexagonWithLowestFscore = openSet.get(0);          // Just pick anyone and then see if we can find any better
        int lowestFscore = hexagonWithLowestFscore.aStarFscore;
        for (Hexagon h : openSet) {
            if (h.aStarFscore < lowestFscore) {
                hexagonWithLowestFscore = h;
                lowestFscore = h.aStarFscore;
            }
        }
        return hexagonWithLowestFscore;
    }

    private ArrayList<Hexagon> reconstruct_path(Hexagon start, Hexagon goal) {
        ArrayList<Hexagon> path = new ArrayList<>();
        Hexagon currentHexagon = goal;
        while (currentHexagon != start) {
            path.add(currentHexagon);
            currentHexagon = currentHexagon.aStarCameFrom;
        }
        Collections.reverse(path);
        return path;
    }

    /**
     * Finds all Hexagons that are on a line between two positions
     *
     * @param origin
     * @param destination
     * @return
     */
    public ArrayList<Hexagon> line(GridPosition origin, GridPosition destination) {
        Hexagon h;
        ArrayList<Hexagon> result = new ArrayList<>();
        ArrayList<GridPosition> positions = origin.line(destination);

        for (GridPosition position : positions) {
            try {
                h = getHexagon(position);
                result.add(h);
            } catch (NoHexagonException ex) {
            }
        }
        return result;
    }

    /**
     * Calculates all Hexagons that are visible from the given position. The
     * line of sight can be blocked by Hexagons that has isVisualObstacle ==
     * true. NOTE: Accuracy is not guaranteed!
     *
     * @param origin
     * @param visibleRange a limit of how long distance can be seen assuming
     * there are no obstacles
     * @return an array of Hexagons that are visible
     */
    public ArrayList<Hexagon> getVisibleHexes(Hexagon origin, int visibleRange) {
        GridPosition[] ringMembers = origin.getPosition().getPositionsInRing(visibleRange);
        ArrayList<Hexagon> result = new ArrayList<>();

        ArrayList<Hexagon> line;
        for (GridPosition ringMemberPosition : ringMembers) {
            line = line(origin.getPosition(), ringMemberPosition);
            for (Hexagon hexagonInLine : line) {
                result.add(hexagonInLine);
                if (hexagonInLine.isVisualObstacle()) {
                    break;
                }
            }
        }
        return result;
    }
}
