package com.tistory.workshop6349.basicbot;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

import java.util.ArrayList;

public class BasicUnits {

    public static final ArrayList<SCV> scvs = new ArrayList<>();

    public static final ArrayList<Fighter> marines = new ArrayList<>();
    public static final ArrayList<Fighter> firebats = new ArrayList<>();
    public static final ArrayList<Fighter> medics = new ArrayList<>();
    public static final ArrayList<Fighter> ghosts = new ArrayList<>();
    public static final ArrayList<Fighter> siegeTanks = new ArrayList<>();
    public static final ArrayList<Fighter> goliaths = new ArrayList<>();
    public static final ArrayList<Fighter> vultures = new ArrayList<>();

    public static final ArrayList<Flyer> wraiths = new ArrayList<>();
    public static final ArrayList<Flyer> dropShips = new ArrayList<>();
    public static final ArrayList<Flyer> valkyries = new ArrayList<>();
    public static final ArrayList<Flyer> vessels = new ArrayList<>();
    public static final ArrayList<Flyer> cruiser = new ArrayList<>();

    public static int countSCV = 0;
    public static int countMarine = 0;
    public static int countFirebat = 0;
    public static int countMedic = 0;
    public static int countGhost = 0;
    public static int countSiegeTank = 0;
    public static int countGoliath = 0;
    public static int countVulture = 0;
    public static int countWraith = 0;
    public static int countDropShip = 0;
    public static int countValkyrie = 0;
    public static int countVessel = 0;
    public static int countCruiser = 0;

    public static boolean hasScout = false;
    
    public static void countUnit(Unit unit) {
        switch (unit.getType()) {
            case Terran_SCV:
                ++countSCV;
                break;
            case Terran_Marine:
                ++countMarine;
                break;
            case Terran_Firebat:
                ++countFirebat;
                break;
            case Terran_Medic:
                ++countMedic;
                break;
            case Terran_Ghost:
                ++countGhost;
                break;
            case Terran_Siege_Tank_Tank_Mode:
            case Terran_Siege_Tank_Siege_Mode:
                ++countSiegeTank;
                break;
            case Terran_Goliath:
                ++countGoliath;
                break;
            case Terran_Vulture:
                ++countVulture;
                break;
            case Terran_Wraith:
                ++countWraith;
                break;
            case Terran_Dropship:
                ++countDropShip;
                break;
            case Terran_Valkyrie:
                ++countValkyrie;
                break;
            case Terran_Science_Vessel:
                ++countVessel;
                break;
            case Terran_Battlecruiser:
                ++countCruiser;
                break;
        }
    }

    public static void appendUnit(Unit unit) {
        if (isTank(unit.getType()) && alreadyIn(unit, siegeTanks)) {
            return;
        }

        switch (unit.getType()) {
            case Terran_SCV:
                appendToSCV(unit, scvs);
                break;
            case Terran_Marine:
                appendToFighter(unit, marines);
                break;
            case Terran_Firebat:
                appendToFighter(unit, firebats);
                break;
            case Terran_Medic:
                appendToFighter(unit, medics);
                break;
            case Terran_Ghost:
                appendToFighter(unit, ghosts);
                break;
            case Terran_Siege_Tank_Tank_Mode:
            case Terran_Siege_Tank_Siege_Mode:
                appendToFighter(unit, siegeTanks);
                break;
            case Terran_Goliath:
                appendToFighter(unit, goliaths);
                break;
            case Terran_Vulture:
                appendToFighter(unit, vultures);
                break;
            case Terran_Wraith:
                appendToFlyer(unit, wraiths);
                break;
            case Terran_Dropship:
                appendToFlyer(unit, dropShips);
                break;
            case Terran_Valkyrie:
                appendToFlyer(unit, valkyries);
                break;
            case Terran_Science_Vessel:
                appendToFlyer(unit, vessels);
                break;
            case Terran_Battlecruiser:
                appendToFlyer(unit, cruiser);
                break;
        }
    }

    public static void removeUnit(Unit unit) {
        switch (unit.getType()) {
            case Terran_SCV:
                countSCV = removeFromSCV(scvs, countSCV);
                break;
            case Terran_Marine:
                countMarine = removeFromFighter(marines, countMarine);
                break;
            case Terran_Firebat:
                countFirebat = removeFromFighter(firebats, countFirebat);
                break;
            case Terran_Medic:
                countMedic = removeFromFighter(medics, countMedic);
                break;
            case Terran_Ghost:
                countGhost = removeFromFighter(ghosts, countGhost);
                break;
            case Terran_Siege_Tank_Tank_Mode:
            case Terran_Siege_Tank_Siege_Mode:
                countSiegeTank = removeFromFighter(siegeTanks, countSiegeTank);
                break;
            case Terran_Goliath:
                countGoliath = removeFromFighter(goliaths, countGoliath);
                break;
            case Terran_Vulture:
                countVulture = removeFromFighter(vultures, countVulture);
                break;
            case Terran_Wraith:
                countWraith = removeFromFlyer(wraiths, countWraith);
                break;
            case Terran_Dropship:
                countDropShip = removeFromFlyer(dropShips, countDropShip);
                break;
            case Terran_Valkyrie:
                countValkyrie = removeFromFlyer(valkyries, countValkyrie);
                break;
            case Terran_Science_Vessel:
                countVessel = removeFromFlyer(vessels, countVessel);
                break;
            case Terran_Battlecruiser:
                countCruiser = removeFromFlyer(cruiser, countCruiser);
                break;
        }
    }


