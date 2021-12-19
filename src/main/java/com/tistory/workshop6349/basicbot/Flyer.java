package com.tistory.workshop6349.basicbot;

import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;

import java.util.Random;

public class Flyer {

    public Unit unit;
    public int id;

    public boolean isCircling;
    public boolean isRaider;

    public Position transportPos;
    public Unit transportUnit;

    private Position attackPos;
    private TilePosition attackTile;
    private Position latestPos;
    private Position retreatPos;

    private int commandQueue;
    private int loopingQueue;
    private int abilityQueue;
    private int attackQueue;

    private int dc;                     // diff Circle -1 / +1
    private int ic;                     // index Circle
    private int ica;                    // index Circle absolute

    private Unit abilityTarget;
    private Unit attackTarget;
    private Unit threatTarget;

    
    public Flyer(Unit unit) {
        this.unit = unit;
        this.id = unit.getID();
        this.attackPos = StateManager.getInstance().gatherPos;
        this.attackTile = attackPos.toTilePosition();
        this.latestPos = StateManager.getInstance().gatherPos;
        this.retreatPos = StateManager.getInstance().retreatPos;
        this.commandQueue = 0;
        this.loopingQueue = 0;
        this.abilityQueue = 0;
        this.attackQueue = 0;
        this.isCircling = true;
        this.isRaider = false;
        this.abilityTarget = null;
        this.attackTarget = null;
        this.threatTarget = null;
        this.transportPos = StateManager.getInstance().retreatPos;
        this.transportUnit = null;

        this.dc = 0;        // ((rand() % 2) * 2) - 1;
        this.ic = 0;
        this.ica = 0;
        for (int i = 0; i < BasicMap.airCircle.size(); i++) {
            if (BasicMap.airCircle.get(i) == BasicMap.myNatural) {
                this.ic = i;
            }
        }
    }

    public void move(Position pos, String s) {
        unit.move(pos);
        if (!s.equals("")) {
            BasicBotAI.BroodWar.drawTextMap(unit.getPosition(), s);
        }
    }

    public void move(Position pos) {
        move(pos, "");
    }

    public void attack() {
        unit.attack(attackPos);
        commandQueue = 128;
    }
    
    public void swarm(boolean immediately) {
        if (immediately || BotUtil.sqDist(unit.getTilePosition(), attackTile) < 16) {
            int x = new Random().nextInt(32767) % BasicMap.wt;
            int y = new Random().nextInt(32767) % BasicMap.ht;
            attackTile = new TilePosition(x, y);
            attackPos = attackTile.toPosition();
            move(attackPos, "SWARMING");
        }
    }

    public void setCirclingDiff(int newDC) {
        this.dc = newDC;
    }

    public int getCirclingDiff() {
        return dc;
    }

    public void update() {

    }

    public void updateDropShip() {

    }

    public boolean evadeAirThreat() {
        if (StateManager.getInstance().supplyAir >= 4 * (EnemyManager.getInstance().supply_army + EnemyManager.getInstance().supply_air)) {
            return false;
        }

        if (Grouper.playerAirSupply >= 48
                && BotUtil.sqDist(unit.getPosition(), Grouper.playerAirPosition) < 65536) {
            return false;
        }

        return true;
    }

    public void loopToEvade(Position pos) {

    }







}
