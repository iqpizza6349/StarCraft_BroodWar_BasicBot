package com.tistory.workshop6349.basicbot;

import bwapi.Position;
import bwapi.Unit;

import java.util.ArrayList;

public class Cleaner {

    public static void clean() {
        addressRenegadeSCV();
        resetPlannedBuilds();
        resetBlockedTiles();
//        correctAllMinerals();
        StateManager.getInstance().blockedMainChoke = checkBlockedEntranceTo();
        checkBlockedEntrance();
    }

    public static void addressRenegadeSCV() {
        ArrayList<Integer> units = new ArrayList<>();

        for (SCV scv : BasicUnits.scvs) {
            units.add(scv.id);
        }
        for (Unit unit : BasicBotAI.BroodWar.self().getUnits()) {
            if (unit.getType().isWorker()
                    && unit.exists()
                    && unit.isIdle()
                    && unit.isCompleted()
                    && !units.contains(unit.getID())) {
                BasicUnits.scvs.add(new SCV(unit));
                BasicBotAI.BroodWar.printf("addressing renegade SCV " + unit.getID());
                return;
            }
        }
    }

    public static void resetPlannedBuilds() {
        // TODO Builds
    }

    public static void resetBlockedTiles() {
        // TODO Builds
    }

    public static void correctAllMinerals() {
        // TODO removed
    }

    public static boolean checkBlockedEntranceTo() {
        if (BasicBotAI.BroodWar.getFrameCount() > 21600
                && BasicBotAI.BroodWar.self().supplyUsed() > 200
                && StateManager.getInstance().strategy != 6) {
            int n = 0;
            int d = MapUtil.getGroundDist(BasicMap.mainChokePos[BasicMap.mm]);
            if (StateManager.getInstance().defTargetPos != Position.None
                    && MapUtil.getGroundDist(StateManager.getInstance().defTargetPos) < d) {
                // except defense in main
                return false;
            }
            for (Fighter fighter : BasicUnits.siegeTanks) {
                if (MapUtil.getGroundDist(fighter.unit) < d) {
                    n += 2;
                }
            }
            for (Fighter fighter : BasicUnits.goliaths) {
                if (MapUtil.getGroundDist(fighter.unit) < d) {
                    n += 2;
                }
            }
            for (Fighter fighter : BasicUnits.vultures) {
                if (MapUtil.getGroundDist(fighter.unit) < d) {
                    n += 2;
                }
            }

            if (2 * n >= StateManager.getInstance().supplyMech) {
                BasicBotAI.BroodWar.printf("main choke must be blocked");
                return true;
            }
        }

        return false;
    }


    public static void checkBlockedEntrance() {
        EnemyManager.getInstance().tlself.clear();
        if (StateManager.getInstance().blockedMainChoke) {
            Position pc = BasicMap.mainChokePos[BasicMap.mm];
            Position pd = BasicMap.mainDefPos[BasicMap.mm];

            // TODO Build
        }
    }

}
