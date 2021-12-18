package com.tistory.workshop6349.tutorial;

import bwapi.*;

public class MapTools {

    static class Grid<T> {
        int m_width;
        int m_height;

        T[][] m_grid;

        public Grid(int width, int height, T val) {
            this.m_width = width;
            this.m_height = height;
            this.m_grid = (T[][]) new Object[width][height];
        }

        public T get(int x, int y) {
            return m_grid[x][y];
        }

        public void set(int x, int y, T val) {
            m_grid[x][y] = val;
        }

        public int width() {
            return m_width;
        }

        public int height() {
            return m_height;
        }

    }

    public static int m_width = 0;
    public static int m_height = 0;
    public static int m_frame = 0;
    public static boolean m_drawMap = false;

    public static Grid<Boolean> m_walkable;
    public static Grid<Boolean> m_buildable;
    public static Grid<Boolean> m_depotBuildable;
    public static Grid<Integer> m_lastSeen;

    public MapTools() {}

    public void onStart() {
        m_width = Main.game.mapWidth();
        m_height = Main.game.mapHeight();
        m_walkable = new Grid<>(m_width, m_height, true);
        m_buildable = new Grid<>(m_width, m_height, false);
        m_depotBuildable = new Grid<>(m_width, m_height, false);
        m_lastSeen = new Grid<>(m_width, m_height, 0);

        for (int x = 0; x < m_width; ++x) {
            for (int y = 0; y < m_height; ++y) {
                m_buildable.set(x, y, canBuild(x, y));
                m_depotBuildable.set(x, y, canBuild(x, y));
                m_walkable.set(x, y, m_buildable.get(x, y) || canWalk(x, y));
            }
        }

        for (Unit resource : Main.game.getStaticNeutralUnits()) {
            if (!resource.getType().isResourceContainer()) {
                continue;
            }

            int tileX = resource.getTilePosition().x;
            int tileY = resource.getTilePosition().y;

            for (int x = tileX; x < tileX + resource.getType().tileWidth(); ++x) {
                for (int y = tileY; y < tileY + resource.getType().tileHeight(); ++y) {
                    m_buildable.set(x, y, false);

                    for (int rx = -3; rx <= 3; rx++) {
                        for (int ry = -3; ry <= 3; ry++) {
                            if (!new TilePosition(x + rx, y + ry).isValid(Main.game)) {
                                continue;
                            }

                            m_depotBuildable.set(x + rx, y + ry, false);
                        }
                    }
                }
            }

        }

    }

    public void onFrame() {
        for (int x = 0; x < m_width; ++x) {
            for (int y = 0; y < m_height; ++y) {
                if (isVisible(x, y)) {
                    m_lastSeen.set(x, y, Main.game.getFrameCount());
                }
            }
        }

        if (m_drawMap) {
            draw();
        }
    }

    public void toggleDraw() {
        m_drawMap = !m_drawMap;
    }

    public boolean isExplored(TilePosition pos) {
        return isExplored(pos.x, pos.y);
    }

    public boolean isExplored(Position pos) {
        return isExplored(pos.toTilePosition());
    }

    public boolean isExplored(int tileX, int tileY) {
        if (!isValidTile(tileX, tileY)) {
            return false;
        }
        return Main.game.isExplored(tileX, tileY);
    }

    public boolean isVisible(int tileX, int tileY) {
        if (!isValidTile(tileX, tileY)) {
            return false;
        }
        return Main.game.isVisible(tileX, tileY);
    }

    public boolean isPowered(int tileX, int tileY) {
        return Main.game.hasPower(new TilePosition(tileX, tileY));
    }

    public boolean isValidTile(int tileX, int tileY) {
        return tileX >= 0 && tileY >= 0 && tileX < m_width && tileY < m_height;
    }

    public boolean isValidTile(TilePosition tile) {
        return isValidTile(tile.x, tile.y);
    }

    public boolean isValidPosition(Position pos) {
        return isValidTile(pos.toTilePosition());
    }

