package com.prettybyte.hexagons;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import java.util.Optional;

/**
 * This class creates a map of Hexagons from an image file. 
 */
class MapGenerator {

    private final HexagonMap map;
    private final Image image;
    private final int mapWidth;
    private Double verticalRelation;
    private Double horizontalRelation;

    /**
     * The image proportions are maintained, therefore only the desired width is specified.
     *
     */
    MapGenerator(HexagonMap map, Image image, int mapWidthInHexes) {
        this.map = map;
        this.image = image;
        this.mapWidth = mapWidthInHexes;
    }

    Optional<Double> getHorizontalRelation() {
        return (horizontalRelation == null) ? Optional.empty() : Optional.of(horizontalRelation);
    }


    Optional<Double> getVerticalRelation() {
        return (verticalRelation == null) ? Optional.empty() : Optional.of(verticalRelation);
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
        double hexagonMapWidthInPixels = map.getGraphicsHorizontalDistanceBetweenHexagons() * mapWidth;
        horizontalRelation = imageWidth / hexagonMapWidthInPixels;
        double estimatedHexagonMapHeightInPixels = imageHeight / horizontalRelation;

        int mapHeight = (int) (estimatedHexagonMapHeightInPixels / map.getGraphicsverticalDistanceBetweenHexagons());
        verticalRelation = imageHeight / ((map.getGraphicsverticalDistanceBetweenHexagons() * mapHeight)+map.getGraphicsHexagonHeight()/2); // Not really sure about the last part but it seems to work. And should I make the corresponding correction on the horizontalRelation ?

        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                int axialQ = x - (y - (y & 1)) / 2;
                int axialR = y;
                Hexagon h = new Hexagon(axialQ, axialR);
                h.setMap(map);
                int xOnImage = (int) ((h.getGraphicsXoffset() - map.graphicsXpadding) * horizontalRelation) ;
                int yOnImage = (int) ((h.getGraphicsYoffset() - map.graphicsYpadding) * verticalRelation);
                Color pixelColor = pr.getColor(xOnImage, yOnImage);
                creator.createHexagon(axialQ, axialR, pixelColor, map);
            }
        }
    }
}
