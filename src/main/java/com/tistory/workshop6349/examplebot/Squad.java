package com.tistory.workshop6349.examplebot;

import java.util.ArrayList;

public class Squad {

    public ArrayList<UnitInfo> units = new ArrayList<>();
    public double strength;
    public int order;

    public Squad(UnitInfo unitInfo) {
        this.strength = 0;
        this.order = 0;
        addToSquad(unitInfo);
    }

    public boolean containsUnit(UnitInfo u) {
        for (UnitInfo ui : units) {
            return (ui == u);
        }
        return false;
    }

    public void addToSquad(UnitInfo u) {
        units.add(units.size(), u);
    }

    public boolean removeFromSquad(UnitInfo u) {
        for (UnitInfo ui : units) {
            if (u == ui) {
                units.remove(ui);
                return true;
            }
        }
        return false;
    }

}