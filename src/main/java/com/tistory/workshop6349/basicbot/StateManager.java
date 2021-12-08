package com.tistory.workshop6349.basicbot;

import bwapi.Position;
import bwapi.Unit;

import java.util.ArrayList;

public class StateManager {

    private static final StateManager STATE_MANAGER = new StateManager();
    public static StateManager getInstance() {
        return STATE_MANAGER;
    }

    // Strategy
    public int strategy = 1;
    public int test_strategy = 0;
    public int orig_strategy = 1;
    public boolean show_info = true;
    public boolean is_vs_human = false;
    public boolean avoidWeakStrategies = false;
    public boolean useHardcodedStrategies = false;
    public boolean umsToPracticeMicro = false;

    // general
    public int myTime = 0;      // in seconds for fastest speed

    // supply counts (natural count)
    public int supplyWorker = 0;
    public int supplyMil = 0;
    public int supplyBio = 0;
    public int supplyMech = 0;
    public int supplyAir = 0;

    //  supply thresholds
    public int attackSupply = 15;
    public int miningSupply = 20;
    public int attackSupplyModifier = 0;

    //available resources containers
    public int mineralCount = 8;
    public int geyserCount = 1;
    public int minSCVCountMineral = 0;
    public int minSCVCountGas = 0;

    // tactic booleans
    public boolean isSwarming = false;
    public boolean doBioSCVRush = false;
    public boolean isChokeDef = false;
    public boolean isRushing = false;

    public boolean goBio = true;
    public int maxBioUpgrade = 3;
    public boolean fastExpand = true;
    public boolean siegeStarted = false;

    public boolean hasNatural = false;
    public boolean hasTransport = false;
    public boolean goIslands = false;
    public boolean goRaiding = false;
    public boolean blockedMainChoke = false;
    public int availableScans = 0;

    public boolean rushAlert = false;
    public boolean eightRax = false;
    public boolean needTurrets = false;
    public boolean needTurrets_2 = false;
    public boolean needDetection = false;
    public boolean stimAllowed = false;
    public boolean avoidGridDef = false;
    public boolean proxyAlert = false;
    public boolean proxyProdAlert = false;
    public boolean cannonRushAlert = false;
    public boolean carrierRushAlert = false;
    public boolean lurkerRushAlert = false;
    public boolean holdBunker = false;

    // boolean guardSiege
    public boolean flyerAttackAirDef = false;
    public boolean needScoutNatural = false;

    // army positions
    public Position retreatPos = Position.None;
    public Position gatherPos = Position.None;
    public Position defTargetPos = Position.None;
    public Position leaderPos = Position.None;

    public Position enemyNaturalPos = Position.None;
    public int leaderDist = 0;

    public ArrayList<Unit> targetList = new ArrayList<>();

    // special counts
    public int countTrapped = 0;
    public int highGroundDefenseTank = 0;
    public int plannedHighGroundDefense = 0;

    public void init() {
        // strategy dependent adaptations
        switch (strategy) {
            case 1:
                doBioSCVRush = true;
                goBio = true;
                fastExpand = false;
                attackSupplyModifier = 0;
                break;

            case 2:
                goBio = true;
                fastExpand = true;
                attackSupplyModifier = 6;
                break;

            case 3:
                goBio = false;
                fastExpand = true;
                attackSupplyModifier = 12;
                break;

            case 4:
                goBio = false;
                fastExpand = false;
                attackSupplyModifier = 12;
                break;

            case 6:
                goBio = true;
                fastExpand = false;
                attackSupplyModifier = 6;
                break;

        }
    }

    public void update(int n_ds) {
        // mining supply thresholds
        miningSupply = 3 + 2 * mineralCount + 3 * geyserCount;
        if (miningSupply > 60) {
            miningSupply = 60;
        }

        // attack supply thresholds
        attackSupply = (myTime / Config.LocalSpeed) + (15 * BasicBotAI.BroodWar.enemies().size()) - 15;
        if (attackSupply < 15) {
            attackSupply = 15;
        }
        if (attackSupply > 60) {
            attackSupply = 60;
        }
        attackSupply += attackSupplyModifier;

        if (strategy == 4) {
            attackSupply = 90;
            if (miningSupply > 40) {
                miningSupply = 40;
            }
            if (miningSupply > 30 && myTime < 900) {
                miningSupply = 30;
            }
        }

        // other stuff
        if (carrierRushAlert) {
            attackSupply = 12;
        }
        if (isRushing && myTime > 600) {
            isRushing = false;
        }
        targetList.clear();
        hasTransport = n_ds > 0;
    }



}
