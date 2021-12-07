package com.tistory.workshop6349.basicbot.worker;

import bwapi.*;
import com.tistory.workshop6349.basicbot.BasicBotModule;
import com.tistory.workshop6349.basicbot.BotUtil;
import com.tistory.workshop6349.basicbot.CommandUtil;

public class WorkerManager {

    /// 각 Worker 에 대한 WorkerJob 상황을 저장하는 자료구조 객체
    private final WorkerData workerData = new WorkerData();

    /// 일꾼 중 한명을 Repair Worker 로 정해서, 전체 수리 대상을 하나씩 순서대로 수리합니다
    private Unit currentRepairWorker = null;

    private static final WorkerManager instance = new WorkerManager();

    /// static singleton 객체를 리턴합니다
    public static WorkerManager getInstance() {
        return instance;
    }

    public int getFrameCount() {
        return BasicBotModule.BroodWar.getFrameCount();
    }

    public void update() {

        if (getFrameCount() % 24 != 0) {
            return;
        }

        updateWorkerStatus();
        handleGasWorkers();
        handleIdleWorkers();
        handleMoveWorkers();
        handleCombatWorkers();
        handleRepairWorkers();
    }

    public void updateWorkerStatus() {
        for (Unit worker : workerData.getWorkers()) {
            if (!worker.isCompleted()) {
                continue;
            }

            if (worker.isIdle()) {
                if ((workerData.getWorkerJob(worker) != WorkerData.WorkerJob.Build)
                        && (workerData.getWorkerJob(worker) != WorkerData.WorkerJob.Move)
                        && (workerData.getWorkerJob(worker) != WorkerData.WorkerJob.Scout)) {
                    workerData.setWorkerJob(worker, WorkerData.WorkerJob.Idle, (Unit) null);
                }
            }

            if (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Gas) {
                Unit refinery = workerData.getWorkerResource(worker);

                if (refinery == null || !refinery.exists() || refinery.getHitPoints() <= 0) {
                    workerData.setWorkerJob(worker, WorkerData.WorkerJob.Idle, (Unit) null);
                }
            }

            if (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Repair) {
                Unit repairTargetUnit = workerData.getWorkerRepairUnit(worker);

                if (repairTargetUnit == null || !repairTargetUnit.exists()
                        || repairTargetUnit.getHitPoints() <= 0
                        || repairTargetUnit.getHitPoints() == repairTargetUnit.getType().maxHitPoints()) {
                    workerData.setWorkerJob(worker, WorkerData.WorkerJob.Idle, (Unit) null);
                }
            }
        }
    }

    public void handleGasWorkers() {
        for (Unit unit : BasicBotModule.BroodWar.self().getUnits()) {
            if (unit.getType().isRefinery() && unit.isCompleted()) {
                int numAssigned = workerData.getNumAssignedWorkers(unit);

                for (int i = 0; i < (3 - numAssigned); ++i) {
                    Unit gasWorker = chooseWorkerClosestTo(unit);
                    if (gasWorker != null) {
                        workerData.setWorkerJob(gasWorker, WorkerData.WorkerJob.Gas, unit);
                    }
                }
            }
        }
    }

    /// Idle 일꾼을 Mineral 일꾼으로 만듭니다
    public void handleIdleWorkers() {
        // for each of our workers
        for (Unit worker : workerData.getWorkers()) {
            if (worker == null) {
                continue;
            }

            // if worker's job is idle
            if (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Idle
                    || workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Default) {
                // send it to the nearest mineral patch
                setMineralWorker(worker);
            }
        }
    }

    public void handleMoveWorkers() {
        // for each of our workers
        for (Unit worker : workerData.getWorkers()) {
            if (worker == null) {
                continue;
            }

            // if it is a move worker
            if (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Move) {
                WorkerMoveData data = workerData.getWorkerMoveData(worker);

                // 목적지에 도착한 경우 이동 명령을 해제한다
                if (worker.getPosition().getDistance(data.getPosition()) < 4) {
                    setIdleWorker(worker);
                } else {
                    CommandUtil.move(worker, data.getPosition());
                }
            }
        }
    }

    // bad micro for combat workers
    public void handleCombatWorkers() {
        for (Unit worker : workerData.getWorkers()) {
            if (worker == null) {
                continue;
            }

            if (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Combat) {
                BasicBotModule.BroodWar.drawCircleMap(worker.getPosition().getX(), worker.getPosition().getY(), 4, Color.Yellow, true);
                Unit target = getClosestEnemyUnitFromWorker(worker);

                if (target != null) {
                    CommandUtil.attackUnit(worker, target);
                }
            }
        }
    }

