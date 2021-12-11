package com.tistory.workshop6349.examplebotT;

import bwapi.*;

import java.util.ArrayList;
import java.util.HashMap;

public class ExampleBot extends DefaultBWListener {

    private BWClient bwClient;
    public static Game BroodWar;

    public static final ArrayList<Worker> WORKERS = new ArrayList<>();
    public static int wantWorkers = 16;
    public static final HashMap<UnitType, Worker> builders = new HashMap<>();

    public void run() {
        bwClient = new BWClient(this);
        bwClient.startGame();
    }

    @Override
    public void onStart() {
        BroodWar = bwClient.getGame();
        BroodWar.setFrameSkip(0);
        BroodWar.setLocalSpeed(10);

        BroodWar.enableFlag(Flag.UserInput);
        BroodWar.setCommandOptimizationLevel(2);

        WORKERS.clear();
    }

    @Override
    public void onEnd(boolean isWinner) {
        System.out.println("I " + (isWinner ? "Won" : "lost") + " the game");
    }

    @Override
    public void onFrame() {
        if (BroodWar.isPaused()) {
            return;
        }

        for (Worker worker : WORKERS) {
            if (worker == null) {
                continue;
            }

            worker.update();
            buildSupply(worker);
            buildGas(worker);
            buildBarracks(worker);
            buildFactory(worker);
        }

        trainWorkers();
        checkBuilder();
    }

    @Override
    public void onUnitComplete(Unit unit) {
        if (unit.getPlayer() != BroodWar.self()) {
            return;
        }

        if (unit.getType().isWorker()) {
            Worker worker = new Worker(unit);
            WORKERS.add(worker);
        }
    }

    @Override
    public void onUnitDestroy(Unit unit) {
        if (unit.getPlayer() != BroodWar.self()) {
            return;
        }

        if (unit.getType().isWorker()) {
            for (Worker worker : WORKERS) {
                if (worker.unit.getID() == unit.getID()) {
                    worker.workerDead();
                    WORKERS.remove(worker);
                    break;
                }
            }
        }
    }

    @Override
    public void onSendText(String text) {
        BroodWar.sendText(text);
        if (text.equals("show")) {
            BroodWar.sendText("show me the money");
        }
    }

    public void trainWorkers() {
        UnitType unitType = ExampleUtil.getWorkerType();
        int currentWorkers = WORKERS.size();

        if (currentWorkers < wantWorkers) {
            Unit depot = null;
            for (Unit unit : BroodWar.self().getUnits()) {
                if (unit == null) {
                    continue;
                }
                if (!unit.exists()) {
                    continue;
                }

                if (unit.getType() != ExampleUtil.getDepotType()) {
                    continue;
                }
                depot = unit;
                break;
            }
            if (depot != null && !depot.isTraining()) {
                depot.train(unitType);
            }
        }
    }

    public void buildSupply(Worker worker) {
        int unUsedSupply = ExampleUtil.getTotalSupply(false) - BroodWar.self().supplyUsed();
        if (unUsedSupply >= 4) {
            return;
        }

        UnitType unitType = ExampleUtil.getSupplyType();
        if (builders.get(unitType) == null && !builders.containsValue(worker)) {
            builders.put(unitType, worker);
            worker.setBuildingsType(unitType);
        }
    }

    public void buildBarracks(Worker worker) {
        UnitType buildingType = UnitType.Terran_Barracks;
        int barracks = ExampleUtil.getTypeCount(buildingType);

        int currentWorkers = WORKERS.size();
        if (currentWorkers < 10) {
            return;
        }

        if (barracks < 1
                && builders.get(buildingType) == null
                && !builders.containsValue(worker)) {
            builders.put(buildingType, worker);
            worker.setBuildingsType(buildingType);
        }
    }

    public void buildGas(Worker worker) {
        UnitType buildingType = UnitType.Terran_Refinery;
        int refinery = ExampleUtil.getTypeCount(buildingType);

        int currentWorkers = WORKERS.size();
        if (currentWorkers < 13) {
            return;
        }

        if (refinery < 1
                && builders.get(buildingType) == null
                && !builders.containsValue(worker)) {
            builders.put(buildingType, worker);
            worker.setBuildingsType(buildingType);
        }
    }

    public void buildFactory(Worker worker) {
        UnitType buildingType = UnitType.Terran_Factory;
        int factory = ExampleUtil.getTypeCount(buildingType);

        int currentWorkers = WORKERS.size();
        if (currentWorkers < 17) {
            return;
        }

        if (factory < 1
                && builders.get(buildingType) == null
                && !builders.containsValue(worker)) {
            builders.put(buildingType, worker);
            worker.setBuildingsType(buildingType);
        }
    }


    public void checkBuilder() {
        for (UnitType buildType : builders.keySet()) {
            Worker currentBuilder = builders.get(buildType);

            if (currentBuilder == null) {
                continue;
            }

            if (!currentBuilder.unit.isConstructing()) {
                currentBuilder.changeJob(currentBuilder.unit, Worker.Jobs.Idle);
                builders.remove(buildType);
                break;
            }
        }
    }

}
