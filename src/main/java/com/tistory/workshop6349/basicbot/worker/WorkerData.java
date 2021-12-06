package com.tistory.workshop6349.basicbot.worker;

import bwapi.Unit;
import bwapi.UnitType;
import bwem.Base;
import bwem.Mineral;
import com.tistory.workshop6349.basicbot.BasicBotModule;
import com.tistory.workshop6349.basicbot.CommandUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WorkerData {

    private final HashMap<Integer, Integer> workersOnMineralPatch = new HashMap<>();

    // 일꾼 유닛에게 지정하는 임무 종류
    public enum WorkerJob {
        Minerals,
        Gas,
        Build,
        Combat,
        Idle,
        Repair,
        Move,
        Scout,
        Default,
    }

    // 미네랄 숫자 대비 미네랄 일꾼 숫자의 적정 비율
    double mineralAndMineralWorkerRatio;

    // 일꾼 목록
    private final ArrayList<Unit> workers = new ArrayList<>();
    // 본진 건물 ( 해처리 / 넥서스 / 커맨드 센터 )
    private final ArrayList<Unit> depots = new ArrayList<>();

    private final HashMap<Integer, WorkerJob> workerJobMap = new HashMap<>();
    private final HashMap<Integer, UnitType> workerBuildingTypeMap = new HashMap<>();
    private final HashMap<Integer, Integer> depotWorkerCount = new HashMap<>();
    private final HashMap<Integer, Integer> refineryWorkerCount = new HashMap<>();
    private final HashMap<Integer, Unit> workerDepotMap = new HashMap<>();
    private final HashMap<Integer, WorkerMoveData> workerMoveDataMap = new HashMap<>();
    private final HashMap<Integer, Unit> workerMineralAssignment = new HashMap<>();
    private final HashMap<Integer, Unit> workerRefineryMap = new HashMap<>();
    private final HashMap<Integer, Unit> workerRepairMap = new HashMap<>();

    public WorkerData() {
        // 멀티 기지간 일꾼 숫자 리밸런싱 조건값 수정 : 미네랄 갯수 * 2 배 초과일 경우 리밸런싱

        mineralAndMineralWorkerRatio = 2;

        for (Base base : BasicBotModule.MAP.getMap().getBases()) {
            for (Mineral mineral : base.getMinerals()) {
                workersOnMineralPatch.put(mineral.getUnit().getID(), 0);
            }
        }
    }

    public final List<Unit> getWorkers() {
        return workers;
    }

    public void workerDestroyed(Unit unit) {
        // workers, depotWorkerCount, refineryWorkerCount 등 자료구조에서 사망한 일꾼 정보를 제거합니다
        clearPreviousJob(unit);
        workers.remove(unit);
    }

    // WorkerJob::Idle 로 일단 추가함
    public void addWorker(Unit unit) {
        if (unit == null) {
            return;
        }

        workers.add(unit);
        workerJobMap.put(unit.getID(), WorkerJob.Idle);
    }

    public void addWorker(Unit unit, WorkerJob job, Unit jobUnit) {
        if (unit == null || jobUnit == null) {
            return;
        }

        workers.add(unit);
        setWorkerJob(unit, job, jobUnit);
    }

    public void addWorker(Unit unit, WorkerJob job, UnitType unitType) {
        if (unit == null) {
            return;
        }

        workers.add(unit);
        setWorkerJob(unit, job, unitType);
    }

    public void addDepot(Unit unit) {
        if (unit == null) {
            return;
        }

        boolean flag = true;
        for (Unit depot : depots) {
            if (depot.getID() == unit.getID()) {
                flag = false;
            }
        }
        if (flag) {
            depots.add(unit);
            depotWorkerCount.put(unit.getID(), 0);
        }
    }

    public void removeDepot(Unit unit) {
        if (unit == null) {
            return;
        }

        depots.remove(unit);
        depotWorkerCount.remove(unit.getID());

        for (Unit worker : workers) {
            if (workerDepotMap.get(worker.getID()) == unit) {
                setWorkerJob(worker, WorkerJob.Idle, (Unit) null);
            }
        }
    }

    public List<Unit> getDepots() {
        return depots;
    }

    public void addToMineralPatch(Unit unit, int num) {
        if (unit == null) {
            return;
        }

        if (!workersOnMineralPatch.containsKey(unit.getID())) {
            workersOnMineralPatch.put(unit.getID(), num);
        }
        else {
            workersOnMineralPatch.put(unit.getID(), workersOnMineralPatch.get(unit.getID()) + num);
        }
    }

    public void setWorkerJob(Unit unit, WorkerJob job, Unit jobUnit) {
        if (unit == null) {
            return;
        }

        clearPreviousJob(unit);
        workerJobMap.put(unit.getID(), job);

        if (job == WorkerJob.Minerals) {
            if (depotWorkerCount.get(jobUnit.getID()) == null) {
                depotWorkerCount.put(jobUnit.getID(), 1);
            }
            else {
                depotWorkerCount.put(jobUnit.getID(), depotWorkerCount.get(jobUnit.getID())+1);
            }
            workerDepotMap.put(unit.getID(), jobUnit);

            Unit mineralToMine = getMineralToMine(unit).getUnit();
            workerMineralAssignment.put(unit.getID(), mineralToMine);
            addToMineralPatch(mineralToMine, 1);

            CommandUtil.rightClick(unit, mineralToMine);
        }
        else if (job == WorkerJob.Gas) {
            if (refineryWorkerCount.get(jobUnit.getID()) == null) {
                refineryWorkerCount.put(jobUnit.getID(), 1);
            }
            else {
                refineryWorkerCount.put(jobUnit.getID(), refineryWorkerCount.get(jobUnit.getID())+1);
            }
            workerRefineryMap.put(unit.getID(), jobUnit);

            CommandUtil.rightClick(unit, jobUnit);
        }
        else if (job == WorkerJob.Repair) {
            if (unit.getType() == UnitType.Terran_SCV) {
                workerRepairMap.put(unit.getID(), jobUnit);

                if (!unit.isRepairing()) {
                    CommandUtil.repair(unit, jobUnit);
                }

            }
        }
    }

    public void setWorkerJob(Unit unit, WorkerJob job, UnitType unitType) {
        if (unit == null) {
            return;
        }

        clearPreviousJob(unit);
        workerJobMap.put(unit.getID(), job);

        if (job == WorkerJob.Build) {
            workerBuildingTypeMap.put(unit.getID(), unitType);
        }
    }

    public void setWorkerJob(Unit unit, WorkerJob job, WorkerMoveData data) {
        if (unit == null) {
            return;
        }

        clearPreviousJob(unit);
        workerJobMap.put(unit.getID(), job);

        if (job == WorkerJob.Move) {
            workerMoveDataMap.put(unit.getID(), data);
        }
//        if (workerJobMap.get(unit.getID()) != WorkerJob.Move) {
//             에러난 거임
//        }

    }

    public void clearPreviousJob(Unit unit) {
        if (unit == null) {
            return;
        }

        WorkerJob previousJob = getWorkerJob(unit);

        if (previousJob == WorkerJob.Minerals) {
            if (workerDepotMap.get(unit.getID()) != null) {
                if (depotWorkerCount.get(workerDepotMap.get(unit.getID()).getID()) != null) {
                    depotWorkerCount.put(workerDepotMap.get(unit.getID()).getID(),
                            depotWorkerCount.get(workerDepotMap.get(unit.getID()).getID())-1);
                }
            }
            workerDepotMap.remove(unit.getID());
            addToMineralPatch(workerMineralAssignment.get(unit.getID()), -1);
            workerMineralAssignment.remove(unit.getID());
        }
        else if (previousJob == WorkerJob.Gas) {
            refineryWorkerCount.put(workerRefineryMap.get(unit.getID()).getID(),
                    refineryWorkerCount.get(workerRefineryMap.get(unit.getID()).getID()) - 1);
            workerRefineryMap.remove(unit.getID());
        }
        else if (previousJob == WorkerJob.Build) {
            workerBuildingTypeMap.remove(unit.getID());
        }
        else if (previousJob == WorkerJob.Repair) {
            workerRepairMap.remove(unit.getID());
        }
        else if (previousJob == WorkerJob.Move) {
            workerMoveDataMap.remove(unit.getID());
        }
        workerJobMap.remove(unit.getID());
    }

    public final int getNumWorkers() {
        return workers.size();
    }

    public final int getNumMineralWorkers() {
        int num = 0;
        for (Unit unit : workers) {
            if (workerJobMap.get(unit.getID()) == WorkerJob.Minerals) {
                num++;
            }
        }
        return num;
    }

    public final int getNumGasWorkers() {
        int num = 0;
        for (Unit unit : workers) {
            if (workerJobMap.get(unit.getID()) == WorkerJob.Gas) {
                num++;
            }
        }
        return num;
    }

    public final int getNumIdleWorkers() {
        int num = 0;
        for (Unit unit : workers) {
            if (workerJobMap.get(unit.getID()) == WorkerJob.Idle) {
                num++;
            }
        }
        return num;
    }

    public WorkerData.WorkerJob getWorkerJob(Unit unit) {
        if (unit == null) {
            return WorkerJob.Default;
        }

        if (workerJobMap.containsKey(unit.getID())) {
            return workerJobMap.get(unit.getID());
        }

        return WorkerJob.Default;
    }

    public boolean depotHasEnoughMineralWorkers(Unit depot) {
        if (depot == null) {
            return false;
        }

        int assignedWorker = getNumAssignedWorkers(depot);
        int mineralsNearDepot = getMineralsNearDepot(depot);

        // 충분한 수의 미네랄 일꾼 수를 얼마로 정할 것인가 :
        // (근처 미네랄 수) * 1.5배 ~ 2배 정도가 적당
        // 근처 미네랄 수가 8개 라면, 일꾼 8마리여도 좋지만, 12마리면 조금 더 채취가 빠르다. 16마리면 충분하다. 24마리면 너무 많은 숫자이다.
        // 근처 미네랄 수가 0개 인 ResourceDepot 은, 이미 충분한 수의 미네랄 일꾼이 꽉 차있는 것이다

        return (assignedWorker >= (int)(mineralsNearDepot * mineralAndMineralWorkerRatio));
    }

    public List<Mineral> getMineralPatchesNearDepot(Unit depot) {
        // depot 이 위치한 곳에서 가장 가까운 Base 를 찾고
        // 해당 Base 에 있는 미네랄을 반환
        Base base = null;
        double minDist = Double.MAX_VALUE;
        for (Base baseLocation : BasicBotModule.MAP.getMap().getBases()) {
            double dist = depot.getDistance(baseLocation.getCenter());
            if (dist < minDist) {
                minDist = dist;
                base = baseLocation;
            }
        }

        if (base == null) {
            return null;
        }
        return base.getMinerals();
    }

    public int getMineralsNearDepot(Unit depot) {
        if (depot == null) {
            return 0;
        }

        Base base = null;
        double minDist = Double.MAX_VALUE;
        for (Base baseLocation : BasicBotModule.MAP.getMap().getBases()) {
            double dist = depot.getDistance(baseLocation.getCenter());
            if (dist < minDist) {
                minDist = dist;
                base = baseLocation;
            }
        }

        if (base == null) {
            return 0;
        }
        return base.getMinerals().size();
    }

    public Unit getWorkerResource(Unit unit) {
        if (unit == null) {
            return null;
        }

        if (getWorkerJob(unit) == WorkerJob.Minerals) {
            if (workerMineralAssignment.containsKey(unit.getID())) {
                return workerMineralAssignment.get(unit.getID());
            }
        }
        else if (getWorkerJob(unit) == WorkerJob.Gas) {
            if (workerRefineryMap.containsKey(unit.getID())) {
                return workerRefineryMap.get(unit.getID());
            }
        }
        return null;
    }

    public Mineral getMineralToMine(Unit worker) {
        if (worker == null) {
            return null;
        }

        Unit depot = getWorkerDepot(worker);
        Mineral bestMineral = null;
        double bestDist = 100000000;
        double bestNumAssigned = 10000000;

        if (depot != null) {
            List<Mineral> mineralPatches = getMineralPatchesNearDepot(depot);
            for (Mineral mineral : mineralPatches) {
                double dist = mineral.getUnit().getDistance(depot);
                double numAssigned = workersOnMineralPatch.get(mineral.getUnit().getID());

                if (numAssigned < bestNumAssigned) {
                    bestMineral = mineral;
                    bestDist = dist;
                    bestNumAssigned = numAssigned;
                }
                else if (numAssigned == bestNumAssigned) {
                    if (dist < bestDist) {
                        bestMineral = mineral;
                        bestDist = dist;
                    }
                }
            }
        }
        return bestMineral;
    }

    public Unit getWorkerRepairUnit(Unit unit) {
        if (unit == null) {
            return null;
        }

        if (workerRepairMap.containsKey(unit.getID())) {
            return workerRepairMap.get(unit.getID());
        }
        return null;
    }

    public Unit getWorkerDepot(Unit unit) {
        if (unit == null) {
            return null;
        }

        if (workerDepotMap.containsKey(unit.getID())) {
            return workerDepotMap.get(unit.getID());
        }

        return null;
    }

    public UnitType getWorkerBuildingType(Unit unit) {
        if (unit == null) {
            return UnitType.None;
        }

        if (workerBuildingTypeMap.containsKey(unit.getID())) {
            return workerBuildingTypeMap.get(unit.getID());
        }

        return UnitType.None;
    }

    public WorkerMoveData getWorkerMoveData(Unit unit) {
        return workerMoveDataMap.get(unit.getID());
    }

    public int getNumAssignedWorkers(Unit unit) {
        if (unit == null) {
            return 0;
        }

        if (unit.getType().isResourceDepot()) {
            if (depotWorkerCount.containsKey(unit.getID())) {
                return depotWorkerCount.get(unit.getID());
            }
        }
        else if (unit.getType().isRefinery()) {
            if (refineryWorkerCount.containsKey(unit.getID())) {
                return refineryWorkerCount.get(unit.getID());
            }
            else {
                refineryWorkerCount.put(unit.getID(), 0);
            }
        }
        return 0;
    }

    public char getJobCode(Unit unit) {
        if (unit == null) {
            return 'X';
        }

        WorkerData.WorkerJob j = getWorkerJob(unit);

        if (j == WorkerData.WorkerJob.Build) {
            return 'B';
        }
        if (j == WorkerData.WorkerJob.Combat) {
            return 'C';
        }
        if (j == WorkerData.WorkerJob.Default) {
            return 'D';
        }
        if (j == WorkerData.WorkerJob.Gas) {
            return 'G';
        }
        if (j == WorkerData.WorkerJob.Idle) {
            return 'I';
        }
        if (j == WorkerData.WorkerJob.Minerals) {
            return 'M';
        }
        if (j == WorkerData.WorkerJob.Repair) {
            return 'R';
        }
        if (j == WorkerData.WorkerJob.Move) {
            return 'O';
        }
        if (j == WorkerData.WorkerJob.Scout) {
            return 'S';
        }
        return 'X';
    }
}