    public void handleRepairWorkers() {
        if (BasicBotModule.BroodWar.self().getRace() != Race.Terran) {
            return;
        }

        for (Unit unit : BasicBotModule.BroodWar.self().getUnits()) {
            // 건물의 경우 아무리 멀어도 무조건 수리. 일꾼 한명이 순서대로 수리
            if (unit.getType().isBuilding() && unit.isCompleted() && unit.getHitPoints() < unit.getType().maxHitPoints()) {
                Unit repairWorker = chooseRepairWorkerClosestTo(unit.getPosition());
                setRepairWorker(repairWorker, unit);
                break;
            }
            // 메카닉 유닛 (SCV, 시즈탱크, 레이쓰 등)의 경우 근처에 SCV 가 있는 경우 수리. 일꾼 한명이 순서대로 수리
            else if (unit.getType().isMechanical() && unit.isCompleted() && unit.getHitPoints() < unit.getType().maxHitPoints()) {
                // SCV 는 수리 대상에서 제외. 전투 유닛만 수리하도록 한다
                if (unit.getType() != UnitType.Terran_SCV) {
                    Unit repairWorker = chooseRepairWorkerClosestTo(unit.getPosition());
                    setRepairWorker(repairWorker, unit);
                    break;
                }
            }
        }
    }

    public Unit chooseRepairWorkerClosestTo(Position p) {
        if (!p.isValid(BasicBotModule.BroodWar)) {
            return null;
        }

        Unit closestWorker = null;

        double closestDist = 1000000000;

        if (currentRepairWorker != null
                && currentRepairWorker.exists()
                && currentRepairWorker.getHitPoints() > 0) {
            return currentRepairWorker;
        }

        // for each of our workers
        for (Unit worker : workerData.getWorkers()) {
            if (worker == null) {
                continue;
            }

            if (worker.isCompleted()
                    && (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Minerals
                    || workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Idle
                    || workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Move)) {
                double dist = worker.getDistance(p);

                if (closestWorker == null
                        || (dist < closestDist && !worker.isCarryingMinerals() && !worker.isCarryingGas())) {
                    closestWorker = worker;
                    dist = closestDist;
                }
            }
        }

        if (currentRepairWorker == null || !currentRepairWorker.exists() || currentRepairWorker.getHitPoints() <= 0) {
            currentRepairWorker = closestWorker;
        }

        return closestWorker;
    }

    /// 해당 일꾼 유닛 unit 의 WorkerJob 값를 Mineral 로 변경합니다
    public void setMineralWorker(Unit unit) {
        if (unit == null) {
            return;
        }

        // check if there is a mineral available to send the worker to
        Unit depot = getClosestResourceDepotFromWorker(unit);

        // if there is a valid ResourceDepot (Command Center, Nexus, Hatchery)
        if (depot != null) {
            // update workerData with the new job
            workerData.setWorkerJob(unit, WorkerData.WorkerJob.Minerals, depot);
        }
    }

    public Unit getClosestMineralWorkerTo(Position position) {
        Unit closestUnit = null;

        double closestDist = 1000000000;

        for (Unit unit : BasicBotModule.BroodWar.self().getUnits()) {
            if (unit.isCompleted()
                    && unit.getHitPoints() > 0
                    && unit.exists()
                    && unit.getType().isWorker()
                    && WorkerManager.getInstance().isMineralWorker(unit)) {
                double dist = unit.getDistance(position);
                if (closestUnit == null || dist < closestDist) {
                    closestUnit = unit;
                    closestDist = dist;
                }
            }
        }
        return closestUnit;
    }

