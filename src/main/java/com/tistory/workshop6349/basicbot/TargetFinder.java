package com.tistory.workshop6349.basicbot;

import bwapi.*;

public class TargetFinder {

    public static void updateAllTargets() {
        EnemyManager EM = EnemyManager.getInstance();

        EM.tlg.clear();
        EM.tlgg.clear();
        EM.tlga.clear();
        EM.tlgs.clear();
        EM.tlgc.clear();
        EM.tlgw.clear();
        EM.tlgm.clear();
        EM.tlgl.clear();
        EM.tla.clear();
        EM.tlag.clear();
        EM.tlaa.clear();
        EM.tlas.clear();
        EM.tlac.clear();

        EM.tleggla.clear();
        EM.tlflare.clear();
        EM.tllockd.clear();
        EM.tlyama.clear();
        EM.tldefg.clear();
        EM.tldefa.clear();

        EM.target_count = 0;

        for (Player p : BasicBotAI.BroodWar.enemies()) {
            for (Unit u : p.getUnits()) {
                if (!u.exists()) {
                    continue;
                }

                UnitType t = u.getType();
                // handle non targetable units
                if ((u.isCloaked() && !u.isDetected())
                        || (u.isBurrowed() && !u.isDetected())
                        || u.isLoaded()
                        || u.isStasised()
                        || u.isInvincible()) {
                    if (u.isFlying()) {
                        EM.tlac.add(u);
                    }
                    else {
                        EM.tlgc.add(u);
                    }
                }
                else {
                    // handle flying units
                    if (u.isFlying()) {
                        if (t.groundWeapon() != WeaponType.None) {
                            EM.tlag.add(u);
                        }
                        if (t.airWeapon() != WeaponType.None) {
                            EM.tlaa.add(u);
                        }
                        if (t == UnitType.Protoss_Carrier) {
                            EM.tlag.add(u);
                            EM.tlaa.add(u);
                            if (!u.isBlind()) {
                                EM.tlflare.add(u);
                            }
                        }
                        if (t == UnitType.Zerg_Overlord
                                || t == UnitType.Zerg_Queen
                                || t == UnitType.Zerg_Cocoon
                                || t == UnitType.Protoss_Observer
                                || t == UnitType.Protoss_Shuttle
                                || t == UnitType.Terran_Science_Vessel
                                || t == UnitType.Terran_Dropship) {
                            EM.tlas.add(u);
                            if (!u.isBlind()) {
                                EM.tlflare.add(u);
                            }
                        }
                        if (t == UnitType.Zerg_Guardian
                                || t == UnitType.Zerg_Devourer
                                || t == UnitType.Terran_Battlecruiser) {
                            if (!u.isBlind()) {
                                EM.tlflare.add(u);
                            }
                        }
                        if (t.isMechanical()
                                && !t.isBuilding()
                                && t != UnitType.Protoss_Interceptor
                                && !u.isLockedDown()) {
                            EM.tllockd.add(u);
                        }
                        EM.tla.add(u);
                    }
                    // handle ground units
                    else {
                        if (t.airWeapon() != WeaponType.None) {
                            EM.tlga.add(u);
                        }
                        if (t.groundWeapon() != WeaponType.None) {
                            EM.tlgg.add(u);
                        }
                        else if (t == UnitType.Zerg_Defiler
                                || t == UnitType.Zerg_Infested_Terran
                                || t == UnitType.Protoss_High_Templar
                                || t == UnitType.Protoss_Dark_Archon
                                || t == UnitType.Protoss_Reaver
                                || t == UnitType.Terran_Medic) {
                            EM.tlgs.add(u);
                        }
                        if ((t.size() == UnitSizeType.Small || t.size() == UnitSizeType.Medium)
                                && t != UnitType.Zerg_Larva
                                && t != UnitType.Zerg_Egg
                                && t != UnitType.Zerg_Lurker_Egg
                                && t != UnitType.Protoss_Scarab) {
                            EM.tlgm.add(u);
                        }
                        else if (t.size() == UnitSizeType.Large && !t.isBuilding()) {
                            EM.tlgl.add(u);
                        }
                        if (t.isMechanical()
                                && !t.isBuilding()
                                && !t.isWorker()
                                && !u.isLockedDown()) {
                            EM.tllockd.add(u);
                        }
                        if (t.isWorker()) {
                            EM.tlgw.add(u);
                        }
                        if (t.isBuilding()) {
                            if (t == UnitType.Terran_Bunker
                                    || t == UnitType.Protoss_Photon_Cannon
                                    || t == UnitType.Zerg_Sunken_Colony) {
                                EM.tldefg.add(u);
                            }
                            if (t == UnitType.Terran_Bunker
                                    || t == UnitType.Terran_Missile_Turret
                                    || t == UnitType.Protoss_Photon_Cannon
                                    || t == UnitType.Zerg_Spore_Colony) {
                                EM.tldefa.add(u);
                            }
                        }
                        if (t == UnitType.Zerg_Larva || t == UnitType.Zerg_Egg) {
                            EM.tleggla.add(u);
                        }
                        else {
                            EM.tlg.add(u);
                        }
                    }
                    EM.target_count++;
                }
            }
        }

        if (BasicBotAI.BroodWar.self().hasResearched(TechType.Yamato_Gun)) {
            for (Player p : BasicBotAI.BroodWar.enemies()) {
                for (Unit u : p.getUnits()) {
                    UnitType t = u.getType();

                    if (t.isBuilding()) {
                        if (t == UnitType.Zerg_Spore_Colony
                                || t == UnitType.Terran_Missile_Turret
                                || t == UnitType.Terran_Bunker
                                || t == UnitType.Protoss_Photon_Cannon) {
                            EM.tlyama.add(u);
                        }
                    }
                    else if (t.isFlyer()) {
                        if (t == UnitType.Zerg_Defiler
                                || t == UnitType.Zerg_Queen
                                || t == UnitType.Zerg_Devourer
                                || t == UnitType.Terran_Wraith
                                || t == UnitType.Terran_Valkyrie
                                || t == UnitType.Terran_Science_Vessel
                                || t == UnitType.Terran_Battlecruiser
                                || t == UnitType.Protoss_Scout
                                || t == UnitType.Protoss_Corsair
                                || t == UnitType.Protoss_Arbiter
                                || t == UnitType.Protoss_Carrier) {
                            EM.tlyama.add(u);
                        }
                    }
                    else {
                        if (t == UnitType.Terran_Goliath
                                || t == UnitType.Protoss_Dragoon) {
                            EM.tlyama.add(u);
                        }
                    }
                }
            }
        }
    }

}
