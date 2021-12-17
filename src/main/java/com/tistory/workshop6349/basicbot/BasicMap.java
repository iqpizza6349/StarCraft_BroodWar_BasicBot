package com.tistory.workshop6349.basicbot;

import bwapi.Position;
import bwapi.TilePosition;
import bwapi.WalkPosition;

import java.util.ArrayList;
import java.util.Arrays;

public class BasicMap {

    public static int wt = 256;
    public static int ht = 256;
    public static int ww = 1024;
    public static int hw = 1024;
    public static int wp = 8192;
    public static int hp = 8192;

    public static Position centerPos = Position.None;

    public static boolean [][]buildMap_Fix = new boolean[256][256];
    public static boolean [][]buildMap_Var = new boolean[256][256];
    public static boolean [][]walk_Map = new boolean[256][256];
    public static boolean [][]spaceMap = new boolean[256][256];

    public static int [][][]mainDistArray = new int[8][256][256];
    public static int [][]centerDistMap = new int[256][256];
    public static int [][]mainPathMap = new int[256][256];

    public static boolean [][][]chokeMap = new boolean[8][256][256];
    public static boolean [][]mainMap = new boolean[256][256];
    public static boolean [][]mainMapOrig = new boolean[256][256];

    public static boolean [][]walkMap = new boolean[1024][1024];
    public static boolean [][]walkMapFixed = new boolean[1024][1024];
    public static boolean [][]chokeWalkMap = new boolean[1024][1024];
    public static int [][]centerDistWalkMap = new int[1024][1024];
    public static int [][]pathWalkMap = new int[1024][1024];

    public static int [][]defenseMap = new int[256][256];
    public static int [][]bunkerMap = new int[256][256];
    public static int [][]turretMap = new int[256][256];
    public static boolean [][]siegeMap = new boolean[256][256];
    public static boolean [][]wallMap = new boolean[256][256];

    public static boolean [][]mainDefMap = new boolean[256][256];
    public static boolean [][]naturalDefMap = new boolean[256][256];
    public static boolean [][]thirdDefMap = new boolean[256][256];

    public static ArrayList<TilePosition> myMainDefList = new ArrayList<>();
    public static ArrayList<TilePosition> myNaturalDefList = new ArrayList<>();
    public static ArrayList<TilePosition> myHighDefList = new ArrayList<>();
    public static ArrayList<TilePosition> myThirdDefList = new ArrayList<>();

    public static TilePosition myMainTile = TilePosition.None;
    public static TilePosition myNaturalTile = TilePosition.None;
    public static Position myMain = Position.None;
    public static Position myNatural = Position.None;
    public static Position myMainDef = Position.None;
    public static Position myNaturalDef = Position.None;
    public static TilePosition myEntrance = TilePosition.None;
    public static Position myBunkerDefPos = Position.None;
    public static int bunkerNaturalSqrtDist = 36864;

    public static TilePosition enemyStart = TilePosition.None;
    public static Position enemyMain = Position.None;
    public static Position enemyNatural = Position.None;

    public static TilePosition []mainTiles = new TilePosition[8];
    public static TilePosition []naturalTiles = new TilePosition[8];
    public static int mn = 8;
    public static int mm = 0;

    public static ArrayList<Position> unScouted = new ArrayList<>();
    public static int entranceCircleIncrement = -1;

    public static Position []mainPos = new Position[8];
    public static Position []naturalPos = new Position[8];
    public static Position []mainChokePos = new Position[8];
    public static Position []naturalChokePos = new Position[8];
    public static Position []mainDefPos = new Position[8];
    public static Position []naturalDefPos = new Position[8];
    public static WalkPosition[]mainChokeEdge = new WalkPosition[16];
    public static WalkPosition []naturalChokeEdge = new WalkPosition[16];

    public static TilePosition [][]mainDefTile = new TilePosition[8][2];
    public static TilePosition [][]naturalDefTile = new TilePosition[8][2];
    public static TilePosition []rushDefTile = new TilePosition[8];
    public static Position []testPos = new Position[8];
    public static TilePosition sas_tile = TilePosition.None;
    public static TilePosition [][]naturalWallTile = new TilePosition[8][2];

    public static ArrayList<TilePosition> planSmall = new ArrayList<>();
    public static ArrayList<TilePosition> planLarge = new ArrayList<>();
    public static ArrayList<TilePosition> planSmallTech = new ArrayList<>();
    public static ArrayList<TilePosition> planLargeTech = new ArrayList<>();

    public static ArrayList<Position> gridCircle = new ArrayList<>();
    public static ArrayList<Position> airCircle = new ArrayList<>();
    public static ArrayList<Position> flyCircle = new ArrayList<>();

    public static TilePosition mySneakyTile = TilePosition.None;
    public static int mySneakyDirection = 0;

