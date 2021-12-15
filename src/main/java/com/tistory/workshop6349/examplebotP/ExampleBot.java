package com.tistory.workshop6349.examplebotP;

import bwapi.*;
import bwem.BWEM;
import bwem.BWMap;

import java.util.ArrayList;
import java.util.HashMap;

public class ExampleBot extends DefaultBWListener {

    private BWClient bwClient;
    public static Game BroodWar;
    public static BWMap map;

    public static final ArrayList<Unit> workers = new ArrayList<>();
    public static final HashMap<UnitType, ArrayList<Unit>> army = new HashMap<>();
    public static final ArrayList<Unit> soldiers = new ArrayList<>();
    public static Unit scoutWorker = null;

    public static boolean StartAttack = false;
    public static boolean ZealotRush = false;
    public static Position waitingPosition = Position.None;
    public static Position enemyBase = Position.Unknown;

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

        BroodWar.setFrameSkip(0);
        BroodWar.setLocalSpeed(5);

        BroodWar.enableFlag(Flag.UserInput);
        BroodWar.setCommandOptimizationLevel(2);

        waitingPosition = map.getArea(BroodWar.self().getStartLocation()).getChokePoints().get(0).getCenter().toPosition();
    }

    @Override
    public void onEnd(boolean isWinner) {
        System.out.println("I " + ((isWinner) ? "Won" : "lost" +" the game"));
    }

    @Override
    public void onFrame() {

        gatherMinerals();

        trainWorker();

        buildPylon();

        buildGateway();

        UnitType trainType = UnitType.Protoss_Zealot;
        if (BroodWar.self().completedUnitCount(UnitType.Protoss_Cybernetics_Core) > 0) {
            trainType =
                    (BroodWar.self().completedUnitCount(UnitType.Protoss_Zealot) > BroodWar.self().completedUnitCount(UnitType.Protoss_Dragoon) * 5)
                            ? UnitType.Protoss_Zealot : UnitType.Protoss_Dragoon;
        }

        trainSoldier(trainType);

        zealotRush();
    }

    @Override
    public void onUnitComplete(Unit unit) {
        if (unit.getPlayer() != BroodWar.self()) {
            return;
        }

        if (unit.getType().isWorker()) {
            workers.add(unit);
        }

        if (unit.getType() == UnitType.Protoss_Zealot
                || unit.getType() == UnitType.Protoss_Dragoon) {
            army.computeIfAbsent(unit.getType(), k -> new ArrayList<>()).add(unit);
            soldiers.add(unit);
        }
    }

    @Override
    public void onUnitDestroy(Unit unit) {
        if (unit.getPlayer() != BroodWar.self()) {
            return;
        }

        if (unit.getType().isWorker()) {
            workers.remove(unit);
        }

        if (unit.getType() == UnitType.Protoss_Zealot
                || unit.getType() == UnitType.Protoss_Dragoon) {
            army.get(unit.getType()).remove(unit);
            soldiers.remove(unit);
        }
    }

    @Override
    public void onSendText(String text) {
        BroodWar.sendText(text);

        if (text.equals("b")) {
            System.out.println(UnitType.Protoss_Pylon.requiresPsi());
        }
    }

    // 자원 채취
    public void gatherMinerals() {

        // 초당 1회씩이면 충분
        if (!ExampleUtil.delay(24)) {
            return;
        }

        for (Unit worker : workers) {
            if (worker == null || !worker.exists()) {
                continue;
            }
            
            if (worker.isGatheringMinerals()) {
                continue;
            }

            Unit closestMineral = ExampleUtil.getClosestUnitTo(worker, BroodWar.getMinerals());

            if (closestMineral != null) {
                worker.gather(closestMineral);
            }
        }
    }

    // 일꾼 생산
    public void trainWorker() {

        // 초당 1회씩이면 충분
        if (!ExampleUtil.delay(24)) {
            return;
        }

        UnitType workerType = UnitType.Protoss_Probe;
        int probeWanted = 10;
        int ownedProbes = ExampleUtil.countUnitType(workerType);

        if (ownedProbes < probeWanted) {
            for (Unit nexus : BroodWar.self().getUnits()) {
                if (nexus == null || !nexus.exists()) {
                    continue;
                }

                if (!nexus.isTraining()) {
                    nexus.train(workerType);
                }
            }
        }
    }

    // 파일런 건설
    public void buildPylon() {
        final int unUsedSupply = ExampleUtil.getTotalSupply(true) - BroodWar.self().supplyUsed();

        UnitType pylon = UnitType.Protoss_Pylon;
        int isFirstPylon = (ExampleUtil.countUnitType(pylon) < 1) ? 4 : 8;

        if (unUsedSupply >= isFirstPylon) {
            return;
        }

        ExampleUtil.buildBuildings(pylon);
    }

    // 게이트 건설
    public void buildGateway() {
        UnitType gateway = UnitType.Protoss_Gateway;
        int gatewayCount = ExampleUtil.countUnitType(gateway);

        if (gatewayCount > 2) {
            return;
        }

        ExampleUtil.buildBuildings(gateway);
    }

    // 질럿 생산
    public void trainSoldier(UnitType unitType) {

        if (unitType == null) {
            return;
        }

        for (Unit trainableBuilding : BroodWar.self().getUnits()) {
            if (trainableBuilding == null || !trainableBuilding.exists()) {
                continue;
            }

            if (!trainableBuilding.getType().isBuilding()) {
                continue;
            }

            if (!trainableBuilding.canTrain(unitType)) {
                continue;
            }
            
            if (!trainableBuilding.isTraining()) {
                trainableBuilding.train(unitType);
            }
        }

    }

    // 공격
    public void zealotRush() {

        if (!ExampleUtil.delay(24)) {
            return;
        }

        if (!StartAttack) {
            // 공격 가기 전에는 첫 번째 길목에서 대기
            for (UnitType type : army.keySet()) {
                for (Unit soldier : army.get(type)) {
                    if (soldier == null || !soldier.exists()) {
                        continue;
                    }

                    ExampleUtil.attackMove(soldier, waitingPosition);
                }
            }

            if (!ZealotRush) {
                for (UnitType type : army.keySet()) {
                    if (type == UnitType.Protoss_Zealot && army.get(type).size() > 4) {
                        ZealotRush = true;
                        return;
                    }
                }
            }
            else {
                StartAttack = soldiers.size() > 15;

                attack();
            }

        }
        else {
            attack();
        }
    }

    public void attack() {
        for (Unit soldier : soldiers) {
            if (soldier == null || !soldier.exists()) {
                continue;
            }

            ExampleUtil.attackMove(soldier, enemyBase);
        }
    }







}
