package com.tistory.workshop6349.basicbot;

import bwapi.Player;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import com.tistory.workshop6349.basicbot.config.Common;
import com.tistory.workshop6349.basicbot.worker.WorkerManager;

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
        TilePosition startLocation = Common.Self().getStartLocation();

        if (startLocation == TilePosition.Unknown || startLocation == TilePosition.None) {
            System.out.println("오류로 인해 진행 불가 상태입니다.");
            return;
        }

        // TODO StrategyManager.getInstance().onStart();
    }

    public void onEnd(boolean isWinner) {

    }

    public void onFrame() {

        if (BasicBotModule.BroodWar.isPaused()
                || BasicBotModule.BroodWar.self() == null || BasicBotModule.BroodWar.self().isDefeated() || BasicBotModule.BroodWar.self().leftGame()
                || BasicBotModule.BroodWar.enemy() == null || BasicBotModule.BroodWar.enemy().isDefeated() || BasicBotModule.BroodWar.enemy().leftGame())
        {
            return;
        }

        WorkerManager.getInstance().update();

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
        WorkerManager.getInstance().onWorkerDestroy(unit);
    }

    public void onUnitMorph(Unit unit) {
        WorkerManager.getInstance().onWorkerMorph(unit);
    }

    public void onUnitRenegade(Unit unit) {

    }

    public void onSaveGame(String gameName) {

    }

    public void onUnitComplete(Unit unit) {
        WorkerManager.getInstance().onWorkerComplete(unit);
    }

    public void onPlayerDropped(Player player) {

    }

    public void onUnitLifted(Unit unit) {
        if (unit.getPlayer() == Common.Self()) {
//            TODO ScvManager::Instance ().onUnitLifted(unit);

        }
    }

    public void onUnitLanded(Unit unit) {
        if (unit.getPlayer() == Common.Self()) {
//            TODO ScvManager::Instance ().onUnitLanded(unit);
        }
    }
}
