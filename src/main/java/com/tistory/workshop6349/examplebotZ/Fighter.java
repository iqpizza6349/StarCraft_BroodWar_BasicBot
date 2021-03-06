package com.tistory.workshop6349.examplebotZ;

import bwapi.Color;
import bwapi.UnitType;

import java.util.HashMap;
import java.util.Map;

public class Fighter {

    public UnitPriorities priorities = new UnitPriorities();
    public HashMap<UnitType, Squad> squads = new HashMap<>();

    public void onStart() {
        squads.clear();
        priorities.onStart();
    }

    public void drawInfo() {
        ExampleBot.BroodWar.drawTextScreen(2, 80, "temp: " + squads.size() + " squads managed");
        for (Map.Entry<UnitType, Squad> s : squads.entrySet()) {
            ExampleBot.BroodWar.drawTextScreen(2, 90, "Type: " + s.getKey() + ", unitCount: " + s.getValue().units.size());
            for (int i = 0; i < s.getValue().units.size(); i++) {
                if (i == 0) {
                    ExampleBot.BroodWar.drawCircleMap(s.getValue().units.get(0).position, 5, ExampleBot.BroodWar.self().getColor());
                }
                else {
                    ExampleBot.BroodWar.drawLineMap(s.getValue().units.get(i).position, s.getValue().units.get(0).position, Color.Teal);
                }
            }
        }
    }

    public Squad getSquad(UnitInfo unitInfo) {
        for (Map.Entry<UnitType, Squad> s : squads.entrySet()) {
            if (s.getKey() == unitInfo.unitType) {
                return s.getValue();
            }
        }
        return null;
    }

    public void assignedSquad(UnitInfo unitInfo) {
        Squad squad = getSquad(unitInfo);

        if (squad != null) {
            squad.addToSquad(unitInfo);
            return;
        }

        squads.put(unitInfo.unitType, new Squad(unitInfo));
    }

    public void onUnitDestroy(UnitInfo unitInfo) {
        Squad squad = getSquad(unitInfo);

        if (squad != null) {
            squad.removeFromSquad(unitInfo);
        }
    }
}
