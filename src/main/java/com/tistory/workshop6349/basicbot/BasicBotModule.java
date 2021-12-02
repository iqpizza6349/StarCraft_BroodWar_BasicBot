package com.tistory.workshop6349.basicbot;

import bwapi.*;
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
            Map.initialize();
            Map.getMap().enableAutomaticPathAnalysis();

        } catch (Exception e) {
            e.printStackTrace();
        }

        BroodWar.setLocalSpeed(Config.setLocalSpeed);
        BroodWar.setFrameSkip(Config.setFrameSkip);

        gameCommander.onStart();
    }

    public void onEnd(boolean isWinner) {
        System.out.println("[ " + BroodWar.getFrameCount() + " ]" + (isWinner ? "won" : "lost") + " the game");

        gameCommander.onEnd(isWinner);
        BroodWar.printf("Game End");
    }

    public void onFrame() {
        try {
            gameCommander.onFrame();
        } catch (Exception e) {
            e.printStackTrace();
            // TODO Common.error(주 전략, 상대 종족, 내 위치, 상대 위치)
        }
        // TODO 현재 상태 계속 보고 (개발단계의 경우: 24프레임(약 1초 ~ 1.2초정도) 마다 보고, 테스트의 경우: 360프레임(약 15초~16.5초) 마다 보고)

        // TODO UXManager.getInstance().update();
    }

    public void parseCommand(String cmd) {
        Player self = BroodWar.self();
        boolean speedChange = false;

        if (cmd.equals("afap") || cmd.equals("vf")) {
            Config.setLocalSpeed = 0;
            speedChange = true;
        }
        else if (cmd.equals("fast") || cmd.equals("f")) {
            Config.setLocalSpeed = 24;
            speedChange = true;
        }
        else if (cmd.equals("slow") || cmd.equals("s")) {
            Config.setLocalSpeed = 42;
            speedChange = true;
        }
        else if (cmd.equals("asap") || cmd.equals("vs")) {
            Config.setLocalSpeed = 100;
            speedChange = true;
        }
        else if (cmd.equals("+")) {
            Config.setLocalSpeed /= 2;
            speedChange = true;
        }
        else if (cmd.equals("-")) {
            Config.setLocalSpeed *= 2;
            speedChange = true;
        }
        else if (cmd.equals("fc")) {
            Config.drawLastCommandInfo = !Config.drawLastCommandInfo;
        }
        else if (cmd.equals("st")) {
            Config.drawUnitStatus = !Config.drawUnitStatus;
        }
        else if (cmd.equals("mu")) {
            Config.drawMyUnit = !Config.drawMyUnit;
            if (Config.drawMyUnit) {
                Config.drawEnemyUnit = false;
            }
        }
        else if (cmd.equals("eu")) {
            Config.drawEnemyUnit = !Config.drawEnemyUnit;
            if (Config.drawEnemyUnit) {
                Config.drawMyUnit = false;
            }
        }
        else if (cmd.equals("end")) {
            BroodWar.setGUI(false);
        }
        else if (cmd.equals("b")) {
            BroodWar.sendText("black sheep wall");
        }
        else if (cmd.equals("show")) {
            BroodWar.sendText("show me the money");
        }

        if (speedChange) {
            BroodWar.setLocalSpeed(Config.setLocalSpeed);
            BroodWar.setFrameSkip(0);
        }
    }

    public void onSendText(String text) {
        parseCommand(text);

        gameCommander.onSendText(text);

        BroodWar.sendText(text);
    }

    public void onReceiveText(Player player, String text) {
        gameCommander.onReceiveText(player, text);
    }

    public void onPlayerLeft(Player player) {
        gameCommander.onPlayerLeft(player);
    }

    public void onNukeDetect(Position target) {
        gameCommander.onNukeDetect(target);
    }

    public void onUnitDiscover(Unit unit) {
        gameCommander.onUnitDiscover(unit);
    }

    public void onUnitEvade(Unit unit) {
        gameCommander.onUnitEvade(unit);
    }

    public void onUnitShow(Unit unit) {
        gameCommander.onUnitShow(unit);
    }

    public void onUnitHide(Unit unit) {
        gameCommander.onUnitHide(unit);
    }

    public void onUnitCreate(Unit unit) {
        gameCommander.onUnitCreate(unit);
    }

    public void onUnitDestroy(Unit unit) {
        gameCommander.onUnitDestroy(unit);

        try {
            if (unit.getType().isMineralField()
                    || unit.getType().isSpecialBuilding()) {
                Map.getMap().onUnitDestroyed(unit);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onUnitMorph(Unit unit) {
        gameCommander.onUnitMorph(unit);
    }

    public void onUnitRenegade(Unit unit) {
        gameCommander.onUnitRenegade(unit);
    }

    public void onSaveGame(String gameName) {
        gameCommander.onSaveGame(gameName);
    }

    public void onUnitComplete(Unit unit) {
        gameCommander.onUnitComplete(unit);
    }

}
