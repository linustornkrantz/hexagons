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
package com.prettybyte.hexagons;

import static java.lang.Math.sqrt;
import java.util.Collection;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class GridDrawer {
    
    private final Group root;
    private final Map map;
    private javafx.scene.text.Font font = new Font(13);

    /**
     *
     * @param root
     * @param map
     */
    public GridDrawer(Group root, Map map) {
        this.root = root;
        this.map = map;
    }
    
    public void drawGroup(boolean drawCoordinates) {
        Collection<Hexagon> hexagons = map.getAllHexagons();
        for (Hexagon hexagon : hexagons) {
            hexagon.addEventFilter(MouseEvent.MOUSE_CLICKED,
                    new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent me) {
                            GridPosition pos = ((Hexagon) me.getSource()).position;
                            hexClicked(pos);
                        }
            });
            root.getChildren().add(hexagon);
            
            if (drawCoordinates) {
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
     * @param hexagonSize
     * @return the GridPosition that contains that pixel
     */
    static GridPosition pixelToPosition(int x, int y, int hexagonSize) {
        double q = ((1.0 / 3.0 * sqrt(3.0) * x - 1.0 / 3.0 * y) / hexagonSize);
        double r = (2.0 / 3.0 * (double) y / (double) hexagonSize);
        return (GridPosition.hexRound(q, r));
    }

    /**
     * Sets the font used to draw the hexagon positions
     *
     * @param font
     */
    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * This function can be overridden. This is where you act when the user
     * clicks somewhere on the grid.
     *
     * @param position the grid position that was clicked
     */
    public void hexClicked(GridPosition position) {
    }
    
}
