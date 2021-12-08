package com.tistory.workshop6349.basicbot;

import bwapi.Position;
import bwapi.Unit;

public class UnitInfo {

    public Unit unit;
    public int id;
    public Position pos;

    public UnitInfo(Unit u) {
        this.unit = u;
        this.id = u.getID();
        this.pos = u.getPosition();
    }

}
