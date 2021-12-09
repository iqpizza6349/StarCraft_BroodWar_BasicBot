package com.tistory.workshop6349.examplebot;

import bwapi.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExampleBot extends DefaultBWListener {

    private BWClient bwClient;
    public static Game BroodWar;

    //////////////////Variables//////////////////

    public final UnitType drone = UnitType.Zerg_Drone;
    public final UnitType hatch = UnitType.Zerg_Hatchery;
    public final UnitType supply = UnitType.Zerg_Overlord;
    public final UnitType soldier = UnitType.Zerg_Zergling;
    public final UnitType producer = UnitType.Zerg_Spawning_Pool;

    public HashMap<Integer, BaseInfo> baseList = new HashMap<>();

    private BaseInfo spawnBase;
    private int minWorkersDesired;
    private int totalMinWorkers;
    private int gasWorkersDesired;
    private int totalGasWorkers;

    private int desHatch;
    private int curHatch;
    private boolean hasPool;
    private boolean startPool, startHatch;
    private boolean addingProduction;
    private int prodDrones;
    private int prodLings;
    private int allLings;
    private  int prodSupply;
    private boolean found;

    private final ArrayList<UnitInfo> zerglings = new ArrayList<>();
    private final ArrayList<UnitInfo> hydralisks = new ArrayList<>();
    private final ArrayList<UnitInfo> mutalisks = new ArrayList<>();
    private final ArrayList<UnitInfo> lurkers = new ArrayList<>();

    private TilePosition buildPool, buildHatch;

    private final HashMap<UnitType, ArrayList<Integer>> buildOrder = new HashMap<>();
    private final HashMap<UnitType, Boolean> unitTypes = new HashMap<>();

    private final ArrayList<Unit> availableScouts = new ArrayList<>();

    private int possible;
    private Position mainEnemyBase;
    private final ArrayList<UnitInfo> friendlyUnits = new ArrayList<>();
    private final ArrayList<UnitInfo> neutralUnits = new ArrayList<>();
    private final ArrayList<UnitInfo> enemyUnits = new ArrayList<>();
    private final ArrayList<PlayerInfo> players = new ArrayList<>();
    private final Fighter fighter = new Fighter();
    //////////////////Variables//////////////////


    public void run() {
        bwClient = new BWClient(this);
        bwClient.startGame();
    }

    @Override
    public void onStart() {
        BroodWar = bwClient.getGame();

        fighter.onStart();
        players.clear();
        for (Player p : BroodWar.enemies()) {
            if (p != null) {
                players.add(new PlayerInfo(p));
            }
        }

        BroodWar.setFrameSkip(0);
        BroodWar.setLocalSpeed(10);

        found = false;
        possible = 0;
        zerglings.clear();
        hydralisks.clear();
        mutalisks.clear();
        minWorkersDesired = 9;
        gasWorkersDesired = 4;
        totalMinWorkers = 0;
        totalGasWorkers = 0;

        baseList.clear();
        hasPool = false;
        desHatch = 1;
        curHatch = 1;
        prodDrones = 4;
        prodLings = 0;
        allLings = 0;

        addingProduction = false;
        startPool = false;
        startHatch = false;
        availableScouts.clear();
        prodSupply = 0;
        enemyUnits.clear();
        neutralUnits.clear();
        friendlyUnits.clear();

        for (int i = 0; i < 32767; i++) {
            baseList.put(i, new BaseInfo(false, false, false, false, null));
        }

        for (Unit u : BroodWar.getStaticNeutralUnits()) {
            BaseInfo base = baseList.get(u.getResourceGroup());
            if (u.getType().isMineralField()) {
                base.minerals.add(u);
            }
            else if (u.getType() == UnitType.Resource_Vespene_Geyser) {
                base.geysers.add(u);
            }
        }
        Unit u = BroodWar.getClosestUnit(BroodWar.self().getStartLocation().toPosition(), UnitFilter.IsMineralField);

        spawnBase = baseList.get(u.getResourceGroup());
        spawnBase.spawn = true;
        spawnBase.owned = true;

        for (TilePosition t : BroodWar.getStartLocations()) {
            if (!t.equals(BroodWar.self().getStartLocation())) {
                Unit m = BroodWar.getClosestUnit(t.toPosition(), UnitFilter.IsMineralField);
                if (m != null) {
                    BaseInfo b = findAssignedBase(m, false);
                    b.possible = true;
                    b.loc = t.toPosition();
                    possible++;
                }
            }
        }

    }

    @Override
    public void onEnd(boolean isWinner) {
        if (isWinner) {
            System.out.println("Victory");
            BroodWar.sendText("Fear for the swarm");
        }
        else {
            System.out.println("Defeat");
            BroodWar.sendText("Well then... more zerglings next time");
        }
        BroodWar.sendText("gg");
    }

    @Override
    public void onFrame() {
        drawInfo();
        fighter.drawInfo();

        for (Unit u : BroodWar.getAllUnits()) {
            UnitInfo ui = new UnitInfo(u);
            ui = createUnitInfo(u);
            ui.update();
        }

        for (UnitInfo u : zerglings) {
            if (u.unit.getPosition().getDistance(u.unit.getTargetPosition()) <= supply.sightRange()) {
                Unit possible = getClosestEnemy(u.unit, supply.sightRange());
                if (possible == null) {
                    removePossibleEnemyLocation(u.unit.getTargetPosition());
                }
            }
            processAttack(u.unit);
        }

        for (BaseInfo base : baseList.values()) {
            if (base.owned) {
                for (Unit u : base.mineralsWorkers) {
                    if (u.isIdle()) {
                        if (base.minerals.size() != 0) {
                            u.gather(base.minerals.get(0));
                        }
                    }
                }
                for (Unit u : spawnBase.gasWorkers) {
                    if (u.isIdle() && base.geysers.size() != 0) {
                        u.gather(base.geysers.get(0));
                    }
                }
            }

            // scout
            if (base.possible) {
                if (base.scout == null) {
                    if (availableScouts.size() != 0) {
                        base.scout = availableScouts.get(0);
                        removeFromAvailable(base.scout);
                        if (base.scout.isIdle()) {
                            base.scout.move(base.loc);
                        }
                    }
                }
                else {
                    if (base.scout.isIdle()) {
                        base.scout.move(base.loc);
                    }
                }
            }
        }

        for (Unit u : BroodWar.self().getUnits()) {
            if (u.getType() == supply) {
                checkBaseForEnemy(u);
            }

            if (u.getType() == hatch) {
                if (!hasPool) {
                    if ((getNumOfUnitType(drone) + prodDrones) < 9 && BroodWar.self().minerals() >= 50) {
                        u.train(drone);
                        break;
                    } else {
                        if (!startPool) {
                            buildPool = BroodWar.getBuildLocation(producer, spawnBase.mineralsWorkers.get(0).getTilePosition(), 64, true);
                            startPool = true;
                        }
                        if (BroodWar.self().minerals() >= 200) {
                            spawnBase.mineralsWorkers.get(0).build(producer, buildPool);
                        }
                        break;
                    }
                }

                // supply
                if (BroodWar.self().supplyUsed() + 8 >= BroodWar.self().supplyTotal()
                        && BroodWar.self().minerals() >= supply.mineralPrice()) {
                    if (u.getType().isResourceDepot() && prodSupply < desHatch && hasPool) {
                        u.train(supply);
                    }
                    break;
                }

                // units
                if (BroodWar.self().minerals() >= 50 && drone.supplyRequired() != 0) {
                    if (prodDrones < desHatch) {
                        if (findAssignedBase(u, false).mineralsWorkers.size() < (spawnBase.minerals.size() * 4)
                                && totalMinWorkers < minWorkersDesired) {
                            u.train(drone);
                        }
                    }
                    if (hasPool) {
                        u.train(soldier);
                    }
                }

                // pool
                if (findAssignedBase(u, false).mineralsWorkers.size() != 0) {
                    Unit worker = findAssignedBase(u, false).mineralsWorkers.get(0);

                    if (BroodWar.self().supplyTotal() >= 18
                            && !hasPool
                            && BroodWar.self().minerals() >= producer.mineralPrice()) {
                        if (!startPool) {
                            buildPool = BroodWar.getBuildLocation(producer, findAssignedBase(u, false).mineralsWorkers.get(0).getTilePosition(), 64, true);
                            startPool = true;
                        }
                        worker.build(producer, buildPool);
                    }
                }
            }

            // hatch
            BaseInfo b = findAssignedBase(u, false);
            if (b != null
                    && b.mineralsWorkers.size() != 0
                    && !spawnBase.constructing
                    && desHatch > curHatch
                    && BroodWar.self().minerals() >= hatch.mineralPrice()
                    && !addingProduction
                    && hasPool) {
                if (!startHatch) {
                    buildHatch = BroodWar.getBuildLocation(hatch, findAssignedBase(u, false).mineralsWorkers.get(1).getTilePosition(), 64, true);
                    startHatch = true;
                }
                findAssignedBase(u, false).mineralsWorkers.get(0).build(hatch, buildHatch);
            }
        }
    }

    @Override
    public void onUnitMorph(Unit unit) {
        if (unit.getBuildType() == supply) {
            prodSupply++;
        }
        else if (unit.getBuildType() == hatch) {
            addingProduction = true;
            curHatch++;
            startHatch = false;
            removeWorker(BroodWar.getClosestUnit(unit.getPosition(), UnitFilter.IsMineralField));
        }
        else if (unit.getBuildType() == producer) {
            hasPool = true;
            startPool = false;
        }
        else if (unit.getBuildType() == drone) {
            prodDrones++;
        }
        else if (unit.getBuildType() == soldier) {
            prodLings += 2;
        }

        if (unit.getBuildType().isBuilding() && unit.getBuildType() != hatch) {
            removeWorker(unit);
        }
    }

    @Override
    public void onUnitComplete(Unit unit) {
        if (unit.getPlayer() != BroodWar.self()) {
            return;
        }

        UnitInfo ui = new UnitInfo(unit);
        ui = createUnitInfo(unit);
        if (unit.getType().isWorker()) {
            prodDrones--;
            Unit m = BroodWar.getClosestUnit(unit.getPosition(), UnitFilter.IsMineralField);
            if (m != null) {
                BaseInfo base = baseList.get(m.getResourceGroup());
                base.mineralsWorkers.add(unit);
                totalMinWorkers++;
            }
        }
        else if (unit.getType().isBuilding()) {
            if (unit.getType().isResourceDepot()) {
                if (unit.getType() == hatch) {
                    addingProduction = false;
                }
                Unit m = BroodWar.getClosestUnit(unit.getPosition(), UnitFilter.IsMineralField);
                if (m != null) {
                    BaseInfo base = findAssignedBase(m, false);
                    if (base != null) {
                        base.constructing = false;
                        base.buildings.add(unit);
                        if (!base.owned) {
                            base.owned = true;
                            base.mainDepot = unit;
                        }
                    }
                }
            }
            else {
                Unit b = BroodWar.getClosestUnit(unit.getPosition(), UnitFilter.IsResourceDepot);
                if (b != null) {
                    BaseInfo base = findAssignedBase(b, false);
                    if (base != null) {
                        base.constructing = false;
                        base.buildings.add(unit);
                    }
                }
            }
        }
        else if (unit.getType() == soldier) {
            zerglings.add(ui);
            fighter.assignedSquad(ui);
            allLings++;
            prodLings--;
        }

        if (unit.getType() == supply) {
            availableScouts.add(unit);
            prodSupply--;

            if (BroodWar.self().supplyTotal() >= 200) {
                desHatch = 12;
            }
            else if (BroodWar.self().supplyTotal() >= 175) {
                desHatch = 11;
            }
            else if (BroodWar.self().supplyTotal() >= 150) {
                desHatch = 10;
            }
            else if (BroodWar.self().supplyTotal() >= 125) {
                desHatch = 9;
            }
            else if (BroodWar.self().supplyTotal() >= 100) {
                desHatch = 8;
            }
            else if (BroodWar.self().supplyTotal() >= 90) {
                desHatch = 7;
                minWorkersDesired = 32;
                gasWorkersDesired = 16;
            }
            else if (BroodWar.self().supplyTotal() >= 75) {
                desHatch = 6;
            }
            else if (BroodWar.self().supplyTotal() >= 60) {
                desHatch = 5;
                minWorkersDesired = 24;
                gasWorkersDesired = 12;
            }
            else if (BroodWar.self().supplyTotal() >= 50) {
                desHatch = 4;
            }
            else if (BroodWar.self().supplyTotal() >= 40) {
                desHatch = 3;
                minWorkersDesired = 16;
                gasWorkersDesired = 8;
            }
            else if (BroodWar.self().supplyTotal() >= 25) {
                desHatch = 2;
            }
            else {
                desHatch = 1;
                minWorkersDesired = 9;
                gasWorkersDesired = 4;
            }
        }
    }

    @Override
    public void onUnitDestroy(Unit unit) {

        UnitInfo ui = getInfo(unit);
        if (unit.getPlayer() == BroodWar.self()) {
            fighter.onUnitDestroy(ui);
        }

        if (unit.getType().isMineralField()) {
            BaseInfo base = baseList.get(unit.getResourceGroup());
            for (Unit m : base.minerals) {
                if (m == unit) {
                    base.minerals.remove(m);
                    return;
                }
            }
        }
        else if (unit.getType() == soldier) {
            allLings--;
            removeFromList(ui);
        }
        else if (unit.getType().isWorker()) {
            removeWorker(unit);
        }
        else if (unit.getType().isBuilding()) {
            if (unit.getType() == producer) {
                hasPool = false;
            }
            for (BaseInfo base : baseList.values()) {
                for (Unit building : base.buildings) {
                    if (building == unit) {
                        base.buildings.remove(building);
                        return;
                    }
                }
            }
        }

        else if (unit.getType() == supply) {
            if (BroodWar.self().supplyTotal() >= 175) {
                desHatch = 8;
            }
            else if (BroodWar.self().supplyTotal() >= 150) {
                desHatch = 7;
            }
            else if (BroodWar.self().supplyTotal() >= 125) {
                desHatch = 6;
            }
            else if (BroodWar.self().supplyTotal() >= 100) {
                desHatch = 5;
            }
            else if (BroodWar.self().supplyTotal() >= 90) {
                minWorkersDesired = 32;
                gasWorkersDesired = 16;
            }
            else if (BroodWar.self().supplyTotal() >= 75) {
                desHatch = 4;
            }
            else if (BroodWar.self().supplyTotal() >= 60) {
                minWorkersDesired = 24;
                gasWorkersDesired = 12;
            }
            else if (BroodWar.self().supplyTotal() >= 50) {
                desHatch = 3;
            }
            else if (BroodWar.self().supplyTotal() >= 40) {
                minWorkersDesired = 16;
                gasWorkersDesired = 8;
            }
            else if (BroodWar.self().supplyTotal() >= 25) {
                desHatch = 2;
            }
            else {
                desHatch = 1;
                minWorkersDesired = 8;
                gasWorkersDesired = 4;
            }
            for (Unit s : availableScouts) {
                if (s == unit) {
                    availableScouts.remove(s);
                    return;
                }
            }
        }

        removeFromList(ui);
    }

    @Override
    public void onUnitDiscover(Unit unit) {
        UnitInfo ui = getInfo(unit);

        if (ui == null) {
            ui = createUnitInfo(unit);
        }
        else {
            ui.update();
        }

        if (unit.getPlayer() == BroodWar.self()) {
            return;
        }
        if (unit.getType().isBuilding() && BroodWar.self().isEnemy(unit.getPlayer())) {
            Unit m = BroodWar.getClosestUnit(unit.getPosition(), UnitFilter.IsMineralField);
            if (m != null) {
                BaseInfo b = findAssignedBase(m, false);
                for (BaseInfo a : baseList.values()) {
                    b.possible = false;
                }
                b.possible = true;
                if (!found) {
                    mainEnemyBase = b.loc;
                    found = true;
                }
            }
        }
    }

    public void drawInfo() {

    }

    public BaseInfo findAssignedBase(Unit unit, boolean ownedOnly) {

        if (unit == null) {
            BroodWar.printf("NULL BASE REF");
            return null;
        }

        Unit u = BroodWar.getClosestUnit(unit.getPosition(), UnitFilter.IsMineralField);
        if (u == null) {
            u = BroodWar.getClosestUnit(unit.getPosition(), UnitFilter.IsBuilding);
        }
        if (u == null) {
            u = BroodWar.getClosestUnit(unit.getPosition());
        }

        for (BaseInfo base : baseList.values()) {
            for (Unit w : base.mineralsWorkers) {
                if (w == u) {
                    if (ownedOnly && base.owned) {
                        return base;
                    }
                    return base;
                }
            }

            for (Unit w : base.gasWorkers) {
                if (w == u) {
                    if (ownedOnly && base.owned) {
                        return base;
                    }
                    return base;
                }
            }

            for (Unit b : base.buildings) {
                if (b == u) {
                    if (ownedOnly && base.owned) {
                        return base;
                    }
                    return base;
                }
            }

            for (Unit m : base.minerals) {
                if (m == u) {
                    if (ownedOnly && base.owned) {
                        return base;
                    }
                    return base;
                }
            }
        }

        return null;
    }

    public BaseInfo findAssignedBase(Unit unit) {
        return findAssignedBase(unit, false);
    }

    public int getNumOfUnitType(UnitType unitType) {
        int i = 0;
        for (Unit u : BroodWar.self().getUnits()) {
            if (u.getType() == unitType) {
                i++;
            }
        }
        return i;
    }

    public int getNumOfOwnedBases() {
        int i = 0;
        for (Map.Entry<Integer, BaseInfo> base : baseList.entrySet()) {
            if (base.getValue().owned) {
                i++;
            }
        }
        return i;
    }

    public boolean removeWorker(Unit unit) {
        for (Map.Entry<Integer, BaseInfo> base : baseList.entrySet()) {
            for (Unit u : base.getValue().mineralsWorkers) {
                if (u == unit) {
                    base.getValue().mineralsWorkers.remove(u);
                    totalMinWorkers--;
                    return true;
                }
            }
            for (Unit u : base.getValue().gasWorkers) {
                if (u == unit) {
                    base.getValue().gasWorkers.remove(u);
                    return true;
                }
            }
        }
        return false;
    }

    public void removePossibleEnemyLocation(Position pos) {
        if (pos.equals(BroodWar.self().getStartLocation().toPosition())) {
            return;
        }

        BaseInfo b = findAssignedBase(BroodWar.getClosestUnit(pos, UnitFilter.IsMineralField));
        if (b != null && b.possible) {
            b.possible = false;
            possible--;
            if (possible == 1 && !found) {
                mainEnemyBase = getFinalPosition();
            }
            if (b.scout != null) {
                Unit u = BroodWar.getClosestUnit(b.scout.getPosition(), UnitFilter.IsBuilding);
                if (u != null) {
                    b.scout.move(u.getPosition());
                }
                else {
                    b.scout.move(BroodWar.self().getStartLocation().toPosition());
                }
                availableScouts.add(b.scout);
                b.scout = null;
            }
        }
    }

    public boolean removeFromAvailable(Unit s) {
        for (Unit unit : availableScouts) {
            if (unit.getID() == s.getID()) {
                availableScouts.remove(unit);
                return true;
            }
        }
        return false;
    }

    public Position getFinalPosition() {
        for (Map.Entry<Integer, BaseInfo> base : baseList.entrySet()) {
            if (base.getValue().possible) {
                found = true;
                return base.getValue().loc;
            }
        }
        return Position.None;
    }

    public boolean checkBaseForEnemy(Unit u) {
        boolean success = false;
        Position pos = u.getTargetPosition();
        if (u.getPosition().getDistance(u.getTargetPosition()) <= supply.sightRange()) {
            Unit possible = getClosestEnemy(u, supply.sightRange());
            if (possible == null) {
                removePossibleEnemyLocation(u.getTargetPosition());
                success = true;
            }
        }

        return success;
    }

    public Unit getClosestEnemy(Unit unit, int r) {
        if (unit == null) {
            return null;
        }

        Unit nearestEnemy = null;
        double minDist = Double.MAX_VALUE;
        for (Unit enemy : unit.getUnitsInRadius(r, UnitFilter.IsBuilding)) {
            if (unit.getPlayer() == BroodWar.self()) {
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

    public void processAttack(Unit unit) {
        if (unit.isAttackFrame() || unit.getTarget() != null) {
            return;
        }
        if (unit.getHitPoints() < 12) {
            Unit t = BroodWar.getClosestUnit(unit.getPosition(), UnitFilter.IsBuilding);
            if (t != null) {
                unit.move(t.getPosition());
            }
            else {
                unit.move(BroodWar.self().getStartLocation().toPosition());
            }
        }
        if (unit.isMoving() && unit.getTarget() == null) {
            Unit t = getClosestEnemy(unit, 9999);
            if (t != null && unit.canAttackUnit(t)) {
                unit.attack(t);
                return;
            }
        }
        if (unit.getTarget() != null && unit.getTarget().getType().isBuilding()) {
            Unit t = getClosestEnemy(unit, 9999);
            if (t != null && unit.canAttack(t)) {
                if (!t.getType().isWorker()) {
                    unit.attack(t.getPosition());
                    return;
                }
                unit.attack(t.getPosition());
                return;
            }
        }
        if (unit.isIdle()) {
            Unit t = getClosestEnemy(unit, 9999);
            if (t != null && t.getDistance(unit) >= 64 && unit.canAttack(t)) {
                if (t.getType().isBuilding()) {
                    unit.attack(t.getPosition());
                }
            }
            else {
                for (BaseInfo b : baseList.values()) {
                    if (b.possible) {
                        unit.attack(b.loc);
                    }
                }
            }
        }
    }

    public UnitInfo getInfo(Unit unit) {
        for (UnitInfo ui : friendlyUnits) {
            if (ui.unit.getID() == unit.getID()) {
                return ui;
            }
        }
        for (UnitInfo ui : neutralUnits) {
            if (ui.unit.getID() == unit.getID()) {
                return ui;
            }
        }
        for (UnitInfo ui : enemyUnits) {
            if (ui.unit.getID() == unit.getID()) {
                return ui;
            }
        }
        return null;
    }

    public PlayerInfo getInfo(Player p) {
        for (PlayerInfo pi : players) {
            if (pi.player == p) {
                return pi;
            }
        }
        return null;
    }

    public BaseInfo getInfo(Position p) {
        for (BaseInfo b : baseList.values()) {
            if (b.loc.equals(p)) {
                return b;
            }
        }
        return null;
    }

    public boolean removeFromList(UnitInfo unitInfo) {
        for (UnitInfo ui : friendlyUnits) {
            if (ui == unitInfo) {
                friendlyUnits.remove(ui);
                // unitInfo = null;
                return true;
            }
        }

        for (UnitInfo ui : neutralUnits) {
            if (ui == unitInfo) {
                neutralUnits.remove(ui);
                // unitInfo = null;
                return true;
            }
        }

        for (UnitInfo ui : enemyUnits) {
            if (ui == unitInfo) {
                enemyUnits.remove(ui);
                // unitInfo = null;
                return true;
            }
        }

        for (UnitInfo ui : zerglings) {
            if (ui == unitInfo) {
                zerglings.remove(ui);
                // unitInfo = null;
                return true;
            }
        }

        BroodWar.printf("ERROR with remove from list (UnitInfo)");
        return false;
    }

    public UnitInfo createUnitInfo(Unit unit) {
        UnitInfo ui = new UnitInfo(unit);

        if (unit.getPlayer() == BroodWar.self()) {
            friendlyUnits.add(friendlyUnits.size(), ui);
        }
        else if (unit.getPlayer() == BroodWar.neutral()) {
            neutralUnits.add(neutralUnits.size(), ui);
        }
        else {
            enemyUnits.add(enemyUnits.size(), ui);
        }
        return ui;
    }

}
