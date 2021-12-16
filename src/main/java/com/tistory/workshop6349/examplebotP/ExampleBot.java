package com.tistory.workshop6349.examplebotP;

import bwapi.*;
import bwem.BWEM;
import bwem.BWMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExampleBot extends DefaultBWListener {

    private BWClient bwClient;
    public static Game BroodWar;
    public static BWMap map;

    public static final ArrayList<Unit> workers = new ArrayList<>();
    public static final HashMap<UnitType, ArrayList<Unit>> army = new HashMap<>();
    public static final ArrayList<Unit> soldiers = new ArrayList<>();
    public static Unit scoutWorker = null;

    public static Unit gasWorker1;
    public static Unit gasWorker2;

    public static boolean StartAttack = false;
    public static boolean ZealotRush = false;
    public static int deadZealots = 0;
    public static Position waitingPosition = Position.None;
    public static Position enemyBase = Position.Unknown;
    public static boolean hasBrokeCenter = false; // 적군 depot 파괴 여부
    public static ArrayList<TilePosition> bases = new ArrayList<>();

    public static final ArrayList<Unit> leftBuildings = new ArrayList<>();

    public void run() {
        bwClient = new BWClient(this);
        bwClient.startGame();
    }

    @Override
    public void onStart() {
        BroodWar = bwClient.getGame();
        BWEM bwem = new BWEM(BroodWar);
        System.out.println("전부 초기화 준비");
        bwem.initialize();
        map = bwem.getMap();
        map.enableAutomaticPathAnalysis();

        leftBuildings.clear();
        workers.clear();
        army.clear();
        soldiers.clear();
        scoutWorker = null;
        gasWorker1 = null;
        gasWorker2 = null;
        StartAttack = false;
        ZealotRush = false;
        deadZealots = 0;
        hasBrokeCenter = false;
        waitingPosition = Position.None;
        enemyBase = Position.Unknown;
        bases.clear();

        System.out.println("전부 초기화 완료");
        BroodWar.setFrameSkip(0);
        BroodWar.setLocalSpeed(5);

        BroodWar.enableFlag(Flag.UserInput);
        BroodWar.setCommandOptimizationLevel(2);

        waitingPosition = map.getArea(BroodWar.self().getStartLocation()).getChokePoints().get(0).getCenter().toPosition();
        System.out.println("게임 시작");
        bases.addAll(BroodWar.getStartLocations());
    }

    @Override
    public void onEnd(boolean isWinner) {
        System.out.println("I " + ((isWinner) ? "Won" : "lost" +" the game"));
    }

    @Override
    public void onFrame() {

        gatherMinerals();

        gatherGas();

        trainWorker();

        if (ExampleUtil.delay(72)) {
            buildPylon();
            buildGateway();
            buildGas();
            buildCore();
        }

        scoutUpdate();

        if (ExampleUtil.delay(24)) {
            UnitType trainType = UnitType.Protoss_Zealot;
            if (BroodWar.self().completedUnitCount(UnitType.Protoss_Cybernetics_Core) > 0) {
                trainType =
                        (BroodWar.self().completedUnitCount(UnitType.Protoss_Zealot) > BroodWar.self().completedUnitCount(UnitType.Protoss_Dragoon) * 3)
                                ? UnitType.Protoss_Zealot : UnitType.Protoss_Dragoon;
            }

            trainSoldier(trainType);
        }

        zealotRush();
    }

    @Override
    public void onUnitComplete(Unit unit) {
        if (unit.getPlayer() != BroodWar.self()) {
            return;
        }

        if (unit.getType().isWorker()) {
            workers.add(unit);

            if (gasWorker1 == null) {
                gasWorker1 = unit;
            }
            else if (gasWorker2 == null) {
                gasWorker2 = unit;
            }

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

            if (unit.getPlayer() == BroodWar.enemy()) {
                if (unit.getType() == BroodWar.enemy().getRace().getResourceDepot()) {
                    hasBrokeCenter = true;
                    System.out.println("파괴");
                }
                if (unit.getType().isBuilding()) {
                    leftBuildings.remove(unit);
                }
            }

            return;
        }

        if (unit.getType().isWorker()) {
            if (scoutWorker != null && scoutWorker.getID() == unit.getID()) {
                scoutWorker = null;
            }
            if (gasWorker1 != null && gasWorker1.getID() == unit.getID()) {
                gasWorker1 = null;
            }
            if (gasWorker2 != null && gasWorker2.getID() == unit.getID()) {
                gasWorker2 = null;
            }

            workers.remove(unit);

        }

        if (unit.getType() == UnitType.Protoss_Zealot
                || unit.getType() == UnitType.Protoss_Dragoon) {
            army.get(unit.getType()).remove(unit);
            soldiers.remove(unit);

            if (unit.getType() == UnitType.Protoss_Zealot) {
                deadZealots++;
            }
        }
    }

    @Override
    public void onUnitShow(Unit unit) {
        if (unit.getPlayer() != BroodWar.enemy()) {
            return;
        }

        if (!unit.getType().isBuilding()) {
            return;
        }

        leftBuildings.add(unit);
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

        for (Unit worker : workers) {
            if (worker == null || !worker.exists()) {
                continue;
            }

            if (scoutWorker != null && scoutWorker.getID() == worker.getID()) {
                continue;
            }
            
            if (!worker.isIdle()) {
                continue;
            }

            Unit closestMineral = ExampleUtil.getClosestUnitTo(worker, BroodWar.getMinerals());

            if (closestMineral != null) {
                worker.gather(closestMineral);
            }
        }
    }

    public void gatherGas() {
        gatherGas(gasWorker1);
        gatherGas(gasWorker2);
    }

    public void gatherGas(Unit worker) {

        if (BroodWar.self().completedUnitCount(UnitType.Protoss_Assimilator) > 1) {
            return;
        }

        if (worker != null && worker.exists()) {
            if (!worker.isIdle()) {
                return;
            }

            Unit closestGas = ExampleUtil.getClosestUnitTo(worker, BroodWar.getStaticGeysers());
            if (closestGas != null) {
                worker.gather(closestGas);
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

        if (gatewayCount > 2 || BroodWar.self().completedUnitCount(UnitType.Protoss_Pylon) < 1) {
            return;
        }

        ExampleUtil.buildBuildings(gateway);
    }

    public void buildGas() {
        UnitType gas = UnitType.Protoss_Assimilator;
        int gasCount = ExampleUtil.countUnitType(gas);

        if (StartAttack || !ZealotRush) {
            return;
        }

        if (gasCount > 1) {
            return;
        }

        ExampleUtil.buildBuildings(gas);
    }

    public void buildCore() {
        UnitType core = UnitType.Protoss_Cybernetics_Core;
        int gasCount = ExampleUtil.countUnitType(UnitType.Protoss_Assimilator);
        int coreCount = ExampleUtil.countUnitType(core);

        if (StartAttack || !ZealotRush) {
            return;
        }

        if (coreCount > 1 || gasCount < 1) {
            return;
        }

        ExampleUtil.buildBuildings(core);
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

                    soldier.move(waitingPosition);
                }
            }

            if (!ZealotRush) {
                if (army.getOrDefault(UnitType.Protoss_Zealot, new ArrayList<>()).size() > 4) {
                    StartAttack = true;
                    ZealotRush = true;
                }
                return;
            }

            if (BroodWar.enemy().getRace() == Race.Zerg) {
                StartAttack = soldiers.size() > 15;
            }
            else {
                StartAttack = soldiers.size() > 20;
            }

        }
        else {
            attack();
            StartAttack = (soldiers.size() > 4);

            if (ZealotRush) {
                if (deadZealots > 6) {
                    StartAttack = false;
                }
            }

        }
    }

    public void attack() {
        for (Unit soldier : soldiers) {
            if (soldier == null || !soldier.exists()) {
                continue;
            }

            if (!hasBrokeCenter) {
                soldier.attack(enemyBase);
            }
            else {
                for (Unit building : leftBuildings) {
                    if (building == null) {
                        continue;
                    }

                    soldier.attack(building.getPosition());
                }
            }
        }
    }

    public void scoutUpdate() {
        assignScoutWorker();
        scout();
        updateBaseInfo();
    }

    public void assignScoutWorker() {

        if (enemyBase.isValid(BroodWar) && enemyBase != Position.Unknown) {
            return;
        }

        if (ExampleUtil.countUnitType(UnitType.Protoss_Pylon) < 1) {
            return;
        }

        if (scoutWorker == null) {
            for (Unit unit : workers) {
                if (unit == null || !unit.exists()) {
                    continue;
                }

                if (unit.isConstructing()) {
                    continue;
                }

                scoutWorker = unit;
                System.out.println("정찰 유닛 ID: " + scoutWorker.getID());
                break;
            }
        }

    }

    public void scout() {

        if (enemyBase.isValid(BroodWar) && enemyBase != Position.Unknown) {
            return;
        }

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
        }
    }

    public void updateBaseInfo() {
        if (scoutWorker == null) {
            return;
        }

        if (ExampleUtil.checkEnemyBase(scoutWorker.getTilePosition())) {
            try {
                enemyBase = map.getArea(scoutWorker.getTilePosition()).getBases().get(0).getLocation().toPosition();
            } catch (NullPointerException e) {
                enemyBase = BroodWar.getStartLocations().get(BroodWar.getStartLocations().size() -1).toPosition();
            }
        }

        if (ExampleBot.enemyBase != Position.Unknown) {
            System.out.println("상대 위치: " + ExampleBot.enemyBase.toTilePosition());
            scoutWorker.move(ExampleBot.BroodWar.self().getStartLocation().toPosition());
            scoutWorker = null;
        }
    }

}
