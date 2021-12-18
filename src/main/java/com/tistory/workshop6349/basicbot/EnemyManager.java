package com.tistory.workshop6349.basicbot;

import bwapi.*;

import java.util.ArrayList;
import java.util.List;

public class EnemyManager {

    private static final EnemyManager ENEMY_MANAGER = new EnemyManager();
    public static EnemyManager getInstance() {
        return ENEMY_MANAGER;
    }

    //enemy basics
    public Race race = Race.Unknown;
    public String name = "enemies";
    public String score = "0-0";
    public int[] result = new int[8];
    public ArrayList<String> history = new ArrayList<>();

    //enemy building positions

    public ArrayList<Position> positions = new ArrayList<>();
    public ArrayList<Position> main_pos = new ArrayList<>();
    public int sem = -1;								// single enemy main (number)
    public ArrayList<TilePosition> incomplete_def = new ArrayList<>();

    //enemy supply of various kinds

    public int supply_work = 0;
    public int supply_army = 0;
    public int supply_small = 0;
    public int supply_large = 0;
    public int supply_air = 0;
    public int supply_cloak = 0;
    public int air_percentage = 0;
    public int small_percentage = 0;
    public int cloak_percentage = 0;

    //enemy units in own base

    public ArrayList<Unit> intruders = new ArrayList<>();	//enemy units in defense range
    public int intruder_totstr = 0;					        //enemy unit evaluated strength
    public int intruder_airstr = 0;					        //enemy unit evaluated strength
    public ArrayList<Unit> near_bunkers = new ArrayList<>();
    public int supply_near_bunkers = 0;

    //enemy defense and production building counts

    public int grddef_count = 0;
    public int airdef_count = 0;
    public int produc_count = 0;				            //all prodction buildings
    public int airprod_count = 0;				            //spaceport, stargate, spires
    public int mainbuild_count = 0;				            //hatcheries, comcenters, nexi

    //enemy tech

    public boolean has_covertops = false;
    public boolean has_physiclab = false;
    public boolean has_nukesilo = false;

    public boolean has_pool = false;
    public boolean has_hydraden = false;
    public boolean has_lair = false;
    public boolean has_hive = false;
    public boolean has_defmound = false;
    public boolean has_ultracav = false;
    public boolean has_lurkers = false;

    public boolean has_forge = false;
    public boolean has_robo = false;
    public boolean has_archives = false;
    public boolean has_tribunal = false;
    public boolean has_fleetbea = false;
    public int time_lair_reaction = 7200;

    //suspected or recognized enemy strategies

    public boolean goes_megamacro = false;

    //target lists

    public ArrayList<Unit> tlg = new ArrayList<>();	    //target list ground
    public ArrayList<Unit> tlgg = new ArrayList<>();	//target list ground to ground
    public ArrayList<Unit> tlga = new ArrayList<>();	//target list ground to air
    public ArrayList<Unit> tlgs = new ArrayList<>();	//target list ground special
    public ArrayList<Unit> tlgc = new ArrayList<>();	//target list ground cloaked
    public ArrayList<Unit> tlgw = new ArrayList<>();	//target list ground worker
    public ArrayList<Unit> tlgm = new ArrayList<>();	//target list ground small/medium
    public ArrayList<Unit> tlgl = new ArrayList<>();	//target list ground large
    public ArrayList<Unit> tla = new ArrayList<>();	    //target list air
    public ArrayList<Unit> tlag = new ArrayList<>();	//target list air to ground
    public ArrayList<Unit> tlaa = new ArrayList<>();	//target list air to air
    public ArrayList<Unit> tlas = new ArrayList<>();	//target list air special
    public ArrayList<Unit> tlac = new ArrayList<>();	//target list air cloaked

