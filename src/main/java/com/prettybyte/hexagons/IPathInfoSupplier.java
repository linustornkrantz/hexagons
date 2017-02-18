package com.prettybyte.hexagons;

public interface IPathInfoSupplier {
    boolean isBlockingPath(Hexagon hexagon);
    int getMovementCost(Hexagon from, Hexagon to);
}
