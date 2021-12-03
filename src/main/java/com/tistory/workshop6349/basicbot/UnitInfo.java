package com.tistory.workshop6349.basicbot;

import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

public class UnitInfo {

    private int unitID;
    private int lastHealth;
    private int lastShields;
    private Player player;
    private Unit unit;
    private Position lastPosition;
    private UnitType type;
    private boolean completed;

    public UnitInfo() {
        unitID = 0;
        lastHealth = 0;
        player = null;
        unit = null;
        lastPosition = Position.None;
        type = UnitType.None;
        completed = false;
    }

    public UnitType getType() {
        return type;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Position getLastPosition() {
        return lastPosition;
    }

    public int getUnitID() {
        return unitID;
    }

    public void setUnitID(int unitID) {
        this.unitID = unitID;
    }

    public int getLastHealth() {
        return lastHealth;
    }

    public void setLastHealth(int lastHealth) {
        this.lastHealth = lastHealth;
    }

    public int getLastShields() {
        return lastShields;
    }

    public void setLastShields(int lastShields) {
        this.lastShields = lastShields;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public void setLastPosition(Position lastPosition) {
        this.lastPosition = lastPosition;
    }

    public void setType(UnitType type) {
        this.type = type;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UnitInfo)) return false;

        UnitInfo that = (UnitInfo) o;

        return this.getUnitID() == that.getUnitID();
    }

}