    public ArrayList<Unit> tleggla = new ArrayList<>();	//target list egg/larva
    public ArrayList<Unit> tlflare = new ArrayList<>();	//target list flare
    public ArrayList<Unit> tllockd = new ArrayList<>();	//target list lock down
    public ArrayList<Unit> tlyama = new ArrayList<>();	//target list yamato gun
    public ArrayList<Unit> tldefg = new ArrayList<>();	//target list ground defense buildings
    public ArrayList<Unit> tldefa = new ArrayList<>();	//target list air defense buildings
    public ArrayList<Unit> tlself = new ArrayList<>();	//target list to force attacking own units

    public int target_count = 0;

    public ArrayList<Position> unclaimed_expo_pos = new ArrayList<>();

    public ArrayList<UnitInfo> siegetanks = new ArrayList<>();

    private boolean need_race = false;
    private ArrayList<Integer> ids = new ArrayList<>();

    public EnemyManager() {
        if (BasicBotAI.BroodWar.enemies().size() == 1) {
            Race r = BasicBotAI.BroodWar.enemy().getRace();
            if (r == Race.Terran || r == Race.Protoss || r == Race.Zerg) {
                race = r;
            }
            else {
                need_race = true;
            }
            name = BasicBotAI.BroodWar.enemy().getName();
        }
        else {
            name = BasicBotAI.BroodWar.enemies().size() + " enemies";
        }
    }

    public void appendUnit(Unit unit) {
        if (!unit.exists()) {
            return;
        }

        if (need_race) {
            determineRace(unit);
        }

        if (!ids.contains(unit.getID())) {
            return;
        }

        ids.add(unit.getID());
        UnitType t = unit.getType();
        Position p = unit.getPosition();

        if (t.supplyRequired() > 0) {
            changeSupply(t, t.supplyRequired());
            checkTechUnit(t);
            appendUnitInfo(unit);
        }
        if (t.isBuilding()) {
            if (!positions.contains(p)) {
                positions.add(p);
            }
            if (t.isResourceDepot()) {
                determineMain(unit);
            }
            if (!unit.isLifted()) {
                MapUtil.setMapArea(BasicMap.buildMap_Var, unit, false);
            }
            checkTech(t, +1);
            checkDefense(unit, +1);
            checkTimeLair(unit, false);
        }

    }

    public void removeUnit(Unit unit) {
        ids.remove(unit.getID());
        UnitType t = unit.getType();
        if (t.supplyRequired() > 0) {
            changeSupply(t, -1 * t.supplyRequired());
            removeUnitInfo(unit);
        }
        if (t.isBuilding()) {
            positions.remove(unit.getPosition());
            if (!unit.isLifted()) {
                MapUtil.clearBuildArea(BasicMap.buildMap_Var, BasicMap.buildMap_Fix, unit);
            }
            checkTech(t, -1);
            checkDefense(unit, -1);
        }
        
    }

    public void changeUnit(Unit unit) {
        if (!unit.exists()) {
            return;
        }

        UnitType t = unit.getType();
        Position p = unit.getPosition();

        if (t.isBuilding()) {
            if (t == UnitType.Resource_Vespene_Geyser) {
                positions.remove(p);
                return;
            }
            if (t == UnitType.Terran_Refinery
                    || t == UnitType.Protoss_Assimilator) {
                positions.add(p);
                return;
            }
            if (t == UnitType.Zerg_Sunken_Colony
                    || t == UnitType.Zerg_Spore_Colony) {
                checkDefense(unit, +1);
                if (!positions.contains(p)) {
                    positions.add(p);
                    MapUtil.setMapArea(BasicMap.buildMap_Var, unit, false);
                }
                return;
            }
            if (t == UnitType.Zerg_Lair
                    || t == UnitType.Zerg_Hive
                    || t == UnitType.Zerg_Greater_Spire) {
                checkTech(t, +1);
                checkTimeLair(unit, true);
                if (!positions.contains(p)) {
                    positions.add(p);
                    MapUtil.setMapArea(BasicMap.buildMap_Var, unit, false);
                }
                return;
            }
            
            if (true) {
                checkTech(t, +1);
                positions.add(p);
                MapUtil.setMapArea(BasicMap.buildMap_Var, unit, false);
                supply_work = BotUtil.safeSum(supply_work, -2);
                return;
            }
        }

        if (t == UnitType.Terran_Siege_Tank_Siege_Mode
                || t == UnitType.Terran_Siege_Tank_Tank_Mode
                || t == UnitType.Zerg_Guardian
                || t == UnitType.Zerg_Devourer
                || t == UnitType.Zerg_Lurker_Egg) {
            return;
        }

        if (t == UnitType.Zerg_Lurker) {
            supply_army += 2;
            supply_large += 2;
            supply_cloak += 4;
        }

        if (t.supplyRequired() > 0) {
            changeSupply(t, t.spaceRequired());
        }
    }


