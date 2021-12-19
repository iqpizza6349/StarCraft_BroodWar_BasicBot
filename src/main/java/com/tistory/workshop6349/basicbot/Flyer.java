package com.tistory.workshop6349.basicbot;

import bwapi.*;

import java.util.ArrayList;
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

        if (!unit.exists() || unit.isLockedDown() || unit.isStasised()) {
            return;
        }

        if (abilityQueue > 0 && abilityTarget != null) {
            --abilityQueue;
            MicroUtil.drawArrow(unit, abilityTarget, Color.Blue);
            return;
        }

        if (attackTarget != null && loopingQueue == 0) {
            --attackQueue;
            if (unit.getAirWeaponCooldown() > 0) {
                loopToEvade(attackTarget.getPosition());
                attackTarget = null;
                attackQueue = 0;
            }
            if (attackQueue == 0) {
                attackTarget = null;
                BasicBotAI.BroodWar.drawTextMap(unit.getPosition(), " RESET");
            }
            return;
        }

        if (loopingQueue > 0) {
            --loopingQueue;
            if (loopingQueue == 0) {
                move(attackPos, "RETURNING");
            }
            return;
        }

        if (evadeAirThreat()
                && (MicroUtil.reachingArea(unit, ThreatManager.airDef) || MicroUtil.reachingArea(unit, ThreatManager.airMap))) {
            loopToEvade(attackPos);
            return;
        }

        if (unit.getType() == UnitType.Terran_Wraith && evadeAirThreat()) {
            threatTarget = getCloseThreat();
            if (threatTarget != null) {
                loopToEvade(threatTarget.getPosition());
                threatTarget = null;
                return;
            }
        }

        if (unit.getType() == UnitType.Terran_Battlecruiser
                && BasicBotAI.BroodWar.self().hasResearched(TechType.Yamato_Gun)
                && unit.getEnergy() >= 150) {
            checkYamato();
        }

        if (unit.getAirWeaponCooldown() < 8) {
            if (unit.getType() == UnitType.Terran_Wraith) {
                if (evadeAirThreat()) {
                    attackTarget = getTargetWraith();
                }
                else {
                    attackTarget = getTargetAll(36864);
                }
            }
            else {
                attackTarget = getTargetAll(65536);
            }

            if (attackTarget != null) {
                unit.attack(attackTarget);
                attackQueue = 24;
                MicroUtil.drawArrow(unit, attackTarget, Color.Orange);
                BasicBotAI.BroodWar.drawTextMap(unit.getPosition(), "ATTACK");
                return;
            }
        }

        if (isCircling) {
            flyInCircles();
        }
        else if (!EnemyManager.getInstance().positions.isEmpty()) {
            flyRandom(EnemyManager.getInstance().positions);
        }
        else {
            swarm(false);
        }

        if (unit.isIdle()
                && attackTile.isValid(BasicBotAI.BroodWar)
                && BotUtil.sqDist(unit.getTilePosition(), attackTile) >= 16) {
            move(attackPos, "MOVING");
        }
    }

    public void updateDropShip() {
        if (!unit.exists()
                || unit.isLockedDown()
                || unit.isStasised()
                || isRaider
                || BasicBotAI.BroodWar.getFrameCount() - unit.getLastCommandFrame() < 16) {
            return;
        }
        
        if (transportPos.isValid(BasicBotAI.BroodWar)) {
            if (transportUnit != null && transportUnit.exists()) {
                if (transportUnit.getTransport() == null) {
                    unit.load(transportUnit);
                    BasicBotAI.BroodWar.drawLineMap(unit.getPosition(), transportUnit.getPosition(), Color.Yellow);
                }
                else {
                    transportUnit = null;
                    abilityQueue = 0;
                }
            }
            else if (unit.getSpaceRemaining() < 8) {
                if (abilityQueue == 0
                        && MapUtil.hasGroundConnection(unit.getPosition(), transportPos)
                        && unit.canUnloadAll()) {
                    unit.unloadAll();
                }
                unit.move(transportPos);
                abilityQueue = BotUtil.safeSum(abilityQueue, -1);
                BasicBotAI.BroodWar.drawLineMap(unit.getPosition(), transportPos, Color.Yellow);
            }
            else {
                transportUnit = null;
                transportPos = Position.None;
                unit.stop();
            }
        }
    }

    public boolean evadeAirThreat() {
        if (StateManager.getInstance().supplyAir >= 4 * (EnemyManager.getInstance().supply_army + EnemyManager.getInstance().supply_air)) {
            return false;
        }

        return Grouper.playerAirSupply < 48
                || BotUtil.sqDist(unit.getPosition(), Grouper.playerAirPosition) >= 65536;
    }

    public void loopToEvade(Position pos) {
        loopingQueue = (unit.getAirWeaponCooldown() > 0) ? (unit.getAirWeaponCooldown() * 3 / 5) : 16;
        move(MicroUtil.getRetreatVector(unit.getPosition(), pos, 256), "LOOPING");
    }

    public void correctDropShip() {
        if (!isRaider && unit.isIdle()) {
            if (unit.getSpaceRemaining() < 8) {
                unit.unloadAll(retreatPos);
            }
            else {
                unit.move(retreatPos);
            }
            transportUnit = null;
            transportPos = Position.None;
        }
    }

    public void flyInCircles() {
        if (BotUtil.sqDist(unit.getTilePosition(), attackTile) < 16 && dc != 0) {
            int iMax = BasicMap.airCircle.size();
            if (ica >= iMax) {
                isCircling = false;
            }
            ic += dc;
            ica++;
            Position pa = BasicMap.airCircle.get((ic + iMax) % iMax);
            int dx = (new Random().nextInt(32767) % 256) - 128;
            int dy = (new Random().nextInt(32767) % 256) - 128;
            latestPos = unit.getPosition();
            attackPos = new Position(pa.x + dx, pa.y + dy);
            attackTile = attackPos.toTilePosition();
            move(attackPos, "CIRCLING");
        }
    }

    public void flyRandom(ArrayList<Position> v) {
        if (!v.isEmpty() && BotUtil.sqDist(unit.getTilePosition(), attackTile) < 16) {
            int i = new Random().nextInt(32767) % v.size();
            attackPos = v.get(i);
            attackTile = attackPos.toTilePosition();
            move(attackPos, "RANDOM");
        }
    }

    public void checkYamato() {
        abilityQueue = 0;
        abilityTarget = getTargetYamato();

        if (abilityTarget != null && unit.canUseTech(TechType.Yamato_Gun, abilityTarget)) {
            unit.useTech(TechType.Yamato_Gun, abilityTarget);
            StateManager.getInstance().targetList.add(abilityTarget);
            abilityQueue = 64;
        }
    }

    public Unit getTargetYamato() {
        for (Unit u : EnemyManager.getInstance().tlyama) {
            if (BotUtil.sqDist(unit, u) <= 196
                    && u.isCompleted()
                    && u.getHitPoints() >= 60
                    && !u.isLockedDown()
                    && !StateManager.getInstance().targetList.contains(u)) {
                return u;
            }
        }

        return null;
    }

    public Unit getTargetWraith() {
        Unit u = null;
        int r = 36864;      // radius 6
        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tla, u, unit.getPosition(), r);
        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlgw, u, unit.getPosition(), r);
        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlgm, u, unit.getPosition(), r);
        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlgl, u, unit.getPosition(), r);

        return u;
    }

    public Unit getTargetAll(int sqd) {
        Unit u = null;

        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlaa, u, unit.getPosition(), sqd);
        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlga, u, unit.getPosition(), sqd);
        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tla, u, unit.getPosition(), sqd);
        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlg, u, unit.getPosition(), sqd);

        return u;
    }

    public Unit getCloseThreat() {
        for (Unit u : EnemyManager.getInstance().tlga) {
            if (u.getType() == UnitType.Terran_Goliath && BotUtil.sqDist(u, unit) < 100) {
                // radius 10
                return u;
            }
            if (BotUtil.sqDist(u, unit) < 64) {
                // radius 8
                return u;
            }
        }

        for (Unit u : EnemyManager.getInstance().tlaa) {
            if (u.getType() == UnitType.Protoss_Carrier && BotUtil.sqDist(u, unit) < 100) {
                // radius 10
                return u;
            }
            if (BotUtil.sqDist(u, unit) < 64) {
                // radius 8
                return u;
            }
        }

        return null;
    }

}
