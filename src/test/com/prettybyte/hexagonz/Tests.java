package test.com.prettybyte.hexagonz;

import com.prettybyte.hexagons.Hexagon;
import com.prettybyte.hexagons.Map;
import org.junit.Assert;
import org.junit.Test;

public class Tests {
    @Test
    public void direction() {
        Map map = new Map(10, 0,0);
        Hexagon center = map.addHexagon(new Hexagon(3, 3));
        Hexagon northWest = map.addHexagon(new Hexagon(3, 2));
        Hexagon northEast = map.addHexagon(new Hexagon(4, 2));
        Hexagon east = map.addHexagon(new Hexagon(4, 3));
        Hexagon southEast = map.addHexagon(new Hexagon(3, 4));
        Hexagon southWest = map.addHexagon(new Hexagon(2,4));
        Hexagon west = map.addHexagon(new Hexagon(2, 3));

        Assert.assertTrue(center.getDirectionTo(northWest).equals(Map.Direction.NORTHWEST));
        Assert.assertTrue(center.getDirectionTo(northEast).equals(Map.Direction.NORTHEAST));
        Assert.assertTrue(center.getDirectionTo(east).equals(Map.Direction.EAST));
        Assert.assertTrue(center.getDirectionTo(southEast).equals(Map.Direction.SOUTHEAST));
        Assert.assertTrue(center.getDirectionTo(southWest).equals(Map.Direction.SOUTHWEST));
        Assert.assertTrue(center.getDirectionTo(west).equals(Map.Direction.WEST));
    }
}
