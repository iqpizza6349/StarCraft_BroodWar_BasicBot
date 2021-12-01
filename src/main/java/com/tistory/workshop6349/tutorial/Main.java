package com.tistory.workshop6349.tutorial;

import bwapi.*;
import bwem.BWEM;

public class Main implements BWEventListener {

    public BWClient client;
    public static Game game;
    public static BWEM bwem;
    public static TutorialBot bot;

    public static void main(String[] args) {
        Main main = new Main();
        main.client = new BWClient(main);
        bot = new TutorialBot();
        main.client.startGame();
    }

    @Override
    public void onStart() {
        game = client.getGame();
        bot.onStart();
    }

    @Override
    public void onEnd(boolean isWinner) {
        bot.onEnd(isWinner);
    }

    @Override
    public void onFrame() {
        bot.onFrame();
    }

    @Override
    public void onSendText(String text) {
        bot.onSendText(text);
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
        bot.onUnitShow(unit);
    }

    @Override
    public void onUnitHide(Unit unit) {
        bot.onUnitHide(unit);
    }

    @Override
    public void onUnitCreate(Unit unit) {
        bot.onUnitCreate(unit);
    }

    @Override
    public void onUnitDestroy(Unit unit) {
        bot.onUnitDestroy(unit);
    }

    @Override
    public void onUnitMorph(Unit unit) {
        bot.onUnitMorph(unit);
    }

    @Override
    public void onUnitRenegade(Unit unit) {
        bot.onUnitRenegade(unit);
    }

    @Override
    public void onSaveGame(String gameName) {

    }

    @Override
    public void onUnitComplete(Unit unit) {
        bot.onUnitComplete(unit);
    }

    @Override
    public void onPlayerDropped(Player player) {

    }
}
