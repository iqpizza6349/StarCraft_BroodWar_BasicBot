package com.tistory.workshop6349.basicbot;

import bwapi.Player;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

import java.util.ArrayList;

public class EnemyUtil {

    public static void collectIntruders(ArrayList<Unit> list, int [][]map) {
        list.clear();

        for (Player player : BasicBotAI.BroodWar.enemies()) {
            for (Unit u : player.getUnits()) {
                if (u.exists() && !u.getType().isInvincible()) {
                    TilePosition t = u.getTilePosition();
                    if (map[t.y][t.x] >= 0) {
                        list.add(u);
                    }
                }
            }
        }
    }

    public static int evaluateStrength(ArrayList<Unit> list) {
        int i = 0;
        for (Unit u : list) {
            if (u.getType() == UnitType.Zerg_Overlord) {
                i += 2;
            }
            else if (u.getType().isWorker()) {
                i++;
            }
            else {
                i += u.getType().supplyRequired();
            }
        }
        return i * 3 / 2;
    }

    public static int evaluateAirStrength(ArrayList<Unit> list) {
        int i = 0;
        for (Unit u : list) {
            if (u.isFlying()) {
                i += u.getType().supplyRequired();
            }
            if (u.getType() == UnitType.Zerg_Overlord) {
                i += 2;
            }
        }
        return i * 3 / 2;
    }

    public static int countSupply(ArrayList<Unit> list) {
        int s = 0;
        for (Unit u : list) {
            s += u.getType().supplyRequired();
        }
        return s;
    }


}
