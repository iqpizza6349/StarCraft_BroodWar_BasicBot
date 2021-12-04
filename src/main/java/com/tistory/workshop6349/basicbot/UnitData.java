package com.tistory.workshop6349.basicbot;

import bwapi.*;
import bwem.util.Utils;

import java.util.*;

public class UnitData {

    private final HashMap<UnitType, ArrayList<UnitInfo>> unitTypeMap = new HashMap<>();
    private final HashMap<UnitType, ArrayList<UnitInfo>> buildingTypeMap = new HashMap<>();

    private final HashMap<Unit, UnitInfo> allUnits = new HashMap<>();
    private final HashMap<Unit, UnitInfo> allBuildings = new HashMap<>();
    private final HashMap<Integer, Pair<Integer, Position>> allSpells = new HashMap<>();

    private final HashMap<UnitType, Integer> completedCount = new HashMap<>();
    private final HashMap<UnitType, Integer> destroyedCount = new HashMap<>();
    private final HashMap<UnitType, Integer> allCount = new HashMap<>();

    public UnitData() {}

    public ArrayList<UnitInfo> getUnitList(UnitType type) {
        unitTypeMap.computeIfAbsent(type, type1 -> new ArrayList<>());
        return unitTypeMap.get(type);
    }

    public ArrayList<UnitInfo> getBuildingList(UnitType type) {
        buildingTypeMap.computeIfAbsent(type, type1 -> new ArrayList<>());
        return buildingTypeMap.get(type);
    }

    public int getCompletedCount(UnitType type) {
        return completedCount.getOrDefault(type, 0);
    }

    public int getDestroyedCount(UnitType type) {
        return destroyedCount.getOrDefault(type, 0);
    }

    public HashMap<UnitType, Integer> getDestroyedCountMap() {
        return destroyedCount;
    }

    public int getAllCount(UnitType type) {
        return allCount.getOrDefault(type, 0);
    }

    public void increaseCompleteUnits(UnitType type) {
        UnitType unitType = getUnitTypeDB(type);

        completedCount.computeIfAbsent(type, type1 -> 0);
        completedCount.replace(type, completedCount.get(type)+1);
    }

    public void increaseDestroyUnits(UnitType type) {
        UnitType unitType = getUnitTypeDB(type);

        destroyedCount.computeIfAbsent(type, type1 -> 0);
        destroyedCount.replace(type, destroyedCount.get(type)+1);
    }

    public void increaseCreateUnits(UnitType type) {
        UnitType unitType = getUnitTypeDB(type);

        allCount.computeIfAbsent(type, type1 -> 0);
        allCount.replace(type, allCount.get(type)+1);
    }

    public void decreaseCompleteUnits(UnitType type) {
        UnitType unitType = getUnitTypeDB(type);

        completedCount.replace(type, completedCount.get(type)+1);
    }

    public void decreaseCreateUnits(UnitType type) {
        UnitType unitType = getUnitTypeDB(type);

        allCount.replace(type, allCount.get(type)+1);
    }

    public boolean addUnitAndBuilding(Unit u) {
        UnitType type = getUnitTypeDB(u.getType());

        HashMap<Unit, UnitInfo> unitMap = type.isBuilding() ? getAllBuildings() : getAllUnits();
        ArrayList<UnitInfo> unitInfoArrayList = type.isBuilding() ? getBuildingList(type) : getUnitList(type);

        if (!unitMap.containsKey(u)) {
            UnitInfo pUnit = new UnitInfo(u);
            unitMap.put(u, pUnit);
            unitInfoArrayList.add(pUnit);

            return true;
        }

        return false;
    }
    
    public void removeUnitAndBuilding(Unit u) {
        UnitType type = getUnitTypeDB(u.getType());

        HashMap<Unit, UnitInfo> unitMap = type.isBuilding() ? getAllBuildings() : getAllUnits();
        ArrayList<UnitInfo> unitInfoArrayList = type.isBuilding() ? getBuildingList(type) : getUnitList(type);

        if (unitMap.containsKey(u)) {
            UnitInfo delUnit = null;

            for (UnitInfo info : unitInfoArrayList) {
                if (info.getUnit() == u) {
                    delUnit = info;
                    break;
                }
            }
            if (delUnit != null) {
                Utils.fastErase(unitInfoArrayList, unitInfoArrayList.indexOf(delUnit));
            }
            else {
                System.out.println("Remove Unit ERROR");
            }

            unitMap.remove(u);

            if (u.getPlayer() == Common.Self()) {
                if (u.isCompleted()) {
                    decreaseCompleteUnits(type);
                }
                decreaseCreateUnits(type);
            }

            increaseDestroyUnits(type);
        }

    }

    public void initializeAllInfo() {
        for (UnitInfo u : allUnits.values()) {
            u.initFrame();
        }

        for (UnitInfo u : allBuildings.values()) {
            u.initFrame();
        }
    }

