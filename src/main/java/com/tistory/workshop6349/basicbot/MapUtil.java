package com.tistory.workshop6349.basicbot;

import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.WalkPosition;

import java.util.ArrayList;
import java.util.Arrays;

public class MapUtil {

    public static void setMapArea(boolean[][] map, int x0, int y0, int dx, int dy, boolean v) {
        for (int y = y0; y < y0 + dy; ++y) {
            for (int x = x0; x < x0 + dx; ++x) {
                map[y][x] = v;
            }
        }
    }

    public static void setMapArea(boolean[][] map, Unit u, boolean v) {
        int x = u.getTilePosition().x;
        int y = u.getTilePosition().y;
        int w = u.getType().tileWidth();
        int h = u.getType().tileHeight();

        setMapArea(map, x, y, w, h, v);
    }

    public static void clearBuildArea(boolean[][] mapVar, boolean[][] mapFix, int x0, int y0, int dx, int dy) {
        for (int y = y0; y < y0 + dy; ++y) {
            for (int x = x0; x < x0 + dx; ++x) {
                if (mapFix[y][x]) {
                    mapVar[y][x] = true;
                }
            }
        }
    }

    public static void clearBuildArea(boolean[][] mapVar, boolean[][] mapFix, Unit u) {
        int x = u.getTilePosition().x;
        int y = u.getTilePosition().y;
        int w = u.getType().tileWidth();
        int h = u.getType().tileHeight();

        clearBuildArea(mapVar, mapFix, x, y, w, h);
    }

