package com.tistory.workshop6349.basicbot;

import bwapi.*;

import java.util.ArrayList;

public class ThreatManager {

    static class Threat {

        public int x0;
        public int y0;
        public int size;
        public int id;
        public int ef;
        public boolean isGround;
        public boolean isAir;
        public boolean isIgnored;
        public int nearSupplyEnemy;
        public int nearSupplySelf;

        public Threat(Position pos, int size, int id, int timer, boolean ground, boolean air, boolean debug) {
            this.x0 = pos.x;
            this.y0 = pos.y;
            this.size = size;
            this.id = id;
            this.ef = BasicBotAI.BroodWar.getFrameCount() + timer;
            this.isGround = ground;
            this.isAir = air;
            this.isIgnored = false;
            this.nearSupplyEnemy = 0;
            this.nearSupplySelf = 0;

            if (this.isGround) {
                MapInfluence.setInfluence(groundMap, this.x0, this.y0, this.size, +1);
            }
            if (this.isAir) {
                MapInfluence.setInfluence(airMap, this.x0, this.y0, this.size, +1);
            }
            if (debug) {
                switch (this.size) {
                    case 16:
                        BasicBotAI.BroodWar.printf("detected Psionic Storm");
                        break;
                    case 56:
                        BasicBotAI.BroodWar.printf("detected lurker");
                        break;
                    case 72:
                        BasicBotAI.BroodWar.printf("detected nuke area");
                        break;
                    case 104:
                        BasicBotAI.BroodWar.printf("detected sieged tank");
                        break;
                    default:
                        BasicBotAI.BroodWar.printf("detected threat");
                        break;
                }
            }
        }

        public void clear() {
            if (!isIgnored) {
                if (isGround) {
                    MapInfluence.setInfluence(groundMap, x0, y0, size, -1);
                }
                if (isAir) {
                    MapInfluence.setInfluence(airMap, x0, y0, size, -1);
                }
                isIgnored = true;
            }
        }
    }

    public static final ArrayList<Threat> siege = new ArrayList<>();
    public static final ArrayList<Threat> nuke = new ArrayList<>();
    public static final ArrayList<Threat> lurker = new ArrayList<>();
    public static final ArrayList<Threat> swarm = new ArrayList<>();
    public static final ArrayList<Threat> storm = new ArrayList<>();
    public static final ArrayList<Threat> disruptionWeb = new ArrayList<>();

    public static int[][] groundDef = new int[1024][1024];
    public static int[][] airDef = new int[1024][1024];
    public static int[][] groundMap = new int[1024][1024];
    public static int[][] airMap = new int[1024][1024];

    public static int[][] staDet = new int[1024][1024];
    public static int[][] dynDet = new int[1024][1024];

    public static boolean debug;

    public static int getGroundDef(Position p) {
        return groundDef[p.y / 8][p.x / 8];
    }

    public static int getAirDef(Position p) {
        return airDef[p.y / 8][p.x / 8];
    }


    public ThreatManager() {
        MapUtil.fillMapInt(groundDef, 0);
        MapUtil.fillMapInt(airDef, 0);
        MapUtil.fillMapInt(groundMap, 0);
        MapUtil.fillMapInt(airMap, 0);
        MapUtil.fillMapInt(staDet, 0);
        MapUtil.fillMapInt(dynDet, 0);
        debug = false;
    }

    public void update() {
        removeExpiredUnit(siege);
        removeExpiredUnit(siege);
        removeExpiredSpell(nuke);
        removeExpiredSpell(swarm);
        removeExpiredSpell(storm);
        removeExpiredSpell(disruptionWeb);

        for (Bullet b : BasicBotAI.BroodWar.getBullets()) {
            if (b.exists()) {
                if (b.getType() == BulletType.Psionic_Storm) {
                    appendSpell(storm, b, 16);
                }
            }
        }

        for (Player p : BasicBotAI.BroodWar.enemies()) {
            for (Unit u : p.getUnits()) {
                if (u.exists()) {
                    if (u.getType() == UnitType.Zerg_Lurker) {
                        if (u.isBurrowed() || u.getOrder() == Order.Burrowing) {
                            appendUnit(lurker, u, 56, 480);
                        }
                        if (u.getOrder() == Order.Unburrowing) {
                            removeUnit(lurker, u);
                        }
                    }
                    if (u.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
                        if (u.isSieged() || u.getOrder() == Order.Sieging) {
                            appendUnit(siege, u, 104, 480);
                        }
                        if (u.getOrder() == Order.Unsieging) {
                            removeUnit(siege, u);
                        }
                    }
                }
            }
        }
    }

