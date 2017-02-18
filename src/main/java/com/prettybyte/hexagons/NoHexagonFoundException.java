package com.prettybyte.hexagons;

/**
 * This exception is thrown when trying to retrieve a Hexagon from a position where ther is no Hexagon
 */
public class NoHexagonFoundException extends Exception {

    public NoHexagonFoundException(String message) {
        super(message);
    }
}
