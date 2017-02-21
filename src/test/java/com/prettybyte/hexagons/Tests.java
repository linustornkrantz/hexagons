package com.prettybyte.hexagons;

import org.junit.Assert;
import org.junit.Test;

public class Tests {
    @Test
    public void remove() {
        HexagonMap map = new HexagonMap(20);
        Hexagon h = new Hexagon(6,7);
        map.addHexagon(h);
        Assert.assertTrue(map.getAllHexagons().size()==1);
        map.removeHexagon(h);
        Assert.assertTrue(map.getAllHexagons().size()==0);
    }

    @Test
    public void direction() {
        HexagonMap map = new HexagonMap(10);
        Hexagon center = map.addHexagon(new Hexagon(3, 3));
        Hexagon northWest = map.addHexagon(new Hexagon(3, 2));
        Hexagon northEast = map.addHexagon(new Hexagon(4, 2));
        Hexagon east = map.addHexagon(new Hexagon(4, 3));
        Hexagon southEast = map.addHexagon(new Hexagon(3, 4));
        Hexagon southWest = map.addHexagon(new Hexagon(2, 4));
        Hexagon west = map.addHexagon(new Hexagon(2, 3));

        Assert.assertTrue(center.getDirectionTo(northWest).equals(HexagonMap.Direction.NORTHWEST));
        Assert.assertTrue(center.getDirectionTo(northEast).equals(HexagonMap.Direction.NORTHEAST));
        Assert.assertTrue(center.getDirectionTo(east).equals(HexagonMap.Direction.EAST));
        Assert.assertTrue(center.getDirectionTo(southEast).equals(HexagonMap.Direction.SOUTHEAST));
        Assert.assertTrue(center.getDirectionTo(southWest).equals(HexagonMap.Direction.SOUTHWEST));
        Assert.assertTrue(center.getDirectionTo(west).equals(HexagonMap.Direction.WEST));

    }
}
