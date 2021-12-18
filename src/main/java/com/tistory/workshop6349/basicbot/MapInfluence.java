package com.tistory.workshop6349.basicbot;

import bwapi.Position;
import bwapi.TilePosition;

public class MapInfluence {

    public static void setInfluenceA(int[][] map, TilePosition tile, int dz) {
        int[] arr = {3,6,7,7,7,8,8,8,8,8,8,7,7,7,6,3};
        int yMin = tile.y - 7;

        for (int i = 0; i < arr.length; i++) {
            if (yMin + i >= 0 && yMin + i < 256) {
                setInfluenceLineEven(map, yMin + i, tile.x, arr[i], dz);
            }
        }

    }

    public static void setInfluenceM(int[][] map, TilePosition tile, int dz) {
        int[] arr = { 3,5,6,7,8,8,9,9,9,9,9,9,8,8,7,6,5,3 };
        int yMin = tile.y - 8;

        for (int i = 0; i < arr.length; i++) {
            if (yMin + i >= 0 && yMin + i < 256) {
                setInfluenceLineEven(map, yMin + i, tile.x, arr[i], dz);
            }
        }
    }

    public static void setInfluenceL(int[][] map, TilePosition tile, int dz) {
        int[] arr = { 4,6,7,8,9,9,10,10,10,10,10,10,10,9,9,8,7,6,4 };
        int yMin = tile.y - 10;

        for (int i = 0; i < arr.length; i++) {
            if (yMin + i >= 0 && yMin + i < 256) {
                setInfluenceLineEven(map, yMin + i, tile.x, arr[i], dz);
            }
        }
    }

    public static void setInfluenceT(boolean[][] map, TilePosition tile) {
        int[] arr = { 5,7,9,10,10,11,11,12,12,12,12,12,12,12,12,12,12,12,11,11,10,10,9,7,5 };
        int yMin = tile.y - 12;

        for (int i = 0; i < arr.length; i++) {
            if (yMin + i >= 0 && yMin + i < 256) {
                setInfluenceLineOdd(map, yMin + i, tile.x, arr[i], true);
            }
        }
    }

    public static void setInfluenceLineEven(int[][] map, int y, int x0, int dx, int dz) {
        for (int x = x0 - dx + 1; x < x0 + dx + 1; x++) {
            if (x >= 0 && x < 256) {
                map[y][x] = map[y][x] + dz;
            }
        }
    }

    public static void setInfluenceLineOdd(boolean[][] map, int y, int x0, int dx, boolean b) {
        for (int x = x0 - dx; x < x0 + dx + 1; x++) {
            if (x >= 0 && x < 256) {
                map[y][x] = b;
            }
        }
    }

    public static void setInfluence(int[][] map, int x0, int y0, int size, int dz) {
        if (x0 < 0
                || x0 >= BasicMap.wp
                || y0 < 0
                || x0 >= BasicMap.hp) {
            return;
        }

        switch (size) {
            case 16:
                // Storm
                setInfluence(map, x0, y0, size, BasicMap.circ_area_r1, dz);
                break;
            case 56:
                // lurker
                setInfluence(map, x0, y0, size, BasicMap.circ_area_r6, dz);
                break;
            case 72:
                // nuke
                setInfluence(map, x0, y0, size, BasicMap.circ_area_r8, dz);
                break;
            case 104:
                // siege
                setInfluence(map, x0, y0, size, BasicMap.circ_area_r12, dz);
                break;
            case 60:
                // bunker
                setInfluence(map, x0, y0, size, BasicMap.circ_area_def6, dz);
                break;
            case 68:
                // other static defense
                setInfluence(map, x0, y0, size, BasicMap.circ_area_def7, dz);
                break;
            case 96:
                // detector sight range
                setInfluence(map, x0, y0, size, BasicMap.circ_area_r11, dz);
                break;
            default:
                break;
        }
    }

    public static void setInfluence(int[][] map, int x0, int y0, int size, int[] arr, int dz) {
        int xMid = x0 / 8;
        int yMid = y0 / 8;
        int yMin = yMid - size / 2;
        int yMax = yMid + size / 2;
        int i = 0;

        if (yMin < 0) {
            i = -yMin;
            yMin = 0;
        }
        if (yMax > 1024) {
            yMax = 1024;
        }

        for (int y = yMin; y < yMax; ++y) {
            setInfluenceLineEven2(map, y, xMid, arr[i], dz);
            i++;
        }

    }

    public static void setInfluence(int[][] map, Position p, int size, int[] arr, int dz) {
        setInfluence(map, p.x, p.y, size, arr, dz);
    }

    public static void setInfluenceLineEven2(int[][] map, int y, int x0, int dx, int dz) {
        int xMin = x0 - dx;
        int xMax = x0 + dx;
        if (xMin < 0) {
            xMin = 0;
        }
        if (xMax > 1024) {
            xMax = 1024;
        }

        for (int x = xMin; x < xMax; x++) {
            map[y][x] += dz;
        }
    }

    public static void setInfluenceLineOdd2(int[][] map, int y, int x0, int dx, int dz) {
        int xMin = x0 - dx;
        int xMax = x0 + dx + 1;
        if (xMin < 0) {
            xMin = 0;
        }
        if (xMax > 1024) {
            xMax = 1024;
        }

        for (int x = xMin; x < xMax; x++) {
            map[y][x] += dz;
        }
    }

}
