package com.tistory.workshop6349.basicbot;

import bwapi.*;
import bwem.BWEM;

import java.io.IOException;

public class BasicBotAI implements BWEventListener {

    private BWClient bwClient;
    public static Game BroodWar;
    public static BWEM bwem;

    public void run() {
        bwClient = new BWClient(this);
        bwClient.startGame();
    }

    @Override
    public void onStart() {
        BroodWar = bwClient.getGame();
        bwem = new BWEM(BroodWar);
        System.out.println("Starting Match");
        bwem.initialize();
        bwem.getMap().enableAutomaticPathAnalysis();
        BroodWar.printf("Hello, World");
        BroodWar.printf("The Map is " + BroodWar.mapName() + ", a " + BroodWar.getStartLocations().size() + " player map");

        //////////////////Config//////////////////
        BroodWar.setFrameSkip(Config.FrameSkip);
        BroodWar.setLocalSpeed(Config.LocalSpeed);
        BroodWar.setCommandOptimizationLevel(2);

        if (Config.EnableInput) {
            BroodWar.enableFlag(Flag.UserInput);
        }
        Config.ShowVisibilityData = false;
        //////////////////Config//////////////////

        //////////////////StateManager//////////////////
        StateManager.getInstance().test_strategy = 0;       // zero for no testing
        StateManager.getInstance().avoidWeakStrategies = true;
        StateManager.getInstance().useHardcodedStrategies = true;
        try {
            StateManager.getInstance().strategy = Learner.chooseStrategy();
        } catch (IOException e) {
            e.printStackTrace();
        }
        StateManager.getInstance().orig_strategy = StateManager.getInstance().strategy;

        StateManager.getInstance().is_vs_human = false;
        StateManager.getInstance().umsToPracticeMicro = true;
        //////////////////StateManager//////////////////


    }

    @Override
    public void onEnd(boolean isWinner) {
        Learner.addResultToHistory(isWinner);
        BroodWar.sendText("gg");
        try {
            System.out.println(ReadWrite.writeLogfile(EnemyManager.getInstance().history, EnemyManager.getInstance().name));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("I " + (isWinner ? "won": "lost") + " the game");
    }

    @Override
    public void onFrame() {

        if (Config.ShowVisibilityData) {
            drawVisibilityData();
        }
        BroodWar.drawTextScreen(300, 0, "FPS: " + BroodWar.getAverageFPS());
    }

    @Override
    public void onSendText(String text) {
        BroodWar.sendText(text);

        boolean changedSpeed = false;
        switch (text) {
            case "show visibility":
                Config.ShowVisibilityData = !Config.ShowVisibilityData;
                break;
            case "afap":
                Config.LocalSpeed = 0;      // CPU 가 낼 수 있는 최대 속도

                changedSpeed = true;
                break;
            case "fast":
                Config.LocalSpeed = 10;
                changedSpeed = true;
                break;
            case "normal":
                Config.LocalSpeed = 24;
                changedSpeed = true;
                break;
            case "slow":
                Config.LocalSpeed = 48;
                changedSpeed = true;
                break;
            case "asap":
                Config.LocalSpeed = 100;    // 엄청 느리게

                changedSpeed = true;
                break;
            default:
                BroodWar.sendText("You typed \"" + text + "\"!");
                break;
        }

        if (changedSpeed) {
            BroodWar.setLocalSpeed(Config.LocalSpeed);
            BroodWar.setFrameSkip(Config.FrameSkip);
        }

    }

    @Override
    public void onReceiveText(Player player, String text) {
        BroodWar.sendText(player.getName() + " said \"" + text + "\"");
    }

    @Override
    public void onPlayerLeft(Player player) {
        BroodWar.sendText(player.getName() + " left the game");
    }

    @Override
    public void onNukeDetect(Position target) {
        if (!target.equals(Position.Unknown)) {
            BroodWar.drawCircleMap(target, 40, Color.Red, true);
            BroodWar.sendText("Nuclear Launch Detected at (" + target.x + ", " + target.y + ")");
        }
    }

    @Override
    public void onUnitDiscover(Unit unit) {

    }

    @Override
    public void onUnitEvade(Unit unit) {

    }

    @Override
    public void onUnitShow(Unit unit) {

    }

    @Override
    public void onUnitHide(Unit unit) {

    }

    @Override
    public void onUnitCreate(Unit unit) {

    }

    @Override
    public void onUnitDestroy(Unit unit) {

    }

    @Override
    public void onUnitMorph(Unit unit) {

    }

    @Override
    public void onUnitRenegade(Unit unit) {

    }

    @Override
    public void onSaveGame(String gameName) {

    }

    @Override
    public void onUnitComplete(Unit unit) {

    }

    @Override
    public void onPlayerDropped(Player player) {

    }

    public void drawVisibilityData() {
        int height = BroodWar.mapHeight(), width = BroodWar.mapWidth();
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                if (BroodWar.isExplored(x, y)) {
                    BroodWar.drawDotMap(x*32+16, y*32+16, (BroodWar.isVisible(x, y) ? Color.Green : Color.Blue));
                }
                else {
                    BroodWar.drawDotMap(x*32+16, y*32+16, Color.Red);
                }
            }
        }
    }
}