    public static boolean checkMapArea(boolean[][] map, int x0, int y0, int w, int h) {
        if (x0 < 0 || x0 + w > BasicMap.wt) {
            return false;
        }
        if (y0 < 0 || y0 + h >  BasicMap.ht) {
            return false;
        }

        for (int y = y0; y < y0 + h; ++y) {
            for (int x = x0; x < x0 + w; ++x) {
                if (!map[y][x]) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean checkMapArea(int[][] map, int x0, int y0, int w, int h, int f) {
        x0 = f * x0;
        w = f * w;
        y0 = f * y0;
        h = f * h;

        for (int y = y0; y < y0 + h; ++y) {
            for (int x = x0; x < x0 + w; ++w) {
                if (map[y][x] != 0) {
                    return false;
                }
            }
        }

        return true;
    }
    
    public static boolean checkBuildable(int x0, int y0, int dx, int dy) {
        if (x0 < 0 || x0 + dx > BasicMap.wt) {
            return false;
        }
        if (y0 < 0 || y0 + dy > BasicMap.ht) {
            return false;
        }

        for (int y = y0; y < y0 + dy; ++y) {
            for (int x = x0; x < x0 + dx; ++x) {
                if (!BasicBotAI.BroodWar.isBuildable(x, y, false)) {
                    return false;
                }
            }
        }

        return true;
    }
    
    public static boolean tileFullyWalkable(int x, int y) {
        x *= 4;
        y *= 4;

        if (!BasicBotAI.BroodWar.isWalkable(x, y)) return false;
        if (!BasicBotAI.BroodWar.isWalkable(x + 3, y)) return false;
        if (!BasicBotAI.BroodWar.isWalkable(x, y + 3)) return false;
        return BasicBotAI.BroodWar.isWalkable(x + 3, y + 3);
    }

    public static void clearMapBoolean(boolean[][] map) {
        for (boolean[] booleans : map) {
            Arrays.fill(booleans, false);
        }
    }

    public static void fillMapInt(int[][] map, int z) {
        for (int[] ints : map) {
            Arrays.fill(ints, z);
        }
    }

    public static TilePosition getCenterTile(Position p) {
        return new TilePosition(p.x / 32 - 1, p.y / 32);
    }

    public static TilePosition getCenterOfMess(ArrayList<TilePosition> v) {
        int x = 0;
        int y = 0;

        for (TilePosition t : v) {
            x += t.x;
            y += t.y;
        }
        int n = v.size();
        return new TilePosition(x / n, y / n);
    }

    public static TilePosition getVaryTile(TilePosition t, int i) {
        int[] dx = { 0, 1, 0, +1, -1, 2, 0, +2, -2, +1, -1, +2, -2, 3, 0, +3, -3, +1, -1 };
        int[] dy = { 0, 0, 1, +1, +1, 0, 2, +1, +1, +2, +2, +2, +2, 0, 3, +1, +1, +3, +3 };
        if (i >= 38) {
            return t;
        }
        if (i % 2 == 0) {
            return new TilePosition(t.x - dx[i / 2], t.y - dy[i / 2]);
        }
        if (i % 2 == 1) {
            return new TilePosition(t.x + dx[i / 2], t.y + dy[i / 2]);
        }

        return t;
    }

    public static boolean isTopl(int x, int y) {
        return (x < 16 && y < 16);
    }

    public static boolean isTopr(int x, int y) {
        return (x > BasicMap.wt - 18 && y < 16);
    }

    public static boolean isBotl(int x, int y) {
        return (x < 16 && y > BasicMap.ht - 18);
    }

    public static boolean isBotr(int x, int y) {
        return (x > BasicMap.wt - 18 && y > BasicMap.ht - 18);
    }

    public static boolean isTop(int x, int y) {
        return (y < 16);
    }

    public static boolean isBot(int x, int y) {
        return (y > BasicMap.ht - 18);
    }

    public static boolean isLeft(int x, int y) {
        return (x < 16);
    }

    public static boolean isRight(int x, int y) {
        return (x > BasicMap.wt - 18);
    }

    public static int getDirection4(Position p0, Position p1) {
        int dx = p1.x - p0.x;
        int dy = p1.y - p0.y;

        if (Math.abs(dx) >= Math.abs(dy)) {
            return (dx > 0) ? 0 : 2;
        }
        else {
            return (dy > 0) ? 1 : 3;
        }
    }

    public static int getDirection8(int x0, int y0, int x1, int y1) {
        int dx = x1 - x0;
        int dy = y1 - y0;

        if (Math.abs(dx) > 2 * Math.abs(dy)) {
            return (dx > 0) ? 0 : 4;
        }
        else if (Math.abs(dy) > 2 * Math.abs(dx)) {
            return (dy > 0) ? 2 : 6;
        }
        else if (dx > 0) {
            return (dy > 0) ? 1 : 7;
        }
        else {
            return (dy > 0) ? 3 : 5;
        }
    }

    public static int getDirection8(Position p0, Position p1) {
        return getDirection8(p0.x, p0.y, p1.x, p1.y);
    }

    public static double getAngle(Position p0, Position p1) {
        int xr = p1.x - p0.x;
        int yr = p1.y - p0.y;
        double r = Math.pow(Math.pow(xr, 2) + Math.pow(yr, 2), 0.5);

        return (yr < 0) ? -Math.acos(xr / r) : +Math.acos(xr / r);
    }

    public static Position getCirclePos(ArrayList<Position> v, int i) {
        return v.get(i % v.size());
    }

    public static boolean hasGroundConnection(Position p0, Position p1) {
        int d0 = getGroundDist(p0);
        int d1 = getGroundDist(p1);

        if (d1 < 0) {
            d1 = getGroundDist(new Position(p1.x - 2, p1.y)); // fix for: mineral_field with right tile in wall
        }

        if (d0 >= 0 && d1 < 0) {
            return false;
        }
        if (d0 < 0 && d1 >= 0) {
            return false;
        }
        return d0 >= 0 || d1 >= 0 || BotUtil.sqDist(p0, p1) <= 65536;
    }

    public static int getGroundDist(int x, int y) {
        x = keepMapSafe(x, BasicMap.wt);
        y = keepMapSafe(y, BasicMap.ht);

        return BasicMap.mainDistArray[BasicMap.mm][y][x];
    }

    public static int getGroundDist(TilePosition tile) {
        return getGroundDist(tile.x, tile.y);
    }
    
    public static int getGroundDist(Position pos) {
        return getGroundDist(pos.x / 32, pos.y / 32);
    }

    public static int getGroundDist(Unit unit) {
        return getGroundDist(unit.getTilePosition());
    }
    
    public static int mapSafeSub(int x, int dx) {
        return Math.max(x - dx, 0);
    }

    public static int mapSafeAdd(int x, int dx, int max) {
        return Math.min(x + dx, max);
    }

    public static int keepMapSafe(int x, int max) {
        if (x < 0) {
            return 0;
        }

        return Math.min(x, max);
    }

    public static ArrayList<WalkPosition> createLinearInterpolation(WalkPosition w1, WalkPosition w2) {
        ArrayList<WalkPosition> walkPositions = new ArrayList<>();
        float dx = (float) (w2.x - w1.x);
        float dy = (float) (w2.y - w1.y);
        float max = Math.max(Math.abs(dy), Math.abs(dx));
        dx /= max;
        dy /= max;

        for (float i = 1F; i < max; i++) {
            walkPositions.add(new WalkPosition(w1.x + (int) (i * dx), w1.y + (int) (i * dy)));
        }
        return walkPositions;
    }

    public static boolean buildSpotVisible(TilePosition t) {
        if (BasicBotAI.BroodWar.isVisible(t.x, t.y)) {
            return true;
        }
        if (BasicBotAI.BroodWar.isVisible(t.x + 3, t.y)) {
            return true;
        }
        if (BasicBotAI.BroodWar.isVisible(t.x, t.y + 2)) {
            return true;
        }
        return BasicBotAI.BroodWar.isVisible(t.x + 3, t.y + 2);
    }

    public static boolean buildSpotVisible(Position p) {
        return buildSpotVisible(new TilePosition(p.x / 32 - 2, p.y / 32 - 1));
    }

    public static ArrayList<Position> createCircleList(Position p, int r) {
        ArrayList<Position> positions = new ArrayList<>();

        double rad = 24.0 / r;
        int max = (int) (6.283 * r) / 24;
        for (int i = 0; i < max; i++) {
            int x = (int) (p.x + r * Math.cos(i * rad));
            int y = (int) (p.y + r * Math.sin(i * rad));
            positions.add(new Position(x, y));
        }
        BasicBotAI.BroodWar.printf("army Circle Vector: " + max);
        return positions;
    }

    public static void createCircle24() {
        double rad = 2.0 * Math.PI / 24.0;
        for (int i = 0; i < 24; i++) {
            BasicMap.cdx24[i] = (int) (64 * Math.cos(i * rad));
            BasicMap.cdy24[i] = (int) (64 * Math.sin(i * rad));
        }
    }

    public static void createCircleR4() {
        for (int i = 0; i < 24; i++) {
            BasicMap.cr4dx[i] = (BasicMap.cdx24[i] * 5) / 16;
            BasicMap.cr4dy[i] = (BasicMap.cdy24[i] * 5) / 16;
        }
    }

    public static Position getCirclePos24(int n, int r, int i) {
        r = 2 * r + 4;

        for (int j = r; j > 6; j--) {
            int x = BasicMap.mainPos[n].x + ((j * BasicMap.cdx24[i]) / 4);
            int y = BasicMap.mainPos[n].y + ((j * BasicMap.cdx24[i]) / 4);
            int xt = x / 32;
            int yt = y / 32;

            if (xt > 0
                    && xt < BasicMap.wt
                    && yt > 0
                    && yt < BasicMap.ht
                    && BasicMap.buildMap_Var[yt][xt]
                    && BasicMap.mainDistArray[n][yt][xt] < r) {
                return new Position(x, y);
            }
        }

        return Position.None;
    }

}