    public static boolean isTank(UnitType type) {
        return (type == UnitType.Terran_Siege_Tank_Siege_Mode || type == UnitType.Terran_Siege_Tank_Tank_Mode);
    }

    public static boolean alreadyIn(Unit unit, ArrayList<Fighter> v) {
        for (Fighter fighter : v) {
            if (fighter.id == unit.getID()) {
                return true;
            }
        }
        return false;
    }

    public static void appendToSCV(Unit unit, ArrayList<SCV> v) {
        v.add(new SCV(unit));
    }

    public static void appendToFighter(Unit unit, ArrayList<Fighter> v) {
        v.add(new Fighter(unit));
    }

    public static void appendToFlyer(Unit unit, ArrayList<Flyer> v) {
        v.add(new Flyer(unit));
    }

    public static int removeFromSCV(ArrayList<SCV> v, int count) {
        for (SCV scv : v) {
            if (!scv.unit.exists()) {
                scv.endMining();
                scv.endBuildMission();
                v.remove(scv);
                count--;
                break;
            }
        }
        return count;
    }

    public static int removeFromFighter(ArrayList<Fighter> v, int count) {
        for (Fighter fighter : v) {
            if (!fighter.unit.exists()) {
                v.remove(fighter);
                count--;
                break;
            }
        }
        return count;
    }

    public static int removeFromFlyer(ArrayList<Flyer> v, int count) {
        for (Flyer flyer : v) {
            if (!flyer.unit.exists()) {
                v.remove(flyer);
                count--;
                break;
            }
        }
        return count;
    }

    public static void confirmExistence(Unit unit) {
        if (unit.exists()) {
            BasicBotAI.BroodWar.printf("Unit still exist!");
        }
        else {
            BasicBotAI.BroodWar.printf("Unit doesn't exist!");
        }
    }

    public static void correct() {
        countSCV = removeFromSCV(scvs, countSCV);
        for (Flyer f : dropShips) {
            f.correctDropShip();
        }
    }

    public static void updateAll() {
        for (SCV scv : scvs) {
            scv.update();
        }
        for (Fighter m : marines) {
            m.update();
            m.checkStim();
        }
        for (Fighter f : firebats) {
            f.update();
            f.checkFallB();
        }
        for (Fighter m : medics) {
            m.update();
            m.checkFlare();
        }
        for (Fighter g : ghosts) {
            g.update();
            g.checkLocked();
            g.checkCloak();
        }
        for (Fighter s : siegeTanks) {
            s.update();
            s.checkSiege();
        }
        for (Fighter g : goliaths) {
            g.update();
        }
        for (Fighter v : vultures) {
            v.updateVulture();
            v.checkMine();
        }
        for (Flyer c : cruiser) {
            c.update();
        }
        for (Flyer w : wraiths) {
            w.update();
        }
        for (Flyer d : dropShips) {
            d.updateDropShip();
        }
    }

    public static void updateEight() {
        StateManager.getInstance().countTrapped = 0;
        for (SCV scv : scvs) {
            scv.checkingBeingTrapped(StateManager.getInstance().countTrapped);
        }
        for (Fighter m : marines) {
            m.checkUnstuck();
        }
    }

    public static void assignScout(Unit unit) {
        if (!hasScout && !BasicMap.unScouted.isEmpty()) {
            scvs.get(0).isScout = true;
            hasScout = true;
        }
    }

    public static void countSupplies() {
        StateManager.getInstance().supplyWorker = scvs.size();
        StateManager.getInstance().supplyBio = marines.size() + firebats.size() + medics.size() + ghosts.size();
        StateManager.getInstance().supplyMech = 2 * (siegeTanks.size() + goliaths.size() + vultures.size());
        StateManager.getInstance().supplyAir = 2 * wraiths.size() + 2 * dropShips.size() + 3 * valkyries.size() + 2 * vessels.size() + 6 * cruiser.size();
        StateManager.getInstance().supplyMil = 0;

        for (SCV scv : scvs) {
            if (scv.isMilitia) {
                StateManager.getInstance().supplyMil++;
            }
        }

    }

    public static SCV chooseSCV(TilePosition tile, boolean noMilitia, boolean noCarry) {
        int minDist = 65536;
        SCV minSCV = scvs.get(0);
        for (SCV scv : scvs) {
            if (scv.unit.isCompleted()
                    && !scv.unit.isConstructing()
                    && !scv.unit.isRepairing()
                    && !scv.isBuilder
                    && !scv.isScout
                    && !scv.isRepair
                    && (!scv.isMilitia || !noMilitia)
                    && (!scv.unit.isCarryingMinerals() || !noCarry)
                    && (!scv.unit.isCarryingGas() || !noCarry)
                    && !scv.unit.isGatheringGas()) {
                int dist = BotUtil.sqDist(scv.unit.getTilePosition(), tile);
                if (dist < minDist) {
                    minDist = dist;
                    minSCV = scv;
                }
            }
        }
        return minSCV;
    }

    public static SCV getScout() {
        for (SCV scv : scvs) {
            if (scv.isScout
                    && scv.unit.exists()
                    && !scv.unit.isConstructing()) {
                return scv;
            }
        }
        return scvs.get(0);
    }

    public static void stopAllBuildMissions() {
        for (SCV scv : scvs) {
            if (scv.isBuilder) {
                scv.endBuildMission();
            }
        }
        BasicBotAI.BroodWar.printf("Stop All Building Missions");
    }

}
