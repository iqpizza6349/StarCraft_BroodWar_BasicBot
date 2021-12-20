package com.tistory.workshop6349.basicbot;

import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;

import java.util.ArrayList;

public class Expo {

    public TilePosition tile;
    public Position pos;

    public ArrayList<Unit> minerals;
    public ArrayList<Unit> geysers;
    public ArrayList<TilePosition> minTiles;
    public ArrayList<TilePosition> gasTiles;

    public boolean isOwned;
    public boolean isEnemy;
    public boolean isMain;
    public boolean isNatural;
    public boolean isIsland;
    public boolean isContinent;
    public boolean isConstructing;
    
    public int minMinerCount;
    public int gasMinerCount;

    public boolean isOverSaturated;
    public boolean isUnderSaturated;

    public Position tPosMin;
    public Position tPosGas;

    public ArrayList<TilePosition> defTiles;

    public int failedBuildAttempts;
    public Position correspondingMain;

    public Expo() {
        tile = TilePosition.Unknown;
        pos = Position.Unknown;
        minerals = new ArrayList<>();
        geysers = new ArrayList<>();
        minTiles = new ArrayList<>();
        gasTiles = new ArrayList<>();

        isOwned = false;
        isEnemy = false;
        isMain = false;
        isNatural = false;
        isIsland = false;
        isContinent = false;
        isConstructing = false;

        minMinerCount = 0;
        gasMinerCount = 0;

        isOverSaturated = false;
        isUnderSaturated = false;

        tPosMin = Position.Unknown;
        tPosGas = Position.Unknown;

        defTiles = new ArrayList<>();

        failedBuildAttempts = 0;
        correspondingMain = Position.None;
    }

    public static ArrayList<Expo> all = new ArrayList<>();








}
