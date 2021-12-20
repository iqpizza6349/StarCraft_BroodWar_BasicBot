package com.tistory.workshop6349.basicbot;

import bwapi.Unit;

import java.util.ArrayList;

public class BasicUnits {

    public static final ArrayList<SCV> scvs = new ArrayList<>();

    public static final ArrayList<Fighter> marines = new ArrayList<>();
    public static final ArrayList<Fighter> firebats = new ArrayList<>();
    public static final ArrayList<Fighter> medics = new ArrayList<>();
    public static final ArrayList<Fighter> ghosts = new ArrayList<>();
    public static final ArrayList<Fighter> siegeTanks = new ArrayList<>();
    public static final ArrayList<Fighter> goliaths = new ArrayList<>();
    public static final ArrayList<Fighter> vultures = new ArrayList<>();

    public static final ArrayList<Flyer> wraiths = new ArrayList<>();
    public static final ArrayList<Flyer> dropShips = new ArrayList<>();
    public static final ArrayList<Flyer> valkyries = new ArrayList<>();
    public static final ArrayList<Flyer> vessels = new ArrayList<>();
    public static final ArrayList<Flyer> cruiser = new ArrayList<>();

    public static int countSCV = 0;
    public static int countMarine = 0;
    public static int countFirebat = 0;
    public static int countMedic = 0;
    public static int countGhost = 0;
    public static int countSiegeTank = 0;
    public static int countGoliath = 0;
    public static int countVulture = 0;
    public static int countWraith = 0;
    public static int countDropShip = 0;
    public static int countValkyrie = 0;
    public static int countVessel = 0;
    public static int countCruiser = 0;

    public static boolean hasScout = false;
    
    public void countUnit(Unit unit) {
        switch (unit.getType()) {
            case Terran_SCV:
                ++countSCV;
                break;
            case Terran_Marine:
                ++countMarine;
                break;
            case Terran_Firebat:
                ++countFirebat;
                break;
            case Terran_Medic:
                ++countMedic;
                break;
            case Terran_Ghost:
                ++countGhost;
                break;
            case Terran_Siege_Tank_Tank_Mode:
            case Terran_Siege_Tank_Siege_Mode:
                ++countSiegeTank;
                break;
            case Terran_Goliath:
                ++countGoliath;
                break;
            case Terran_Vulture:
                ++countVulture;
                break;
            case Terran_Wraith:
                ++countWraith;
                break;
            case Terran_Dropship:
                ++countDropShip;
                break;
            case Terran_Valkyrie:
                ++countValkyrie;
                break;
            case Terran_Science_Vessel:
                ++countVessel;
                break;
            case Terran_Battlecruiser:
                ++countCruiser;
                break;
        }
    }

    public void appendUnit(Unit unit) {
        
    }

    





















}
