package com.tistory.workshop6349.examplebotT;

import bwapi.*;
import bwem.Tile;
import com.tistory.workshop6349.tutorial.Main;

import java.util.ArrayList;
import java.util.HashMap;
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

}
