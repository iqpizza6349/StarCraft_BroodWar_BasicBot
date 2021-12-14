package com.tistory.workshop6349.examplebotT;

import bwapi.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Worker {

    public enum Jobs {
        Mineral,
        Gas,
        Idle,
        Scout
    }

    public static final HashMap<Jobs, ArrayList<Unit>> workerJobs = new HashMap<>();
    public static Unit scoutWorker;

    public Unit unit;

    public Worker(Unit unit) {
        this.unit = unit;

        getAllWorkerJobs(Jobs.Idle).add(unit);
    }

    public void update() {
        debugWorker();
        if (getWorkerJob() == Jobs.Idle) {
            // Idle 이라면 미네랄 채취하도록 한다.
            changeJob(Jobs.Mineral);
        }

        if (ExampleUtil.hasRefinery()) {
            if (getAllWorkerJobs(Jobs.Gas).size() < 3) {
                changeJob(Jobs.Gas);
            }
        }

        processWork(unit);
    }

    public void workerDead() {
        getAllWorkerJobs(getWorkerJob()).remove(unit);
    }

    public ArrayList<Unit> getAllWorkerJobs(Jobs job) {
        return workerJobs.computeIfAbsent(job, k -> new ArrayList<>());
    }

    public void changeJob(Jobs jobs) {
        if (getAllWorkerJobs(jobs).contains(unit)) {
            return;
        }

        if (jobs == Jobs.Scout) {
            if (scoutWorker != null) {
                return;
            }
            scoutWorker = unit;
            for (Jobs job : Jobs.values()) {
                getAllWorkerJobs(job).remove(unit);
            }

            getAllWorkerJobs(jobs).add(unit);
            unit.stop();
            return;
        }

        if (getWorkerJob() == Jobs.Scout && jobs != Jobs.Idle) {
            return;
        }

        getAllWorkerJobs(getWorkerJob()).remove(unit);
        getAllWorkerJobs(jobs).add(unit);
        unit.stop();
    }

    public Jobs getWorkerJob() {
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

        switch (getWorkerJob()) {
            case Idle:
                break;
            case Mineral:
                gatherMinerals(unit);
                break;
            case Gas:
                gatherGas(unit);
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

    public void processScout() {

        if (scoutWorker == null) {
            return;
        }

        List<TilePosition> startLocations = ExampleBot.BroodWar.getStartLocations();
        for (TilePosition tp : startLocations) {
            if (ExampleBot.BroodWar.isExplored(tp)) {
                continue;
            }

            Position pos = tp.toPosition();

            scoutWorker.move(pos);
            if (ExampleUtil.checkEnemyBase(scoutWorker.getTilePosition())) {
                ExampleBot.enemyBase = scoutWorker.getPosition();
                System.out.println("상대 본진 위치 알아냄 (" + ExampleBot.enemyBase.toTilePosition().x + ", " + ExampleBot.enemyBase.toTilePosition().y + ")");
            }
            break;
        }

        if (ExampleBot.enemyBase != Position.Unknown && scoutWorker != null) {
            System.out.println("상대 위치: " + ExampleBot.enemyBase);
            scoutWorker.move(ExampleBot.BroodWar.self().getStartLocation().toPosition());
            changeJob(Jobs.Idle);
            scoutWorker = null;
        }

    }

    public void setScoutWorker() {
        changeJob(Jobs.Scout);
    }

    public void debugWorker() {
        ExampleBot.BroodWar.drawTextMap(unit.getPosition(), unit.getID() + "," + getWorkerJob());
    }

}
