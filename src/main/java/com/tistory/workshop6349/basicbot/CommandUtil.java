package com.tistory.workshop6349.basicbot;

import bwapi.*;

import java.util.HashSet;

public class CommandUtil {

    // 한 프레임에 여러 명령을 받지 않도록 hash 로 관리함
    public static HashSet<Unit> commandHash = new HashSet<>();

    // 매 프레임 마다 초기화 한다.
    public static void clearCommandHash() {
        commandHash.clear();
    }

    public static int getFrameCount() {
        return BasicBotModule.BroodWar.getFrameCount();
    }

    public static void burrow(Unit unit) {
        if (commandHash.contains(unit))
            return;
        else
            commandHash.add(unit);
        unit.stop();
        unit.burrow();
    }

    public static void unBurrow(Unit unit) {
        if (commandHash.contains(unit))
            return;
        else
            commandHash.add(unit);
        unit.unburrow();
    }

    static public void useStim_Packs(Unit unit) {
        if (commandHash.contains(unit)) {
            return;
        } else {
            commandHash.add(unit);
        }
        unit.useTech(TechType.Stim_Packs);
    }

    static public void holdPosition(Unit unit) {
        if (commandHash.contains(unit)) {
            return;
        } else {
            commandHash.add(unit);
        }
        if (unit.canHoldPosition()) {
            unit.holdPosition();
        }
    }

    static public void siege(Unit unit) {
        if (commandHash.contains(unit)) {
            return;
        } else {
            commandHash.add(unit);
        }
        unit.siege();
    }

    static public void unsiege(Unit unit) {
        if (commandHash.contains(unit)) {
            return;
        } else {
            commandHash.add(unit);
        }
        unit.unsiege();
    }

    static public void unloadAll(Unit unit) {
        if (commandHash.contains(unit)) {
            return;
        } else {
            commandHash.add(unit);
        }
        unit.unloadAll();
    }

    static public void useTech(Unit unit, TechType techType) {
        if (commandHash.contains(unit)) {
            return;
        } else {
            commandHash.add(unit);
        }
        if (unit.canUseTech(techType)) {
            unit.useTech(techType);

        }
    }

    static public void useTech(Unit unit, TechType techType, Unit enemy) {
        if (commandHash.contains(unit)) {
            return;
        } else {
            commandHash.add(unit);
        }
        if (unit.canUseTech(techType, enemy)) {
            unit.useTech(techType, enemy);
        }
    }

    static public void useTech(Unit unit, TechType techType, Position position) {
        if (commandHash.contains(unit)) {
            return;
        } else {
            commandHash.add(unit);
        }
        if (unit.canUseTech(techType, position)) {
            unit.useTech(techType, position);
        }
    }

    public static void attackUnit(Unit attacker, Unit target) {
        if (attacker == null || target == null) {
            return;
        }

        if (attacker.getLastCommandFrame() >= getFrameCount() || attacker.isAttackFrame()) {
            return;
        }

        UnitCommand currentCommand = attacker.getLastCommand();

        if (currentCommand.getType() == UnitCommandType.Attack_Unit && currentCommand.getTarget() == target) {
            return;
        }

        attacker.attack(target);
    }

    public static void attackMove(Unit attacker, final Position targetPosition) {
        if (attacker == null || !targetPosition.isValid(BasicBotModule.BroodWar)) {
            return;
        }

        if (attacker.getLastCommandFrame() >= getFrameCount() || attacker.isAttackFrame()) {
            return;
        }

        UnitCommand currentCommand = attacker.getLastCommand();

        if (currentCommand.getType() == UnitCommandType.Attack_Move && currentCommand.getTargetPosition().equals(targetPosition)) {
            return;
        }

        attacker.attack(targetPosition);
    }

    public static void move(Unit attacker, final Position targetPosition) {
        if (attacker == null || !targetPosition.isValid(BasicBotModule.BroodWar)) {
            return;
        }

        if (attacker.getLastCommandFrame() >= getFrameCount() || attacker.isAttackFrame()) {
            return;
        }

        UnitCommand currentCommand = attacker.getLastCommand();
        if ((currentCommand.getType() == UnitCommandType.Move)
                && (currentCommand.getTargetPosition().equals(targetPosition)) && attacker.isMoving()) {
            return;
        }

        attacker.move(targetPosition);
    }

    public static void rightClick(Unit unit, Unit target) {
        if (unit == null || target == null) {
            return;
        }

        if (unit.getLastCommandFrame() >= getFrameCount() || unit.isAttackFrame()) {
            return;
        }

        UnitCommand currentCommand = unit.getLastCommand();

        if (unit.getLastCommandFrame() > 10) {

            if ((currentCommand.getType() == UnitCommandType.Right_Click_Unit)
                    && (target.getPosition().equals(currentCommand.getTargetPosition()))) {
                return;
            }
        }

        unit.rightClick(target);
    }

    public static void repair(Unit unit, Unit target) {
        if (unit == null || target == null) {
            return;
        }

        if (unit.getLastCommandFrame() >= getFrameCount() || unit.isAttackFrame()) {
            return;
        }

        UnitCommand currentCommand = unit.getLastCommand();
        if (unit.getLastCommandFrame() > 10) {
            if ((currentCommand.getType() == UnitCommandType.Repair) && (currentCommand.getTarget() == target)) {
                return;
            }
        }

        unit.repair(target);
    }

    public static boolean CanAttack(Unit attacker, Unit target) {
        return GetWeapon(attacker, target) != WeaponType.None;
    }

    public static boolean CanAttackAir(Unit unit) {
        return unit.getType().airWeapon() != WeaponType.None;
    }

    public static boolean CanAttackGround(Unit unit) {
        return unit.getType().groundWeapon() != WeaponType.None;
    }

    public static WeaponType GetWeapon(Unit attacker, Unit target) {
        return target.isFlying() ? attacker.getType().airWeapon() : attacker.getType().groundWeapon();
    }

    public static  WeaponType GetWeapon(UnitType attacker, UnitType target) {
        return target.isFlyer() ? attacker.airWeapon() : attacker.groundWeapon();
    }

    public static int GetAttackRange(Unit attacker, Unit target) {
        WeaponType weapon = GetWeapon(attacker, target);

        if (weapon == WeaponType.None) {
            return 0;
        }

        int range = weapon.maxRange();

        if ((attacker.getType() == UnitType.Protoss_Dragoon)
                && (attacker.getPlayer() == BasicBotModule.BroodWar.self())
                && BasicBotModule.BroodWar.self().getUpgradeLevel(UpgradeType.Singularity_Charge) > 0) {
            range = 6 * 32;
        }

        return range;
    }
}
