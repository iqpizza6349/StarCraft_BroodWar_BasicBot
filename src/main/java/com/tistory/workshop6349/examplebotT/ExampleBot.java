package com.tistory.workshop6349.examplebotT;

import bwapi.*;
import bwem.BWEM;
import bwem.BWMap;

import java.util.ArrayList;
import java.util.Random;

public class ExampleBot extends DefaultBWListener {

    private BWClient bwClient;
    public static Game BroodWar;
    public static BWMap map;

    public static final ArrayList<Worker> WORKERS = new ArrayList<>();
    public static int wantWorkers = 16;

    public static Position enemyBase = Position.Unknown;
    public static Position TempPlace = Position.None;

//    public static Unit builder;
//    public static Unit currentBuilding;

    public static final ArrayList<Vulture> VULTURES = new ArrayList<>();
    public static final ArrayList<Unit> MARINES = new ArrayList<>();
    public static final ArrayList<Unit> MEDICS = new ArrayList<>();

    public static boolean StartAttack = false;

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

        TempPlace = map.getArea(BroodWar.self().getStartLocation()).getChokePoints().get(0).getCenter().toPosition();
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
        }

        if (BroodWar.getFrameCount() % 48 == 0) {
            Worker worker = WORKERS.get(new Random().nextInt(WORKERS.size() - 1));

            buildSupply(worker);
            buildBarracks(worker);
            buildAcademy(worker);
            buildGas(worker);
            buildFactory(worker);
        }


        if (StartAttack) {

            for (Vulture vulture : VULTURES) {
                if (vulture == null) {
                    continue;
                }

                vulture.actionExecute();
            }

            for (Unit unit : MARINES) {
                ExampleUtil.attackMove(unit, enemyBase);
            }
        }

        //updateBuilding();

        UnitType marineOrMedic = UnitType.Terran_Marine;

        if (BroodWar.self().completedUnitCount(UnitType.Terran_Academy) > 0) {
            marineOrMedic = (MARINES.size() > MEDICS.size() * 4) ? UnitType.Terran_Medic : UnitType.Terran_Marine;
        }
        trainUnit(marineOrMedic);
        trainUnit(UnitType.Terran_Vulture);
        trainWorkers();
        scoutWorker();

        if (!StartAttack) {
            if (MARINES.size() > 25) {
                StartAttack = true;
            }
            for (Unit unit : MARINES) {
                ExampleUtil.attackMove(unit, TempPlace);
            }
        }

        for (Unit u : MEDICS) {
            Position goPlace = (StartAttack) ? enemyBase : TempPlace;
            ExampleUtil.move(u, goPlace);
        }
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
        
        if (unit.getType() == UnitType.Terran_Marine) {
            MARINES.add(unit);
        }

        if (unit.getType() == UnitType.Terran_Medic) {
            MEDICS.add(unit);
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

        if (unit.getType() == UnitType.Terran_Vulture) {
            for (Vulture deadVulture : VULTURES) {
                if (deadVulture.vulture.getID() == unit.getID()) {
                    VULTURES.remove(deadVulture);
                    break;
                }
            }
        }

        if (unit.getType() == UnitType.Terran_Marine) {
            MARINES.remove(unit);
        }

        if (unit.getType() == UnitType.Terran_Medic) {
            MEDICS.remove(unit);
        }
    }

    @Override
    public void onSendText(String text) {
        BroodWar.sendText(text);
        if (text.equals("show")) {
            BroodWar.sendText("show me the money");
        }
        else if (text.equals("modify")) {
            BroodWar.sendText("show me the money");
            BroodWar.sendText("Modify the phase variance");
            BroodWar.sendText("Food for thought");
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
//        for (Unit building : BroodWar.self().getUnits()) {
//            if (building.isCompleted() || building.getType().isBuilding()) {
//                continue;
//            }
//
//            if (building.isBeingConstructed()) {
//                currentBuilding = building;
//            }
//        }
//
//        if (currentBuilding == null) {
//            return;
//        }
//
//        if (currentBuilding.isCompleted()) {
//            currentBuilding = null;
//        }
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

//        if (builder == null) {
//            builder = worker.unit;
//        }

        if (BroodWar.self().minerals() < unitType.mineralPrice()
                || BroodWar.self().gas() < unitType.gasPrice()) {
            return;
        }

//        if (currentBuilding != null) {
//            return;
//        }

        buildSomething(worker.unit, unitType);
    }

    public void buildBarracks(Worker worker) {

        if (worker.getWorkerJob() == Worker.Jobs.Scout) {
            return;
        }

        UnitType unitType = UnitType.Terran_Barracks;

        if (BroodWar.self().completedUnitCount(UnitType.Terran_Supply_Depot) < 1
                || BroodWar.self().completedUnitCount(unitType) > 1) {
            return;
        }

        if (BroodWar.self().minerals() < unitType.mineralPrice()) {
            return;
        }

//        if (builder == null) {
//            builder = worker.unit;
//        }
//
//        if (currentBuilding != null) {
//            return;
//        }

        buildSomething(worker.unit, unitType);
    }

    public void buildAcademy(Worker worker) {
        if (worker.getWorkerJob() == Worker.Jobs.Scout) {
            return;
        }

        UnitType unitType = UnitType.Terran_Academy;

        if (BroodWar.self().completedUnitCount(UnitType.Terran_Barracks) < 1
                || BroodWar.self().completedUnitCount(unitType) > 0) {
            return;
        }

        if (BroodWar.self().minerals() < unitType.mineralPrice()) {
            return;
        }

//        if (builder == null) {
//            builder = worker.unit;
//        }
//
//        if (currentBuilding != null) {
//            return;
//        }

        buildSomething(worker.unit, unitType);
    }

    public void buildGas(Worker worker) {

        if (worker.getWorkerJob() == Worker.Jobs.Scout) {
            return;
        }

        UnitType unitType = UnitType.Terran_Refinery;

//        if (builder == null) {
//            builder = worker.unit;
//        }

        if (BroodWar.self().completedUnitCount(UnitType.Terran_Barracks) < 1
                || BroodWar.self().completedUnitCount(unitType) > 0) {
            return;
        }

        if (BroodWar.self().minerals() < unitType.mineralPrice()
                || BroodWar.self().gas() < unitType.gasPrice()) {
            return;
        }

//        if (currentBuilding != null) {
//            return;
//        }

        buildSomething(worker.unit, unitType);
    }

    public void buildFactory(Worker worker) {

        if (worker.getWorkerJob() == Worker.Jobs.Scout) {
            return;
        }

        UnitType unitType = UnitType.Terran_Factory;

        if (BroodWar.self().completedUnitCount(UnitType.Terran_Refinery) < 1
                || BroodWar.self().completedUnitCount(unitType) > 0
                || BroodWar.self().completedUnitCount(UnitType.Terran_Barracks) < 1) {
            return;
        }

        if (BroodWar.self().minerals() < unitType.mineralPrice()
                || BroodWar.self().gas() < unitType.gasPrice()) {
            return;
        }
//
//        if (builder == null) {
//            builder = worker.unit;
//        }
//
//        if (currentBuilding != null) {
//            return;
//        }

        buildSomething(worker.unit, unitType);
    }

    public void buildSomething(Unit worker, UnitType unitType) {
        worker.build(unitType, BroodWar.getBuildLocation(unitType, desiredPosition(), 64, false));
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

            if (worker.unit.isConstructing()) {
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

        for (Unit fac : BroodWar.self().getUnits()) {
            if (!fac.isCompleted() || !fac.getType().isBuilding()) {
                continue;
            }

            if (!fac.isTraining()) {
                if (fac.canTrain(unitType)) {
                    fac.train(unitType);
                }
            }
        }

    }

}
