package com.tistory.workshop6349.basicbot;

import bwapi.*;
import bwem.Area;
import bwem.Base;
import bwem.ChokePoint;

import java.util.*;

public class InformationManager {

    private static final InformationManager manager = new InformationManager();
    public static InformationManager getInstance() {
        return manager;
    }

    public enum TypeKind {
        AllUnitKind,
        AirUnitKind,
        GroundCombatKind, //나중에..
        GroundUnitKind,
        BuildingKind,
        AllDefenseBuildingKind,
        AirDefenseBuildingKind,
        GroundDefenseBuildingKind,
        AllKind
    }

    public int mapPlayerLimit;  // 맵 플레이어 수
    public HashMap<Player, UnitData> unitData = new HashMap<>();
    public HashMap<Player, Base> mainBaseLocations = new HashMap<>(); // 해당 플레이어의 주요 건물들이 있는 Map
    public HashMap<Player, Boolean> mainBaseLocationChanged = new HashMap<>();  // 메인 베이스가 바뀌었는 가
    public HashMap<Player, List<Base>> occupiedBaseLocations = new HashMap<>(); // 해당 플레이어가 점령한 Base
    public HashMap<Player, HashSet<Area>> occupiedAreas = new HashMap<>();      // 해당 플레이어가 점령한 Area
    public HashMap<Player, ChokePoint> firstChokePoint = new HashMap<>();       // 플레이어게서 제일 가까운 ChokePoint
    public HashMap<Player, Base> firstExpansiveLocation = new HashMap<>();      // 본진에서 제일 가까운 Base(앞마당)
    public HashMap<Player, Base> secondExpansiveLocation = new HashMap<>();     // 두 번째 멀티
    public HashMap<Player, Base> thirdExpansiveLocation = new HashMap<>();      // 세 번째 멀티
    public HashMap<Player, Base> islandExpansiveLocation = new HashMap<>();     // 섬 멀티

    public ArrayList<Base> additionalExpansiveList = new ArrayList<>();         // 추가 멀티들
    public HashMap<Base, Integer> expansiveMap = new HashMap<>();               // 추가 멀티 관리용 Map
    public HashMap<Player, ChokePoint> secondChokePoint = new HashMap<>();      // 본진에서 2번째로 가까운 ChokePoint

    public HashMap<Base, Base> firstExpansiveOfAllStartPosition = new HashMap<>();
    // 맵의 모든 앞마당 위치

    public HashMap<Area, Area> mainBaseAreaPair = new HashMap<>();              // 베이스와 확장 사이에 Area 가 있는 경우 동일하게 취급하기 위해 저장.


    public ArrayList<Base> startBaseLocations = new ArrayList<>();
    public ArrayList<Base> allBaseLocations = new ArrayList<>();

    public ArrayList<UnitInfo> enemyInMyArea = new ArrayList<>();
    public ArrayList<UnitInfo> enemyInMyYard = new ArrayList<>();

    public boolean isEnemyScanResearched;
    public int availableScanCount;

    public HashSet<TechType> researchSet = new HashSet<>();
    public Position firstWaitLinePosition = Position.Unknown;                   // 앞마당 먹는 시점에서 최초 1회 실행
    public HashMap<TilePosition, Unit> baseSafeMineralMap = new HashMap<>();

    public int activationMineralBaseCount;
    public int activationGasBaseCount;

    public ArrayList<TilePosition> scanPositionOfMainBase;
    public int scanIndex = 0;

    public Player selfPlayer;
    public Race selfRace;
    public Player enemyPlayer;
    public Race enemyRace;

    public static boolean isEnemyBaseFound = false;

    private static boolean needMoveInside = false;
    private static int nuclearLaunchedTime = 0;

    public InformationManager() {
        mapPlayerLimit = BasicBotModule.BroodWar.getStartLocations().size();

        selfPlayer = Common.Self();
        enemyPlayer = Common.Enemy();

        selfRace = selfPlayer.getRace();
        enemyRace = enemyPlayer.getRace();

        unitData.put(selfPlayer, new UnitData());
        unitData.put(enemyPlayer, new UnitData());

        // updateStartAndBaseLocation();

//        mainBaseLocations.put(selfPlayer, );



    }

    public void update() {
        // TODO
    }

    public void checkEnemyInMyArea() {
        enemyInMyArea.clear();
        enemyInMyYard.clear();

        HashMap<Unit, UnitInfo> allUnits = unitData.get(Common.Enemy()).getAllUnits();

        for (UnitInfo eu : allUnits.values()) {
            if (BasicUtil.isInMyArea(eu)) {
                enemyInMyArea.add(eu);
                enemyInMyYard.add(eu);
            }
            else {
                if (!eu.getType().isFlyer()
                        && eu.getUnit().getPosition().getApproxDistance(InformationManager.getInstance().getSecondAverageChokePosition(Common.Self())) < 300) {
                    // 300TILE 은 시즈탱크 최대 사거리에 약간 모자르다.
                    enemyInMyYard.add(eu);
                }
            }
        }
    }

