package com.tistory.workshop6349.basicbot;

import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

public class SCV {

    public Unit unit;
    public int id;

    // states
    public boolean isMiner;
    public boolean isBuilder;
    public boolean isMilitia;
    public boolean isJanitor;
    public boolean isRepair;
    public boolean isScout;
    public boolean isTrapped;
    public boolean calledTransport;
    public boolean isEntering;
    public Unit transportUnit;

    // positions
    public Position destination;
    public Position expoPos;

    // positions and integers
    private Position securePos;
    private Position attackPos;

    private UnitType buildType;
    private Position buildPos;
    private TilePosition buildTile;

    private int buildQueue;
    private int buildSqrtDist;
    private int blockCount;
    private int x0;
    private int y0;
    private int x1;
    private int y1;

    private Unit damagedUnit;
    private Unit resourceContainer;

    // other queues and states
    private int circleIncrement;
    private Position scoutPos;
    private boolean hasResource;
    private int stuckQueue;
    private int minTargetDistance;





















}
