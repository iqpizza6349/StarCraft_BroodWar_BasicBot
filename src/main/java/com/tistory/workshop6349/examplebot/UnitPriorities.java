package com.tistory.workshop6349.examplebot;

import bwapi.UnitType;

import java.util.HashMap;

public class UnitPriorities {

    public HashMap<UnitType, Integer> structures = new HashMap<>();

    public void onStart() {
        structures.clear();

        for (UnitType t : UnitType.values()) {
            structures.put(t, 1);
            if (t == UnitType.Terran_Supply_Depot
                    || t == UnitType.Zerg_Overlord
                    || t == UnitType.Protoss_Pylon) {
                structures.put(t, 2);
            }
            if (t.isResourceDepot()) {
                structures.put(t, 3);
            }
            if (t.isWorker()) {
                structures.put(t, 4);
            }
            if (t.canProduce()) {
                structures.put(t, 5);
            }
            if (t == UnitType.Zerg_Hydralisk_Den
                    || t == UnitType.Zerg_Spawning_Pool
                    || t == UnitType.Zerg_Spire
                    || t == UnitType.Zerg_Greater_Spire
                    || t == UnitType.Protoss_Forge
                    || t == UnitType.Protoss_Cybernetics_Core
                    || t == UnitType.Terran_Academy) {
                structures.put(t, 6);
            }
            if (t == UnitType.Terran_Bunker
                    || t == UnitType.Protoss_Photon_Cannon
                    || t == UnitType.Zerg_Spore_Colony
                    || t == UnitType.Zerg_Creep_Colony
                    || t == UnitType.Zerg_Sunken_Colony) {
                structures.put(t, 7);
            }
        }
    }

}
