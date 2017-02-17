Hexagons
========

This is a hexagon library for JavaFX. 

Features:
- Render the hexagons on the screen
- Pathfinding
- Find which hexagons are visible from a specific hexagon
- Calculations (e.g. distance between hexes, line drawing)
- Transform an image file (e.g. png) into hexagons


Installation
============
git clone git@gitlab.com:linustornkrantz/hexagons.git

Note that it depends on JavaFX. If you are not using the Oracle JRE, you may have to handle that dependency on your own (e.g. sudo apt-get install openjfx).


Description
===========
On http://www.redblobgames.com/grids/hexagons you can find a lot of information about the mathematical properties of hexagons. In fact, I had much help from the information there when writing this library.

When creating your hexagons, make sure that you also add them to a map:

Map map = new Map(10, 0,0);
map.addHexagon(new Hexagon(3, 3));
map.addHexagon(new Hexagon(3, 4));

To render the hexagons in a JavaFX Group:

Group hexagonGroup = new Group();
new GridDrawer(hexagonGroup, map, false).draw();

