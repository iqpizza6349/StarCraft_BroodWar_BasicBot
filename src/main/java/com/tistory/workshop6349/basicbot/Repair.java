package com.tistory.workshop6349.basicbot;

import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

public class Repair {

    public static void updateRepairing() {
        if (BasicUnits.scvs.isEmpty()) {
            return;
        }

        int nMax = 4;
        if (StateManager.getInstance().strategy == 3 && StateManager.getInstance().myTime < 600) {
            nMax = 5;
        }
        int n = countRepairers();
        // TODO Buildings from BasicBuild
        if (StateManager.getInstance().holdBunker && n < 2) {
            holdTheBunker();
        }
    }

    public static void checkCanceling(Unit u) {
        if (!u.isCompleted()
                && u.exists() 
                && u.isUnderAttack()
                && u.getHitPoints() * 8 < u.getType().maxHitPoints()) {
            u.cancelConstruction();
        }
    }

    public static void checkContinue(Unit u) {
        if (!u.isCompleted()
                && !u.isBeingConstructed()
                && u.exists()) {
            // TODO chooseSCV();
        }
    }

    public static void checkRepairing(Unit u) {
        if (BasicBotAI.BroodWar.self().minerals() > 0
                && u.exists()
                && u.isCompleted()
                && !u.isFlying()) {
            if ((u.getType() == UnitType.Terran_Bunker && u.getHitPoints() < 350)
                    || (u.getType() == UnitType.Terran_Missile_Turret && u.getHitPoints() < 200)
                    || (u.getType() == UnitType.Terran_Command_Center && u.getHitPoints() < 1200)
                    || u.getHitPoints() * 2 < u.getType().maxHitPoints()) {
                // TODO chooseSCV();
            }
        }
    }

    public static void holdTheBunker() {
        // TODO
    }

    public static int countRepairers() {
        int i = 0;
        for (SCV scv : BasicUnits.scvs) {
            if (scv.isRepair) {
                i++;
            }
        }
        return i;
    }

    public void updateTransport() {
        for (SCV scv : BasicUnits.scvs) {
            if (scv.calledTransport && !scv.isTrapped) {
                for (Flyer flyer : BasicUnits.dropShips) {
                    if (flyer.transportUnit.getID() == scv.id) {
                        flyer.transportUnit = null;
                        flyer.transportPos = Position.None;
                        flyer.unit.stop();
                        scv.calledTransport = false;
                        scv.transportUnit = null;
                        scv.isEntering = false;
                        break;
                    }
                }
            }
        }

        for (SCV scv : BasicUnits.scvs) {
            if (scv.isTrapped && !scv.calledTransport) {
                for (Flyer flyer : BasicUnits.dropShips) {
                    if (!flyer.isRaider
                            && flyer.transportUnit != null
                            && flyer.unit.getSpaceRemaining() == 8) {
                        flyer.transportUnit = scv.unit;
                        flyer.transportPos = scv.destination;
                        scv.calledTransport = true;
                        scv.transportUnit = flyer.unit;
                        break;
                    }
                }
            }
        }
    }

}
