package com.tistory.workshop6349.basicbot;

import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;

import java.util.ArrayList;
import java.util.Arrays;

public class BaseFinder {

    public static int mapX, mapY, scanW;
    // true if tile blocked by terrain
    public static ArrayList<Boolean> walkGrid = new ArrayList<>();
    // true if blocked (for depot top-left) by mineral proximity
    public static ArrayList<Boolean> resBlock = new ArrayList<>();
    // value of tile (for depot top-left)
    public static ArrayList<Integer> resval = new ArrayList<>();
    
    public static int TILEOFF(int x, int y) {
        return x + 1 + (y + 1) * (mapX + 2);
    }

    // simplified version for standalone, doesn't bother with bitmask
    public static void MakeWalkGrid() {
        walkGrid.set(scanW*(mapY + 2), true);
        int curOff = mapX + 3;
        for (int y = 0; y < mapY; y++, curOff += 2) {
            for (int x = 0; x < mapX; x++, curOff++) {
                walkGrid.set(curOff, false);
                for (int ym = 0; ym < 4; ym++) {
                    for (int xm = 0; xm < 4; xm++) {
                        if (BasicBotAI.BroodWar.isWalkable(x * 4 + xm, y * 4 + ym)) {
                            continue;
                        }
                        walkGrid.set(curOff, true);
                        break;
                    }
                }
            }
        }
    }

    // mark area around resource for depot top-left blocking
    public static void MarkResBlock(TilePosition p, int tw, int th) {
        TilePosition p1 = new TilePosition(Math.max(0, p.x - 6), Math.max(0, p.y - 5));
        TilePosition p2 = new TilePosition(Math.min(mapX - 1, p.x + 2 + tw), Math.min(mapY - 1, p.y + 2 + th));

        for (int y = p1.y; y <= p2.y; y++) {
            int off = TILEOFF(p1.x, y);
            for (int x = p1.x; x <= p2.x; x++) {
                resBlock.set(off, true);
            }
        }
    }

    public static int MarkRow(int midOff, int[] distRow, int mid, int end, int inc, int valMod) {
        int writes = 0;
        for (int i = 1, rOff = midOff + inc; i <= end; i++, rOff += inc, ++writes) {
            if (walkGrid.get(rOff)) {
                break; // blocked tile, don't continue in this dir
            }
            resval.set(rOff, resval.get(rOff) + valMod * distRow[i]);
        }
        for (int i = 1 - mid, rOff = midOff; i <= end; i++, rOff -= inc, ++writes) {
            if (walkGrid.get(rOff)) {
                break;
            }
            resval.set(rOff, resval.get(rOff) + valMod * distRow[Math.max(i, 0)]);
        }

        return writes;
    }

    public static void MarkBorderValue(TilePosition p, int tw, int th, int valMod) {
        int[] sqrtArr = { 0, 300, 150, 100, 75, 60, 50, 42, 300, 212, 134, 94, 72, 58, 49, 42, 150, 134, 106, 83, 67, 55, 47, 41,
                100, 94, 83, 70, 60, 51, 44, 39, 75, 72, 67, 60, 53, 46, 41, 37, 60, 58, 55, 51, 46, 42, 38, 34, 50, 49, 47, 44, 41, 38, 35, 32, 42, 42, 41, 39, 37, 34, 32, 30 };
        int cOff = TILEOFF(p.x + tw - 1, p.y + th -1);

        boolean c = false;
        for (int i = th; i < th + 6; i++) {
            if (walkGrid.get(cOff - i * scanW)) {
                c = true;
                break;
            }
        }

        if (!c) {
            for (int s = 3; s < 7; s++) {
                int[] arr =  Arrays.copyOfRange(sqrtArr, s * 8, sqrtArr.length - 1);

                if (MarkRow(cOff - (s + 2 + th) * scanW, arr, tw + 3, s, 1, valMod) == 0) {
                    // top
                    break;
                }
            }
        }

        c = false;
        for (int i = 1; i < 5; i++) {
            if (walkGrid.get(cOff + i * scanW)) {
                c = true;
                break;
            }
        }

        if (!c) {
            for (int s = 3; s < 7; s++) {
                int[] arr =  Arrays.copyOfRange(sqrtArr, s * 8, sqrtArr.length - 1);

                if (MarkRow(cOff - (s + 1) * scanW, arr, tw + 3, s, 1, valMod) == 0) {
                    // bot
                    break;
                }
            }
        }

        c = false;
        for (int i = tw; i < tw + 7; i++) {
            if (walkGrid.get(cOff - i)) {
                c = true;
                break;
            }
        }

        if (!c) {
            for (int s = 3; s < 7; s++) {
                int[] arr =  Arrays.copyOfRange(sqrtArr, s * 8, sqrtArr.length - 1);

                if (MarkRow(cOff - (s + 3 + tw) * scanW, arr, tw + 2, s + 1, scanW, valMod) == 0) {
                    // left
                    break;
                }
            }
        }

        c = false;
        for (int i = 1; i < 5; i++) {
            if (walkGrid.get(cOff + i)) {
                c = true;
                break;
            }
        }

        if (!c) {
            for (int s = 3; s < 7; s++) {
                int[] arr =  Arrays.copyOfRange(sqrtArr, s * 8, sqrtArr.length - 1);

                if (MarkRow(cOff - (s + 1) * scanW, arr, tw + 2, s + 1, scanW, valMod) == 0) {
                    // right
                    break;
                }
            }
        }
    }

