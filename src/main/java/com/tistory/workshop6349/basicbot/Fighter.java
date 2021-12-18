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
    public int spiderMineCount;
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
        this.spiderMineCount = 0;
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

    // ------- M E T H O D S -------

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
        attackQueue = BotUtil.safeSum(attackQueue, -1);

        if (!BotUtil.isNone(BasicMap.myBunkerDefPos)
                && unit.getType() != UnitType.Terran_Vulture
                && MicroUtil.checkVentureOut(position)) {
            // don't venture out (high priority)
            setRetreat();
            return;
        }

        if (attackQueue == 0
                && !isSupport
                && EnemyManager.getInstance().target_count < 16) {
            // medic no targeting
            target = getTargetAll(attackSqrt);
        }

        if (target == null && BasicBotAI.BroodWar.getFrameCount() <= unit.getLastCommandFrame() + commandFrames) {
            return;
        }

        if ((StateManager.getInstance().avoidGridDef
                && MicroUtil.reachingArea(unit, ThreatManager.groundDef))
                || MicroUtil.reachingArea(unit, ThreatManager.groundMap)) {
            setRetreat();
            return;
        }

        switch (unit.getType()) {
            case Terran_Medic:
                target = getTargetHeal();
                break;
            case Terran_Siege_Tank_Siege_Mode:
                target = getTargetSiege();
                break;
            case Terran_Goliath:
                target = getTargetGoliath();
                break;
            case Terran_Vulture:
                target = getTargetVulture();
                break;
            default:
                target = getTargetAll(attackSqrt);
                break;
        }

        if (target != null) {
            if (!isSupport) {
                if (BotUtil.sqDist(position, target.getPosition()) < retreatSqrt && BotUtil.sqDist(position, securePosition) > 9216) {
                    setRetreat();
                }
                else {
                    unit.attack(target);
                    attackQueue = 256;
                }
            }
            else {
                unit.move(target.getPosition());
                MicroUtil.drawArrow(position, target.getPosition(), Color.Yellow);
            }
        }
        else {
            Position dest = (!BotUtil.isNone(specialPosition)) ? specialPosition : attackPosition;
            if (dest.isValid(BasicBotAI.BroodWar) && BotUtil.sqDist(position, dest) > 9216) {
                unit.attack(dest);
            }
            checkCohesionRetreat();
        }
        specialsAllowed = true;
    }

    public void updateVulture() {
        specialsAllowed = false;
        tile = unit.getTilePosition();
        position = unit.getPosition();
        destinPosition = (!BotUtil.isNone(specialPosition)) ? specialPosition : attackPosition;

        if (!unit.exists()
                || unit.getTransport() != null
                || unit.isLockedDown()
                || unit.isStasised()) {
            return;
        }

        if (attackQueue > 0 && !isLooping) {
            // looping start after attack
            --attackQueue;
            if (unit.getGroundWeaponCooldown() > 0) {
                setRetreat();
                isLooping = true;
                target = null;
            }
            if (attackQueue == 0) {
                target = null;
            }
            return;
        }
        
        if (isLooping) {
            // looping phase: check back or forth
            if (unit.getGroundWeaponCooldown() % 5 == 0) {
                target = getTargetVulture();
            }
            if (target != null) {
                int d = BotUtil.dist(position, target.getPosition());
                int s = unit.getGroundWeaponCooldown() * 8;

                if (s < d) {
                    unit.patrol(target.getPosition());
                    isLooping = false;
                }

            }
            if (unit.getGroundWeaponCooldown() < 15 && target == null) {
                unit.move(destinPosition);
                isLooping = false;
            }
            return;
        }

        if ((StateManager.getInstance().avoidGridDef && MicroUtil.reachingArea(unit, ThreatManager.groundDef))
                || MicroUtil.reachingArea(unit, ThreatManager.groundMap)) {
            // avoid ground threats
            setRetreat();
            retreatQueue = 32;
            return;
        }

        target = getTargetVulture();
        if (target != null) {
            // issue attack command
            unit.attack(target);
            attackQueue = 8;
            return;
        }

        if (unit.isIdle()
                && destinPosition.isValid(BasicBotAI.BroodWar)
                && BotUtil.sqDist(position, destinPosition) >= 15384) {
            // get moving
            unit.move(destinPosition);
            return;
        }

        checkCohesionRetreat();
        specialsAllowed = true;
    }

    public void setTarget(Position pos, boolean isAttack) {
        if (!attackPosition.equals(pos)) {
            attackPosition = pos;
            isAttacker = isAttack;
            changedAttackPos = true;
        }
    }

    public void setSwarm(boolean immediately) {
        if (immediately || unit.isIdle()) {
            setTarget(BotUtil.get_random_position(), false);
        }
    }

    public void updateRaider(int state, Position attackPos, Position defPos, Unit transport) {
        securePosition = defPos;
        attackPosition = attackPos;

        switch (state) {
            case 0:
                isRaider = false;
                this.transport = null;
                break;
            case 1:
                this.transport = transport;
                forceUnsiege();
                break;
            case 2:
                this.transport = null;
                break;
            default:
                break;
        }
    }

    public void setRetreat() {
        if (!isRaider) {
            unit.move(securePosition);
        }
    }

    public void checkCohesionRetreat() {
        if (retreatQueue > 0) {
            --retreatQueue;
            if (retreatQueue == 0) {
                unit.move(destinPosition);
            }
            return;
        }
        if (isAttacker
                && !isRaider
                && BotUtil.isNone(specialPosition)
                && StateManager.getInstance().leaderDist > 0
                && MapUtil.getGroundDist(tile) > StateManager.getInstance().leaderDist) {
            unit.move(securePosition);
            retreatQueue = 32;
        }
    }

    // ------- S P E C I A L S -------

    public void checkStim() {
        if (StateManager.getInstance().stimAllowed
                && !unit.isStimmed()
                && unit.isAttacking()
                && unit.getHitPoints() >= 40) {
            unit.useTech(TechType.Stim_Packs);
        }
    }

    public void checkFallB() {
        if (specialsAllowed && unit.getHitPoints() < 20) {
            unit.move(securePosition);
        }
    }

    public void checkFlare() {
        if (BasicBotAI.BroodWar.self().hasResearched(TechType.Optical_Flare)
                && specialsAllowed
                && unit.getEnergy() >= 100) {
            Unit u = MicroUtil.getMaximumTarget(EnemyManager.getInstance().tlflare, tile, 100);
            if (u != null && unit.canUseTech(TechType.Optical_Flare, u)) {
                unit.useTech(TechType.Optical_Flare, u);
                StateManager.getInstance().targetList.add(u);
                BasicBotAI.BroodWar.drawLineMap(position, u.getPosition(), Color.White);
            }
        }
    }

    public void checkLocked() {
        if (BasicBotAI.BroodWar.self().hasResearched(TechType.Lockdown)
                && specialsAllowed && unit.getEnergy() >= 100) {
            Unit u = MicroUtil.getMaximumTarget(EnemyManager.getInstance().tllockd, tile, 100);
            if (u != null && unit.canUseTech(TechType.Lockdown, u)) {
                unit.useTech(TechType.Lockdown, u);
                StateManager.getInstance().targetList.add(u);
                BasicBotAI.BroodWar.drawLineMap(position, u.getPosition(), Color.White);
            }
        }
    }

    public void checkCloak() {
        if (!unit.isCloaked()
                && unit.isUnderAttack()
                && unit.canUseTech(TechType.Personnel_Cloaking)) {
            unit.cloak();
        }
        if (unit.isCloaked() && !unit.isUnderAttack()) {
            unit.decloak();
        }
    }

    public void checkUnstuck() {
        if (unit.getPosition().equals(stuckPosition)
                && !unit.isMaelstrommed()
                && !unit.isStasised()
                && !unit.isLockedDown()
                && unit.getTransport() == null) {
            stuckQueue++;
            if (stuckQueue >= 16) {
                unit.stop();
                stuckQueue = 0;
            }
        }
        else {
            stuckPosition = unit.getPosition();
            stuckQueue = 0;
        }
    }

    // ------- M E C H A N I C A L S -------

    public void checkSiege() {
        if (!BasicBotAI.BroodWar.self().hasResearched(TechType.Tank_Siege_Mode)) {
            return;
        }

        // general sieging behavior
        if (!unit.isSieged() && transport == null) {
            target = getTargetSiege();
            if (target != null && BotUtil.sqDist(position, BotUtil.estimate_next_pos(target, 24)) >= 16384) {
                unit.siege();
                specialQueue = 128;
            }
        }
        else {
            specialQueue = BotUtil.safeSum(specialQueue, -1);
            if (unit.isStartingAttack() || unit.isAttackFrame()) {
                specialQueue = 128;
            }
            else if (specialQueue == 0) {
                unit.unsiege();
            }

        }

        // choke defense case
        if (StateManager.getInstance().isChokeDef) {
            if (specialPosition == Position.None) {
                specialPosition = MicroUtil.checkDefenseSiegeTile();
            }
            else if (position.equals(specialPosition)) {
                if (!unit.isSieged()) {
                    unit.siege();
                }
                if (specialQueue < 32) {
                    specialQueue = 32;
                }
            }
        }

        if (changedAttackPos) {
            changedAttackPos = false;

            if (specialPosition != Position.None
                    && (!StateManager.getInstance().isChokeDef || !MicroUtil.checkDefenseSiegeTile(tile))) {
                specialPosition = Position.None;
            }
        }

    }

    public void checkMine() {
        if (!specialsAllowed || !BasicBotAI.BroodWar.self().hasResearched(TechType.Spider_Mines)) {
            return;
        }

        if (spiderMineCount != unit.getSpiderMineCount()) {
            spiderMineCount = unit.getSpiderMineCount();
            specialPosition = Position.None;
        }

        if (spiderMineCount > 0 && !specialPosition.isValid(BasicBotAI.BroodWar)) {
            specialPosition = MicroUtil.getMinePosition();
            if (specialPosition.isValid(BasicBotAI.BroodWar)) {
                int dt = 64 + (BotUtil.dist(unit.getPosition(), specialPosition) / 3);
                specialQueue = BasicBotAI.BroodWar.getFrameCount() + dt;
            }
        }

        if (specialPosition.isValid(BasicBotAI.BroodWar)) {
            if (BotUtil.sqDist(position, specialPosition) <= 1024 && attackQueue == 0) {
                unit.useTech(TechType.Spider_Mines, specialPosition);
                attackQueue = 16;
            }
            if (BasicBotAI.BroodWar.getFrameCount() > specialQueue) {
                specialPosition = Position.None;
            }
        }
    }

    public void setSpecialPosition(Position pos) {
        specialPosition = pos;
        destinPosition = (!BotUtil.isNone(pos)) ? pos : attackPosition;
    }

    public void forceUnsiege() {
        specialQueue = 0;
        if (unit.isSieged()) {
            unit.unsiege();
            BasicBotAI.BroodWar.printf(position + " LEAVE");
        }
    }

    public boolean checkSneakiness() {
        return (StateManager.getInstance().strategy == 4);
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
