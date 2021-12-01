package com.tistory.workshop6349.basicbot;

import bwapi.Flag;
import bwapi.Game;
import bwem.BWEM;

public class BasicBotModule {

    public static Game BroodWar;
    public static BWEM Map;

    public GameCommander gameCommander = new GameCommander();

    private static String mapFileName;

    public BasicBotModule(Game game, BWEM bwem) {
        BroodWar = game;
        Map = bwem;

        Common.makeDirectory(Config.writeDirectory);
        Config.logFileName = Common.getDateTimeOfNow() + BroodWar.mapFileName() + BroodWar.enemy().getName() + "_LastGameLog.dat";
        Config.timeOutFileName = "TimeOut_" + Common.getDateTimeOfNow() + BroodWar.mapFileName() + BroodWar.enemy().getName() + ".dat";
        Config.errorLogFileName = "Error_" + Common.getDateTimeOfNow() + BroodWar.mapFileName() + BroodWar.enemy().getName() + ".dat";
    }

    public void onStart() {
        System.out.println("Map File Name: " + BroodWar.mapFileName());
        mapFileName = BroodWar.mapFileName() + "_" + Common.getDateTimeOfNow() + ".csv";
        String header = "SECONDS, REMAINING_MINERAL, REMAINING_GAS, GATHERED_MINERAL, GATHERED_GAS, NUMBER_OF_SCV_OF_FOR_MINERAL, NUMBER_OF_SCV_FOR_GAS\n";
        Common.appendTextToFile(mapFileName, header);

        if (Config.enableCompleteMapInformation) {
            BroodWar.enableFlag(Flag.CompleteMapInformation);
        }

        if (Config.enableUserInput) {
            BroodWar.enableFlag(Flag.UserInput);
        }

        BroodWar.setCommandOptimizationLevel(1);

        try {
            if (BroodWar.enemy() != null) {
                System.out.println("The match up is " + BroodWar.self().getRace() + " VS " +  BroodWar.enemy().getRace());
            }
            System.out.println("Map initialization...");

            // Map init

        } catch (Exception e) {
            e.printStackTrace();
        }

        BroodWar.setLocalSpeed(Config.setLocalSpeed);
        BroodWar.setFrameSkip(Config.setFrameSkip);

        // TODO gameCommander.onStart();
    }














}
