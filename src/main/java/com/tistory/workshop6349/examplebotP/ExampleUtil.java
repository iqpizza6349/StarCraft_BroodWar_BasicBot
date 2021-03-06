package com.tistory.workshop6349.examplebotP;

import bwapi.*;

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
        return (unit == null) ? null : getClosestUnitTo(unit.getPosition(), units);
    }

    public static int countUnitType(UnitType unitType) {
        return ExampleBot.BroodWar.self().allUnitCount(unitType);
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

    public static void buildBuildings(UnitType type) {
        Unit builder = ExampleBot.workers.get(0);

        if (builder.isConstructing()
                || (ExampleBot.scoutWorker != null && builder.getID() == ExampleBot.scoutWorker.getID())) {
            return;
        }

        TilePosition desiredPos = ExampleBot.BroodWar.self().getStartLocation();
        TilePosition buildPos = ExampleBot.BroodWar.getBuildLocation(type, desiredPos, 64, false);

        if (type.requiresPsi()) {
            for (int i = 64; i < 128; i += 8) {
                if (ExampleBot.BroodWar.hasPowerPrecise(buildPos.toPosition())) {
                    break;
                }
                buildPos = ExampleBot.BroodWar.getBuildLocation(type, desiredPos, i, false);
            }
        }

        builder.build(type, buildPos);
    }

    public static boolean delay(int delay) {
        return ExampleBot.BroodWar.getFrameCount() % delay == 0;
    }

    public static void attackUnit(Unit attacker, Unit target, boolean repeat) {
        if (attacker == null || target == null) {
            return;
        }

        if (!target.exists()) {
            return;
        }

        if (attacker.getLastCommandFrame() >= ExampleBot.BroodWar.getFrameCount()) {
            return;
        }

        int coolDown = target.isFlying() ? attacker.getAirWeaponCooldown() : attacker.getGroundWeaponCooldown();

        int maxCoolDown = attacker.getPlayer().weaponDamageCooldown(attacker.getType());

        if (coolDown > (int)(maxCoolDown * 0.8)) {
            return;
        }

        if (ExampleBot.BroodWar.getFrameCount() - attacker.getLastCommandFrame() > 5 * 24) {
            repeat = true;
        }

        if (!repeat) {
            UnitCommand currentCommand = attacker.getLastCommand();

            if (currentCommand.getType() == UnitCommandType.Attack_Unit
                    && currentCommand.getTarget() == target) {
                return;
            }
        }

        attacker.attack(target);
    }

    public static void attackUnit(Unit attacker, Unit target) {
        attackUnit(attacker, target, false);
    }

    public static void attackMove(Unit attacker, Position targetPosition, boolean repeat) {
        if (attacker == null || !targetPosition.isValid(ExampleBot.BroodWar)) {
            return;
        }

        if (attacker.getLastCommandFrame() >= ExampleBot.BroodWar.getFrameCount()) {
            return;
        }

        int coolDown = Math.max(attacker.getAirWeaponCooldown(), attacker.getGroundWeaponCooldown());

        int maxCoolDown = attacker.getPlayer().weaponDamageCooldown(attacker.getType());

        if (coolDown > (int)(maxCoolDown * 0.8)) {
            return;
        }

        if (ExampleBot.BroodWar.getFrameCount() - attacker.getLastCommandFrame() > 5 * 24) {
            repeat = true;
        }

        if (!repeat) {
            UnitCommand currentCommand = attacker.getLastCommand();

            if (currentCommand.getType() == UnitCommandType.Attack_Move && currentCommand.getTargetPosition().equals(targetPosition)) {
                return;
            }
        }

        attacker.attack(targetPosition);
    }

    public static void attackMove(Unit attacker, Position targetPosition) {
        attackMove(attacker, targetPosition, false);
    }

    public static void move(Unit attacker, Position position, boolean repeat) {

        if (attacker == null || !position.isValid(ExampleBot.BroodWar)) {
            return;
        }

        if (attacker.getLastCommandFrame() >= ExampleBot.BroodWar.getFrameCount()) {
            return;
        }

        if (ExampleBot.BroodWar.getFrameCount() - attacker.getLastCommandFrame() > 5 * 24) {
            repeat = true;
        }

        if (!repeat) {
            UnitCommand currentCommand = attacker.getLastCommand();
            if ((currentCommand.getType() == UnitCommandType.Move)
                    && (currentCommand.getTargetPosition().equals(position))
                    && attacker.isMoving()) {
                return;
            }
        }

        attacker.move(position);
    }

    public static void move(Unit attacker, Position position) {
        move(attacker, position, false);
    }

    public static boolean checkEnemyBase(TilePosition tilePosition) {
        for (Unit u : ExampleBot.BroodWar.getUnitsInRadius(tilePosition.toPosition(), 256)) {
            if (u.getPlayer() == ExampleBot.BroodWar.enemy()) {
                return true;
            }
        }
        return false;
    }

    public static Unit getClosestEnemy(Unit unit, int r) {
        if (unit == null) {
            return null;
        }

        Unit nearestEnemy = null;
        double minDist = Double.MAX_VALUE;
        for (Unit enemy : unit.getUnitsInRadius(r)) {
            if (unit.getPlayer() != ExampleBot.BroodWar.enemy()) {
                continue;
            }

            double dist = unit.getDistance(enemy);

            if (dist < minDist) {
                minDist = dist;
                nearestEnemy = enemy;
            }
        }
        return nearestEnemy;
    }

}
