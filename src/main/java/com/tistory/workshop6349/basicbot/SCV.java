package com.tistory.workshop6349.basicbot;

import bwapi.*;

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

    public void updateMilitia() {
        Position pos = unit.getPosition();
        if (unit.isStartingAttack()
                || unit.isAttackFrame()
                || unit.isRepairing()
                || BasicBotAI.BroodWar.getFrameCount() - unit.getLastCommandFrame() <= 10) {
            return;
        }

        if ((StateManager.getInstance().avoidGridDef
                && MicroUtil.reachingArea(unit, ThreatManager.groundDef)
                || MicroUtil.reachingArea(unit, ThreatManager.groundMap))) {
            unit.move(securePos);
            return;
        }

        Unit target = getTargetSCV(16384);
        if (target != null && target.exists()) {
            unit.attack(target);
        }
        else if (BotUtil.sqDist(pos, attackPos) > 16384) {
            unit.move(attackPos);
        }
    }

    public void setBuildMission(UnitType type, TilePosition tile) {
        endMining();
        isBuilder = true;
        buildType = type;
        buildTile = tile;

        x0 = 32 * tile.x;
        y0 = 32 * tile.y;
        x1 = x0 + 32 * buildType.tileWidth() - 1;
        y1 = y0 + 32 * buildType.tileHeight() - 1;

        buildPos = new Position((x0 + x1) / 2, (y0 + y1) / 2);
        buildQueue = 128 + 4 * unit.getPosition().getApproxDistance(buildPos);
        buildSqrtDist = 2304;

        if (type == UnitType.Terran_Missile_Turret) {
            buildQueue += 8192;
            MapUtil.mapSafeSub(y0, 16);
        }
        if (type == UnitType.Terran_Refinery) {
            buildSqrtDist = 9216;
        }
        blockCount = 0;
        BasicBuild.plannedBuildings.add(type);
    }

    public void endBuildMission() {
        BasicBuild.plannedBuildings.remove(buildType);
        isBuilder = false;
        isJanitor = false;
        buildType = UnitType.None;
        buildTile = TilePosition.None;
        buildPos = Position.None;
        buildQueue = 0;
        blockCount = 0;
    }

    public void build() {
        if (buildQueue > 128) {
            if (unit.isUnderAttack() || startedConstruction()) {
                buildQueue = 128;
            }
            if (BotUtil.sqDist(unit.getPosition(), buildPos) > buildSqrtDist && !isJanitor) {
                unit.move(buildPos);
                buildQueue--;
            }
            else {
                ArrayList<Unit> blockers = getBlockers();
                clearBuildTiles(blockers);
                checkBuildBlock(blockers);
                if (isJanitor) {
                    buildQueue--;
                }
                else {
                    unit.build(buildType, buildTile);
                    buildQueue -= 8;
                }
            }
        }
        else if (buildQueue > 0) {
            buildQueue--;
            if (buildQueue < 96 && !unit.isConstructing()) {
                endBuildMission();
            }
        }
        else {
            if (buildType.isRefinery()) {
                resourceContainer = BasicBuild.refineries.get(0); // 원래는 맨 마지막 인덱스를 사용하는 거지만 걍 0번째 함
                setResource(BasicBuild.geysers, resourceContainer, +1);
            }
            endBuildMission();
        }
        drawBuildInfo();
    }

    public boolean startedConstruction() {
        for (Unit u : BasicBuild.buildings) {
            if (buildType == u.getType() && buildTile.equals(u.getTilePosition())) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Unit> getBlockers() {
        ArrayList<Unit> v = new ArrayList<>();
        for (Unit u : BasicBotAI.BroodWar.getUnitsInRectangle(x0, y0, x1, y1)) {
            if (u.getID() != unit.getID()
                    && !u.isFlying()
                    && !u.getType().isBuilding()) {
                v.add(u);
            }
        }
        if (buildType == UnitType.Terran_Command_Center) {
            for (Unit u : BasicBotAI.BroodWar.getUnitsInRectangle(x0 - 96, y0 - 96, x1 + 96, y1 + 96)) {
                if (u.getType().isMineralField() && u.getResources() <= 8) {
                    v.add(u);
                }
            }
        }
        
        return v;
    }

    public void clearBuildTiles(ArrayList<Unit> v) {
        int dx = 0;
        int dy = 0;

        switch ((BasicBotAI.BroodWar.getFrameCount() * 128) % 4) {
            case 0:
                dx = 128;
            case 1:
                dy = 128;
            case 2:
                dx = -128;
            case 3:
                dy = -128;
        }
        
        for (Unit blocker : v) {
            if (blocker.getPlayer() == BasicBotAI.BroodWar.self()) {
                int x = blocker.getPosition().x + dx;
                int y = blocker.getPosition().y + dy;
                blocker.move(new Position(x, y));
            }
            if (blocker.getType().isMineralField() && blocker.getResources() <= 8) {
                if (!unit.isGatheringMinerals()) {
                    unit.gather(blocker);
                }
                isJanitor = true;
                return;
            }
            if (blocker.isBurrowed()) {
                if (!unit.isAttacking()) {
                    unit.attack(blocker);
                }
                isJanitor = true;
                return;
            }
        }
        isJanitor = false;
    }

    public void checkBuildBlock(ArrayList<Unit> v) {
        if (BasicBotAI.BroodWar.self().minerals() >= buildType.mineralPrice()
                && BasicBotAI.BroodWar.self().gas() >= buildType.gasPrice()
                && unit.canBuild(buildType)
                && v.isEmpty()
                && !BasicBotAI.BroodWar.hasCreep(buildTile)) {
            ++blockCount;
        }
        if (blockCount > 16) {
            BasicBuild.blockedTile = buildTile;
        }
    }

    public void drawBuildInfo() {
        BasicBotAI.BroodWar.drawBoxMap(x0, y0, x1, y1, Color. Brown, false);
        BasicBotAI.BroodWar.drawTextMap(buildTile.toPosition(), buildType.toString());
        BasicBotAI.BroodWar.drawTextMap(unit.getPosition(), String.valueOf(buildQueue));
    }

    public void setRepairMission(Unit u) {
        endMining();
        isRepair = true;
        damagedUnit = u;
        unit.repair(u);
    }

    public void updateRepair() {
        if (!damagedUnit.exists()
                || damagedUnit.getHitPoints() == damagedUnit.getType().maxHitPoints()
                || (BasicBotAI.BroodWar.self().minerals() == 0 && BotUtil.sqDist(unit, damagedUnit) <= 9216)) {
            if (!StateManager.getInstance().holdBunker) {
                isRepair = false;
            }
        }

        if (BasicBotAI.BroodWar.getFrameCount() - unit.getLastCommandFrame() >= 24
                && !unit.isRepairing()
                && damagedUnit.exists()
                && damagedUnit.getHitPoints() < damagedUnit.getType().maxHitPoints()) {
            unit.repair(damagedUnit);
        }
    }

    public void scout() {
        if (!BasicMap.unScouted.isEmpty()) {
            if (BasicMap.unScouted.contains(unit.getTargetPosition())) {
                scoutPos = BasicMap.unScouted.get(0); // 원래는 맨 마지막 인덱스를 사용하는 거지만 걍 0번째 함
                unit.move(scoutPos);
            }
        }
        else {
            if (EnemyManager.getInstance().sem == -1
                    && !unit.getTargetPosition().equals(BasicMap.myNatural)) {
                unit.move(BasicMap.myNatural);
                isScout = false;
                return;
            }
            if (StateManager.getInstance().needScoutNatural
                    && circleIncrement == BasicMap.entranceCircleIncrement - 1) {
                scoutPos = BasicMap.enemyNatural;
                unit.move(scoutPos);
                StateManager.getInstance().needScoutNatural = false;
            }
            if (circleIncrement == -1) {
                // initiate circling
                circleIncrement = (BasicMap.entranceCircleIncrement != -1) ? BasicMap.entranceCircleIncrement : 0;
                scoutCircle();
            }
            if (BotUtil.sqDist(unit.getPosition(), scoutPos) < 16384) {
                // continue circle
                scoutCircle();
            }
        }
    }

    public void scoutCircle() {
        circleIncrement = (circleIncrement + 1) % 24;
        scoutPos = MapUtil.getCirclePos24(EnemyManager.getInstance().sem, 14, circleIncrement);
        unit.move(scoutPos);
    }
    
    public Unit getTargetSCV(int r) {
        Unit u = null;
        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlgg, u, unit.getPosition(), r);
        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlga, u, unit.getPosition(), r);
        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlg, u, unit.getPosition(), r);

        return u;
    }

    public int checkingBeingTrapped(int count) {
        stuckQueue++;
        isTrapped = false;

        if (!unit.isCarryingMinerals()
                && !unit.isCarryingGas()
                && hasResource) {
            hasResource = false;
            stuckQueue = 0;
            minTargetDistance = 0;
        }
        if ((unit.isCarryingMinerals() && !hasResource)
                || (unit.isCarryingGas() && !hasResource)) {
            hasResource = true;
            stuckQueue = 0;
            minTargetDistance = 0;
        }
        if (unit.getTransport() != null
                || unit.getBuildUnit() != null
                || unit.isAttackFrame()
                || isScout) {
            stuckQueue = 0;
            minTargetDistance = 0;
        }
        if (calledTransport && unit.getTransport() != null) {
            calledTransport = false;
            isEntering = false;
            transportUnit = null;
        }

        destination = MicroUtil.getCurrentTargetPosition(unit);
        if (stuckQueue > 0 && !BotUtil.isNone(destination)) {
            int mySqrtDist = BotUtil.sqDist(unit.getPosition(), destination);
            if (minTargetDistance == 0) {
                minTargetDistance = mySqrtDist;
            }
            if (mySqrtDist < minTargetDistance) {
                minTargetDistance = mySqrtDist;
                stuckQueue = 0;
            }
            if (stuckQueue > 31) {
                isTrapped = true;
                count++;
            }
        }

        if (transportUnit != null
                && transportUnit.exists()
                && BotUtil.sqDist(transportUnit, unit) < 36) {
            isEntering = true;
            unit.rightClick(transportUnit);
        }

        return count;
    }



}