    public static int []cdx8 = { +1,+1,+0,-1,-1,-1,+0,+1 };
    public static int []cdy8 = { +0,+1,+1,+1,+0,-1,-1,-1 };
    public static int []cdz8 = {  2, 3, 2, 3, 2, 3, 2, 3 };
    public static int []cdx24 = new int[24];
    public static int []cdy24 = new int[24];
    public static int []cr4dx = new int[24];
    public static int []cr4dy = new int[24];

    public static int []circ_area_r1 = { 6,7,8,8,8,8,8,8,8,8,8,8,8,8,7,6 };

    public static int []circ_area_r6 = { 10,13,15,17,18,20,21,22,23,24,25,25,26,27,27,28,28,29,29,29,30,30,30,30,30,30,30,30,30,30,
            30,30,30,30,30,30,30,30,30,30,29,29,29,28,28,27,27,26,25,25,24,23,22,21,20,18,17,15,13,10 };

    public static int []circ_area_r8 = { 11,14,16,18,20,22,23,24,25,26,27,28,29,30,31,31,32,32,33,33,34,34,35,35,35,36,36,36,36,36,36,36,36,36,36,36,
            36,36,36,36,36,36,36,36,36,36,36,35,35,35,34,34,33,33,32,32,31,31,30,29,28,27,26,25,24,23,22,20,18,16,14,11 };

    public static int []circ_area_r9 = { 11,14,17,19,21,23,25,26,27,28,29,30,31,32,33,34,34,35,35,36,36,37,37,38,38,38,39,39,39,40,40,40,40,40,40,40,40,40,40,40,
            40,40,40,40,40,40,40,40,40,40,40,39,39,39,38,38,38,37,37,36,36,35,35,34,34,33,32,31,30,29,28,27,26,25,23,21,19,17,14,11 };

    public static int []circ_area_r11 = { 16,18,21,23,25,28,30,32,35,37,38,39,39,40,40,40,41,41,42,42,43,43,43,44,44,45,45,46,46,46,47,47,48,48,48,48,48,48,48,48,48,48,48,48,48,48,48,48,
            48,48,48,48,48,48,48,48,48,48,48,48,48,48,48,48,47,47,46,46,46,45,45,44,44,43,43,43,42,42,41,41,40,40,40,39,39,38,37,35,32,30,28,25,23,21,18,16 };

    public static int []circ_area_r12 = { 13,17,20,22,24,26,28,30,31,33,34,35,36,37,38,39,40,41,42,43,43,44,45,45,46,46,47,47,48,48,49,49,50,50,50,51,51,51,51,52,52,52,52,52,52,52,52,52,52,52,52,52,
            52,52,52,52,52,52,52,52,52,52,52,52,52,51,51,51,51,50,50,50,49,49,48,48,47,47,46,46,45,45,44,43,43,42,41,40,39,38,37,36,35,34,33,31,30,28,26,24,22,20,17,13 };

    public static int []circ_area_def6 = { 13,16,18,20,22,23,24,25,26,27,28,28,29,29,30,30,31,31,31,32,32,32,32,32,32,32,32,32,32,32,
            32,32,32,32,32,32,32,32,32,32,32,31,31,31,30,30,29,29,28,28,27,26,25,24,23,22,20,18,16,13 };

    public static int []circ_area_def7 = { 14,16,19,21,24,26,28,28,29,29,30,30,30,31,31,32,32,32,33,33,34,34,34,34,34,34,34,34,34,34,34,34,34,34,
            34,34,34,34,34,34,34,34,34,34,34,34,34,34,33,33,32,32,32,31,31,30,30,30,29,29,28,28,26,24,21,19,16,14 };

    static {
        Arrays.fill(mainTiles, TilePosition.None);
        Arrays.fill(naturalTiles, TilePosition.None);
        Arrays.fill(mainChokePos, Position.None);
        Arrays.fill(naturalChokePos, Position.None);
        Arrays.fill(mainDefPos, Position.None);
        Arrays.fill(naturalDefPos, Position.None);
        Arrays.fill(mainChokeEdge, WalkPosition.None);
        Arrays.fill(naturalChokeEdge, WalkPosition.None);

        for (TilePosition[] a : mainDefTile) {
            Arrays.fill(a, TilePosition.None);
        }
        for (TilePosition[] a : naturalDefTile) {
            Arrays.fill(a, TilePosition.None);
        }

        Arrays.fill(rushDefTile, TilePosition.None);
        Arrays.fill(testPos, Position.None);

        for (TilePosition[] a : naturalWallTile) {
            Arrays.fill(a, TilePosition.None);
        }

        Arrays.fill(cdx24, 0);
        Arrays.fill(cdy24, 0);
        Arrays.fill(cr4dx, 0);
        Arrays.fill(cr4dy, 0);
    }

}