    public void removeDestroyed(Unit u) {
        if (u.getType() == UnitType.Terran_Siege_Tank_Siege_Mode) {
            removeUnit(siege, u);
            if (debug) {
                BasicBotAI.BroodWar.printf("tank destroyed");
            }
        }
        if (u.getType() == UnitType.Zerg_Lurker) {
            removeUnit(lurker, u);
            if (debug) {
                BasicBotAI.BroodWar.printf("lurker destroyed");
            }
        }
    }

    public void appendNuke(Position p) {
        if (p != Position.Unknown) {
            Threat threat = new Threat(p, 72, 0, 420, true, true, debug);
            nuke.add(threat);
        }
    }

    public void appendUnit(ArrayList<Threat> v, Unit u, int s, int timer) {
        if (!arrayHoldsAt(v, u.getPosition())) {
            Threat threat = new Threat(u.getPosition(), s, u.getID(), timer, true, false, debug);
            v.add(threat);
        }
    }

    public void appendSpell(ArrayList<Threat> v, Bullet b, int s) {
        if (!arrayHoldsAt(v, b.getPosition())) {
            Threat threat = new Threat(b.getPosition(), s, b.getID(), b.getRemoveTimer(), true, true, debug);
            v.add(threat);
        }
    }

    public boolean arrayHoldsId(ArrayList<Threat> v, int id) {
        for (Threat t : v) {
            if (t.id == id) {
                return true;
            }
        }
        return false;
    }

    public boolean arrayHoldsAt(ArrayList<Threat> v, Position p) {
        for (Threat t : v) {
            if (t.x0 == p.x && t.y0 == p.y) {
                return true;
            }
        }
        return false;
    }

    public void removeUnit(ArrayList<Threat> v, Unit u) {
        for (Threat t : v) {
            if (u.getID() == t.id) {
                t.clear();
                v.remove(t);
            }
        }
    }

    public void removeExpiredUnit(ArrayList<Threat> v) {
        for (Threat t : v) {
            if (BasicBotAI.BroodWar.getFrameCount() > t.ef) {
                Unit u = BasicBotAI.BroodWar.getUnit(t.id);

                if (u.exists()
                        && (u.isSieged() || u.isBurrowed())) {
                    t.ef += 480;
                    if (debug) {
                        BasicBotAI.BroodWar.printf("unit still there");
                    }
                }
                else {
                    t.clear();
                    v.remove(t);
                }
            }
        }
    }

    public void removeExpiredSpell(ArrayList<Threat> v) {
        for (Threat t : v) {
            if (BasicBotAI.BroodWar.getFrameCount() > t.ef) {
                t.clear();
                v.remove(t);
            }
        }
    }

    public void ignoreStrayLurkers(int scanCount) {
        if (!lurker.isEmpty() && scanCount > 0) {
            int i = (BasicBotAI.BroodWar.getFrameCount() / 8) % lurker.size();

        }
    }

    public void updateNearSupply(Threat t) {
        t.nearSupplyEnemy = 0;
        t.nearSupplySelf = 0;

        for (Player p : BasicBotAI.BroodWar.enemies()) {
            for (Unit u : p.getUnits()) {
                if (u.getType().supplyRequired() > 0
                        && !u.getType().isWorker()
                        && BotUtil.sqDist(t.x0, t.y0, u.getPosition().x, u.getPosition().y) < 147456) {
                    t.nearSupplyEnemy += u.getType().supplyRequired();
                }
            }
        }

        for (Unit u : BasicBotAI.BroodWar.self().getUnits()) {
            if (u.getType().supplyRequired() > 0
                    && !u.getType().isWorker()
                    && BotUtil.sqDist(t.x0, t.y0, u.getPosition().x, u.getPosition().y) < 147456) {
                t.nearSupplySelf += u.getType().supplyRequired();
            }
        }

        if (!t.isIgnored
                && t.nearSupplySelf >= 12
                && t.nearSupplySelf >= 3 * t.nearSupplyEnemy) {
            t.clear();
        }
    }


}