    public boolean isBuildable(int tileX, int tileY) {
        if (!isValidTile(tileX, tileY)) {
            return false;
        }

        return m_buildable.get(tileX, tileY);
    }

    public boolean isBuildable(TilePosition tile) {
        return isBuildable(tile.x, tile.y);
    }

    public void printMap() {
        for (int y = 0; y < m_height; ++y) {
            for (int x = 0; x < m_width; ++x) {
                System.out.printf("%b", isWalkable(x, y));
            }
            System.out.println();
        }
    }

    public boolean isDepotBuildableTile(int tileX, int tileY) {
        if (!isValidTile(tileX, tileY)) {
            return false;
        }

        return m_depotBuildable.get(tileX, tileY);
    }

    public boolean isWalkable(int tileX, int tileY) {
        if (!isValidTile(tileX, tileY)) {
            return false;
        }

        return m_walkable.get(tileX, tileY);
    }

    public boolean isWalkable(TilePosition tile) {
        return isWalkable(tile.x, tile.y);
    }

    public int width() {
        return m_width;
    }

    public int height() {
        return m_height;
    }

    public void drawTile(int tileX, int tileY, final Color color) {
        final int padding = 2;
        final int px = tileX * 32 + padding;
        final int py = tileY * 32 + padding;
        final int d = 32 - 2 * padding;

        Main.game.drawLineMap(px, py, px + d, py, color);
        Main.game.drawLineMap(px + d, py, px + d, py + d, color);
        Main.game.drawLineMap(px + d, py + d, px, py + d, color);
        Main.game.drawLineMap(px, py + d, px, py, color);
    }

    public boolean canWalk(int tileX, int tileY) {
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 4; ++j) {
                if (!Main.game.isWalkable(tileX * 4 + i, tileY * 4 + j)) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean canBuild(int tileX, int tileY) {
        return Main.game.isBuildable(new TilePosition(tileX, tileY));
    }

    public void draw() {
        final TilePosition screen = Main.game.getScreenPosition().toTilePosition();
        final int sx = screen.x;
        final int sy = screen.y;
        final int ex = sx + 20;
        final int ey = sy + 15;

        for (int x = sx; x < ex; ++x) {
            for (int y = sy; y < ey; y++) {
                final TilePosition tilePos = new TilePosition(x, y);
                if (!tilePos.isValid(Main.game)) {
                    continue;
                }
                
                if (true) {
                    Color color = isWalkable(x, y) ? new Color(0, 255, 0) : new Color(255, 0, 0);
                    if (isWalkable(x, y) && !isBuildable(x, y)) {
                        color = new Color(255, 255, 0);
                    }
                    if (isBuildable(x, y) && !isDepotBuildableTile(x, y)) {
                        color = new Color(127, 255, 255);
                    }
                    drawTile(x, y, color);
                }
            }
        }

        final String red = Integer.toHexString(8);
        final String green = Integer.toHexString(7);
        final String white =  Integer.toHexString(4);
        final String yellow = Integer.toHexString(3);

        Main.game.drawBoxScreen(0, 0, 200, 100, Color.Black, true);
        Main.game.setTextSize(Text.Size.Huge);
        Main.game.drawTextScreen(10, 5, white + "Map Legend");
        Main.game.setTextSize(Text.Size.Default);
        Main.game.drawTextScreen(10, 30, red + "Red");
        Main.game.drawTextScreen(60, 30, white + "Can't walk or build");
        Main.game.drawTextScreen(10, 45, green + "Green");
        Main.game.drawTextScreen(60, 45, white + "Can't walk or build");
        Main.game.drawTextScreen(10, 60, yellow + "YELLOW");
        Main.game.drawTextScreen(60, 60, white + "Resource Tile, Can't Build");
        Main.game.drawTextScreen(10, 75, "Teal:");
        Main.game.drawTextScreen(60, 75, white + "Can't Build Depot");
    }

}
