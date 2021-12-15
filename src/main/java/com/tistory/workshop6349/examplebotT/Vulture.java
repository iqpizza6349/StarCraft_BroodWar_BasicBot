package com.tistory.workshop6349.examplebotT;

import bwapi.*;

import java.util.ArrayList;

public class Vulture {

    public static final Boolean DEBUG = true;

    public Unit vulture;
    public boolean checkBase;
    public boolean requireKite;

    public ArrayList<Unit> rangedUnitTargets;

    public Vulture(Unit vulture) {
        this.vulture = vulture;
        this.checkBase = false;
        this.requireKite = true;
        rangedUnitTargets = new ArrayList<>();
    }

    public void update() {
        // 모든 적군 유닛들 중 시야에 보이는 적군만 rangedUnitTargets 에 넣고
        // 그 중에서도 우선순위를 정해서 최종 타겟을 정한 후, 카이팅을 한다.
        rangedUnitTargets.clear();

        for (Unit unit : ExampleBot.BroodWar.enemy().getUnits()) {
            if (unit == null || !unit.exists()) {
                continue;
            }

            if (!unit.isCompleted()) {
                continue;
            }

            if (!unit.isVisible(ExampleBot.BroodWar.self())) {
                continue;
            }

            rangedUnitTargets.add(unit);
        }

        Unit target = getTarget(rangedUnitTargets);

        if (target != null) {


            if (DEBUG) {
                ExampleBot.BroodWar.drawLineMap(vulture.getPosition(), vulture.getTargetPosition(), Color.Purple);
            }

//            requireKite = needKiteState(rangedUnitTargets);

            if (requireKite) {
                action(target);
            } else {
                ExampleUtil.attackUnit(vulture, target);
            }
        }
        else {
            if (vulture.getDistance(ExampleBot.enemyBase) > 32 * 3) {
                ExampleUtil.attackMove(vulture, ExampleBot.enemyBase);
            }
        }
    }

    public boolean needKiteState(ArrayList<Unit> targets) {

        // 내가 상대보다 유리하다면 그냥 공격 (1.2배)
        // 너무 불리하면 후퇴 (2배)
        // 해볼만 하면 카이팅 (1.5배)
        double enemyScore = 0.0;
        int selfScore = ExampleBot.VULTURES.size();

        for (Unit t : targets) {
            if (t == null || !t.exists()) {
                continue;
            }

            // 방어용 건물이 아니라면 continue
            if (t.getType().isBuilding() && !t.canAttack(vulture)) {
                continue;
            }

            if (t.getType().isWorker()) {
                enemyScore += 0.1;
            }
            else if (t.getType().isBuilding() && t.canAttack(vulture)) {
                enemyScore += 0.5;
            }
            else if (t.getType() == UnitType.Terran_Marine
                    || t.getType() == UnitType.Zerg_Zergling
                    || t.getType() == UnitType.Protoss_Zealot) {
                enemyScore += 0.8;
            }
            else if (t.isFlying() && t.canAttack()) {
                enemyScore += 3;
            }
            else if (t.getType() == vulture.getType()) {
                enemyScore += 1;
            }
            else if (t.getType() == UnitType.Protoss_Dragoon
                    || t.getType() == UnitType.Zerg_Hydralisk) {
                enemyScore += 1.5;
            }
            else if (t.getType() == UnitType.Terran_Goliath
                    || t.getType() == UnitType.Protoss_Dark_Templar
                    || t.getType() == UnitType.Protoss_Reaver) {
                enemyScore += 3;
            }
            else if (t.getType() == UnitType.Terran_Siege_Tank_Siege_Mode
                    || t.getType() == UnitType.Terran_Siege_Tank_Tank_Mode) {
                enemyScore += 5;
            }
        }

        // 내가 무조건 유리할 경우
        if (selfScore >= enemyScore) {
            return false;
        }
        // 애매하지만 카이팅하면 될 것 같을 때
        else return selfScore >= enemyScore * 0.8;

    }


    public Unit getTarget(ArrayList<Unit> targets) {
        int bestPriorityDist = 128*128*32;
        int bestPriority = 0;

        double bestLTD = 0;

        int highPriority = 0;
        double closestDist = Double.MAX_VALUE;
        Unit closestTarget = null;

        for (Unit t : targets) {
            double dist = vulture.getDistance(t);
            double LTD = calculateLTD(t, vulture);
            int priority = getAttackPriority(vulture, t);
            boolean targetIsTreat = LTD > 0;

            if (closestTarget == null
                    || priority > highPriority
                    || (priority == highPriority && dist < closestDist)) {
                closestDist = dist;
                highPriority = priority;
                closestTarget = t;
            }
        }

        return closestTarget;
    }

