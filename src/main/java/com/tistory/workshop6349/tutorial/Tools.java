package com.tistory.workshop6349.tutorial;

import bwapi.*;

import java.util.List;

public class Tools {

    public static Unit getClosestUnitTo(Position p, final List<Unit> units) {
        Unit closestUnit = null;

        for (Unit unit : units) {
            if (closestUnit == null) {
                closestUnit = unit;
                continue;
            }

            if (unit.getDistance(p) < closestUnit.getDistance(p)) {
                closestUnit = unit;
            }
        }

        return closestUnit;
    }

    public static Unit getClosestUnitTo(Unit unit, final List<Unit> units) {
        if (unit == null) {
            return null;
        }
        return getClosestUnitTo(unit.getPosition(), units);
    }

    public static int countUnitsOfType(UnitType type, final List<Unit> units) {
        int sum = 0;
        for (Unit unit : units) {
            if (unit.getType() == type) {
                sum++;
            }
        }

        return sum;
    }

    public static Unit getUnitOfType(UnitType type) {
        for (Unit unit : Main.game.self().getUnits()) {
            if (unit.getType() == type && unit.isCompleted()) {
                return unit;
            }
        }

        return null;
    }

    public static Unit getDepot() {
        final UnitType depot = Main.game.self().getRace().getResourceDepot();
        return getUnitOfType(depot);
    }

    public static boolean buildBuilding(UnitType type) {
        UnitType builderType = type.whatBuilds().getFirst();

        Unit builder = Tools.getUnitOfType(builderType);
        if (builder == null) {
            return false;
        }

        TilePosition desiredPos = Main.game.self().getStartLocation();

        int maxBuildingRange = 64;
        boolean buildingOnCreep = type.requiresCreep();
        TilePosition buildPos = Main.game.getBuildLocation(type, desiredPos, maxBuildingRange, buildingOnCreep);
        return builder.build(type, buildPos);
    }

    public static void drawUnitCommands() {
        for (Unit unit : Main.game.self().getUnits()) {
            final UnitCommand command = unit.getLastCommand();
            if (command == null) {
                continue;
            }

            if (command.getTargetPosition() != Position.None) {
                Main.game.drawLineMap(unit.getPosition(), command.getTargetPosition(), Color.Red);
            }

            if (command.getTargetTilePosition() != TilePosition.None) {
                Main.game.drawLineMap(unit.getPosition(), command.getTargetTilePosition().toPosition(), Color.Green);
            }

            if (command.getTarget() != null) {
                Main.game.drawLineMap(unit.getPosition(), command.getTarget().getPosition(), Color.White);
            }
        }
    }

    public static void drawUnitBoundingBoxes() {
        for (Unit unit : Main.game.self().getUnits()) {
            Position topLeft = new Position(unit.getLeft(), unit.getTop());
            Position bottomRight = new Position(unit.getRight(), unit.getBottom());
            Main.game.drawBoxMap(topLeft, bottomRight, Color.White);
        }
    }

    public static void smartRightClick(Unit unit, Unit target) {
        if (unit == null || target == null) {
            return;
        }

        if (unit.getLastCommandFrame() >= Main.game.getFrameCount()) {
            return;
        }

        if (unit.getLastCommand().getTarget() == target) {
            return;
        }

        unit.rightClick(target);
    }

    public static int getTotalSupply(boolean inProgress) {
        int totalSupply = Main.game.self().supplyTotal();

        if (!inProgress) {
            return totalSupply;
        }

        for (Unit unit : Main.game.self().getUnits()) {
            if (unit.isCompleted()) {
                continue;
            }

            totalSupply += unit.getType().supplyProvided();
        }

        return totalSupply;
    }

    public static void drawUnitHealthBars() {
        int verticalOffSet = -10;

        for (Unit unit : Main.game.self().getUnits()) {
            final Position pos = unit.getPosition();
            int left = pos.x - unit.getType().dimensionLeft();
            int right = pos.x + unit.getType().dimensionRight();
            int top = pos.y - unit.getType().dimensionUp();
            int bottom = pos.y + unit.getType().dimensionDown();

            if (unit.getType().isResourceContainer() && unit.getInitialResources() > 0) {
                double mineralRatio = (double) unit.getResources() / (double) unit.getInitialResources();
                drawHealthBar(unit, mineralRatio, Color.Cyan, 0);
            }
            else if (unit.getType().maxHitPoints() > 0) {
                double hpRatio = (double) unit.getHitPoints() / (double) unit.getInitialHitPoints();
                Color hpColor = Color.Green;
                if (hpRatio < 0.66) {
                    hpColor = Color.Orange;
                }
                if (hpRatio < 0.33) {
                    hpColor = Color.Red;
                }
                drawHealthBar(unit, hpRatio, hpColor, 0);

                if (unit.getType().maxShields() > 0) {
                    double shieldRatio = (double) unit.getShields() / (double) unit.getType().maxShields();
                    drawHealthBar(unit, shieldRatio, Color.Blue, -3);
                }
            }
        }
    }

    public static void drawHealthBar(Unit unit, double ratio, Color color, int yOffSet) {
        int verticalOffSet = -10;
        final Position pos = unit.getPosition();

        int left = pos.x - unit.getType().dimensionLeft();
        int right = pos.x + unit.getType().dimensionRight();
        int top = pos.y - unit.getType().dimensionUp();
        int bottom = pos.y + unit.getType().dimensionDown();

        int ratioRight = left + (int)((right - left) * ratio);
        int hpTop = top + yOffSet + verticalOffSet;
        int hpBottom = top + 4 + yOffSet + verticalOffSet;

        Main.game.drawBoxMap(new Position(left, hpTop), new Position(right, hpBottom), Color.Grey);
        Main.game.drawBoxMap(new Position(left, hpTop), new Position(ratioRight, hpBottom), color, true);
        Main.game.drawBoxMap(new Position(left, hpTop), new Position(right, hpBottom), Color.Black, false);

        int tickWidth = 3;

        for (int i = left; i < right - 1; i += tickWidth) {
            Main.game.drawLineMap(new Position(i, hpTop), new Position(i, hpBottom), Color.Black);
        }

    }

}
