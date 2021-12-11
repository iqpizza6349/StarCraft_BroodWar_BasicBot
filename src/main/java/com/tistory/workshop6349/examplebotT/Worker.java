package com.tistory.workshop6349.examplebotT;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

import java.util.ArrayList;
import java.util.HashMap;

public class Worker {

    public enum Jobs {
        Mineral,
        Gas,
        Construction,
        Idle,
    }

    public static final HashMap<Jobs, ArrayList<Unit>> workerJobs = new HashMap<>();

    public Unit unit;
    public UnitType buildingsType;

    public Worker(Unit unit) {
        this.unit = unit;

        getAllWorkerJobs(Jobs.Idle).add(unit);
    }

    public void update() {
        if (getWorkerJob(unit) == Jobs.Idle) {
            // Idle 이라면 미네랄 채취하도록 한다.
            changeJob(unit, Jobs.Mineral);
        }

        if (ExampleUtil.hasRefinery()) {
            if (getAllWorkerJobs(Jobs.Gas).size() < 3) {
                changeJob(unit, Jobs.Gas);
                unit.stop();
            }
        }

        if (buildingsType != null) {
            if (!getAllWorkerJobs(Jobs.Construction).contains(unit)) {
                changeJob(unit, Jobs.Construction);
                unit.stop();
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

        getAllWorkerJobs(getWorkerJob(unit)).remove(unit);
        getAllWorkerJobs(jobs).add(unit);
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

}
