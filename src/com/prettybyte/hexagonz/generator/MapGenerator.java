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

package com.prettybyte.hexagonz.generator;

import com.prettybyte.hexagonz.GridPosition;
import com.prettybyte.hexagonz.Hexagon;
import com.prettybyte.hexagonz.Map;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

/**
 * This class creates a map of Hexagons from an image file. 
 */
public class MapGenerator {

    private final Map map;
    private final Image image;
    private final int mapWidth;
    private int mapHeight;
    private final int hexagonSize;
    private double verticalRelation;
    private double horizontalRelation;

    /**
     * The image proportions are maintained, therefore only the width is specified.
     *
     * @param map
     * @param image
     * @param mapWidth
     * @param hexagonSize
     */
    public MapGenerator(Map map, Image image, int mapWidth, int hexagonSize) {
        this.map = map;
        this.image = image;
        this.mapWidth = mapWidth;
        this.hexagonSize = hexagonSize;
    }

    /**
     *
     * @return the horizontal pixel relation between the image and the generated
     * map
     */
    public double getHorizontalRelation() {
        return horizontalRelation;
    }

    /**
     *
     * @return the horizontal pixel relation between the image and the generated
     * map
     */
    public double getVerticalRelation() {
        return verticalRelation;
    }

    /**
     * You will have to supply an object that will create the Hexagons as you like. E.g.
     * 
     * class HexagonCreator implements IHexagonCreator {
     *   @Override
     *   public void createHexagon(GridPosition position, javafx.scene.paint.Color color) {
     *       Hexagon h = new Hexagon(position, 20, 0, 0);
     *       h.setBackgroundColor(color);
     *       map.addHexagon(h);
     *   }
     * }
     * 
     * @param creator the object that will actually create the Hexagon.
     */
    public void generate(IHexagonCreator creator) {
        PixelReader pr = image.getPixelReader();
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();
        double hexagonMapWidthInPixels = Hexagon.getGraphicsHorizontalDistanceBetweenHexagons(hexagonSize) * mapWidth;
        horizontalRelation = imageWidth / hexagonMapWidthInPixels;
        double estimatedHexagonMapHeightInPixels = imageHeight / horizontalRelation;

        mapHeight = (int) (estimatedHexagonMapHeightInPixels / Hexagon.getGraphicsverticalDistanceBetweenHexagons(hexagonSize));
        verticalRelation = imageHeight / ((Hexagon.getGraphicsverticalDistanceBetweenHexagons(hexagonSize) * mapHeight)+Hexagon.getGraphicsHexagonHeight(hexagonSize)/2); // Not really sure about the last part but it seems to work. And should I make the corresponding correction on the horizontalRelation ?

        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                int axialQ = x - (y - (y & 1)) / 2;
                int axialR = y;
                GridPosition position = new GridPosition(axialQ, axialR);
                Hexagon h = new Hexagon(position, hexagonSize, 0, 0);
                Color pixelColor = pr.getColor((int) (h.getGraphicsXoffset() * horizontalRelation), (int) (h.getGraphicsYoffset() * horizontalRelation));
                creator.createHexagon(position, pixelColor);
            }
        }
    }
}
