package com.tistory.workshop6349.basicbot;

import bwapi.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class DropManager {

    private static final DropManager DROP_MANAGER = new DropManager();
    public static DropManager getInstance() {
        return DROP_MANAGER;
    }

    public Unit dropShip = null;
    public ArrayList<Unit> raiders = new ArrayList<>();
    public String state = "none";

    public int ci = 0;
    public int di = 0;

    public Position lastPos = Position.None;
    public Position nextPos = Position.None;
    public Position sidePos = Position.None;

    public int leaveQueue = 0;
    public int idleQueue = 0;

    public int kills = 0;
    public int killsOffset = 0;
    public int killsAlive = 0;
    public int killsDead = 0;

    public ArrayList<Integer> killsPos = new ArrayList<>();
    public ArrayList<Integer> killsNeg = new ArrayList<>();

    public void update() {
        StateManager.getInstance().goRaiding = evaluateDropping();

        if (state.equals("none")) {
            if (StateManager.getInstance().goRaiding && getAvailableDropShip() != BasicUnits.dropShips.get(BasicUnits.dropShips.size() - 1)) {
                initiateDrop();
                state = "init";
            }
            return;
        }
        else {
            if (!dropShip.exists() || countRemainingRaiders() <= 2) {
                terminateDrop();
                state = "none";
                return;
            }
        }

        if (state.equals("init") && abortDroppingInit()) {
            terminateDrop();
            state = "none";
        }

        if ((state.equals("load") || state.equals("init")) && loadRaiders()) {
            flyToNextExpo();
            state = "move";
        }

        if (state.equals("move")) {
            if (MicroUtil.reachingArea(dropShip, ThreatManager.groundDef) || MicroUtil.reachingArea(dropShip, ThreatManager.airDef)) {
                sidePos = findSaveLocation(dropShip.getTilePosition(), nextPos.toTilePosition());
                dropShip.unloadAll(sidePos);
                state = "raid";
            }

            if (dropShipIsNear(nextPos, 8) && raidThisBase(nextPos)) {
                unloadRaiders();
                state = "raid";
            }

            if (dropShipIsNear(nextPos, 2) && loadRaiders()) {
                flyToNextExpo();
                state = "move";
            }
        }

        if (state.equals("raid")) {
            if (raidThisBase(nextPos)) {
                if (BotUtil.isNone(sidePos)) {
                    // TODO raidersUpdate();
                    unloadRaiders();
                }
                else {
                    // TODO raidersUpdate();
                    BasicBotAI.BroodWar.drawLineMap(dropShip.getPosition(), sidePos, Color.Blue);
                }
                leaveQueue = 0;
            }
            else if (leaveQueue < 4) {
                leaveQueue++;
            }
            else {
                loadRaiders();
                state = "load";
                sidePos = Position.None;
            }
        }
        
        checkIdleDrop();
        kills = countKillsTotal() + killsDead - killsOffset;
    }

    public void initiateDrop() {
        Flyer it = getAvailableDropShip();
        it.isRaider = true;
        dropShip = it.unit;
        Position pos = dropShip.getPosition();

        ci = getExpoIndex(BasicMap.myMain);
        di = chooseDirection();
        lastPos = BasicMap.myMain;
        sidePos = Position.None;
        
        raiders.clear();
        if (StateManager.getInstance().strategy == 4) {
            if (BasicUnits.vultures.size() >= 4) {
                assignFrom(BasicUnits.vultures, 4, pos);
            }
            else {
                assignFrom(BasicUnits.siegeTanks, 2, pos);
            }
        }
        else if (2 * StateManager.getInstance().supplyBio >= StateManager.getInstance().supplyMech) {
            int nf = (6 * BasicUnits.firebats.size() + 3) / (BasicUnits.marines.size() + BasicUnits.firebats.size());
            int nm = 6 - nf;
            assignFrom(BasicUnits.marines, nm, pos);
            assignFrom(BasicUnits.firebats, nf, pos);
            assignFrom(BasicUnits.medics, 2, pos);
        }
        else {
            int nv = (8 * BasicUnits.vultures.size() + 4) / StateManager.getInstance().supplyMech;
            int nt = (4 * BasicUnits.siegeTanks.size() + 2) / StateManager.getInstance().supplyMech;
            int ng = 4 - nv - 2 * nt;

            assignFrom(BasicUnits.vultures, nv, pos);
            assignFrom(BasicUnits.siegeTanks, nt, pos);
            assignFrom(BasicUnits.goliaths, ng, pos);
        }

        killsOffset = 0;
        kills = 0;
        killsAlive = 0;
        killsDead = 0;
        leaveQueue = 0;
        idleQueue = 0;

        loadRaiders();
        BasicBotAI.BroodWar.printf("initialize Drop");
    }

    public void terminateDrop() {
        if (dropShip.exists()) {
            dropShip.move(BasicMap.myMain);
        }

        for (Flyer f : BasicUnits.dropShips) {
            f.isRaider = false;
        }
        dropShip = null;
        // TODO  raidersUpdate(0, StateManager.getInstance().gatherPos);
        raiders.clear();
        kills = countKillsTotal() + killsDead + killsOffset;
        if (!state.equals("init")) {
            if (di == 1) {
                killsPos.add(kills);
            }
            if (di == -1) {
                killsNeg.add(kills);
            }
        }
        kills = 0;
        BasicBotAI.BroodWar.printf("terminate drop");
    }

    public boolean evaluateDropping() {
        if (StateManager.getInstance().strategy != 4
                && StateManager.getInstance().supplyBio + StateManager.getInstance().supplyMech >= 32
                && StateManager.getInstance().countTrapped == 0
                && mainArmyAtHome()
                && !shouldAvoidDrops()) {
            return true;
        }

        if (StateManager.getInstance().strategy == 4
                && StateManager.getInstance().countTrapped == 0
                && StateManager.getInstance().goIslands
                && killsPos.isEmpty()
                && killsNeg.isEmpty()) {
            return true;
        }

        if (StateManager.getInstance().strategy == 4
                && BasicUnits.vultures.size() >= 6
                && mainArmyAtHome()) {
            return true;
        }

        return (BasicBotAI.BroodWar.self().supplyUsed() >= 380
                && BasicBotAI.BroodWar.self().minerals() > 1200
                && BasicBotAI.BroodWar.self().gas() > 800);
    }

    public boolean abortDroppingInit() {
        return (BasicBotAI.BroodWar.self().supplyUsed() < 380 && !mainArmyAtHome());
    }

    public boolean mainArmyAtHome() {
        return BotUtil.sqDist(Grouper.playerGroundPosition, BasicMap.myNatural) < 409600;
    }

    public void assignFrom(ArrayList<Fighter> v, int i, Position pos) {
        Iterator<Fighter> iterator = v.iterator();
        while (i > 0 && iterator.hasNext()) {
            Fighter it = iterator.next();
//            it.updateRaider(1, pos, StateManager.getInstance().retreatPos, dropShip);
            raiders.add(it.unit);
            it.isRaider = true;
            i--;
        }
    }
    
    public Flyer getAvailableDropShip() {
        for (Flyer flyer : BasicUnits.dropShips) {
            if (flyer.unit.exists()
                    && flyer.unit.getSpaceRemaining() == 8
                    && BotUtil.sqDist(flyer.unit.getPosition(), BasicMap.myMain) < 262144) {
                return flyer;
            }
        }

        return BasicUnits.dropShips.get(BasicUnits.dropShips.size() - 1);
    }

    public int countRemainingRaiders() {
        int mySpace = 0;
        for (Unit u : raiders) {
            if (u.exists()) {
                mySpace += u.getType().spaceRequired();
            }
        }

        return mySpace;
    }

    public boolean loadRaiders() {
        // TODO ArmyAttacker
        return true;
    }

    public void unloadRaiders() {
        for (Unit u : dropShip.getLoadedUnits()) {
            dropShip.unload(u);
            return;
        }
    }

    public int countKillsTotal() {
        int i = 0;

        for (Unit u : raiders) {
            i += u.getKillCount();
        }

        return i;
    }

    public void keepsKills(Unit unit) {
        for (Unit u : raiders) {
            if (u.getID() == unit.getID()) {
                raiders.remove(unit);
                killsDead += unit.getKillCount();
                return;
            }
        }
    }

    public int chooseDirection() {
        if (!killsPos.isEmpty() && killsNeg.isEmpty()) {
            return -1;
        }
        if (killsPos.isEmpty() && !killsNeg.isEmpty()) {
            return +1;
        }

        if (!killsPos.isEmpty() && killsPos.get(killsPos.size() - 1) + killsNeg.get(killsNeg.size() - 1) >= 8) {
            if (killsPos.get(killsPos.size() -1) >= killsNeg.get(killsNeg.size() - 1)) {
                return +1;
            }
            if (killsNeg.get(killsNeg.size() - 1) >= killsPos.get(killsPos.size() - 1)) {
                return -1;
            }
        }
        // 문제 발생 시, 오류 발생하더라도 !killsNeg.isEmpty 추가할 것

        if (BasicMap.mySneakyDirection != 0) {
            return BasicMap.mySneakyDirection;
        }

        return chooseDirectionRandom();
    }

    public int chooseDirectionRandom() {
        return ((new Random().nextInt(32767) % 2) * 2) - 1; // -1 or +1
    }

    public boolean shouldAvoidDrops() {
        return killsPos.size() + killsNeg.size() >= 4
                && killsPos.get(killsPos.size() - 1) + killsNeg.get(killsPos.size() - 1) < 6;
    }

    public void checkIdleDrop() {

        if (dropShip.isIdle() && dropShip.getSpaceRemaining() < 8) {
            idleQueue++;
        }
        if (idleQueue > 16) {
            terminateDrop();
            state = "none";
        }
    }

    public int getExpoIndex(Position pos) {
        Position posExpo = BotUtil.getClosestTo(BasicMap.airCircle, pos);
        for (int i = 0; i < BasicMap.airCircle.size(); i++) {
            if (BasicMap.airCircle.get(i).equals(posExpo)) {
                return i;
            }
        }

        return 0;
    }

    public void flyToNextExpo() {
        lastPos = BasicMap.flyCircle.get(ci);
        ci = (ci + di + BasicMap.flyCircle.size()) % BasicMap.flyCircle.size();
        nextPos = BasicMap.flyCircle.get(ci);
        dropShip.move(nextPos);
    }

    public boolean dropShipIsNear(Position pos, int s) {
        return BotUtil.sqDist(dropShip.getPosition(), pos) < 1024 * s * s;
    }

    public boolean raidThisBase(Position pos) {
        if (enemyWorkersNear(pos) || ThreatManager.getAirDef(pos) != 0) {
            return true;
        }
        return BasicMap.centerDistWalkMap[pos.y / 32][pos.x / 32] < 0 && enemyBuildingsNear(pos);
    }

    public boolean enemyWorkersNear(Position pos) {
        for (Player p : BasicBotAI.BroodWar.enemies()) {
            for (Unit u : p.getUnits()) {
                if (u.getType().isWorker() && BotUtil.sqDist(u.getPosition(), pos) < 65536) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean enemyBuildingsNear(Position pos) {
        for (Player p : BasicBotAI.BroodWar.enemies()) {
            for (Unit u : p.getUnits()) {
                if (u.getType().isBuilding() && BotUtil.sqDist(u.getPosition(), pos) < 65536) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean shouldUnloadOutside(Position pos) {
        return StateManager.getInstance().strategy == 4
                && raiders.size() == 4
                && BasicMap.mainDistArray[BasicMap.mm][pos.y / 32][pos.x / 32] > 120;
    }

    public Position findSaveLocation(TilePosition td, TilePosition tn) {
        int x0 = MapUtil.mapSafeSub(td.x, 8);
        int x1 = MapUtil.mapSafeAdd(td.x, 8, BasicMap.wt);
        int y0 = MapUtil.mapSafeSub(td.y, 8);
        int y1 = MapUtil.mapSafeAdd(td.y, 8, BasicMap.ht);
        int dopt = 1048576;
        int xopt = td.x;
        int yopt = td.y;
        int gd = MapUtil.getGroundDist(tn);

        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                if (BasicMap.walk_Map[y][x]
                        && ThreatManager.airDef[4 * y][4 * x] == 0
                        && ThreatManager.groundDef[4 * y][4 * x] == 0) {
                    int d = BotUtil.sqDist(td.x, td.y, x, y) + BotUtil.sqDist(tn.x, tn.y, x, y);
                    if (d < dopt) {
                        dopt = d;
                        xopt = x;
                        yopt = y;
                    }
                }
            }
        }

        if (Math.abs(MapUtil.getGroundDist(tn.x, tn.y) - MapUtil.getGroundDist(xopt, yopt)) > 24) {
            BasicBotAI.BroodWar.printf("no altering drop location found");
            return new Position(32 * tn.x + 16, 32 * tn.y + 16);
        }
        BasicBotAI.BroodWar.printf("base defended, altering drop location");
        return new Position(32 * xopt + 16, 32 * yopt + 16);
    }

    public void showInfo(int x, int y) {
        if (!BasicUnits.dropShips.isEmpty() && (StateManager.getInstance().goRaiding || !state.equals("none"))) {
            BasicBotAI.BroodWar.drawTextScreen(x, y, "Drops : " + killsPos.size() + ", " + killsNeg.size());
            BasicBotAI.BroodWar.drawTextScreen(x, y + 10, "State: " + state);
            BasicBotAI.BroodWar.drawTextScreen(x, y + 20, "kills: " + kills);
            BasicBotAI.BroodWar.drawTextScreen(x, y + 30, "leave: " + leaveQueue);
        }
    }

}