    public void updateStartAndBaseLocation() {
        for (Area area : BasicBotModule.Map.getMap().getAreas()) {
            for (Base base : area.getBases()) {
                if (base.isStartingLocation()) {
                    startBaseLocations.add(base);
                }
                allBaseLocations.add(base);
            }
        }

        // 2인용 맵은 정렬 X
        if (startBaseLocations.size() <= 2) {
            return;
        }

        if (enemyRace == Race.Protoss) {
            startBaseLocations.sort((a, b) -> {
                TilePosition myBase = InformationManager.getInstance().getStartLocation(Common.Self()).getLocation();
                return Boolean.compare(myBase.getApproxDistance(a.getLocation()) < myBase.getApproxDistance(b.getLocation()), false); // TODO 오류 생기면 다시 해야함
            });
        }
        else {
            // 시계방향 (맵 중앙 기준)으로 정렬
            startBaseLocations.sort((a, b) -> {
                TilePosition C = BasicBotModule.Map.getMap().getCenter().toTilePosition();
                TilePosition A = new TilePosition(a.getLocation().x - C.x, a.getLocation().y - C.y);
                TilePosition B = new TilePosition(b.getLocation().x - C.x, b.getLocation().y - C.y);

                int d1 = C.getApproxDistance(A);
                int d2 = C.getApproxDistance(B);

                double ang1 = Math.atan2(A.y, A.x);
                double ang2 = Math.atan2(B.y, B.x);

                if (ang1 < 0) {
                    ang1 += 2 * 3.141592;
                }
                if (ang2 < 0) {
                    ang2 += 2 * 3.141592;
                }

                return Boolean.compare(ang1 < ang2 || (ang1 == ang2 && d1 < d2) , false);   // TODO 오류 생기면 다시 해야함
            });
        }

        for (Base base : startBaseLocations) {
            System.out.println("Base (" + base.getLocation().x + ", " + base.getLocation().y + ") " + (base.getLocation() == Common.Self().getStartLocation() ? "<-- My Base" : "EMPTY"));
        }
    }

    public Base getBaseLocation(TilePosition pos) {
        for (Base base : allBaseLocations) {
            if (base.getLocation().equals(pos)) {
                return base;
            }
        }
        return null;
    }

    public Base getStartLocation(Player p) {
        for (Base base : startBaseLocations) {
            if (p.getStartLocation().equals(base.getLocation())) {
                return base;
            }
        }
        return null;
    }

    public Base getNearestBaseLocation(Position pos, boolean groundDist) {
        Base ret = null;

        int dist = 10000;

        for (Base base : allBaseLocations) {
            int temp = groundDist ? BasicUtil.getGroundDistance(pos, base.getLocation().toPosition()) : pos.getApproxDistance(base.getLocation().toPosition());

            if (temp >= 0 && dist > temp) {
                dist = temp;
                ret = base;
            }
        }

        return ret;
    }

    // getMineChokePoints() 는 안쓰는 메소드이기에 PASS





































    // 헤더 메소드 처리
    public int getMapPlayerLimit() {
        return mapPlayerLimit;
    }

    public ArrayList<Base> getStartLocations() {
        return startBaseLocations;
    }

    public ArrayList<Base> getBaseLocations() {
        return allBaseLocations;
    }

    public UnitData getUnitData(Player player) {
        return unitData.get(player);
    }

    public Position getFirstChokePosition(Player player) {
        return firstChokePoint.get(player).getCenter().toPosition();
    }

    public Position getSecondChokePosition(Player player) {
        return secondChokePoint.get(player).getCenter().toPosition();
    }

    public Position getSecondAverageChokePosition(Player player) {
        return getSecondAverageChokePosition(player, 1, 1, 1024);
    }

    public Position getSecondAverageChokePosition(Player player, int m, int n, int l) {
        if (secondChokePoint.get(player) == null) {
            return Position.None;
        }

        return Position.Invalid; //TODO ERROR
    }

    public ArrayList<UnitInfo> getUnits(UnitType t, Player p) {
        return unitData.get(p).getUnitList(t);
    }

    public ArrayList<UnitInfo> getBuildings(UnitType t, Player p) {
        return unitData.get(p).getBuildingList(t);
    }

    public HashMap<Unit, UnitInfo> getUnits(Player p) {
        return unitData.get(p).getAllUnits();
    }

    public HashMap<Unit, UnitInfo> getBuildings(Player p) {
        return unitData.get(p).getAllBuildings();
    }

    public boolean getEnemyScanResearched() {
        return isEnemyScanResearched;
    }
    
    public int getAvailableScanCount() {
        return availableScanCount;
    }

    public Area getMainBasePairArea(Area area) {
        return mainBaseAreaPair.get(area);
    }

    public ArrayList<UnitInfo> enemyInMyArea() {
        return enemyInMyArea;
    }

    public ArrayList<UnitInfo> enemyInMyYard() {
        return enemyInMyYard;
    }

}