    public int getAttackPriority(Unit unit, Unit target) {

        UnitType unitType = unit.getType();
        UnitType targetType = target.getType();

        if (unitType == UnitType.Zerg_Scourge) {
            if (targetType == UnitType.Protoss_Carrier) {
                return 100;
            }
            if (targetType == UnitType.Protoss_Corsair) {
                return 90;
            }
        }

        boolean isTreat = unitType.isFlyer() ? targetType.airWeapon() != WeaponType.None : targetType.groundWeapon() != WeaponType.None;

        if (targetType.isWorker()) {
            isTreat = false;
        }

        if (targetType == UnitType.Zerg_Larva || targetType == UnitType.Zerg_Egg) {
            return 0;
        }

        if (unit.isFlying() && targetType == UnitType.Protoss_Carrier) {
            return 101;
        }

        Position ourBasePosition = ExampleBot.BroodWar.self().getStartLocation().toPosition();
        if (targetType.isWorker()
                && (target.isConstructing() || target.isRepairing())
                && target.getDistance(ourBasePosition) < 32 * 35) {
            return 100;
        }

        if (targetType.isBuilding()
                && (target.isCompleted() || target.isBeingConstructed())
                && target.getDistance(ourBasePosition) < 32 * 35) {
            return 90;
        }

        if (targetType == UnitType.Terran_Bunker || isTreat) {
            return 11;
        }
        else if (targetType.isWorker()) {
            if (unitType == UnitType.Terran_Vulture) {
                return 11;
            }

            return 11;
        }
        else if (targetType == UnitType.Zerg_Spawning_Pool
                || targetType == UnitType.Protoss_Pylon) {
            return 5;
        }
        else if (targetType.gasPrice() > 0) {
            return 4;
        }
        else if (targetType.mineralPrice() > 0) {
            return 3;
        }
        else {
            return 1;
        }
    }

    public double calculateLTD(Unit attacker, Unit target) {
        WeaponType weapon = getWeapon(attacker, target);

        if (weapon == WeaponType.None) {
            return 0;
        }

        return (double) (weapon.damageAmount()) / weapon.damageCooldown();
    }
    
    public WeaponType getWeapon(Unit attacker, Unit target) {
        return target.isFlying() ? attacker.getType().airWeapon() : attacker.getType().groundWeapon();
    }

    public WeaponType getWeapon(UnitType attacker, UnitType target) {
        return target.isFlyer() ? attacker.airWeapon() : attacker.groundWeapon();
    }
    
    public void action(Unit target) {
        if (vulture == null || target == null) {
            return;
        }

        int coolDown = vulture.getType().groundWeapon().damageCooldown();
        int latency = ExampleBot.BroodWar.getLatency().ordinal();
        double speed = vulture.getType().topSpeed();
        double range = vulture.getType().groundWeapon().maxRange();
        double distanceToTarget = vulture.getDistance(target);
        double distanceToFiringRange = Math.max(distanceToTarget - range, 0.0);
        double timeToEnterFiringRange = distanceToFiringRange / speed;
        int framesToAttack = (int) (timeToEnterFiringRange) + 2 * latency;

        int currentCoolDown = vulture.isStartingAttack() ? coolDown : vulture.getGroundWeaponCooldown();

        Position fleeVector = getKiteVector(target, vulture);
        Position moveToPosition = new Position(
                vulture.getPosition().x + fleeVector.x,
                vulture.getPosition().y + fleeVector.y
        );

        if (currentCoolDown <= framesToAttack) {
            vulture.attack(target);
        }
        else {
            if (moveToPosition.isValid(ExampleBot.BroodWar)) {
                vulture.rightClick(moveToPosition);
            }
        }
    }

    public Position getKiteVector(Unit unit, Unit target) {
        int targetX = target.getPosition().x;
        int targetY = target.getPosition().y;
        int unitX = unit.getPosition().x;
        int unitY = unit.getPosition().y;

        Position fleeVector = new Position(targetX - unitX, targetY - unitY);
        double fleeAngle = Math.atan2(fleeVector.y, fleeVector.x);
        fleeVector = new Position((int)(64 * Math.cos(fleeAngle)), (int)(64 * Math.sin(fleeAngle)));
        return fleeVector;
    }

    public void actionExecute() {
        update();
    }
    
    
}