    public void update() {
        if (supply_army > 0) {
            air_percentage = 100 * supply_air / supply_army;
            cloak_percentage = 100 * supply_cloak / supply_army;
            small_percentage = 100 * supply_small /  supply_army;
        }
        else {
            air_percentage = 0;
            cloak_percentage = 0;
            small_percentage = 0;
        }

        EnemyUtil.collectIntruders(intruders, BasicMap.defenseMap);
        intruder_totstr = EnemyUtil.evaluateStrength(intruders);
        intruder_airstr = EnemyUtil.evaluateAirStrength(intruders);

        EnemyUtil.collectIntruders(near_bunkers, BasicMap.bunkerMap);
        supply_near_bunkers = EnemyUtil.countSupply(near_bunkers);
        checkIncompleteDefense();

        updateList(siegetanks);
    }

    public void determineRace(Unit unit) {
        Race r = unit.getType().getRace();
        if (r == Race.Terran
                || r == Race.Protoss
                || r == Race.Zerg) {
            race = r;
            need_race = false;
        }

    }

    public void correct() {
        ArrayList<Position> temp = new ArrayList<>();

        for (Position position : positions) {
            TilePosition myTile = position.toTilePosition();
            if (BasicBotAI.BroodWar.isVisible(myTile)) {
                boolean found = false;
                for (Unit u : BasicBotAI.BroodWar.getUnitsOnTile(myTile)) {
                    if (u.getType().isBuilding()
                            && u.getPlayer().isEnemy(BasicBotAI.BroodWar.self())) {
                        found = true;
                    }
                }
                if (!found) {
                    clearDeductedBuildTiles(position);
                    temp.add(position);
                }
            }
        }

        for (Position p : temp) {
            positions.remove(p);
        }
    }

    public void determineMain(Unit unit) {
        for (int i = 0; i < BasicMap.mn; i++) {
            if (BotUtil.sqDist(unit.getPosition(), BasicMap.mainPos[i]) < 256) {
                if (BasicBotAI.BroodWar.enemies().size() == 1 && sem == -1) {
                    sem = i;
                    BasicMap.unScouted.clear();
                    BasicMap.enemyStart = BasicMap.mainTiles[i];
                    BasicMap.enemyMain = BasicMap.mainPos[i];
                    BasicMap.enemyNatural = BasicMap.naturalPos[i];
                    BasicMap.entranceCircleIncrement = MapUtil.getDirection8(
                            BasicMap.mainPos[i],
                            new Position(BasicMap.mainChokePos[i].x * 3, BasicMap.mainChokePos[i].y * 3)
                    );
                }
                if (BasicBotAI.BroodWar.enemies().size() >= 2 || main_pos.isEmpty()) {
                    main_pos.add(unit.getPosition());
                }
                return;
            }
        }
    }

    public void changeSupply(UnitType t, int ds) {
        if (t.isWorker()) {
            supply_work = BotUtil.safeSum(supply_work, ds);
        }
        else {
            supply_army = BotUtil.safeSum(supply_army, ds);
            if (t.isFlyer()) {
                supply_air = BotUtil.safeSum(supply_air, ds);
            }
            else if (isSmall(t)) {
                supply_small = BotUtil.safeSum(supply_small, ds);
            }
            else {
                supply_large = BotUtil.safeSum(supply_large, ds);
            }

            if (canCloak(t)) {
                supply_cloak = BotUtil.safeSum(supply_cloak, ds);
            }
        }
    }

