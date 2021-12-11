package com.tistory.workshop6349.examplebot;

import bwapi.*;

public class UnitInfo {

    public Unit unit;
    public UnitType unitType;

    public int id = -1;
    public int health = 0;
    public int shields = 0;
    public int energy = 0;
    public int resources = 0;

    public Position position = Position.None;
    public Position target = Position.None;
    public Player owner = null;

    public UnitInfo(Unit unit) {
        if (unit == null) {
            return;
        }

        if (!unit.exists()) {
            return;
        }

        this.unit = unit;
        this.unitType = unit.getType();
        this.health = unit.getHitPoints();
        this.shields = unit.getShields();
        this.energy = unit.getEnergy();
        this.position = unit.getPosition();
        if (unitType.isResourceContainer()) {
            this.resources = unit.getResources();
        }
        this.owner = unit.getPlayer();
        this.id = unit.getID();
    }

    public void update() {
        if (unit == null) {
            return;
        }

        health = unit.getHitPoints();
        if (unit.getType() != unitType) {
            unitType = unit.getType();
        }

        shields = unit.getShields();
        energy = unit.getEnergy();
        position = unit.getPosition();
        if (!unit.isIdle()) {
            target = unit.getTargetPosition();
        }
        if (unit.getPlayer() != owner) {
            owner = unit.getPlayer();
        }
    }
}
