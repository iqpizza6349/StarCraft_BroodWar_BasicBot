package com.tistory.workshop6349.examplebot;

import bwapi.Player;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;

public class UnitInfo {

    public boolean isAssigned = false;
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

    public Position text(int x, int y) {
        Position p = new Position(
                unit.getPosition().x - (unit.getType().tileSize().x / 2),
                unit.getPosition().y - (unit.getType().tileSize().y / 2)
        );

        return new Position(p.x + x, p.y + y);
    }

    public Position text() {
        return text(0, 0);
    }

    public Position text(UnitType t, int y) {
        Position p = new Position(
                unit.getPosition().x - (unit.getType().tileSize().x / 2),
                unit.getPosition().y - (unit.getType().tileSize().y / 2)
        );

        return new Position(p.x - t.width(), p.y - y);
    }

    public Position text(UnitType t) {
        return text(t, 0);
    }

    public boolean canSee(Position p) {
        return (position.getDistance(p) <= unitType.sightRange());
    }

    // header methods
    public boolean isDepot() {
        return unitType.isResourceDepot();
    }

    public boolean isWorker() {
        return unitType.isWorker();
    }

    public boolean isBuilding() {
        return unitType.isBuilding();
    }
    
    public boolean isSupply() {
        return unitType.supplyProvided() > 0;
    }

    public boolean isLightAir() {
        return unitType == UnitType.Terran_Wraith
                || unitType == UnitType.Protoss_Scout
                || unitType == UnitType.Zerg_Mutalisk;
    }

    public boolean isCapital() {
        return unitType == UnitType.Terran_Battlecruiser
                || unitType == UnitType.Protoss_Carrier
                || unitType == UnitType.Zerg_Guardian;
    }

    public boolean isTransport() {
        return unitType == UnitType.Zerg_Overlord
                || unitType == UnitType.Terran_Dropship
                || unitType == UnitType.Protoss_Shuttle;
    }

    public boolean isAirSuperiority() {
        return unitType == UnitType.Terran_Valkyrie
                || unitType == UnitType.Protoss_Corsair
                || unitType == UnitType.Zerg_Devourer;
    }

    public boolean isCaster() {
        return unitType.isSpellcaster();
    }

}
