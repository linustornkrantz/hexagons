package com.prettybyte.hexagons;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

import static java.lang.Math.sqrt;

public class HexagonMap {

    final int hexagonSize;
    int graphicsXpadding = 0;
    int graphicsYpadding = 0;
    private MapGenerator mapGenerator;
    boolean renderCoordinates = false;
    private GridDrawer gridDrawer = new GridDrawer(this);
    private HashMap<GridPosition, Hexagon> hexagons = new HashMap<>();
    IHexagonClickHandler onHexClickedCallback = new IHexagonClickHandler() {
        @Override
        public void onClicked(Hexagon hexagon) {
        }
    };

    public enum Direction {NORTHWEST, NORTHEAST, EAST, SOUTHEAST, SOUTHWEST, WEST}


    /**
     * Creates an empty HexagonMap
     *
     * @param hexagonSize the distance between the center and one corner
     */
    public HexagonMap(int hexagonSize) {
        this.hexagonSize = hexagonSize;
    }

    /**
     * Generates a HexagonMap from an Image
     *
     * @param hexagonSize     the distance between the center and one corner
     * @param image           an Image which will be used to generate a HexagonMap
     * @param mapWidthInHexes the number of hexagons on the x-axis
     */
    public HexagonMap(int hexagonSize, Image image, int mapWidthInHexes) {
        this.hexagonSize = hexagonSize;
        mapGenerator = new MapGenerator(this, image, mapWidthInHexes);
        mapGenerator.generate(new IHexagonCreator() {
            @Override
            public void createHexagon(int q, int r, Color imagePixelColor, HexagonMap map) {
                Hexagon h = new Hexagon(q, r);
                h.setBackgroundColor(imagePixelColor);
                map.addHexagon(h);
            }
        });
    }

    /**
     * Generates a HexagonMap from an Image
     *
     * @param hexagonSize     the distance between the center and one corner
     * @param image           an Image which will be used to generate a HexagonMap
     * @param mapWidthInHexes the number of hexagons on the x-axis
     * @param hexagonCreator  a class implementing IHexagonCreator. This is how you decide HOW the HexagonMap should be
     *                        generated from the Image. In it's most basic form:
     *                        <p>
     *                        public void createHexagon(int q, int r, Color imagePixelColor, HexagonMap map) {
     *                        Hexagon h = new Hexagon(q, r);
     *                        h.setBackgroundColor(imagePixelColor);
     *                        map.addHexagon(h);
     *                        }
     */
    public HexagonMap(int hexagonSize, Image image, int mapWidthInHexes, IHexagonCreator hexagonCreator) {
        this.hexagonSize = hexagonSize;
        mapGenerator = new MapGenerator(this, image, mapWidthInHexes);
        mapGenerator.generate(hexagonCreator);
    }

    /**
     * Tells the renderer that you want some space before the HexagonMap is rendered
     */
    public void setPadding(int left, int top) {
        graphicsXpadding = left;
        graphicsYpadding = top;
        for (Hexagon h : getAllHexagons()) {
            h.getPoints().removeAll(h.getPoints());
            h.init();
        }
    }

    double getGraphicsHexagonWidth() {
        return sqrt(3) / 2 * hexagonSize * 2;
    }

    int getGraphicsHexagonHeight() {
        return hexagonSize * 2;
    }

    double getGraphicsHorizontalDistanceBetweenHexagons() {
        return getGraphicsHexagonWidth();
    }

    double getGraphicsverticalDistanceBetweenHexagons() {
        return (3.0 / 4.0 * hexagonSize * 2.0);
    }

    /**
     * Add a Hexagon to the HexagonMap
     *
     * @return the same hexagon
     */
    public Hexagon addHexagon(Hexagon hexagon) {
        hexagon.setMap(this);
        hexagons.put(hexagon.position, hexagon);
        return hexagon;
    }

    /**
     * Removes a Hexagon from the HexagonMap
     *
     * @return the same hexagon
     */
    public void removeHexagon(Hexagon hexagon) {
        hexagon.setMap(null);
        hexagons.remove(hexagon);
    }

    /**
     * @return the hexagon that is rendered on a specific position on the screen
     * @throws NoHexagonFoundException if there is no Hexagon at the specified position
     */
    public Hexagon getHexagonContainingPixel(int x, int y) throws NoHexagonFoundException {
        return getHexagon(GridDrawer.pixelToPosition(x, y, getGraphicsHexagonHeight(), graphicsXpadding, graphicsYpadding));
    }

    /**
     * Retrieves the Hexagon at the specified position (axial coordinates)
     *
     * @param q the Q coordinate
     * @param r the R coordinate
     * @return the Hexagon
     * @throws NoHexagonFoundException if there is no Hexagon at the specified position
     */
    public Hexagon getHexagon(int q, int r) throws NoHexagonFoundException {
        GridPosition position = new GridPosition(q, r);
        Hexagon result = hexagons.get(position);
        if (result == null) {
            throw new NoHexagonFoundException("There is no Hexagon on q:" + q + " r:" + r);
        }
        return result;
    }

    Hexagon getHexagon(GridPosition position) throws NoHexagonFoundException {
        return getHexagon(position.q, position.r);
    }

    Hexagon getHexagonByCube(int x, int y, int z) throws NoHexagonFoundException {
        return getHexagon(x, z);
    }

    /**
     * @return all Hexagons that has been added to the map
     */
    public Collection<Hexagon> getAllHexagons() {
        return hexagons.values();
    }


    static class DefaultPathInfoSupplier implements IPathInfoSupplier {
        @Override
        public boolean isBlockingPath(Hexagon hexagon) {
            return hexagon.isBlockingPath();
        }

        @Override
        public int getMovementCost(Hexagon from, Hexagon to) {
            return 1;
        }
    }

    /**
     * If the map was created from an Image, this will return the horizontal pixel relation between the image and
     * the generated map
     */
    public Optional<Double> getImageMapHorizontalRelation() {
        return mapGenerator == null ? Optional.empty() : mapGenerator.getHorizontalRelation();
    }

    /**
     * If the map was created from an Image, this will return the vertical pixel relation between the image and
     * the generated map
     */
    public Optional<Double> getImageMapVerticalRelation() {
        return mapGenerator == null ? Optional.empty() : mapGenerator.getVerticalRelation();
    }

    /**
     * If you want the coordinates rendered on the screen
     */
    public void setRenderCoordinates(boolean b) {
        renderCoordinates = b;
    }

    /**
     * Sets the font used to draw the hexagon positions
     */
    public void setRenderFont(Font font) {
        gridDrawer.setFont(font);
    }

    /**
     * Renders the HexagonMap
     *
     * @param group the JaxaFX Group where all the hexagons should be rendered
     */
    public void render(Group group) {
        gridDrawer.draw(group);
    }

    /**
     * A callback when the user clicks on a Hexagon
     */
    public void setHexagonClickHandler(IHexagonClickHandler handler) {
        onHexClickedCallback = handler;
    }

}