    public Unit getClosestResourceDepotFromWorker(Unit worker) {
        // 멀티 기지간 일꾼 숫자 리밸런싱이 잘 일어나도록 버그 수정

        if (worker == null) {
            return null;
        }

        Unit closestDepot = null;
        double closestDistance = 1000000000;

        // 완성된, 공중에 떠있지 않고 땅에 정착해있는, ResourceDepot 혹은 Lair 나 Hive로 변형중인 Hatchery 중에서
        // 첫째로 미네랄 일꾼수가 꽉 차지않은 곳
        // 둘째로 가까운 곳을 찾는다
        for (Unit unit : BasicBotModule.BroodWar.self().getUnits()) {
            if (unit == null) {
                continue;
            }

            if (unit.getType().isResourceDepot()
                    && (unit.isCompleted() || unit.getType() == UnitType.Zerg_Lair || unit.getType() == UnitType.Zerg_Hive)
                    && !unit.isLifted()) {
                if (!workerData.depotHasEnoughMineralWorkers(unit)) {
                    double distance = unit.getDistance(worker);
                    if (closestDistance > distance) {
                        closestDepot = unit;
                        closestDistance = distance;
                    }
                }
            }
        }

        // 모든 ResourceDepot 이 다 일꾼수가 꽉 차있거나, 완성된 ResourceDepot 이 하나도 없고 건설중이라면,
        // ResourceDepot 주위에 미네랄이 남아있는 곳 중에서 가까운 곳이 선택되도록 한다
        if (closestDepot == null) {
            for (Unit unit : BasicBotModule.BroodWar.self().getUnits()) {
                if (unit == null) {
                    continue;
                }

                if (unit.getType().isResourceDepot()) {
                    if (workerData.getMineralsNearDepot(unit) > 0) {
                        double distance = unit.getDistance(worker);
                        if (closestDistance > distance) {
                            closestDepot = unit;
                            closestDistance = distance;
                        }
                    }
                }
            }

        }

        // 모든 ResourceDepot 주위에 미네랄이 하나도 없다면, 일꾼에게 가장 가까운 곳을 선택한다
        if (closestDepot == null) {
            for (Unit unit : BasicBotModule.BroodWar.self().getUnits()) {
                if (unit == null) {
                    continue;
                }

                if (unit.getType().isResourceDepot()) {
                    double distance = unit.getDistance(worker);
                    if (closestDistance > distance) {
                        closestDepot = unit;
                        closestDistance = distance;
                    }
                }
            }
        }

        return closestDepot;
    }

    public void setIdleWorker(Unit unit) {
        if (unit == null) {
            return;
        }

        workerData.setWorkerJob(unit, WorkerData.WorkerJob.Idle, (Unit) null);
    }

    public void setConstructionWorker(Unit worker, UnitType buildingType) {
        if (worker == null) {
            return;
        }

        workerData.setWorkerJob(worker, WorkerData.WorkerJob.Build, buildingType);
    }

    public Unit chooseConstructionWorkerClosestTo(UnitType buildingType, TilePosition buildingPosition, boolean setJobAsConstructionWorker, int avoidWorkerID) {
        // variables to hold the closest worker of each type to the building
        Unit closestMovingWorker = null;
        Unit closestMiningWorker = null;

        // BasicBot 1.1 Patch Start ////////////////////////////////////////////////
        // 변수 기본값 수정

        double closestMovingWorkerDistance = 1000000000;
        double closestMiningWorkerDistance = 1000000000;

        // BasicBot 1.1 Patch End //////////////////////////////////////////////////

        // look through each worker that had moved there first
        for (Unit unit : workerData.getWorkers()) {
            if (unit == null) continue;

            // worker 가 2개 이상이면, avoidWorkerID 는 피한다
            if (workerData.getWorkers().size() >= 2 && avoidWorkerID != 0 && unit.getID() == avoidWorkerID) continue;

            // Move / Idle Worker
            if (unit.isCompleted() && (workerData.getWorkerJob(unit) == WorkerData.WorkerJob.Move || workerData.getWorkerJob(unit) == WorkerData.WorkerJob.Idle)) {
                // if it is a new closest distance, set the pointer
                double distance = unit.getDistance(buildingPosition.toPosition());
                if (closestMovingWorker == null || (distance < closestMovingWorkerDistance && !unit.isCarryingMinerals() && !unit.isCarryingGas())) {
                    if (BotUtil.isConnected(unit.getTilePosition(), buildingPosition)) {
                        closestMovingWorker = unit;
                        closestMovingWorkerDistance = distance;
                    }
                }
            }

            // Move / Idle Worker 가 없을때, 다른 Worker 중에서 차출한다
            if (unit.isCompleted()
                    && (workerData.getWorkerJob(unit) != WorkerData.WorkerJob.Move && workerData.getWorkerJob(unit) != WorkerData.WorkerJob.Idle && workerData.getWorkerJob(unit) != WorkerData.WorkerJob.Build)) {
                // if it is a new closest distance, set the pointer
                double distance = unit.getDistance(buildingPosition.toPosition());
                if (closestMiningWorker == null || (distance < closestMiningWorkerDistance && !unit.isCarryingMinerals() && !unit.isCarryingGas())) {
                    if (BotUtil.isConnected(unit.getTilePosition(), buildingPosition)) {
                        closestMiningWorker = unit;
                        closestMiningWorkerDistance = distance;
                    }
                }
            }
        }

        Unit chosenWorker = closestMovingWorker != null ? closestMovingWorker : closestMiningWorker;

        // if the worker exists (one may not have been found in rare cases)
        if (chosenWorker != null && setJobAsConstructionWorker) {
            workerData.setWorkerJob(chosenWorker, WorkerData.WorkerJob.Build, buildingType);
        }

        return chosenWorker;
    }

