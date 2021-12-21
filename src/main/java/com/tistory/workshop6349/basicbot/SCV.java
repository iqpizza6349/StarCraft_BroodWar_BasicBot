package com.tistory.workshop6349.basicbot;

import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

import java.util.ArrayList;

public class SCV {

    public Unit unit;
    public int id;

    // states
    public boolean isMiner;
    public boolean isBuilder;
    public boolean isMilitia;
    public boolean isJanitor;
    public boolean isRepair;
    public boolean isScout;
    public boolean isTrapped;
    public boolean calledTransport;
    public boolean isEntering;
    public Unit transportUnit;

    // positions
    public Position destination;
    public Position expoPos;

    // positions and integers
    private Position securePos;
    private Position attackPos;

    private UnitType buildType;
    private Position buildPos;
    private TilePosition buildTile;

    private int buildQueue;
    private int buildSqrtDist;
    private int blockCount;
    private int x0;
    private int y0;
    private int x1;
    private int y1;

    private Unit damagedUnit;
    private Unit resourceContainer;

    // other queues and states
    private int circleIncrement;
    private Position scoutPos;
    private boolean hasResource;
    private int stuckQueue;
    private int minTargetDistance;

    public SCV(Unit unit) {
        this.unit = unit;
        this.id = unit.getID();

        this.isMiner = false;
        this.isMilitia = false;
        this.isBuilder = false;
        this.isRepair = false;
        this.isScout = false;
        this.isTrapped = false;
        this.hasResource = false;
        this.calledTransport = false;
        this.isEntering = false;
        this.transportUnit = null;

        this.destination = Position.None;
        this.expoPos = Position.None;

        this.securePos = StateManager.getInstance().retreatPos;
        this.attackPos = StateManager.getInstance().gatherPos;

        this.buildType = UnitType.None;
        this.buildPos = Position.None;
        this.buildSqrtDist = 0;
        this.buildQueue = 0;
        this.blockCount = 0;

        this.x0 = 0;
        this.y0 = 0;
        this.x1 = 0;
        this.y1 = 0;

        this.damagedUnit = null;
        this.resourceContainer = null;

        this.circleIncrement = -1;
        this.scoutPos = Position.None;
        this.stuckQueue = 0;
        this.minTargetDistance = 0;
    }

    public void update() {

    }

    public void move(Position p) {
        unit.move(p);
    }

    public void setAttack(Position p) {
        attackPos = p;
    }

    public void setSwarm(boolean immediately) {
        if (immediately || unit.isIdle()) {
            setAttack(BotUtil.get_random_position());
        }
    }

    public void changeResource(Unit u, boolean removeStack) {
        endMining();
        resourceContainer = u;
        setResource(resourceContainer, +1);
        unit.gather(resourceContainer);
        if (removeStack) {
            buildTile = u.getTilePosition();
        }
    }

    public void endMining() {
        setResource(resourceContainer, -1);
        resourceContainer = null;
        isMiner = false;
    }

    public void setExpoPos(Position p) {
        expoPos = p;
    }

    public void mine() {
        if (resourceContainer == null) {
            if (!BotUtil.isNone(buildTile)) {
                if (getMineralOn(buildTile) != null) {
                    resourceContainer = getMineralOn(buildTile);
                }
                else {
                    buildTile = TilePosition.None;
                }
            }
            else if (StateManager.getInstance().mineralCount > 0 && StateManager.getInstance().minSCVCountMineral < 3) {
                resourceContainer = getMineral(true, StateManager.getInstance().minSCVCountMineral);
            }
            else if (BasicBuild.minerals.size() > 0) {
                resourceContainer = getMineral(false, 6); // long distance mining
            }

            if (resourceContainer != null) {
                setResource(BasicBuild.minerals, resourceContainer, +1);
            }
        }

        if (!unit.isIdle()
                && (unit.getOrderTarget() == resourceContainer
                || unit.isCarryingMinerals()
                || unit.isCarryingGas())) {
            return;
        }

        if (resourceContainer != null && resourceContainer.exists()) {
            if (MapUtil.hasGroundConnection(unit.getPosition(), resourceContainer.getPosition())) {
                unit.gather(resourceContainer);
            }
            else {
                move(resourceContainer.getPosition());
            }
        }
    }

    public Unit getMineral(boolean isOwned, int minCount) {
        Unit minRes = null;
        int minSqrtDist = 65536;
        for (BasicBuild.Resource resource : BasicBuild.minerals) {
            if (resource.isOwned == isOwned
                    && !resource.isEnemy
                    && resource.scvCount <= minCount
                    && minSqrtDist > BotUtil.sqDist(unit, resource.unit)) {
                minSqrtDist = BotUtil.sqDist(unit, resource.unit);
                minRes = resource.unit;
            }
        }
        return minRes;
    }

    public Unit getMineralOn(TilePosition tilePosition) {
        for (Unit u : BasicBotAI.BroodWar.getUnitsOnTile(tilePosition)) {
            if (u.getType().isMineralField()) {
                return u;
            }
        }
        return null;
    }

    public void setResource(Unit u, int d) {
        if (u != null) {
            if (u.getType().isMineralField()) {
                setResource(BasicBuild.minerals, u, d);
            }
            else {
                setResource(BasicBuild.geysers, u, d);
            }
        }
    }

    public void setResource(ArrayList<BasicBuild.Resource> v, Unit u, int d) {
        for (BasicBuild.Resource resource : v) {
            if (resource.unit.getID() == u.getID()) {
                resource.scvCount = BotUtil.safeSum(resource.scvCount, d);
                return;
            }
        }
    }

    public Unit getResourceContainer() {
        return resourceContainer;
    }





















}
