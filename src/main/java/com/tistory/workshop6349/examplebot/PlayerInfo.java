package com.tistory.workshop6349.examplebot;

import bwapi.*;

import java.util.ArrayList;

public class PlayerInfo {

    public String name = "[Unknown]";
    public Race race = Race.Unknown;
    public Player player = null;
    public Position mainBase;
    public ArrayList<UnitInfo> units = new ArrayList<>();
    public ArrayList<UnitInfo> structures = new ArrayList<>();
    public ArrayList<BaseInfo> bases = new ArrayList<>();
    public boolean active = false;

    public PlayerInfo(Player p) {
        this.player = p;
        this.name = p.getName();
        this.race = p.getRace();
        this.active = !p.isDefeated();
        units.clear();
        structures.clear();
        bases.clear();
        mainBase = Position.Unknown;
    }

    public void update() {

    }

}