    // TODO updateAllInfo
    public void updateAllInfo() {
        initializeAllInfo();

        HashMap<Integer, Pair<Integer, Position>> spellMap = getAllSpells();

        for (Bullet b : BasicBotModule.BroodWar.getBullets()) {
            if (b.getType() == BulletType.Fusion_Cutter_Hit
                    && b.getSource() != null
                    && b.getSource().getPlayer() == Common.Self()
                    && b.getTarget() != null
                    && b.getTarget().isBeingGathered()) {
                HashMap<Unit, UnitInfo> m = getAllUnits();
                m.get(b.getSource()).setGatheringMinerals();
            }

            if (b.getType() == BulletType.EMP_Missile
                    && b.getSource() != null
                    && b.getSource().getPlayer() == Common.Self()) {
                spellMap.put(b.getID(), new Pair<>(Common.TIME(), b.getTargetPosition()));
            }

            if (Common.TIME() % 12 == 0
                    && b.getType() == BulletType.Gauss_Rifle_Hit
                    && b.getSource() == null) {
                int gap;

                if (Common.Enemy().getUpgradeLevel(UpgradeType.U_238_Shells) == 1) {
                    gap = 8;
                }
                else {
                    gap = 7;
                }

                if (b.getTarget() != null && b.getTarget().getPlayer() == Common.Self()) {
                    ArrayList<UnitInfo> bunkers; // TODO Information;
                }
            }


        }
        
    }




    // TODO updateAndCheckTypeAllInfo

    public UnitType getUnitTypeDB(UnitType type) {
        if (type == UnitType.Terran_Siege_Tank_Siege_Mode) {
            return UnitType.Terran_Siege_Tank_Tank_Mode;
        }
        if (type == UnitType.Zerg_Lurker_Egg) {
            return UnitType.Zerg_Lurker;
        }

        return type;
    }


    static class UListSet {
        private final ArrayList<UnitInfo> units = new ArrayList<>();

        Position getPos() {
            int avgPosX = 0;
            int avgPosY = 0;

            if (!units.isEmpty()) {
                for (UnitInfo u : units) {
                    avgPosX += u.getPos().x;
                    avgPosY += u.getPos().y;
                }
                avgPosX /= units.size();
                avgPosY /= units.size();
            }
            return new Position(avgPosX, avgPosY);
        }

        void add(UnitInfo uInfo) {
            if (!units.isEmpty()) {
                UnitInfo addUnit = null;
                for (UnitInfo up : units) {
                    if (up == uInfo) {
                        addUnit = up;
                        break;
                    }
                }
                if (addUnit != null && !units.contains(addUnit)) {
                    units.add(addUnit);
                }
            }
            else {
                units.add(uInfo);
            }
        }

        void del(Unit u) {
            if (!units.isEmpty()) {
                UnitInfo delUnit = null;
                for (UnitInfo up : units) {
                    if (up.getUnit() == u) {
                        delUnit = up;
                        break;
                    }
                }
                if (delUnit != null && units.contains(delUnit)) {
                    Utils.fastErase(units, units.indexOf(delUnit));
                }
            }
        }

        void del(UnitInfo u) {
            if (!units.isEmpty()) {
                UnitInfo delUnit = null;
                for (UnitInfo up : units) {
                    if (up == u) {
                        delUnit = up;
                        break;
                    }
                }
                if (delUnit != null && units.contains(delUnit)) {
                    Utils.fastErase(units, units.indexOf(delUnit));
                }
            }
        }

        int size() {
            return units.size();
        }

        ArrayList<UnitInfo> getUnits() {
            return units;
        }

        void clear() {
            this.units.clear();
        }

        boolean isEmpty() {
            return units.isEmpty();
        }

        UnitInfo getFrontUnitFromPosition(Position t) {
            int distance = Integer.MAX_VALUE;
            int temp = 0;
            UnitInfo frontUnit = null;

            for (UnitInfo u : units) {
                temp = BasicBotModule.Map.getMap().getPathLength(u.getPos(), t);
                if (temp >= 0 && temp < distance) {
                    frontUnit = u;
                    distance = temp;
                }
            }

            return frontUnit;
        }

        ArrayList<UnitInfo> getSortedUnitList(Position targetPos, boolean reverseOrder) {
            ArrayList<Pair<Integer, UnitInfo>> sortList = new ArrayList<>();

            for (UnitInfo t : units) {
                int tempDist = BasicBotModule.Map.getMap().getPathLength(t.getPos(), targetPos);

                if (tempDist < 0) {
                    continue;
                }

                sortList.add(new Pair<Integer, UnitInfo>(tempDist, t));
            }

            if (reverseOrder) {
                sortList.sort((o1, o2) -> {
                    if (o1.getFirst() > o2.getFirst()) return 1;
                    else if (o1.getFirst() < o2.getFirst()) return -1;
                    return 0;
                });
            }
            else {
                sortList.sort((o1, o2) -> {
                    if (o1.getFirst() < o2.getFirst()) return 1;
                    else if (o1.getFirst() > o2.getFirst()) return -1;
                    return 0;
                });
            }

            ArrayList<UnitInfo> sortedList = new ArrayList<>();

            for (Pair<Integer, UnitInfo> integerUnitInfoPair : sortList) {
                sortedList.add(integerUnitInfoPair.getSecond());
            }

            return sortedList;
        }
    }

    public HashMap<Unit, UnitInfo> getAllUnits() {
        return allUnits;
    }

    public HashMap<Unit, UnitInfo> getAllBuildings() {
        return allBuildings;
    }

    public HashMap<Integer, Pair<Integer, Position>> getAllSpells() {
        return allSpells;
    }

    public HashMap<UnitType, ArrayList<UnitInfo>> getUnitTypeMap() {
        return unitTypeMap;
    }

    public HashMap<UnitType, ArrayList<UnitInfo>> getBuildingTypeMap() {
        return buildingTypeMap;
    }

    public HashMap<UnitType, Integer> getCompletedCount() {
        return completedCount;
    }

    public HashMap<UnitType, Integer> getDestroyedCount() {
        return destroyedCount;
    }

    public HashMap<UnitType, Integer> getAllCount() {
        return allCount;
    }

}