    /// Mineral 혹은 Idle 일꾼 유닛들 중에서 Scout 임무를 수행할 일꾼 유닛을 정해서 리턴합니다
    public Unit getScoutWorker() {
        // for each of our workers
        for (Unit worker : workerData.getWorkers()) {
            if (worker == null) {
                continue;
            }
            // if it is a scout worker
            if (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Scout) {
                return worker;
            }
        }

        return null;
    }

    // sets a worker as a scout
    public void setScoutWorker(Unit worker) {
        if (worker == null) {
            return;
        }

        workerData.setWorkerJob(worker, WorkerData.WorkerJob.Scout, (Unit) null);
    }


    // get a worker which will move to a current location

    public Unit chooseWorkerClosestTo(Unit unit) {
        return chooseWorkerClosestTo(unit.getPosition());
    }

    public Unit chooseWorkerClosestTo(Position p) {
        Unit closestWorker = null;

        double closestDistance = 1000000000;

        // for each worker we currently have
        for (Unit unit : workerData.getWorkers()) {
            if (unit == null) {
                continue;
            }

            // only consider it if it's a mineral worker
            if (unit.isCompleted() && workerData.getWorkerJob(unit) == WorkerData.WorkerJob.Minerals) {
                // if it is a new closest distance, set the pointer
                double distance = unit.getDistance(p);
                if (closestWorker == null || (distance < closestDistance && !unit.isCarryingMinerals() && !unit.isCarryingGas())) {
                    closestWorker = unit;
                    closestDistance = distance;
                }
            }
        }

        // return the worker
        return closestWorker;
    }

    /// position 에서 가장 가까운 Mineral 혹은 Idle 일꾼 유닛들 중에서 Move 임무를 수행할 일꾼 유닛을 정해서 리턴합니다
    public void setMoveWorker(Unit worker, int mineralsNeeded, int gasNeeded, Position p) {
        Unit closestWorker = null;

        double closestDistance = 1000000000;

        // for each worker we currently have
        for (Unit unit : workerData.getWorkers()) {
            if (unit == null) {
                continue;
            }

            // only consider it if it's a mineral worker or idle worker
            if (unit.isCompleted() && (workerData.getWorkerJob(unit) == WorkerData.WorkerJob.Minerals || workerData.getWorkerJob(unit) == WorkerData.WorkerJob.Idle)) {
                // if it is a new closest distance, set the pointer
                double distance = unit.getDistance(p);
                if (closestWorker == null || distance < closestDistance) {
                    closestWorker = unit;
                    closestDistance = distance;
                }
            }
        }

        if (closestWorker != null) {
            workerData.setWorkerJob(closestWorker, WorkerData.WorkerJob.Move, new WorkerMoveData(mineralsNeeded, gasNeeded, p));
        }
    }


    /// 해당 일꾼 유닛으로부터 가장 가까운 적군 유닛을 리턴합니다
    public Unit getClosestEnemyUnitFromWorker(Unit worker) {
        if (worker == null) {
            return null;
        }

        Unit closestUnit = null;
        double closestDist = 10000;

        for (Unit unit : BasicBotModule.BroodWar.enemy().getUnits()) {
            double dist = unit.getDistance(worker);

            if ((dist < 400) && (closestUnit == null || (dist < closestDist))) {
                closestUnit = unit;
                closestDist = dist;
            }
        }

        return closestUnit;
    }

    /// 해당 일꾼 유닛에게 Combat 임무를 부여합니다
    public void setCombatWorker(Unit worker) {
        if (worker == null) {
            return;
        }

        workerData.setWorkerJob(worker, WorkerData.WorkerJob.Combat, (Unit) null);
    }

