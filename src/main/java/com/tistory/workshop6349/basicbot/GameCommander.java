package com.tistory.workshop6349.basicbot;

import bwapi.Player;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;

public class GameCommander {

    boolean isToFindError;  // 디버깅용
    private static final GameCommander gameCommander = new GameCommander();
    public static GameCommander getInstance() {
        return gameCommander;
    }

    public GameCommander() {
        isToFindError = false;
    }

    public void onStart() {
        TilePosition startLocation = BasicBotModule.BroodWar.self().getStartLocation();

        if (startLocation == TilePosition.Unknown || startLocation == TilePosition.None) {
            System.out.println("오류로 인해 진행 불가 상태입니다.");
            return;
        }

        // TODO StrategyManager.getInstance().onStart();
    }

    public void onEnd(boolean isWinner) {

    }

    public void onFrame() {

    }

    public void onSendText(String text) {

    }

    public void onReceiveText(Player player, String text) {

    }

    public void onPlayerLeft(Player player) {

    }

    public void onNukeDetect(Position target) {

    }

    public void onUnitDiscover(Unit unit) {

    }

    public void onUnitEvade(Unit unit) {

    }

    public void onUnitShow(Unit unit) {

    }

    public void onUnitHide(Unit unit) {

    }

    public void onUnitCreate(Unit unit) {

    }

    public void onUnitDestroy(Unit unit) {

    }

    public void onUnitMorph(Unit unit) {

    }

    public void onUnitRenegade(Unit unit) {

    }

    public void onSaveGame(String gameName) {

    }

    public void onUnitComplete(Unit unit) {

    }

    public void onPlayerDropped(Player player) {

    }

    public void onUnitLifted(Unit unit) {
        if (unit.getPlayer() == BasicBotModule.BroodWar.self()) {
//            TODO ScvManager::Instance ().onUnitLifted(unit);

        }
    }

    public void onUnitLanded(Unit unit) {
        if (unit.getPlayer() == BasicBotModule.BroodWar.self()) {
//            TODO ScvManager::Instance ().onUnitLanded(unit);
        }
    }

}
