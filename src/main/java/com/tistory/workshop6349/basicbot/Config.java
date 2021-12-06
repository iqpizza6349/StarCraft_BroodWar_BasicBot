package com.tistory.workshop6349.basicbot;

public class Config {

    /// 봇 이름
    public static final String BotName = "SCB";
    /// 봇 개발자 이름
    public static final String BotAuthors = "iqpizza";

    public static int setLocalSpeed = 20;
    public static int setFrameSkip = 0;

    public static boolean enableUserInput = true;
    public static boolean enableCompleteMapInformation = false;

    public static int MAP_GRID_SIZE = 32;
    public static int TILE_SIZE = 32;

    public static String logFilename;
    public static String timeoutFilename;
    public static String errorLogFilename;
    public static String readDirectory = "bwapi-data\\read\\";
    public static String writeDirectory = "bwapi-data\\write\\";

    public static boolean drawGameInfo = true;
    public static boolean drawScoutInfo = true;
    public static boolean drawMouseCursorInfo = true;
    public static boolean drawBWEMInfo = true;
    public static boolean drawUnitTargetInfo = false;
    // 아래는 둘중 한가지만
    public static boolean drawMyUnit = false;
    public static boolean drawEnemyUnit = false;
    public static boolean drawLastCommandInfo = false;
    public static boolean drawUnitStatus = true;


}