    public static int BASE_MIN = 400;
    public static int MINERAL_MIN = 500;
    public static int INC_DIST = 9 * 32;

    public void INIT() {
        mapX = BasicBotAI.BroodWar.mapWidth();
        mapY = BasicBotAI.BroodWar.mapHeight();
        MakeWalkGrid();

        Expo.all.clear();
        resBlock.set((mapX + 2) * (mapY + 2), false);
        resval.set((mapX + 2) * (mapY + 2), 0);
        ArrayList<Unit> res = new ArrayList<>();
        
        for (Unit u : BasicBotAI.BroodWar.getStaticMinerals()) {
            if (u.getInitialResources() < MINERAL_MIN) {
                continue;
            }
            TilePosition tp = u.getInitialTilePosition();
            MarkResBlock(tp, 2, 1);
            MarkBorderValue(tp, 2, 1, 1);
            res.add(u);
        }
        for (Unit u : BasicBotAI.BroodWar.getStaticGeysers()) {
            if (u.getInitialResources() == 0) {
                continue;
            }
            TilePosition tp = u.getInitialTilePosition();
            MarkResBlock(tp, 4, 2);
            MarkBorderValue(tp, 4, 2, 3);
            res.add(u);
        }
        
        ArrayList<Integer> potBase = new ArrayList<>();
        for (int off = scanW; off < scanW * (mapY + 1); off++) {
            if (resval.get(off) > BASE_MIN && !resBlock.get(off)) {
                potBase.add(off);
            }
        }
        potBase.sort((a, b) -> resval.get(b) < resval.get(a) ? 1 : 0);

        // MAKE SOME FUCKING BASES
        for (int off : potBase) {
            if (resval.get(off) <= BASE_MIN || resBlock.get(off)) {
                // can get wiped
                continue;
            }
            Expo expo = new Expo();
            expo.tile = new TilePosition((off - mapX - 3) % scanW, (off - mapY - 3) / scanW);
            expo.pos = new Position(expo.tile.x * 32 + 64, expo.tile.y * 32 + 48);
            expo.tPosMin = new Position(expo.tile.x * 32 + 48, expo.tile.y * 32 + 36);
            expo.tPosGas = new Position(expo.tile.x * 32 + 48, expo.tile.y * 32 + 48);

            Position bp = expo.pos;
            if (!BasicBotAI.BroodWar.getRegionAt(bp).isAccessible()) {
                expo.isIsland = true;
            }

            for (int i = 0; i < res.size();) {
                Position diff = new Position(
                        bp.x - res.get(i).getInitialPosition().x,
                        bp.y - res.get(i).getInitialPosition().y
                );
                if (Math.pow(diff.x, diff.x) + Math.pow(diff.y, diff.y) > Math.pow(INC_DIST, INC_DIST)) {
                    ++i;
                    continue;
                }

                if (res.get(i).getInitialType().isMineralField()) {
                    expo.minerals.add(res.get(i));
                    expo.minTiles.add(res.get(i).getTilePosition());
                    MarkBorderValue(res.get(i).getInitialTilePosition(), 2, 1, -1);
                }
                else {
                    expo.geysers.add(res.get(i));
                    expo.gasTiles.add(res.get(i).getTilePosition());
                    MarkBorderValue(res.get(i).getInitialTilePosition(), 4, 2, -3);
                }
                res.set(i, res.get(res.size() - 1));
                res.remove(res.get(res.size() - 1));
            }
        }
    }

}
