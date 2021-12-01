package com.tistory.workshop6349.basicbot;

public class Config {

    public static String logFileName;
    public static String timeOutFileName;
    public static String errorLogFileName;
    public static String readDirectory = "bwapi-data/read/";
    public static String writeDirectory = "bwapi-data/write/";

    public static int setLocalSpeed = 0;
    public static int setFrameSkip = 0;
    public static boolean enableUserInput = true;
    public static boolean enableCompleteMapInformation = false;

    public static final int MAP_GRID_SIZE = 32;

    public static boolean drawGameInfo = true;
    public static boolean drawScoutInfo = true;
    public static boolean drawMouseCursorInfo = true;
    public static boolean drawBWEMInfo = true;
    public static boolean drawUnitTargetInfo = true;
    public static boolean drawMyUnit = true;
    public static boolean drawEnemyUnit = true;
    public static boolean drawLastCommandInfo = true;
    public static boolean drawUnitStatus = true;

    public static int duration = 1;
    public static boolean recording = true;

    public enum columns {
        REMAINING_GAS,
        REMAINING_MINERAL,
        GATHERED_GAS,
        GATHERED_MINERAL,
        NUMBER_OF_SCV_FOR_GAS,
        NUMBER_OF_SCV_OF_FOR_MINERAL
    }

}
