package com.prettybyte.hexagons;

import java.util.ArrayList;
import java.util.Collections;

class Calculations {
    static ArrayList<Hexagon> getPathBetween(Hexagon start, Hexagon destination, IPathInfoSupplier pathInfoSupplier) throws NoPathFoundException {
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

            for (Hexagon neighbour : currentHexagon.getNeighbours()) {
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
        throw new NoPathFoundException("Can't find any path to the goal Hexagon");
    }

    private static Hexagon findHexagonWithLowestFscore(ArrayList<Hexagon> openSet) {
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

    private static ArrayList<Hexagon> reconstruct_path(Hexagon start, Hexagon goal) {
        ArrayList<Hexagon> path = new ArrayList<>();
        Hexagon currentHexagon = goal;
        while (currentHexagon != start) {
            path.add(currentHexagon);
            currentHexagon = currentHexagon.aStarCameFrom;
        }
        Collections.reverse(path);
        return path;
    }

    static ArrayList<Hexagon> getLine(GridPosition origin, GridPosition destination, HexagonMap map) {
        Hexagon h;
        ArrayList<Hexagon> result = new ArrayList<>();
        ArrayList<GridPosition> positions = origin.line(destination);

        for (GridPosition position : positions) {
            try {
                h = map.getHexagon(position);
                result.add(h);
            } catch (NoHexagonFoundException ex) {
            }
        }
        return result;
    }

    static ArrayList<Hexagon> getVisibleHexes(Hexagon origin, int visibleRange, HexagonMap map) {
        ArrayList<GridPosition> ringMembers = origin.position.getPositionsOnCircleEdge(visibleRange);
        ArrayList<Hexagon> result = new ArrayList<>();
        ArrayList<Hexagon> line;
        for (GridPosition ringMemberPosition : ringMembers) {
            line = getLine(origin.position, ringMemberPosition, map);
            for (Hexagon hexagonInLine : line) {
                result.add(hexagonInLine);
                if (hexagonInLine.isVisualObstacle()) {
                    break;
                }
            }
        }
        return result;
    }

    static ArrayList<Hexagon> getHexagonsOnRingEdge(Hexagon center, int radius, HexagonMap map) {
        ArrayList<Hexagon> result = new ArrayList<>();
        for (GridPosition position : center.position.getPositionsOnCircleEdge(radius)) {
            try {
                Hexagon hexagon = map.getHexagon(position);
                result.add(hexagon);
            } catch (NoHexagonFoundException e) {
            }
        }
        return result;
    }

    static ArrayList<Hexagon> getHexagonsInRingArea(Hexagon center, int radius, HexagonMap map) {
        ArrayList<Hexagon> result = new ArrayList<>();
        for (GridPosition position : center.position.getPositionsInCircleArea(radius)) {
            try {
                Hexagon hexagon = map.getHexagon(position);
                result.add(hexagon);
            } catch (NoHexagonFoundException e) {
            }
        }
        return result;
    }
}
