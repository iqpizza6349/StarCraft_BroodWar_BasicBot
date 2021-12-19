package com.tistory.workshop6349.basicbot;

import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;

import java.util.ArrayList;

public class Grouper {

    public enum Pos {
        Ground,
        Air,
        Enemy
    }

    public enum SUP {
        Ground,
        Air,
        Enemy
    }


    public static final ArrayList<Integer> maxSqrtDist = new ArrayList<>();

    public static final ArrayList<Unit> playerGroundUnits = new ArrayList<>();
    public static final ArrayList<Unit> playerAirUnits = new ArrayList<>();
    public static final ArrayList<Unit> enemyGroundUnits = new ArrayList<>();

    public static Position playerGroundPosition;
    public static Position playerAirPosition;
    public static Position enemyGroundPosition;

    public static int playerGroundSupply;
    public static int playerAirSupply;
    public static int enemyGroundSupply;

    static {
        maxSqrtDist.add(1048576);
        maxSqrtDist.add(262144);
        maxSqrtDist.add(65536);

        playerGroundPosition = Position.None;
        playerAirPosition = Position.None;
        enemyGroundPosition = Position.None;

        playerGroundSupply = 0;
        playerAirSupply = 0;
        enemyGroundSupply = 0;
    }

    public void updateArmyGroups() {
        playerGroundUnits.clear();
        playerAirUnits.clear();
        enemyGroundUnits.clear();

        // determine player army groups
        for (Unit u : BasicBotAI.BroodWar.self().getUnits()) {
            if (!u.getType().isBuilding()
                    && !u.getType().isWorker()
                    && u.getType().supplyRequired() != 0
                    && u.isCompleted()) {
                if (u.getType().isFlyer()) {
                    playerAirUnits.add(u);
                }
                else {
                    playerGroundUnits.add(u);
                }
            }
        }

        updateGroup(playerGroundUnits, playerGroundPosition, playerGroundSupply, Pos.Ground, SUP.Ground);
        updateGroup(playerAirUnits, playerAirPosition, playerAirSupply, Pos.Air, SUP.Air);

        // determine enemy army groups
        for (Player p : BasicBotAI.BroodWar.enemies()) {
            for (Unit u : p.getUnits()) {
                if (!u.getType().isBuilding()
                        && !u.getType().isWorker()
                        && u.getType().supplyRequired() != 0
                        && u.isCompleted()
                        && !u.getType().isFlyer()) {
                    enemyGroundUnits.add(u);
                }
            }
        }

        updateGroup(enemyGroundUnits, enemyGroundPosition, enemyGroundSupply, Pos.Enemy, SUP.Enemy);

        if (enemyGroundPosition == Position.None) {
            // TODO lastPos;
        }
    }

    public void updateGroup(ArrayList<Unit> units, Position position, int supply, Pos pos, SUP sup) {
        if (!units.isEmpty()) {
            for (int maxSqDist : maxSqrtDist) {
                position = calculateCenterPos(units);
                units = reduceArmyGroup(units, position, maxSqDist);
            }
        }
        position = calculateCenterPos(units);
        supply = calculateArmySupply(units);

        switch (pos) {
            case Air:
                playerAirPosition = position;
                break;
            case Ground:
                playerGroundPosition = position;
                break;
            case Enemy:
                enemyGroundPosition = position;
        }

        switch (sup) {
            case Air:
                playerAirSupply = supply;
                break;
            case Ground:
                playerGroundSupply = supply;
                break;
            case Enemy:
                enemyGroundSupply = supply;
                break;
        }

    }

    public Position calculateCenterPos(ArrayList<Unit> units) {
        int s = 0;
        int x = 0;
        int y = 0;

        for (Unit u : units) {
            x += (u.getType().supplyRequired() * u.getPosition().x);
            y += (u.getType().supplyRequired() * u.getPosition().y);
            s += u.getType().supplyRequired();
        }
        if (s != 0) {
            return new Position(x / s, y /s);
        }
        return Position.None;
    }

    public ArrayList<Unit> reduceArmyGroup(ArrayList<Unit> oldUnits, Position pos, int maxSqDist) {
        ArrayList<Unit> newUnits = new ArrayList<>();
        for (Unit u : oldUnits) {
            if (BotUtil.sqDist(u.getPosition(), pos) < maxSqDist) {
                newUnits.add(u);
            }
        }

        return newUnits;
    }

    public int calculateArmySupply(ArrayList<Unit> units) {
        int s = 0;

        for (Unit u : units) {
            s += u.getType().supplyRequired();
        }
        return s;
    }





}