    public boolean isSmall(UnitType t) {
        return (t.size() == UnitSizeType.Small);
    }

    public boolean canCloak(UnitType t) {
        if (t.isBuilding()) {
            return false;
        }
        return (t == UnitType.Protoss_Dark_Templar
                || t == UnitType.Protoss_Arbiter
                || t == UnitType.Terran_Ghost 
                || t == UnitType.Terran_Wraith
                || t == UnitType.Zerg_Lurker);
    }

    public void clearDeductedBuildTiles(Position p) {
        if (p == Position.None) {
            return;
        }
        if (p.x % 32 == 0 && p.y % 32 == 16) {
            MapUtil.clearBuildArea(BasicMap.buildMap_Var, BasicMap.buildMap_Fix, p.x / 32 - 2, p.y / 32 - 1, 4, 3);
            return;
        }
        if (p.x % 32 == 16 && p.y % 32 == 0) {
            MapUtil.clearBuildArea(BasicMap.buildMap_Var, BasicMap.buildMap_Fix, p.x / 32 - 1, p.y / 32 - 1, 3, 2);
            return;
        }
        if (p.x % 32 == 0 && p.y % 32 == 0) {
            MapUtil.clearBuildArea(BasicMap.buildMap_Var, BasicMap.buildMap_Fix, p.x / 32 - 1, p.y / 32 - 1, 2, 2);
        }
    }

    public void checkDefense(Unit unit, int dz) {
        switch (unit.getType()) {
            case Terran_Bunker: handleDefense(unit, dz, 60, true, true, false); break;
            case Terran_Missile_Turret:
            case Zerg_Spore_Colony:
                handleDefense(unit, dz, 68, false, true, true); break;
            case Zerg_Sunken_Colony: handleDefense(unit, dz, 68, true, false, false); break;
            case Protoss_Photon_Cannon: handleDefense(unit, dz, 68, true, true, true); break;
        }
    }

    public void handleDefense(Unit u, int dz, int size, boolean isGrid, boolean isAir, boolean isDet) {
        Position p = u.getPosition();
        TilePosition t = u.getTilePosition();

        if (u.isCompleted()) {
            if (isGrid && p.isValid(BasicBotAI.BroodWar)) {
                MapInfluence.setInfluence(ThreatManager.groundDef, p.x, p.y, size, dz);
                grddef_count = BotUtil.safeSum(grddef_count, dz);
            }
            if (isAir && p.isValid(BasicBotAI.BroodWar)) {
                MapInfluence.setInfluence(ThreatManager.airDef, p.x, p.y, size, dz);
                airdef_count = BotUtil.safeSum(airdef_count, dz);
            }
            if (isDet && p.isValid(BasicBotAI.BroodWar)) {
                MapInfluence.setInfluence(ThreatManager.staDet, p.x, p.y, size, dz);
            }
        }
        else {
            if (dz > 0 && t.isValid(BasicBotAI.BroodWar)) {
                incomplete_def.add(t);
            }
        }

    }

    public void checkIncompleteDefense() {
        ArrayList<TilePosition> temp = new ArrayList<>();

        for (TilePosition t : incomplete_def) {
            if (BasicBotAI.BroodWar.isVisible(t)) {
                List<Unit> myList = BasicBotAI.BroodWar.getUnitsOnTile(t);
                for (Unit u : myList) {
                    if (u.isCompleted()
                            && u.getType().isBuilding()
                            && !u.isFlying()) {
                        checkDefense(u, +1);
                        temp.add(t);
                    }
                }
                if (myList.isEmpty()) {
                    incomplete_def.remove(t);
                    return;
                }
            }
        }
        for (TilePosition tilePosition : temp) {
            incomplete_def.remove(tilePosition);
        }
    }


