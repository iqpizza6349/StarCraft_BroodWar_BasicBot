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

    public static ArrayList<Unit> getBuildingsInRadius(Player p, Position pos, int radius, boolean ground, boolean air, boolean hide, boolean groundDistance) {
        ArrayList<Unit> buildings = new ArrayList<>();

        Player player = (p == ExampleBot.BroodWar.self() ? ExampleBot.BroodWar.self() : ExampleBot.BroodWar.enemy());

        for (Unit u : player.getUnits()) {
            if (!u.getType().isBuilding()) {
                continue;
            }

            if (!hide) {
                continue;
            }

            if (!air && u.isLifted()) {
                continue;
            }

            if (!ground && !u.isLifted()) {
                continue;
            }

            if (radius != 0) {
                if (u.getPosition() == Position.Unknown) {
                    continue;
                }

                if (groundDistance) {
                    int dist = ExampleBot.map.getPathLength(pos, u.getPosition());

                    if (dist < 0 || dist > radius) {
                        continue;
                    }

                    buildings.add(u);
                }
                else {
                    Position newPos = new Position(pos.x - u.getPosition().x, pos.y - u.getPosition().y);
                    if (Math.abs(newPos.x) > radius || Math.abs(newPos.y) > radius) {
                        continue;
                    }

                    if (Math.abs(newPos.x) + Math.abs(newPos.y) <= radius) {
                        buildings.add(u);
                    }
                    else {
                        if ((newPos.x * newPos.x) + (newPos.y * newPos.y) <= radius * radius) {
                            buildings.add(u);
                        }
                    }
                }
            }
            else {
                buildings.add(u);
            }
        }

        return buildings;
    }

    public static ArrayList<Unit> getBuildingsInRadius(Player p, Position pos, int radius, boolean ground, boolean air, boolean hide) {
        return getBuildingsInRadius(p, pos, radius, ground, air, hide, false);
    }

    public static ArrayList<Unit> getBuildingsInRadius(Player p, Position pos, int radius, boolean ground, boolean air) {
        return getBuildingsInRadius(p, pos, radius, ground, air, false, false);
    }

    public static ArrayList<Unit> getBuildingsInRadius(Player p, Position pos, int radius, boolean ground) {
        return getBuildingsInRadius(p, pos, radius, ground, true, false, false);
    }

    public static ArrayList<Unit> getBuildingsInRadius(Player p, Position pos, int radius) {
        return getBuildingsInRadius(p, pos, radius, true, true, false, false);
    }

    public static ArrayList<Unit> getBuildingsInRadius(Player p, Position pos) {
        return getBuildingsInRadius(p, pos, 0, true, true, false, false);
    }

    public static ArrayList<Unit> getBuildingsInRadius(Player p) {
        return getBuildingsInRadius(p, Position.Origin, 0, true, true, false, false);
    }

    public static ArrayList<Unit> getUnitsInRadius(Player p, Position pos, int radius, boolean ground, boolean air, boolean worker, boolean hide, boolean groundDistance) {
        ArrayList<Unit> units = new ArrayList<>();

        Player player = (p == ExampleBot.BroodWar.self() ? ExampleBot.BroodWar.self() : ExampleBot.BroodWar.enemy());

        for (Unit u : player.getUnits()) {
            if (u.getType() != UnitType.Zerg_Lurker && !hide) {
                continue;
            }

            if (u.getType() == UnitType.Terran_Vulture_Spider_Mine) {
                continue;
            }

            if (!air && u.isLifted()) {
                continue;
            }

            if (!ground && !u.isLifted()) {
                continue;
            }

            if (!worker && u.getType().isWorker()) {
                continue;
            }

            if (radius != 0) {
                if (u.getPosition() == Position.Unknown) {
                    continue;
                }

                if (groundDistance) {
                    int dist = ExampleBot.map.getPathLength(pos, u.getPosition());

                    if (dist < 0 || dist > radius) {
                        continue;
                    }

                    units.add(u);
                }
                else {
                    Position newPos = new Position(pos.x - u.getPosition().x, pos.y - u.getPosition().y);

                    if (Math.abs(newPos.x) > radius || Math.abs(newPos.y) > radius) {
                        continue;
                    }
                    if (Math.abs(newPos.x) + Math.abs(newPos.y) <= radius) {
                        units.add(u);
                    }
                    else {
                        if ((newPos.x * newPos.x) + (newPos.y * newPos.y) <= radius * radius) {
                            units.add(u);
                        }
                    }

                }
            }
            else {
                units.add(u);
            }

        }

        return units;
    }

    public static ArrayList<Unit> getUnitsInRadius(Player p, Position pos, int radius, boolean ground, boolean air, boolean worker) {
        return getUnitsInRadius(p, pos, radius, ground, air, worker, false, false);
    }

    public static ArrayList<Unit> getUnitsInRadius(Player p, Position pos, int radius, boolean ground, boolean air) {
        return getUnitsInRadius(p, pos, radius, ground, air, true, false, false);
    }

    public static ArrayList<Unit> getUnitsInRadius(Player p, Position pos, int radius, boolean ground) {
        return getUnitsInRadius(p, pos, radius, ground, true, true, false, false);
    }

    public static ArrayList<Unit> getUnitsInRadius(Player p, Position pos, int radius) {
        return getUnitsInRadius(p, pos, radius, true, true, true, false, false);
    }

    public static ArrayList<Unit> getUnitsInRadius(Player p, Position pos) {
        return getUnitsInRadius(p, pos, 0, true, true, true, false, false);
    }

    public static ArrayList<Unit> getUnitsInRadius(Player p) {
        return getUnitsInRadius(p, Position.Origin, 0, true, true, true, false, false);
    }

    public static ArrayList<Unit> getTypeUnitsInRadius(UnitType t, Player p, Position pos, int radius, boolean hide) {
        ArrayList<Unit> units = new ArrayList<>();

        Player player = (p == ExampleBot.BroodWar.self() ? ExampleBot.BroodWar.self() : ExampleBot.BroodWar.enemy());

        for (Unit u : player.getUnits()) {
            if (u.getType() != t) {
                continue;
            }

            if (!hide) {
                continue;
            }

            if (radius != 0) {
                if (u.getPosition() == Position.Unknown) {
                    continue;
                }

                Position newPos = new Position(pos.x - u.getPosition().x, pos.y - u.getPosition().y);

                if (Math.abs(newPos.x) > radius || Math.abs(newPos.y) > radius) {
                    continue;
                }

                if (Math.abs(newPos.x) + Math.abs(newPos.y) <= radius) {
                    units.add(u);
                }
                else {
                    if ((newPos.x * newPos.x) + (newPos.y * newPos.y) <= radius * radius) {
                        units.add(u);
                    }
                }
            }
            else {
                units.add(u);
            }
        }

        return units;
    }

    public static ArrayList<Unit> getTypeUnitsInRadius(UnitType t, Player p, Position pos, int radius) {
        return getTypeUnitsInRadius(t, p, pos, radius, false);
    }

    public static ArrayList<Unit> getTypeUnitsInRadius(UnitType t, Player p, Position pos) {
        return getTypeUnitsInRadius(t, p, pos, 0, false);
    }

    public static ArrayList<Unit> getTypeUnitsInRadius(UnitType t, Player p) {
        return getTypeUnitsInRadius(t, p, Position.Origin, 0, false);
    }

    public static void kiting(Unit attacker, Unit target, int distance, int threshold) {
        int backDist = 3;
        int weaponRange = attacker.getType().groundWeapon().maxRange();
        int distToTarget = attacker.getPosition().getApproxDistance(target.getPosition());

        if (target.getType().isWorker()) {
            backDist = 2;
        }

        if (distance > threshold) {
            if (attacker.getGroundWeaponCooldown() == 0) {
                attackUnit(attacker, target);
            }

            if (!target.getType().isWorker() && distToTarget < weaponRange) {
                moveBackPosition(attacker, target.getPosition(), backDist * 32);
            }
        }
        else {
            moveBackPosition(attacker, target.getPosition(), backDist * 32);
        }
    }

    public static void moveBackPosition(Unit unit, Position enemyPos, int length) {
        if (unit.getLastCommandFrame() >= ExampleBot.BroodWar.getFrameCount()) {
            return;
        }

        Position backPos = new Position(0, 0);
        Position myPos = unit.getPosition();

        double distance = myPos.getDistance(enemyPos);
        double alpha = (double) length / distance;

        int backPosX = myPos.x + (int)((myPos.x - enemyPos.x) * alpha);
        int backPosY = myPos.y + (int)((myPos.y - enemyPos.y) * alpha);

        backPos = new Position(backPosX, backPosY);
        
        int angle = 30;
        int totalAngle = 0;

        int value = unit.getType().isFlyer() ? getPathValueForAir(backPos) : getPathValue(myPos, backPos);
        Position bPos = backPos;

        while (totalAngle <= 360) {
            backPos = getCirclePosFromPosByDegree(myPos, backPos, angle);
            totalAngle += angle;

            int tempValue = unit.getType().isFlyer() ? getPathValueForAir(backPos) : getPathValue(myPos, backPos);

            if (value < tempValue) {
                value = tempValue;
                bPos = backPos;
            }

        }

        unit.move(bPos);

        ExampleBot.BroodWar.drawCircleMap(bPos, 2, Color.Blue, true);

        if (!unit.getType().isFlyer()) {
            Position behind = getDirectionDistancePosition(myPos, bPos, 2 * 32);

            ArrayList<Unit> tempUnits = getUnitsInRadius(ExampleBot.BroodWar.self(), behind, (int)(1.5 * 32), true, false, false);

            for (Unit u : tempUnits) {
                moveBackPositionByMe(u, myPos);
            }
        }
    }

    public static void moveBackPositionByMe(Unit unit, Position pos) {

        if (unit.getLastCommandFrame() >= ExampleBot.BroodWar.getFrameCount()
                || unit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
            return;
        }
        
        Position backPos = new Position(0, 0);
        Position myPos = unit.getPosition();

        double backDistance = 2 * 32;
        double distance = myPos.getDistance(pos);
        double alpha = backDistance / distance;

        int backPosX = myPos.x + (int)((myPos.x - pos.x) * alpha);
        int backPosY = myPos.y + (int)((myPos.y - pos.y) * alpha);

        backPos = new Position(backPosX, backPosY);

        double[] degrees = { 30, 60, 90, 120, 150, 180, 210, 240, 270, 300, 330 };
        Position newPos = backPos;

        if (!isValidPath(myPos, backPos)) {
            for (int i = 0; i <= 10; i++) {
                newPos = getCirclePosFromPosByDegree(myPos, backPos, degrees[i]);

                if (isValidPath(myPos, newPos)) {
                    break;
                }
            }
        }

        ArrayList<Unit> tempUnits = getUnitsInRadius(ExampleBot.BroodWar.self(), newPos, 32, true, false, false);
        ExampleBot.BroodWar.drawCircleMap(newPos, 32, Color.Red);

        unit.move(newPos);
        for (Unit u : tempUnits) {
            moveBackPositionByMe(u, myPos);
        }

    }

    public static boolean isValidPath(Position s, Position e) {
        TilePosition ts = s.toTilePosition();
        TilePosition te = e.toTilePosition();
        WalkPosition ws = s.toWalkPosition();
        WalkPosition we = e.toWalkPosition();

        if (!s.isValid(ExampleBot.BroodWar) || !e.isValid(ExampleBot.BroodWar) || getAltitude(we) <= 0) {
            return false;
        }

        List<Unit> temp = ExampleBot.BroodWar.getUnitsInRadius(e, 40, UnitFilter.IsBuilding);
        List<Unit> tmp = ExampleBot.BroodWar.getUnitsInRadius(e, 40, UnitFilter.IsNeutral);
        ArrayList<Unit> units = getUnitsInRadius(ExampleBot.BroodWar.enemy(), e, 30, true, false, false);

        if (temp.size() != 0 || tmp.size() != 0 || units.size() != 0) {
            return false;
        }

        return true;
    }


    public static Position getDirectionDistancePosition(Position source, Position direction, int distance) {
        Position forwardPos = new Position(0, 0);

        double forwardDistanceNeed = distance;
        double dist = source.getDistance(direction);
        double alpha = forwardDistanceNeed / dist;

        int forwardPosX = source.x - (int)((source.x - direction.x) * alpha);
        int forwardPosY = source.y - (int)((source.y - direction.y) * alpha);

        return new Position(forwardPosX, forwardPosY);
    }

    public static Position getCirclePosFromPosByDegree(Position center, Position fromPos, double degree) {
        return getCirclePosFromPosByRadian(center, fromPos, (degree * Math.PI / 180));
    }

    public static Position getCirclePosFromPosByRadian(Position center, Position fromPos, double radian) {
        int x = (int)((double)(fromPos.x - center.x) * Math.cos(radian) - (double) (fromPos.y - center.y) * Math.sin(radian) + center.x);
        int y = (int)((double)(fromPos.x - center.x) * Math.sin(radian) - (double) (fromPos.y - center.y) * Math.cos(radian) + center.y);

        return new Position(x, y);
    }

    public static int getPathValueForAir(Position p) {
        if (!p.isValid(ExampleBot.BroodWar)) {
            return -1;
        }

        return getDangerUnitNPoint(p, 0, true);
    }

    public static int getDangerUnitNPoint(Position pos, int point, boolean isFlyer) {

        int minGap = 1000;
        ArrayList<Unit> enemyWorkers = getTypeUnitsInRadius(
                ExampleBot.BroodWar.enemy().getRace().getWorker(),
                ExampleBot.BroodWar.enemy(),
                pos,
                10 * 32
        );

        for (Unit w : enemyWorkers) {
            int gap = pos.getApproxDistance(w.getPosition()) - w.getType().groundWeapon().maxRange();

            if (minGap > gap) {
                minGap = gap;
            }
        }

        return minGap;
    }

    public static int getPathValue(Position st, Position en) {
        TilePosition ts = st.toTilePosition();
        TilePosition te = en.toTilePosition();
        WalkPosition ws = st.toWalkPosition();
        WalkPosition we = en.toWalkPosition();

        int point = 0;

        if (!en.isValid(ExampleBot.BroodWar)
                || getAltitude(we) <= 0
                || ExampleBot.BroodWar.getUnitsInRadius(en, 16, UnitFilter.IsBuilding).size() != 0
                || ExampleBot.BroodWar.getUnitsInRadius(en, 16, UnitFilter.IsNeutral).size() != 0 ) {
            return -1;
        }

        boolean nearChoke = false;

        if (ExampleBot.map.getArea(ts) == null || ExampleBot.map.getArea(te) == null) {
            nearChoke = true;
        }
        else {
            if (ExampleBot.map.getArea(ts) != ExampleBot.map.getArea(te)
                    && ExampleBot.BroodWar.getGroundHeight(ts) != ExampleBot.BroodWar.getGroundHeight(te)) {
                int dist = ExampleBot.map.getPathLength(st, en);
                if (dist == -1 || dist > st.getApproxDistance(en) * 2) {
                    return -1;
                }
                else {
                    nearChoke = true;
                }
            }
        }

        int dangerPoint = getDangerUnitNPoint(en, 0, false);
        point = 2 * dangerPoint;

        if (nearChoke) {
            point = point + getAltitude(we);
        }
        else {
            int chokeAlt = getAltitude(we) < 100 ? 100 : getAltitude(we);
            point += chokeAlt;
        }

        return point;
    }

    public static int getAltitude(Position p) {
        return getAltitude(p.toWalkPosition());
    }

    public static int getAltitude(TilePosition p) {
        return getAltitude(p.toWalkPosition());
    }

    public static int getAltitude(WalkPosition p) {
        return p.isValid(ExampleBot.BroodWar) ? (int) (Math.random() * 16) : -1;
    }

    public static int getPathValueForMarine(Position st, Position en) {
        TilePosition ts = st.toTilePosition();
        TilePosition te = en.toTilePosition();
        WalkPosition ws = st.toWalkPosition();
        WalkPosition we = en.toWalkPosition();

        int point = 0;

        if (!en.isValid(ExampleBot.BroodWar) || getAltitude(we) <= 0) {
            return -1;
        }

        int gap = 16;
        int gapS = 8;
        Position pos = en;
        ArrayList<Unit> buildings = getBuildingsInRadius(ExampleBot.BroodWar.self(), pos, 4 * 32, true, false);
        ArrayList<Unit> SCV = getTypeUnitsInRadius(UnitType.Terran_SCV, ExampleBot.BroodWar.self(), pos, 3 * 32);
        List<Unit> minerals = ExampleBot.BroodWar.getUnitsInRadius(en, 32, UnitFilter.IsNeutral);

        for (Unit m : minerals) {
            if (m.getTop() - gapS <= pos.y
                    && m.getBottom() + gapS >= pos.y
                    && m.getLeft() - gapS <= pos.x
                    && m.getRight() + gapS >= pos.x) {
                return -1;
            }
        }

        for (Unit b : buildings) {
            if (b.getTop() - (gap * 2) <= pos.y
                    && b.getBottom() + gap >= pos.y
                    && b.getLeft() - gap <= pos.x
                    && b.getRight() + gap >= pos.x) {
                return -1;
            }
        }

        for (Unit s : buildings) {
            if (s.getTop() - gapS <= pos.y
                    && s.getBottom() + gapS >= pos.y
                    && s.getLeft() - gapS <= pos.x
                    && s.getRight() + gapS >= pos.x) {
                return -1;
            }
        }

        int dangerPoint = getDangerUnitNPoint(en, 0, false);
        point = 2 * dangerPoint;

        point += (2 * getAltitude(we));

        ArrayList<Unit> centers = getBuildings(UnitType.Terran_Command_Center, ExampleBot.BroodWar.self());
        if (centers.size() == 0) {
            return -1;
        }

        Unit center = centers.get(0);
        Unit mineral = ExampleBot.BroodWar.getClosestUnit(center.getPosition(), UnitFilter.IsMineralField);
        Position temp = new Position(center.getPosition().x + mineral.getPosition().x, center.getPosition().y + mineral.getPosition().y);
        Position target = new Position(temp.x / 2, temp.y / 2);
        point -= en.getApproxDistance(target);

        return point;
    }

    public static ArrayList<Unit> getBuildings(UnitType unitType, Player p) {
        ArrayList<Unit> result = new ArrayList<>();

        Player player = (p == ExampleBot.BroodWar.self() ? ExampleBot.BroodWar.self() : ExampleBot.BroodWar.enemy());

        for (Unit u : player.getUnits()) {
            if (u.getType().isBuilding()) {
                result.add(u);
            }
        }

        return result;
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

    public static Unit getClosestTypeUnit(Player p, Unit unit, UnitType type, int radius, boolean hide, boolean groundDist, boolean detectedOnly) {
        
        if (unit == null || !unit.exists()) {
            return null;
        }

        if (!unit.getPosition().isValid(ExampleBot.BroodWar)) {
            return null;
        }

        Unit closest = null;
        int closestDist = Integer.MAX_VALUE;

        Player player = (p == ExampleBot.BroodWar.self() ? ExampleBot.BroodWar.self() : ExampleBot.BroodWar.enemy());

        HashMap<UnitType, ArrayList<Unit>> buildings = new HashMap<>();
        HashMap<UnitType, ArrayList<Unit>> unitAll = new HashMap<>();
        for (Unit u : p.getUnits()) {
            if (u.getType().isBuilding()) {
                buildings.computeIfAbsent(u.getType(), k -> new ArrayList<>()).add(u);
            }
            unitAll.computeIfAbsent(u.getType(), k -> new ArrayList<>()).add(u);
        }

        ArrayList<Unit> units = type.isBuilding() ? buildings.get(type) : unitAll.get(type);
        
        for (Unit u : units) {
            if (!hide
                    || u.getPosition() == Position.Unknown
                    || (detectedOnly && !u.isDetected())
                    || u.getID() == unit.getID()) {
                continue;
            }

            Position newPos = new Position(unit.getPosition().x - u.getPosition().x, unit.getPosition().y - u.getPosition().y);

            if (groundDist) {
                int dist = ExampleBot.map.getPathLength(unit.getPosition(), u.getPosition());
                
                if (radius != 0 && dist > radius) {
                    continue;
                }

                if (dist >= 0 && dist < closestDist) {
                    closest = u;
                    closestDist = dist;
                }
                
            }
            else {
                if (radius != 0) {
                    if (Math.abs(newPos.x) > radius || Math.abs(newPos.y) > radius) {
                        continue;
                    }

                    if ((newPos.x * newPos.x) + (newPos.y * newPos.y) > radius * radius) {
                        continue;
                    }
                }

                if ((newPos.x * newPos.x + newPos.y * newPos.y) < closestDist) {
                    closest = u;
                    closestDist = (newPos.x * newPos.x + newPos.y * newPos.y);
                }
            }
        }

        return closest;
    }

    public static boolean checkEnemyBase(TilePosition tilePosition) {
        for (Unit u : ExampleBot.BroodWar.getUnitsInRadius(tilePosition.toPosition(), 32 * 9)) {
            if (u.getPlayer() == ExampleBot.BroodWar.enemy()) {
                return true;
            }
        }
        return false;
    }

}
