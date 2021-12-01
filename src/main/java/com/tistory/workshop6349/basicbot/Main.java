package com.tistory.workshop6349.basicbot;

import bwapi.*;
import bwem.BWEM;

public class Main implements BWEventListener {

    public BWClient client;
    public static Game game;
    public static BWEM bwem;
    public static BasicBotModule botModule;

    public static void main(String[] args) {
        Main main = new Main();
        main.client = new BWClient(main);
        game = main.client.getGame();
        bwem = new BWEM(game);
        botModule = new BasicBotModule(game, bwem);
        main.client.startGame();
    }

    @Override
    public void onStart() {
        botModule.onStart();
    }

    @Override
    public void onEnd(boolean isWinner) {

    }

    @Override
    public void onFrame() {

    }

    @Override
    public void onSendText(String text) {

    }

    @Override
    public void onReceiveText(Player player, String text) {

    }

    @Override
    public void onPlayerLeft(Player player) {

    }

    @Override
    public void onNukeDetect(Position target) {

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
}
