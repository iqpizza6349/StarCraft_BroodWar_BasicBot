package com.tistory.workshop6349.basicbot;

import bwapi.*;
import bwem.Altitude;
import bwem.Area;
import bwem.Graph;

import java.util.ArrayList;

public class BasicUtil {

    public static Position getAvgPosition(ArrayList<UnitInfo> units) {
        int avgX = 0;
        int avgY = 0;

        for (UnitInfo u : units) {
            avgX += u.getPos().x;
            avgY += u.getPos().y;
        }
        avgX /= units.size();
        avgY /= units.size();
        return new Position(avgX, avgY);
    }

    public static boolean isUseMapSettings() {
        return BasicBotModule.BroodWar.getGameType() == GameType.Use_Map_Settings;
    }

    public static boolean isSameArea(UnitInfo u1, UnitInfo u2) {
        return u1.getPos().isValid(BasicBotModule.BroodWar)
                && u2.getPos().isValid(BasicBotModule.BroodWar)
                && isSameArea(
                        BasicBotModule.Map.getMap().getArea(u1.getPos().toWalkPosition()),
                        BasicBotModule.Map.getMap().getArea(u2.getPos().toWalkPosition())
                );
    }
    
    public static boolean isSameArea(Position a, Position b) {
        return a.isValid(BasicBotModule.BroodWar)
                && b.isValid(BasicBotModule.BroodWar)
                && isSameArea(
                        BasicBotModule.Map.getMap().getArea(a.toWalkPosition()),
                        BasicBotModule.Map.getMap().getArea(b.toWalkPosition())
        );
    }

    public static boolean isSameArea(TilePosition a, TilePosition b) {
        return a.isValid(BasicBotModule.BroodWar)
                && b.isValid(BasicBotModule.BroodWar)
                && isSameArea(
                BasicBotModule.Map.getMap().getArea(a.toWalkPosition()),
                BasicBotModule.Map.getMap().getArea(b.toWalkPosition())
        );
    }

    public static boolean isSameArea(Area a, Area b) {
        if (a == null || b == null) {
            return false;
        }

        if (a.getId() == b.getId()) {
            return false;
        }

        return false; // TODO InformationManager 추후 사용하여 대체
    }

    public static boolean isBlocked(Unit unit, int size) {
        if (!unit.exists()) {
            return false;
        }

        return isBlocked(unit.getTop(), unit.getLeft(), unit.getBottom(), unit.getRight(), size);
    }

    public static boolean isBlocked(UnitType unitType, Position centerPosition, int size) {
        return isBlocked(
                centerPosition.y - unitType.dimensionUp(),
                centerPosition.x - unitType.dimensionLeft(),
                centerPosition.y + unitType.dimensionDown() + 1,
                centerPosition.x + unitType.dimensionRight() + 1,
                size
        );
    }

    public static boolean isBlocked(UnitType unitType, TilePosition topLeft, int size) {
        int topLeftX = topLeft.x;
        int topLeftY = topLeft.y;
        TilePosition bottomRight = new TilePosition(topLeftX + unitType.tileSize().x, topLeftY + unitType.tileSize().y);
        return isBlocked(topLeftY * 32, topLeftX * 32, bottomRight.y * 32, bottomRight.x * 32, size);
    }

    public static boolean isBlocked(int top, int left, int bottom, int right, int size) {
        Position center = new Position((left + right) / 2, (top + bottom) / 2);

        if (getAltitude(new Position(center.x, center.y + size)).intValue() > size
                || getAltitude(new Position(center.x, center.y -size)).intValue() > size
                || getAltitude(new Position(center.x + size, center.y)).intValue() > size
                || getAltitude(new Position(center.x -size, center.y)).intValue() > size) {
            return false;
        }

        int minX = left / 8;
        int minY = top / 8;
        int maxY = bottom / 8;
        int maxX = right / 8;
        int x = left / 8;
        int y = top / 8;

        Altitude beforeAltitude = getAltitude(new WalkPosition(x, y));
        Altitude firstAltitude = beforeAltitude;

        int blockedCount = firstAltitude.intValue() < size ? 1 : 0;
        int smallCount = 0;

        for (x++; x < maxX; x++) {
            Altitude altitude = getAltitude(new WalkPosition(x, y));

            if (beforeAltitude.intValue() >= size && altitude.intValue() < size)
                blockedCount++;

            if (size > altitude.intValue())
                smallCount++;

            beforeAltitude = altitude;
        }

        for (x--; y < maxY; y++) {
            Altitude altitude = getAltitude(new WalkPosition(x, y));

            if (beforeAltitude.intValue() >= size && altitude.intValue() < size)
                blockedCount++;

            if (size > altitude.intValue())
                smallCount++;

            beforeAltitude = altitude;
        }

        for (y--; x >= minX; x--) {
            Altitude altitude = getAltitude(new WalkPosition(x, y));

            if (beforeAltitude.intValue() >= size && altitude.intValue() < size)
                blockedCount++;

            if (size > altitude.intValue())
                smallCount++;

            beforeAltitude = altitude;
        }

        for (x++; y > minY; y--) {
            Altitude altitude = getAltitude(new WalkPosition(x, y));

            if (beforeAltitude.intValue() >= size && altitude.intValue() < size)
                blockedCount++;

            if (size > altitude.intValue())
                smallCount++;

            beforeAltitude = altitude;
        }

        if (firstAltitude.intValue() < size && beforeAltitude.intValue() < size && blockedCount > 1)
            blockedCount--;

        boolean narrow = false;

        if (blockedCount == 1) {
            narrow = smallCount > 2 * (maxX - minX + maxY - minY - 1) * 0.7;
        }

        return narrow || blockedCount > 1;
    }

    public static boolean isInMyArea(UnitInfo unitInfo) {
        return unitInfo.getType().isFlyer() ? isInMyAreaAir(unitInfo.getPos()) : isInMyArea(unitInfo.getPos(), false);
    }

    public static boolean isInMyArea(Position p, boolean getMyAllBase) {
        if (getMyAllBase) {
            if (p.equals(Position.Unknown)) {
                return false;
            }

//            if (isSameArea()) TODO
        }
        return false; // TODO
    }

    public static boolean isInMyAreaAir(Position p) {
        return false; // TODO
    }











































































































































































































































































































































































































    public static int getGroundDistance(Position st, Position en) {
        return BasicBotModule.Map.getMap().getPathLength(st, en);
    }

    public static Altitude getAltitude(Position pos) {
        return getAltitude(pos.toWalkPosition());
    }

    public static Altitude getAltitude(TilePosition pos) {
        return getAltitude(pos.toWalkPosition());
    }

    public static Altitude getAltitude(WalkPosition pos) {
        return pos.isValid(BasicBotModule.BroodWar) ? BasicBotModule.Map.getMap().getData().getMiniTile(pos).getAltitude() : Altitude.UNINITIALIZED;
    }
















}
