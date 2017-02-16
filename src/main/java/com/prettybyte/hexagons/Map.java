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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * Holds all the hexes.
 * Pathfinding, getLine-of-sight and other useful functions.
 */
public class Map {

    final int graphicsSize;
    final int graphicsXpadding;
    final int graphicsYpadding;

    public enum Direction {NORTHWEST, NORTHEAST, EAST, SOUTHEAST, SOUTHWEST, WEST}

    private HashMap<GridPosition, Hexagon> hexagons = new HashMap<>();

    private static Map instance = null;

    public Map(int graphicsSize, int graphicsXpadding, int graphicsYpadding) {
        this.graphicsSize = graphicsSize;
        this.graphicsXpadding = graphicsXpadding;
        this.graphicsYpadding = graphicsYpadding;
    }

    public Hexagon addHexagon(Hexagon hexagon) {
        hexagon.setMap(this);
        hexagons.put(hexagon.position, hexagon);
        return hexagon;
    }

    public Hexagon getHexagonContainingPixel(int x, int y) throws NoHexagonException {
        return getHexagon(GridDrawer.pixelToPosition(x, y, graphicsSize));
    }

    /**
     * Retrieves the Hexagon at the specified position
     *
     * @param q
     * @param r
     * @return the Hexagon
     * @throws NoHexagonException if there is no Hexagon at the specified position
     */
    public Hexagon getHexagon(int q, int r) throws NoHexagonException {
        GridPosition position = new GridPosition(q, r);
        Hexagon result = hexagons.get(position);
        if (result == null) {
            throw new NoHexagonException("The Hexagon does not exist");
        }
        return result;
    }

    Hexagon getHexagon(GridPosition position) throws NoHexagonException {
        return getHexagon(position.q, position.r);
    }

    Hexagon getHexagonByCube(int x, int y, int z) throws NoHexagonException {
        return getHexagon(x, z);
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
     * @throws com.prettybyte.hexagons.NoHexagonException
     */
    public Hexagon getNeighbour(Hexagon hexagon, Direction direction) throws NoHexagonException {
        GridPosition neighborPosition = hexagon.position.getNeighborPosition(direction);
        return getHexagon(neighborPosition);
    }

    /**
     * Finds all neighbors of the given Hexagon
     *
     * @param hexagon
     * @return
     */
    public ArrayList<Hexagon> getNeighbours(Hexagon hexagon) {
        ArrayList<Hexagon> result = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            try {
                Hexagon neighbour = getNeighbour(hexagon, GridPosition.getDirectionFromNumber(i));
                result.add(neighbour);
            } catch (NoHexagonException ex) {
            }
        }
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
        start.aStarFscore = start.aStarGscore + GridPosition.getDistance(start.position, destination.position);

        Hexagon currentHexagon;
        int tentative_g_score;
        while (openSet.size() > 0) {
            currentHexagon = findHexagonWithLowestFscore(openSet);
            if (currentHexagon.position.equals(destination.position)) {
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
                            neighbour.aStarFscore = neighbour.aStarGscore + GridPosition.getDistance(neighbour.position, destination.position);

                            /*
                            TODO: Vill få den att generera path som är mer som getLine() så att de inte rör sig kantigt på kartan. Nedanstående funkar sådär:
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
     * Finds all Hexagons that are on a getLine between two Hexagons
     *
     * @param origin
     * @param destination
     * @return
     */
    public ArrayList<Hexagon> getLine(Hexagon origin, Hexagon destination) {
        return getLine(origin.position, destination.position);
    }

    private ArrayList<Hexagon> getLine(GridPosition origin, GridPosition destination) {
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
     * Calculates all Hexagons that are visible from a specific Hexagon. The
     * getLine of sight can be blocked by Hexagons that has isVisualObstacle ==
     * true. NOTE: Accuracy is not guaranteed!
     *
     * @param origin
     * @param visibleRange a limit of how long distance can be seen assuming
     * there are no obstacles
     * @return an array of Hexagons that are visible
     */
    public ArrayList<Hexagon> getVisibleHexes(Hexagon origin, int visibleRange) {
        ArrayList<GridPosition> ringMembers = origin.position.getPositionsOnCircleEdge(visibleRange);
        ArrayList<Hexagon> result = new ArrayList<>();
        ArrayList<Hexagon> line;
        for (GridPosition ringMemberPosition : ringMembers) {
            line = getLine(origin.position, ringMemberPosition);
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
