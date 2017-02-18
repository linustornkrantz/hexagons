package com.prettybyte.hexagons;

import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.util.Collection;
import static java.lang.Math.sqrt;

class GridDrawer {
    
    private final HexagonMap map;
    private javafx.scene.text.Font font = new Font(13);

    /**
     * @param map
     */
    GridDrawer(HexagonMap map) {
        this.map = map;
    }
    
    void draw(Group root) {
        Collection<Hexagon> hexagons = map.getAllHexagons();
        for (Hexagon hexagon : hexagons) {
            hexagon.addEventFilter(MouseEvent.MOUSE_CLICKED,
                    new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent me) {
                            GridPosition pos = ((Hexagon) me.getSource()).position;
                            try {
                                map.onHexClickedCallback.onClicked(map.getHexagon(pos));
                            } catch (NoHexagonFoundException e) {
                            }
                        }
            });
            root.getChildren().add(hexagon);
            
            if (map.renderCoordinates) {
                Text text = new Text(hexagon.position.getCoordinates());
                text.setFont(font);
                double textWidth = text.getBoundsInLocal().getWidth();
                double textHeight = text.getBoundsInLocal().getHeight();
                text.setX(hexagon.getGraphicsXoffset() - textWidth / 2);
                text.setY(hexagon.getGraphicsYoffset() + textHeight / 4);           // Not sure why but 4 seems like a good value
                root.getChildren().add(text);
            }
        }
    }

    /**
     *
     * @param x
     * @param y
     * @param hexagonHeight
     * @return the GridPosition that contains that pixel
     */
    static GridPosition pixelToPosition(int x, int y, int hexagonHeight, int xPadding, int yPadding) {
        x = x - xPadding;
        y = y - yPadding;
        double hexagonRadius = ((double) hexagonHeight) / 2;
        double q = ((1.0 / 3.0 * sqrt(3.0) * x - 1.0 / 3.0 * y) / hexagonRadius);
        double r = (2.0 / 3.0 * (double) y / hexagonRadius);
        return (GridPosition.hexRound(q, r));
    }

    void setFont(Font font) {
        this.font = font;
    }

}
