package com.tistory.workshop6349.basicbot;

import bwapi.*;
import bwem.Area;
import bwem.Base;
import bwem.ChokePoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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

    public ArrayList<Unit> enemyInMyArea = new ArrayList<>();
    public ArrayList<Unit> enemyInMyYard = new ArrayList<>();

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

        selfPlayer = BasicBotModule.BroodWar.self();
        enemyPlayer = BasicBotModule.BroodWar.enemy();

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

        HashMap<Unit, UnitInfo> allUnits = unitData.get(BasicBotModule.BroodWar.enemy()).getAllUnits();

        

    }



































    // 헤더 메소드 처리
    public int getMapPlayerLimit() {
        return mapPlayerLimit;
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






}
