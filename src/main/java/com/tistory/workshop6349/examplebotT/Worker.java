package com.tistory.workshop6349.examplebotT;

import bwapi.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Worker {

    public enum Jobs {
        Mineral,
        Gas,
        Construction,
        Idle,
        Scout
    }

    public static final HashMap<Jobs, ArrayList<Unit>> workerJobs = new HashMap<>();
    public static Unit scoutWorker;

    public Unit unit;
    public UnitType buildingsType;

    public Worker(Unit unit) {
        this.unit = unit;

        getAllWorkerJobs(Jobs.Idle).add(unit);
    }

    public void update() {
        debugWorker();
        if (getWorkerJob(unit) == Jobs.Idle) {
            // Idle 이라면 미네랄 채취하도록 한다.
            changeJob(unit, Jobs.Mineral);
        }

        if (ExampleUtil.hasRefinery()) {
            if (getAllWorkerJobs(Jobs.Gas).size() < 3) {
                changeJob(unit, Jobs.Gas);
            }
        }

        if (buildingsType != null) {
            if (!getAllWorkerJobs(Jobs.Construction).contains(unit)) {
                changeJob(unit, Jobs.Construction);
            }
        }

        processWork(unit);
    }

    public void workerDead() {
        getAllWorkerJobs(getWorkerJob(unit)).remove(unit);
    }

    public ArrayList<Unit> getAllWorkerJobs(Jobs job) {
        return workerJobs.computeIfAbsent(job, k -> new ArrayList<>());
    }

    public void changeJob(Unit unit, Jobs jobs) {
        if (getAllWorkerJobs(jobs).contains(unit)) {
            return;
        }

        if (getWorkerJob(unit) == Jobs.Scout) {
            return;
        }

        getAllWorkerJobs(getWorkerJob(unit)).remove(unit);
        getAllWorkerJobs(jobs).add(unit);
        unit.stop();
    }

    public Jobs getWorkerJob(Unit unit) {
        for (Jobs jobs : Jobs.values()) {
            if (getAllWorkerJobs(jobs).contains(unit)) {
                return jobs;
            }
        }

        return Jobs.Idle;
    }

    public void processWork(Unit unit) {
        if (unit == null) {
            return;
        }

        if (!unit.exists()) {
            return;
        }
        
        if (!unit.isIdle()) {
            return;
        }

        switch (getWorkerJob(unit)) {
            case Idle:
                break;
            case Mineral:
                gatherMinerals(unit);
                break;
            case Gas:
                gatherGas(unit);
                break;
            case Construction:
                processBuild(unit);
                break;
            case Scout:
                processScout();
                break;
        }

    }

    public void gatherMinerals(Unit unit) {
        Unit closestMineral = ExampleUtil.getClosestUnitTo(unit, ExampleBot.BroodWar.getMinerals());

        if (closestMineral != null) {
            unit.gather(closestMineral);
        }
    }

    public void gatherGas(Unit unit) {

        if (!ExampleUtil.hasRefinery()) {
            return;
        }

        Unit closestGas = ExampleUtil.getClosestUnitTo(unit, ExampleBot.BroodWar.getStaticGeysers());

        if (closestGas != null) {
            unit.gather(closestGas);
        }
    }

    public void processBuild(Unit unit) {
        if (buildingsType == null) {
            return;
        }

        TilePosition desiredPos = ExampleBot.BroodWar.self().getStartLocation();

        int maxBuildingRange = 64;
        boolean buildingOnCreep = buildingsType.requiresCreep();
        TilePosition buildPos = ExampleBot.BroodWar.getBuildLocation(buildingsType, desiredPos, maxBuildingRange, buildingOnCreep);
        if (unit.build(buildingsType, buildPos)) {
            buildingsType = null;
        }
    }

    public void setBuildingsType(UnitType unitType) {
        if (this.buildingsType == unitType) {
            return;
        }

        this.buildingsType = unitType;
    }

    public void processScout() {

        if (scoutWorker != null) {
            return;
        }

        List<TilePosition> startLocations = ExampleBot.BroodWar.getStartLocations();
        for (TilePosition tp : startLocations) {
            if (ExampleBot.BroodWar.isExplored(tp)) {
                continue;
            }

            Position pos = tp.toPosition();
            ExampleBot.BroodWar.drawCircleMap(pos, 4 * 32, Color.Orange, true);

            scoutWorker.move(pos);
            if (ExampleUtil.checkEnemyBase(scoutWorker.getTilePosition())) {
                ExampleBot.enemyBase = scoutWorker.getPosition();
                System.out.println("상대 본진 위치 알아냄 (" + ExampleBot.enemyBase.x + ", " + ExampleBot.enemyBase.y + ")");
            }
            break;
        }

        if (ExampleBot.enemyBase != Position.Unknown) {
            scoutWorker.move(ExampleBot.BroodWar.self().getStartLocation().toPosition());
            changeJob(scoutWorker, Jobs.Idle);
            scoutWorker = null;
        }

    }

    public void setScoutWorker() {
        scoutWorker = unit;
        changeJob(scoutWorker, Jobs.Scout);
    }

    public void debugWorker() {
        ExampleBot.BroodWar.drawTextMap(unit.getPosition(), String.valueOf(unit.getID() + "," + getWorkerJob(unit)));
    }

}
