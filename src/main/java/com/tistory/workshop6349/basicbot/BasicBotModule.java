package com.tistory.workshop6349.basicbot;

import bwapi.*;
import bwem.BWEM;
import com.tistory.workshop6349.basicbot.config.Common;
import com.tistory.workshop6349.basicbot.config.Config;

public class BasicBotModule implements BWEventListener {

    private static BWClient bwClient;
    public static Game BroodWar;
    public static BWEM MAP;

    private static final GameCommander gameCommander = new GameCommander();
    private static String mapFileName = "";

    public void run() {
        bwClient = new BWClient(this);
        bwClient.startGame();
    }

    @Override
    public void onStart() {
        BroodWar = bwClient.getGame();
        MAP = new BWEM(BroodWar);

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
            MAP.initialize();
            MAP.getMap().enableAutomaticPathAnalysis();

        } catch (Exception e) {
            e.printStackTrace();
        }

        BroodWar.setLocalSpeed(Config.setLocalSpeed);
        BroodWar.setFrameSkip(Config.setFrameSkip);

        gameCommander.onStart();
    }

    @Override
    public void onEnd(boolean isWinner) {
        System.out.println("[ " + BroodWar.getFrameCount() + " ] " + (isWinner ? "won" : "lost") + " the game");

        gameCommander.onEnd(isWinner);
        BroodWar.printf("Game End");
    }

    @Override
    public void onFrame() {
        try {
            gameCommander.onFrame();
        } catch (Exception e) {
            e.printStackTrace();
            // TODO Common.error(주 전략, 상대 종족, 내 위치, 상대 위치)
        }
        // TODO 현재 상태 계속 보고 (개발단계의 경우: 24프레임(약 1초 ~ 1.2초정도) 마다 보고, 테스트의 경우: 360프레임(약 15초~16.5초) 마다 보고)
        if (Common.Time() % 360 == 0) {
            int gatheredMinerals = BroodWar.self().gatheredMinerals();
            int gatheredGas = BroodWar.self().gatheredGas();
            int currentMinerals = BroodWar.self().minerals();
            int currentGas = BroodWar.self().gas();

            int time = Common.Time() / 24;
            String logMsg = "{log: }" + time + ", " + currentMinerals + ", "
                    + currentGas + ", " + gatheredMinerals + ", " + gatheredGas;
            Common.appendTextToFile(mapFileName, logMsg);
        }

        // TODO UXManager.getInstance().update();
    }

    public void parseCommand(String cmd) {
        boolean speedChange = false;

        switch (cmd) {
            case "afap":
            case "vf":
                Config.setLocalSpeed = 0;
                speedChange = true;
                break;
            case "fast":
            case "f":
                Config.setLocalSpeed = 24;
                speedChange = true;
                break;
            case "slow":
            case "s":
                Config.setLocalSpeed = 42;
                speedChange = true;
                break;
            case "asap":
            case "vs":
                Config.setLocalSpeed = 100;
                speedChange = true;
                break;
            case "+":
                Config.setLocalSpeed /= 2;
                speedChange = true;
                break;
            case "-":
                Config.setLocalSpeed *= 2;
                speedChange = true;
                break;
            case "fc":
                Config.drawLastCommandInfo = !Config.drawLastCommandInfo;
                break;
            case "st":
                Config.drawUnitStatus = !Config.drawUnitStatus;
                break;
            case "mu":
                Config.drawMyUnit = !Config.drawMyUnit;
                if (Config.drawMyUnit) {
                    Config.drawEnemyUnit = false;
                }
                break;
            case "eu":
                Config.drawEnemyUnit = !Config.drawEnemyUnit;
                if (Config.drawEnemyUnit) {
                    Config.drawMyUnit = false;
                }
                break;
            case "end":
                BroodWar.setGUI(false);
                break;
            case "b":
                BroodWar.sendText("black sheep wall");
                break;
            case "show":
                BroodWar.sendText("show me the money");
                break;
        }

        if (speedChange) {
            BroodWar.setLocalSpeed(Config.setLocalSpeed);
            BroodWar.setFrameSkip(0);
        }
    }

    @Override
    public void onSendText(String text) {
        parseCommand(text);

        gameCommander.onSendText(text);

        BroodWar.sendText(text);
    }

    @Override
    public void onReceiveText(Player player, String text) {
        gameCommander.onReceiveText(player, text);
    }

    @Override
    public void onPlayerLeft(Player player) {
        gameCommander.onPlayerLeft(player);
    }

    @Override
    public void onNukeDetect(Position target) {
        gameCommander.onNukeDetect(target);
    }

    @Override
    public void onUnitDiscover(Unit unit) {
        gameCommander.onUnitDiscover(unit);
    }

    @Override
    public void onUnitEvade(Unit unit) {
        gameCommander.onUnitEvade(unit);
    }

    @Override
    public void onUnitShow(Unit unit) {
        gameCommander.onUnitShow(unit);
    }

    @Override
    public void onUnitHide(Unit unit) {
        gameCommander.onUnitHide(unit);
    }

    @Override
    public void onUnitCreate(Unit unit) {
        gameCommander.onUnitCreate(unit);
    }

    @Override
    public void onUnitDestroy(Unit unit) {
        gameCommander.onUnitDestroy(unit);

        try {
            if (unit.getType().isMineralField()
                    || unit.getType().isSpecialBuilding()) {
                MAP.getMap().onUnitDestroyed(unit);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUnitMorph(Unit unit) {
        gameCommander.onUnitMorph(unit);
    }

    @Override
    public void onUnitRenegade(Unit unit) {
        gameCommander.onUnitRenegade(unit);
    }

    @Override
    public void onSaveGame(String gameName) {
        gameCommander.onSaveGame(gameName);
    }

    @Override
    public void onUnitComplete(Unit unit) {
        gameCommander.onUnitComplete(unit);
    }

    @Override
    public void onPlayerDropped(Player player) {

    }
}
