package com.tistory.workshop6349.examplebotT;

import bwapi.*;
import bwem.BWEM;
import bwem.BWMap;

import java.util.ArrayList;

public class ExampleBot extends DefaultBWListener {

    private BWClient bwClient;
    public static Game BroodWar;
    public static BWMap map;

    public static final ArrayList<Worker> WORKERS = new ArrayList<>();
    public static int wantWorkers = 16;

    public static Position enemyBase = Position.Unknown;

    public static Unit builder;
    public static Unit currentBuilding;

    public static final ArrayList<Vulture> VULTURES = new ArrayList<>();

    public void run() {
        bwClient = new BWClient(this);
        bwClient.startGame();
    }

    @Override
    public void onStart() {
        BroodWar = bwClient.getGame();
        BWEM bwem = new BWEM(BroodWar);
        bwem.initialize();
        map = bwem.getMap();
        map.enableAutomaticPathAnalysis();
        BroodWar.setFrameSkip(0);
        BroodWar.setLocalSpeed(5);

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

        updateEnemy();

        if (BroodWar.getFrameCount() % 24 != 0) {
            return;
        }

        for (Worker worker : WORKERS) {
            if (worker == null) {
                continue;
            }

            worker.update();
            buildSupply(worker);
            buildBarracks(worker);
            buildGas(worker);
            buildFactory(worker);
        }

        for (Vulture vulture : VULTURES) {
            if (vulture == null) {
                continue;
            }

            vulture.actionExecute();
        }

        updateBuilding();
        trainUnit(UnitType.Terran_Vulture);
        trainWorkers();
        scoutWorker();
        drawInfo();
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

        if (unit.getType() == UnitType.Terran_Vulture) {
            Vulture vulture = new Vulture(unit);
            VULTURES.add(vulture);
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

    public void updateBuilding() {
        for (Unit building : BroodWar.self().getUnits()) {
            if (building.isCompleted() || building.getType().isBuilding()) {
                continue;
            }

            if (building.isBeingConstructed()) {
                currentBuilding = building;
                break;
            }
        }

        if (currentBuilding == null) {
            return;
        }

        if (currentBuilding.isCompleted()) {
            currentBuilding = null;
        }
    }


    public TilePosition desiredPosition() {
        return BroodWar.self().getStartLocation();
    }

    public void buildSupply(Worker worker) {
        int unUsedSupply = ExampleUtil.getTotalSupply(true) - BroodWar.self().supplyUsed();
        if (unUsedSupply > 4) {
            return;
        }

        if (worker.getWorkerJob() == Worker.Jobs.Scout) {
            return;
        }

        UnitType unitType = ExampleUtil.getSupplyType();

        if (builder == null) {
            builder = worker.unit;
        }

        if (BroodWar.self().minerals() < unitType.mineralPrice()
                || BroodWar.self().gas() < unitType.gasPrice()) {
            return;
        }

        if (currentBuilding != null) {
            return;
        }

        int maxBuildingRange = 64;
        boolean buildingOnCreep = unitType.requiresCreep();
        TilePosition buildPos = BroodWar.getBuildLocation(unitType, desiredPosition(), maxBuildingRange, buildingOnCreep);
        builder.build(unitType, buildPos);
    }

    public void buildBarracks(Worker worker) {

        if (worker.getWorkerJob() == Worker.Jobs.Scout) {
            return;
        }

        UnitType unitType = UnitType.Terran_Barracks;

        if (currentBuilding != null) {
            return;
        }

        if (BroodWar.self().completedUnitCount(UnitType.Terran_Supply_Depot) < 1
                || BroodWar.self().completedUnitCount(unitType) > 0) {
            return;
        }

        if (BroodWar.self().minerals() < unitType.mineralPrice()
                || BroodWar.self().gas() < unitType.gasPrice()) {
            return;
        }

        if (builder == null) {
            builder = worker.unit;
        }

        int maxBuildingRange = 64;
        boolean buildingOnCreep = unitType.requiresCreep();
        TilePosition buildPos = BroodWar.getBuildLocation(unitType, desiredPosition(), maxBuildingRange, buildingOnCreep);
        builder.build(unitType, buildPos);
    }

    public void buildGas(Worker worker) {

        if (worker.getWorkerJob() == Worker.Jobs.Scout) {
            return;
        }

        UnitType unitType = UnitType.Terran_Refinery;

        if (builder == null) {
            builder = worker.unit;
        }

        if (currentBuilding != null) {
            return;
        }

        if (BroodWar.self().completedUnitCount(UnitType.Terran_Barracks) < 1
                || BroodWar.self().completedUnitCount(unitType) > 0) {
            return;
        }

        if (BroodWar.self().minerals() < unitType.mineralPrice()
                || BroodWar.self().gas() < unitType.gasPrice()) {
            return;
        }
        int maxBuildingRange = 64;
        boolean buildingOnCreep = unitType.requiresCreep();
        TilePosition buildPos = BroodWar.getBuildLocation(unitType, desiredPosition(), maxBuildingRange, buildingOnCreep);
        builder.build(unitType, buildPos);
    }

    public void buildFactory(Worker worker) {

        if (worker.getWorkerJob() == Worker.Jobs.Scout) {
            return;
        }

        UnitType unitType = UnitType.Terran_Factory;

        if (currentBuilding != null) {
            return;
        }

        if (BroodWar.self().completedUnitCount(UnitType.Terran_Refinery) < 1
                || BroodWar.self().completedUnitCount(unitType) > 0) {
            return;
        }

        if (BroodWar.self().minerals() < unitType.mineralPrice()
                || BroodWar.self().gas() < unitType.gasPrice()) {
            return;
        }

        if (builder == null) {
            builder = worker.unit;
        }

        int maxBuildingRange = 64;
        boolean buildingOnCreep = unitType.requiresCreep();
        TilePosition buildPos = BroodWar.getBuildLocation(unitType, desiredPosition(), maxBuildingRange, buildingOnCreep);
        builder.build(unitType, buildPos);
    }

    public void scoutWorker() {
        // 보급고 건설 시작과 동시에 바로 정찰 지정해서 정찰함

        boolean availableToScout = false;
        for (Unit u : BroodWar.self().getUnits()) {
            if (u.getType().isBuilding() && u.getType() != BroodWar.self().getRace().getResourceDepot()) {
                availableToScout = true;
                break;
            }
        }

        if (!availableToScout) {
            return;
        }

        if (Worker.scoutWorker != null) {
            return;
        }

        if (enemyBase != Position.Unknown) {
            return;
        }

        for (Worker worker : WORKERS) {
            if (worker.getWorkerJob() != Worker.Jobs.Mineral) {
                continue;
            }

            if (worker.unit.getID() == builder.getID()) {
                continue;
            }

            worker.setScoutWorker();
            System.out.println("정찰 유닛 지정함: " + Worker.scoutWorker.getID());
            break;
        }
    }

    public void updateEnemy() {
        if (Worker.scoutWorker == null) {
            return;
        }

        if (enemyBase != Position.Unknown) {
            return;
        }

        if (ExampleUtil.checkEnemyBase(Worker.scoutWorker.getTilePosition())) {
            ExampleBot.enemyBase = Worker.scoutWorker.getPosition();
            System.out.println("상대 본진 위치 알아냄 (" + ExampleBot.enemyBase.toTilePosition().x + ", " + ExampleBot.enemyBase.toTilePosition().y + ")");
        }
    }

    public void trainUnit(UnitType unitType) {
        // 유닛 생산
        if (unitType == null) {
            return;
        }
        
        // 벌처 8기 모이면 러쉬
        if (VULTURES.size() > 8) {
            return;
        }

        for (Unit fac : BroodWar.self().getUnits()) {
            if (!fac.isCompleted() && fac.getType() != UnitType.Terran_Factory) {
                continue;
            }

            if (!fac.isTraining()) {
                fac.train(unitType);
            }
        }

    }

    public void drawInfo() {
        if (enemyBase != Position.Unknown && enemyBase.isValid(BroodWar)) {
            BroodWar.drawCircleMap(enemyBase, 32, Color.Cyan, true);
        }

        if (Worker.scoutWorker != null) {
            BroodWar.drawCircleMap(Worker.scoutWorker.getPosition(), 128, Color.Black);
        }
    }

}
