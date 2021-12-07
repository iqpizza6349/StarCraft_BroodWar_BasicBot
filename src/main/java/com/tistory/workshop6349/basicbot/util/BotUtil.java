package com.tistory.workshop6349.basicbot.util;

import bwapi.TilePosition;
import com.tistory.workshop6349.basicbot.BasicBotModule;

public class BotUtil {

    public static boolean isConnected(TilePosition a, TilePosition b) {
        return BasicBotModule.MAP.getMap().getNearestArea(a).isAccessibleFrom(BasicBotModule.MAP.getMap().getNearestArea(b));
    }

}