    /// 모든 Combat 일꾼 유닛에 대해 임무를 해제합니다
    public void stopCombat() {
        for (Unit worker : workerData.getWorkers()) {
            if (worker == null) {
                continue;
            }

            if (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Combat) {
                setMineralWorker(worker);
            }
        }
    }

    public void setRepairWorker(Unit worker, Unit unitToRepair) {
        workerData.setWorkerJob(worker, WorkerData.WorkerJob.Repair, unitToRepair);
    }

    public void stopRepairing(Unit worker) {
        workerData.setWorkerJob(worker, WorkerData.WorkerJob.Idle, (Unit) null);
    }

    public void onWorkerMorph(Unit unit) {
        if (unit == null) {
            return;
        }

        if (unit.getType().isBuilding() && unit.getPlayer() == BasicBotModule.BroodWar.self()
                && unit.getPlayer().getRace() == Race.Zerg) {
            workerData.workerDestroyed(unit);
            rebalancedWorkers();
        }
    }

    public void onWorkerComplete(Unit unit) {
        if (unit == null) {
            return;
        }

        if (unit.getType().isResourceDepot() && unit.getPlayer() == BasicBotModule.BroodWar.self()) {
            workerData.addDepot(unit);
            rebalancedWorkers();
        }
        if (unit.getType().isWorker() && unit.getPlayer() == BasicBotModule.BroodWar.self() && unit.getHitPoints() >= 0) {
            workerData.addWorker(unit);
            rebalancedWorkers();
        }
    }

    public void rebalancedWorkers() {
        for (Unit worker : workerData.getWorkers()) {
            if (workerData.getWorkerJob(worker) != WorkerData.WorkerJob.Minerals) {
                continue;
            }

            Unit depot = workerData.getWorkerDepot(worker);

            if (depot != null && workerData.depotHasEnoughMineralWorkers(depot)) {
                workerData.setWorkerJob(worker, WorkerData.WorkerJob.Idle, (Unit) null);
            } else if (depot == null) {
                workerData.setWorkerJob(worker, WorkerData.WorkerJob.Idle, (Unit) null);
            }
        }
    }

    /// 일꾼 유닛들의 상태를 저장하는 workerData 객체를 업데이트합니다
    public void onWorkerDestroy(Unit unit) {
        if (unit == null) {
            return;
        }

        // ResourceDepot 건물이 파괴되면, 자료구조 삭제 처리를 한 후, 일꾼들을 Idle 상태로 만들어 rebalancedWorkers 한 효과가 나게 한다
        if (unit.getType().isResourceDepot() && unit.getPlayer() == BasicBotModule.BroodWar.self()) {
            workerData.removeDepot(unit);
        }

        // 일꾼이 죽으면, 자료구조 삭제 처리를 한 후, rebalancedWorkers 를 한다
        if (unit.getType().isWorker() && unit.getPlayer() == BasicBotModule.BroodWar.self()) {
            workerData.workerDestroyed(unit);
            rebalancedWorkers();
        }

        // 미네랄을 다 채취하면 rebalancedWorkers 를 한다
        if (unit.getType() == UnitType.Resource_Mineral_Field
                || unit.getType() == UnitType.Resource_Mineral_Field_Type_2
                || unit.getType() == UnitType.Resource_Mineral_Field_Type_3
                || unit.getType() == UnitType.Powerup_Mineral_Cluster_Type_1
                || unit.getType() == UnitType.Powerup_Mineral_Cluster_Type_2) {
            rebalancedWorkers();
        }
    }

    public boolean isMineralWorker(Unit worker) {
        if (worker == null) {
            return false;
        }

        return workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Minerals || workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Idle;
    }

    public boolean isScoutWorker(Unit worker) {
        if (worker == null) {
            return false;
        }

        return (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Scout);
    }

    public boolean isConstructionWorker(Unit worker) {
        if (worker == null) {
            return false;
        }

        return (workerData.getWorkerJob(worker) == WorkerData.WorkerJob.Build);
    }

    public int getNumMineralWorkers() {
        return workerData.getNumMineralWorkers();
    }

    /// idle 상태인 일꾼 유닛 unit 의 숫자를 리턴합니다
    public int getNumIdleWorkers() {
        return workerData.getNumIdleWorkers();
    }

    public int getNumGasWorkers() {
        return workerData.getNumGasWorkers();
    }

    /// 일꾼 유닛들의 상태를 저장하는 workerData 객체를 리턴합니다
    public WorkerData getWorkerData() {
        return workerData;
    }

}