    public void checkTech(UnitType type, int dz) {
        if (type.canProduce()) {
            produc_count = BotUtil.safeSum(produc_count, dz);
        }
        if (type.isResourceDepot()) {
            mainbuild_count = BotUtil.safeSum(mainbuild_count, dz);
        }
        switch (type) {
            case Zerg_Spire:
            case Zerg_Greater_Spire:
            case Terran_Starport:
            case Protoss_Stargate:
                airprod_count = BotUtil.safeSum(airprod_count, dz);
                break;
        }
        if (dz < 0) {
            return;
        }

        switch (type) {
            case Terran_Covert_Ops: has_covertops = true; break;
            case Terran_Physics_Lab: has_physiclab = true; break;
            case Terran_Nuclear_Silo: has_nukesilo = true; has_covertops = true; break;

            case Zerg_Spawning_Pool: has_pool = true; break;
            case Zerg_Hydralisk_Den: has_hydraden = true; break;
            case Zerg_Lair: has_lair = true; break;
            case Zerg_Hive: has_lair = true; has_hive = true; break;
            case Zerg_Defiler_Mound: has_defmound = true; has_hive = true; break;
            case Zerg_Ultralisk_Cavern: has_ultracav = true; has_hive = true; break;

            case Protoss_Forge:
            case Protoss_Photon_Cannon:
                has_forge = true; break;
            case Protoss_Robotics_Facility:
            case Protoss_Observatory:
            case Protoss_Robotics_Support_Bay:
                has_robo = true; break;
            case Protoss_Templar_Archives: has_archives = true; break;
            case Protoss_Arbiter_Tribunal: has_tribunal = true; has_archives = true; break;
            case Protoss_Fleet_Beacon: has_fleetbea = true; break;
        }
    }

    public void checkTechUnit(UnitType type) {
        if (race == Race.Zerg) {
            if (!has_lurkers
                    && (type == UnitType.Zerg_Lurker
                    || type == UnitType.Zerg_Lurker_Egg)) {
                has_lurkers = true;
                has_hydraden = true;
                has_lair = true;
            }
        }
    }

    public void appendUnitInfo(Unit u) {
        switch (u.getType()) {
            case Terran_Siege_Tank_Tank_Mode:
            case Terran_Siege_Tank_Siege_Mode:
                siegetanks.add(new UnitInfo(u));
                break;
        }
    }

    public void removeUnitInfo(Unit unit) {
        switch (unit.getType()) {
            case Terran_Siege_Tank_Tank_Mode:
            case Terran_Siege_Tank_Siege_Mode:
                removeFrom(siegetanks, unit);
                break;
        }
    }

    public void removeFrom(ArrayList<UnitInfo> list, Unit u) {
        ArrayList<UnitInfo> temp = new ArrayList<>();
        for (UnitInfo unitInfo : list) {
            if (unitInfo.unit == u) {
                temp.add(unitInfo);
            }
        }
        for (UnitInfo ui : temp) {
            list.remove(ui);
        }
    }

    public void updateList(ArrayList<UnitInfo> list) {
        for (UnitInfo u : list) {
            if (u.unit.exists()) {
                u.pos = u.unit.getPosition();
            }
            else if (!BotUtil.isNone(u.pos)
                    && BasicBotAI.BroodWar.isVisible(u.pos.x / 32, u.pos.y / 32)) {
                u.pos = Position.None;
            }
        }
    }

    public void checkTimeLair(Unit u, boolean justChange) {
        if (u.getType() == UnitType.Zerg_Lair) {
            int t = BasicBotAI.BroodWar.getFrameCount() * 42 / 1000;
            if (justChange && !u.isCompleted()) {
                t += 63 + 40;
            }
            if (justChange && u.isCompleted()) {
                t += 40;
            }
            if (!justChange && !u.isCompleted()) {
                t += 30 + 40;
            }
            time_lair_reaction = t;
        }
    }


    public int getIntruderCount(UnitType type) {
        int i = 0;
        for (Unit u : intruders) {
            if (u.getType() == type) {
                i++;
            }
        }
        return i;
    }

}
