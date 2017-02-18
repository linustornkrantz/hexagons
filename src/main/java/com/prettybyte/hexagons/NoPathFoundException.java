package com.prettybyte.hexagons;

/**
 * This exception is thrown when the pathfinding algorithm cannot find any path to the goal
 */
public class NoPathFoundException extends Exception {

    public NoPathFoundException(String message) {
        super(message);
    }
}
