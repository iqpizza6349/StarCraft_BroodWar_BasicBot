package com.tistory.workshop6349.examplebot;

import bwapi.Position;
import bwapi.Unit;

import java.util.ArrayList;

public class BaseInfo {

//    private static final BaseInfo BASE_INFO = new BaseInfo();
//    public static BaseInfo getInstance() {
//        return BASE_INFO;
//    }

    public ArrayList<Unit> mineralsWorkers;
    public ArrayList<Unit> gasWorkers;
    public ArrayList<Unit> minerals;
    public ArrayList<Unit> geysers;
    public ArrayList<Unit> buildings;

    public boolean spawn;
    public boolean owned;
    public boolean constructing;
    public boolean possible;
    
    public Unit mainDepot;
    public Position loc;
    public Unit scout;

    public BaseInfo(boolean spawn, boolean owned, boolean constructing, boolean possible, Unit scout) {
        this.mineralsWorkers = new ArrayList<>();
        this.gasWorkers = new ArrayList<>();
        this.minerals = new ArrayList<>();
        this.geysers = new ArrayList<>();
        this.buildings = new ArrayList<>();
        this.spawn = spawn;
        this.owned = owned;
        this.constructing = constructing;
        this.possible = possible;
        this.scout = scout;
        this.mainDepot = null;
        this.loc = Position.Unknown;
    }

    public boolean hasDepot() {
        return false;
    }

    public void update() {

    }
}
