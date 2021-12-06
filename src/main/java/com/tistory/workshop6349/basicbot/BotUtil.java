package com.tistory.workshop6349.basicbot;

import bwapi.TilePosition;

public class BotUtil {

    public static boolean isConnected(TilePosition a, TilePosition b) {
        return BasicBotModule.MAP.getMap().getNearestArea(a).isAccessibleFrom(BasicBotModule.MAP.getMap().getNearestArea(b));
    }

}
