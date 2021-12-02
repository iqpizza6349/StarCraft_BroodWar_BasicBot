package com.tistory.workshop6349.basicbot;

import bwapi.TilePosition;

public class GameCommander {

    boolean isToFindError = false;  // 디버깅용
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













}
