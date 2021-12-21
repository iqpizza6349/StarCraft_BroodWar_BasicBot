package com.tistory.workshop6349.basicbot;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

import java.util.ArrayList;

public class BasicBuild {

    public static class Resource {
        public Resource(Unit unit) {
            this.unit = unit;
            this.tile = unit.getTilePosition();
            this.id = unit.getID();
            this.isIsland = false;
            this.isOwned = false;
            this.isEnemy = false;
            this.scvCount = 0;
        }

        public Unit unit;
        public TilePosition tile;
        public int id;
        public boolean isIsland;
        public boolean isOwned;
        public boolean isEnemy;
        public int scvCount;
    }

    // building lists
    public static ArrayList<Unit> commandCenters = new ArrayList<>();
    public static ArrayList<Unit> refineries = new ArrayList<>();

    public static ArrayList<Unit> depots = new ArrayList<>();
    public static ArrayList<Unit> academies = new ArrayList<>();
    public static ArrayList<Unit> armories = new ArrayList<>();
    public static ArrayList<Unit> bunkers = new ArrayList<>();
    public static ArrayList<Unit> turrets = new ArrayList<>();

    public static ArrayList<Unit> barracks = new ArrayList<>();
    public static ArrayList<Unit> factories = new ArrayList<>();
    public static ArrayList<Unit> starports = new ArrayList<>();
    public static ArrayList<Unit> engineerBays = new ArrayList<>();
    public static ArrayList<Unit> scienceFacs = new ArrayList<>();

    public static ArrayList<Unit> comsatStation = new ArrayList<>();
    public static ArrayList<Unit> nuclearSilos = new ArrayList<>();
    public static ArrayList<Unit> machineShops = new ArrayList<>();
    public static ArrayList<Unit> controlTowers = new ArrayList<>();
    public static ArrayList<Unit> covertOps = new ArrayList<>();
    public static ArrayList<Unit> physicsLabs = new ArrayList<>();

    public static ArrayList<Unit> buildings = new ArrayList<>();
    public static ArrayList<Unit> smallBuildings = new ArrayList<>();
    public static ArrayList<UnitType> plannedBuildings = new ArrayList<>();

    public static int plannedBuildQueue = 0;
    public static int lockingTime = 300;
    public static TilePosition blockedTile = TilePosition.None;
    public static boolean scannedBlockedTile = false;

    // resource lists
    public static ArrayList<Resource> minerals = new ArrayList<>();
    public static ArrayList<Resource> geysers = new ArrayList<>();


    public static void appendBuilding(Unit unit) {
        if (!unit.getType().isBuilding()) {
            return;
        }

        switch (unit.getType()) {
            case Terran_Command_Center:
                commandCenters.add(unit);
                break;
            case Terran_Refinery:
                refineries.add(unit);
                break;

            case Terran_Supply_Depot:
                depots.add(unit);
                break;
            case Terran_Academy:
                academies.add(unit);
                break;
            case Terran_Armory:
                armories.add(unit);
                break;
            case Terran_Bunker:
                bunkers.add(unit);
                break;
            case Terran_Missile_Turret:
                turrets.add(unit);
                break;

            case Terran_Barracks:
                barracks.add(unit);
                break;
            case Terran_Factory:
                factories.add(unit);
                break;
            case Terran_Starport:
                starports.add(unit);
                break;
            case Terran_Engineering_Bay:
                engineerBays.add(unit);
                break;
            case Terran_Science_Facility:
                scienceFacs.add(unit);
                break;

            case Terran_Comsat_Station:
                comsatStation.add(unit);
                break;
            case Terran_Nuclear_Silo:
                nuclearSilos.add(unit);
                break;
            case Terran_Machine_Shop:
                machineShops.add(unit);
                break;
            case Terran_Control_Tower:
                controlTowers.add(unit);
                break;
            case Terran_Covert_Ops:
                covertOps.add(unit);
                break;
            case Terran_Physics_Lab:
                physicsLabs.add(unit);
                break;
        }
        if (BotUtil.isNone(BasicMap.enemyStart) || BotUtil.sqDist(BasicMap.enemyStart, unit.getTilePosition()) > 1024) {
            // excluding proxy buildings
            buildings.add(unit);
        }

        if (!unit.isLifted()) {
            if (unit.getType().canBuildAddon()) {
                TilePosition t = unit.getTilePosition();
                MapUtil.setMapArea(BasicMap.buildMap_Var, t.x + 4, t.y + 1, 2, 2, false);
            }
            MapUtil.setMapArea(BasicMap.buildMap_Var, unit, false);
        }
        if (!unit.getType().isAddon() && !unit.isLifted()) {
            changeInfluence(unit, -1);
        }
    }

    public static void changeInfluence(Unit unit, int dz) {
        if (unit.getType().tileWidth() == 4) {
            MapInfluence.setInfluenceL(BasicMap.defenseMap, unit.getTilePosition(), dz);
        }
        else {
            MapInfluence.setInfluenceM(BasicMap.defenseMap, unit.getTilePosition(), dz);
        }
    }

    public static void changeInfBunker(Unit unit, int dz) {
        MapInfluence.setInfluenceM(BasicMap.bunkerMap, unit.getTilePosition(), dz);
    }

    public static void changeInfTurret(Unit unit, int dz) {
        MapInfluence.setInfluenceM(BasicMap.turretMap, unit.getTilePosition(), dz);
    }

    public void lift(Unit unit) {
        MapUtil.setMapArea(BasicMap.buildMap_Var, unit, true);
        MapInfluence.setInfluenceL(BasicMap.defenseMap, unit.getTilePosition(), -1);
        unit.lift();
    }

    public void land(Unit unit, TilePosition tilePosition) {
        MapUtil.setMapArea(BasicMap.buildMap_Var, tilePosition.x, tilePosition.y, 4, 3, false);
        MapInfluence.setInfluenceL(BasicMap.defenseMap, tilePosition, +1);
        unit.land(tilePosition);
    }

}
