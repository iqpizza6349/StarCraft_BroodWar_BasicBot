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
        main.client.startGame();
    }

    @Override
    public void onStart() {
        game = client.getGame();
        bwem = new BWEM(game);
        botModule = new BasicBotModule(game, bwem);

        botModule.onStart();
    }

    @Override
    public void onEnd(boolean isWinner) {
        botModule.onEnd(isWinner);
    }

    @Override
    public void onFrame() {
        botModule.onFrame();
    }

    @Override
    public void onSendText(String text) {
        botModule.onSendText(text);
    }

    @Override
    public void onReceiveText(Player player, String text) {
        botModule.onReceiveText(player, text);
    }

    @Override
    public void onPlayerLeft(Player player) {
        botModule.onPlayerLeft(player);
    }

    @Override
    public void onNukeDetect(Position target) {
        botModule.onNukeDetect(target);
    }

    @Override
    public void onUnitDiscover(Unit unit) {
        botModule.onUnitDiscover(unit);
    }

    @Override
    public void onUnitEvade(Unit unit) {
        botModule.onUnitEvade(unit);
    }

    @Override
    public void onUnitShow(Unit unit) {
        botModule.onUnitShow(unit);
    }

    @Override
    public void onUnitHide(Unit unit) {
        botModule.onUnitHide(unit);
    }

    @Override
    public void onUnitCreate(Unit unit) {
        botModule.onUnitCreate(unit);
    }

    @Override
    public void onUnitDestroy(Unit unit) {
        botModule.onUnitDestroy(unit);
    }

    @Override
    public void onUnitMorph(Unit unit) {
        botModule.onUnitMorph(unit);
    }

    @Override
    public void onUnitRenegade(Unit unit) {
        botModule.onUnitRenegade(unit);
    }

    @Override
    public void onSaveGame(String gameName) {
        botModule.onSaveGame(gameName);
    }

    @Override
    public void onUnitComplete(Unit unit) {
        botModule.onUnitComplete(unit);
    }

    @Override
    public void onPlayerDropped(Player player) {

    }
}
