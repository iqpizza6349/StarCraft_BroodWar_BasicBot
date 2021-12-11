package com.tistory.workshop6349.examplebotT;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import com.tistory.workshop6349.tutorial.Main;

import java.util.List;

public class ExampleUtil {

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

    public static boolean hasRefinery() {
        return ExampleBot.BroodWar.self().completedUnitCount(ExampleBot.BroodWar.self().getRace().getRefinery()) > 0;
    }

    public static UnitType getWorkerType() {
        return ExampleBot.BroodWar.self().getRace().getWorker();
    }

    public static UnitType getDepotType() {
        return ExampleBot.BroodWar.self().getRace().getResourceDepot();
    }

    public static UnitType getSupplyType() {
        return ExampleBot.BroodWar.self().getRace().getSupplyProvider();
    }

    public static int getTypeCount(UnitType unitType) {
        return ExampleBot.BroodWar.self().completedUnitCount(unitType);
    }

    public static int getTotalSupply(boolean inProgress) {
        int totalSupply = ExampleBot.BroodWar.self().supplyTotal();

        if (!inProgress) {
            return totalSupply;
        }

        for (Unit unit : ExampleBot.BroodWar.self().getUnits()) {
            if (unit.isCompleted()) {
                continue;
            }

            totalSupply += unit.getType().supplyProvided();
        }

        return totalSupply;
    }

}
