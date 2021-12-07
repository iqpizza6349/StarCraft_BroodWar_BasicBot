package com.tistory.workshop6349.studyBasicBot;

import bwapi.UnitType;
import com.tistory.workshop6349.studyBasicBot.buildOrder.BuildManager;

public class Main {

    public static void main(String[] args) {
        BuildManager.getInstance().buildOrderQueue.addLowestPriority(UnitType.Terran_SCV, 1, true);
        BuildManager.getInstance().buildOrderQueue.addLowestPriority(UnitType.Terran_SCV, 2, true);
        BuildManager.getInstance().buildOrderQueue.addLowestPriority(UnitType.Terran_SCV, 3, true);
        BuildManager.getInstance().buildOrderQueue.addLowestPriority(UnitType.Terran_SCV, 4, true);
        BuildManager.getInstance().buildOrderQueue.addLowestPriority(UnitType.Terran_Supply_Depot, 5, true);
        BuildManager.getInstance().buildOrderQueue.addLowestPriority(UnitType.Terran_SCV, 6, true);
        BuildManager.getInstance().buildOrderQueue.addLowestPriority(UnitType.Terran_SCV, 7, true);
        BuildManager.getInstance().sayHighestInfo();
    }
}
