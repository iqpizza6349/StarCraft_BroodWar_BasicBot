package com.tistory.workshop6349.basicbot;

import bwapi.*;

public class Fighter {

    public Unit unit;
    public int id;
    public boolean isRaider;
    public boolean isAttacker;

    public boolean isOnlyGround;
    public boolean isSupport;
    public boolean isLooping;
    public boolean specialsAllowed;
    public boolean changedAttackPos;

    public int attackSqrt;
    public int retreatSqrt;
    public TilePosition tile;
    public Position position;
    public Position securePosition;
    public Position attackPosition;
    public Position specialPosition;
    public Position destinPosition;
    public Position stuckPosition;

    public int attackQueue;
    public int specialQueue;
    public int retreatQueue;
    public int spiderMineQueue;
    public int commandFrames;
    public int stuckQueue;

    public Unit target;

    public Unit transport;

    public Fighter(Unit unit) {
        this.unit = unit;
        this.id = unit.getID();

        this.isOnlyGround = false;
        this.isSupport = false;
        this.isLooping = false;
        this.specialsAllowed = true;
        this.changedAttackPos = true;
        this.isRaider = false;
        this.isAttacker = false;

        this.tile = unit.getTilePosition();
        this.position = unit.getPosition();
        this.securePosition = StateManager.getInstance().retreatPos;
        this.attackPosition = StateManager.getInstance().gatherPos;
        this.specialPosition = Position.None;
        this.destinPosition = Position.None;
        this.stuckPosition = Position.None;
        
        this.attackQueue = 0;
        this.specialQueue = 0;
        this.retreatQueue = 0;
        this.spiderMineQueue = 0;
        this.commandFrames = 0;
        this.stuckQueue = 0;

        this.target = null;
        this.transport = null;

        switch (unit.getType()) {
            case Terran_Marine:
                this.retreatSqrt = (int) (Math.pow(112, 2));
                this.attackSqrt = (int) (Math.pow(192, 2));
                break;

            case Terran_Firebat:
                this.isOnlyGround = true;
                this.retreatSqrt = 0;
                this.attackSqrt = (int) (Math.pow(192, 2));
                break;

            case Terran_Medic:
                this.isSupport = true;
                this.retreatSqrt = 0;
                this.attackSqrt = (int) (Math.pow(128, 2));
                break;

            case Terran_Ghost:
                this.retreatSqrt = (int) (Math.pow(224, 2));
                this.attackSqrt = (int) (Math.pow(256, 2));
                break;

            case Terran_Siege_Tank_Tank_Mode:
                this.isOnlyGround = true;
                this.retreatSqrt = (int) (Math.pow(224, 2));
                this.attackSqrt = (int) (Math.pow(256, 2));
                break;

            case Terran_Goliath:
                this.retreatSqrt = (int) (Math.pow(128, 2));
                this.attackSqrt = (int) (Math.pow(320, 2));
                break;

            case Terran_Vulture:
                this.isOnlyGround = true;
                this.commandFrames = 6;
                this.retreatSqrt = (int) (Math.pow(112, 2));
                this.attackSqrt = (int) (Math.pow(192, 2));
                break;

            default:
                if (unit.getType().airWeapon() == WeaponType.None) {
                    this.isOnlyGround = true;
                }
                int r = unit.getType().groundWeapon().maxRange();
                this.retreatSqrt = (int) (Math.pow(r - 32, 2));
                this.attackSqrt = (int) (Math.pow(r + 32, 2));
                break;
        }

    }

    public void update() {

        if (unit == null) {
            return;
        }

        if (!unit.exists()
                || unit.isLockedDown()
                || unit.isMaelstrommed()
                || unit.isStasised()
                || unit.getTransport() == null
                || unit.isStartingAttack()
                || unit.isAttackFrame()
                || unit.getOrder() == Order.MedicHeal
                || unit.getOrder() == Order.Sieging
                || unit.getOrder() == Order.Unsieging) {
            return;
        }

        specialsAllowed = false;
        tile = unit.getTilePosition();
        position = unit.getPosition();
        target = null;
        // TODO saveSum(attackQueue, -1);














    }








    // ------- T A R G E T I N G ------- //

    public Unit getTargetAll(int r) {
        Unit u = null;

        if (isOnlyGround) {
            u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlgm, u, position, r);
            u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlgg, u, position, r);
            u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlga, u, position, r);
        }
        else {
            u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlgg, u, position, r);
            u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlag, u, position, r);
            u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlaa, u, position, r);
            u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlga, u, position, r);
            u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tla, u, position, r);
        }
        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlg, u, position, r);
        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlself, u, position, r);

        return u;
    }

    public Unit getTargetHeal() {
        Unit minU = null;
        int dMin = 65536;

        for (Unit u : BasicBotAI.BroodWar.self().getUnits()) {
            if (u.getType().isOrganic()
                    && !u.getType().isWorker()
                    && u.getType() != UnitType.Terran_Medic
                    && u.isCompleted()) {
                int d = BotUtil.sqDist(unit, u);
                if (d < dMin) {
                    dMin = d;
                    minU = u;
                }
                if (d <= 9) {
                    return null;
                }
            }
        }
        return minU;
    }


    public Unit getTargetSiege() {
        Unit u = null;
        int r = 173056; // radius 13

        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlgl, u, position, r);
        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tldefg, u, position, r);
        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlgm, u, position, r);
        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlself, u, position, r);

        return u;
    }

    public Unit getTargetGoliath() {
        Unit u = null;
        int rg = 36864; // radius 6
        int ra = 82944; // radius 9

        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlag, u, position, ra);
        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlas, u, position, ra);
        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlgg, u, position, rg);
        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlaa, u, position, ra);
        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlga, u, position, rg);
        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlgs, u, position, rg);
        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlg, u, position, rg);
        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlself, u, position, rg);

        return u;
    }

    // Vulture
    public Unit getTargetVulture() {
        Unit u = null;
        int ri = 16384; // radius 4
        int ro = 82944; // radius 9

        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlgm, u, position, ro);
        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlgl, u, position, ri);
        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlg, u, position, ri);
        u = MicroUtil.getTargetFrom(EnemyManager.getInstance().tlself, u, position, ri);

        return u;
    }



}
