package test.com.prettybyte.hexagonz;

import com.prettybyte.hexagonz.GridPosition;
import com.prettybyte.hexagonz.Map;
import com.prettybyte.hexagonz.NoHexagonException;
import org.junit.Assert;
import org.junit.Test;

public class Tests {
    @Test
    public void direction() {
        GridPosition center = new GridPosition(3,3);
        GridPosition northWest = new GridPosition(3, 2);
        GridPosition northEast = new GridPosition(4, 2);
        GridPosition east = new GridPosition(4, 3);
        GridPosition southEast = new GridPosition(3, 4);
        GridPosition southWest = new GridPosition(2,4);
        GridPosition west = new GridPosition(2, 3);

        Assert.assertTrue(center.getDirectionTo(northWest).equals(Map.Direction.NORTHWEST));
        Assert.assertTrue(center.getDirectionTo(northEast).equals(Map.Direction.NORTHEAST));
        Assert.assertTrue(center.getDirectionTo(east).equals(Map.Direction.EAST));
        Assert.assertTrue(center.getDirectionTo(southEast).equals(Map.Direction.SOUTHEAST));
        Assert.assertTrue(center.getDirectionTo(southWest).equals(Map.Direction.SOUTHWEST));
        Assert.assertTrue(center.getDirectionTo(west).equals(Map.Direction.WEST));
    }
}
