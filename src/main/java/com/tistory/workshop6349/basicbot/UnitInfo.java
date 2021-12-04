package com.tistory.workshop6349.basicbot;

import bwapi.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class UnitInfo {

    private Unit unit;
    private UnitType type;
    private Player player;

    private int unitID;
    private int remainingBuildTime;
    private int lastHealth;
    private boolean beingRepaired;
    private int lastShields;
    private double lastEnergy;
    private int lastCommandFrame;
    private int expectedDamage;     // 공격받을 피해량 예상
    private int lastPositionTime;   // lastPosition 에 처음 도달한 시간

    private Position lastSeenPosition;
    private Position lastPosition;
    private Position vPosition;
    private boolean completed;
    private boolean morphing;
    private boolean burrowed;
    private boolean canBurrow;
    private boolean hide;

    private int killMe;
    private int spaceRemaining;
    private boolean powered;
    private int completedTime;
    private boolean dangerMine;
    private Queue<Boolean> blockedQueue;
    private int blockedCount;   // 120 frame 동안 길막당한 횟수

    private int nearUnitFrameCount; // burrow 된 유닛의 위치를 Unknown 처리하기 위함
    private int lastNearUnitFrame;

    private boolean lifted;
    private int marineInBunker;
    private boolean gatheringMinerals;

    private ArrayList<Unit> enemiesTargetMe;
    private Position avgEnemyPosition;
    private Unit veryFrontEnemyUnit;

    private int lastSiegeOrUnSiegeTime;

    public UnitInfo(Unit unit) {
        this.unit = unit;

    }

    public UnitInfo(Unit unit, Position pos) {

    }

    void update() {
        if (unit.exists() && !unit.isLoaded()) {
            boolean isShowThisFrame = hide;
            hide = false;

            if (lastPosition.equals(unit.getPosition()) && !vPosition.equals(lastPosition) && unit.getGroundWeaponCooldown() == 0) {
                blockedQueue.add(true);
                blockedCount++;

                if (blockedQueue.size() > 120) {
                    if (blockedQueue.peek()) {
                        blockedCount--;
                    }
                    blockedQueue.poll();
                }
            }
            else {
                blockedQueue.add(false);

                if (blockedQueue.size() > 120) {
                    if (blockedQueue.peek()) {
                        blockedCount--;
                    }
                    blockedQueue.poll();
                }
            }

            if (!lastPosition.equals(unit.getPosition())) {
                lastPositionTime = BasicBotModule.BroodWar.getFrameCount();
                lastPosition = unit.getPosition();
                lastSeenPosition = lastPosition;
            }
            else if (unit.getAirWeaponCooldown() == 0
                    || unit.getGroundWeaponCooldown() == 0
                    || unit.getSpellCooldown() == 0
                    || unit.isConstructing()
                    || unit.isHoldingPosition()
                    || unit.isGatheringMinerals()) {
                lastPositionTime = BasicBotModule.BroodWar.getFrameCount();
            }

            int lastPositionX = lastPosition.x;
            int lastPositionY = lastPosition.y;
            Position newPos = new Position((int)(unit.getVelocityX() * 8), (int)(unit.getVelocityY() * 8));
            vPosition = new Position(lastPositionX + newPos.x, lastPositionY + newPos.y);
            completed = unit.isCompleted();
            morphing = unit.isMorphing();

            if (unit.getHitPoints() > lastHealth) {
                beingRepaired = true;
            }
            else {
                beingRepaired = false;
            }

            lastHealth = unit.getHitPoints();
            lastShields = unit.getShields();
            type = unit.getType();
            expectedDamage = 0;
            powered = unit.isPowered();

            remainingBuildTime = unit.getRemainingBuildTime(); // TODO getRemainingBuildFrame() 의 원천을 찾으면 바꿔야함

            if (!completed) {
                completedTime = BasicBotModule.BroodWar.getFrameCount() + remainingBuildTime;
            }

            if (unit.getPlayer() == Common.Self()) {
                lastEnergy = unit.getEnergy();
                spaceRemaining = unit.getSpaceRemaining();
            }
            else {
                lastEnergy = Math.min((double)Common.Enemy().maxEnergy(type), lastEnergy + 0.03125);

                if (isShowThisFrame) {
                    if (!type.isFlyer() && !type.isBuilding()) {
                        boolean isUnloaded = true;

                        List<TilePosition> tempTilePositions = new ArrayList<>();
                        tempTilePositions.add(new TilePosition(1, 0));
                        tempTilePositions.add(new TilePosition(-1, 0));
                        tempTilePositions.add(new TilePosition(0, 1));
                        tempTilePositions.add(new TilePosition(0, -1));

                        for (TilePosition direction : tempTilePositions) {
                            TilePosition t = new TilePosition(
                                    unit.getTilePosition().x + direction.x,
                                    unit.getTilePosition().y + direction.y
                            );

                            if (t.isValid(BasicBotModule.BroodWar) && !BasicBotModule.BroodWar.isVisible(t)) {
                                isUnloaded = false;
                                break;
                            }
                        }

                        if (isUnloaded) {
                            UnitInfo ui = null;

                            if (InformationManager.getInstance().enemyRace == Race.Zerg) {
                                // TODO InformationManager.getInstance().getClosestTypeUnit() 처리해야함
                            }
                            else if (InformationManager.getInstance().enemyRace == Race.Protoss) {
                                // TODO InformationManager.getInstance().getClosestTypeUnit() 처리해야함
                            }
                            else {
                                // TODO InformationManager.getInstance().getClosestTypeUnit() 처리해야함

                                if (ui == null) {
                                    // TODO InformationManager.getInstance().getClosestTypeUnit() 처리해야함 (벙커로 처리)
                                }
                            }

                            if (ui != null) {
                                ui.spaceRemaining = Math.min(ui.spaceRemaining + type.spaceRequired(), ui.getType().spaceProvided());
                            }
                        }
                    }
                    else if (type == UnitType.Zerg_Overlord
                            || type == UnitType.Protoss_Shuttle
                            || type == UnitType.Terran_Dropship
                            || type == UnitType.Terran_Bunker) {
                        spaceRemaining = 0;
                    }
                }
            }

            if (type == UnitType.Terran_Vulture_Spider_Mine) {
                if (unit.getPlayer() == Common.Self()) {
                    dangerMine = false;

                    // TODO Information getUnitsInRadius() 처리해야함
                }
                else {
                    burrowed = true;
                }
            }

            if (type.isBurrowable()) {
                if (canBurrow && !unit.canBurrow(false)) {
                    burrowed = true;
                }
                else if (!canBurrow && unit.canBurrow(false)) {
                    burrowed = false;
                }
                canBurrow = unit.canBurrow(false);
            }

            if (lifted != unit.isFlying()) {
                if (lifted) {
                    GameCommander.getInstance().onUnitLanded(unit);
                }
                else {
                    GameCommander.getInstance().onUnitLifted(unit);
                }
                lifted = unit.isFlying();
            }

            if (BasicBotModule.BroodWar.getFrameCount() % 12 == 0
                    && unit.getPlayer() != Common.Self()
                    && type == UnitType.Terran_Bunker) {
                int gap;

                if (Common.Enemy().getUpgradeLevel(UpgradeType.U_238_Shells) == 1) {
                    gap = 8;
                }
                else {
                    gap = 7;
                }

                // TODO Information.getInstance().getClosestUnit 처리해야함
            }

            if (unit.getPlayer() != Common.Self() && type.canAttack()) {
                if (unit.getOrderTarget() != null
                        && unit.getOrderTarget().exists()
                        && unit.getOrderTarget().getPlayer() == Common.Self()) {
                    // TODO Information.getInstance().getUnitInfo() 처리해야함
                }
                else if (unit.getTarget() != null
                        && unit.getTarget().exists()
                        && unit.getTarget().getPlayer() == Common.Self()) {
                    // TODO Information.getInstance().getUnitInfo() 처리해야함
                }
                else if (!unit.isDetected()) {
                    // TODO Information.getInstance().getUnitInfo() 처리해야함
                }
            }

            // TODO 위치 이동. 불필요하게 여러번 세팅될 수 있음
            if (!enemiesTargetMe.isEmpty()) {
                // TODO UnitUtil 찾아서 호출 처리해야함
            }
        }
        else {
            // 이전 프레임에 보였다가 갑자기 안보이는 경우
            if (!hide) {
                if (!type.isFlyer() && !type.isBuilding()) {
                    UnitInfo ui = null;

                    if (InformationManager.getInstance().enemyRace == Race.Zerg) {
                        // TODO InformationManager.getInstance().getClosestTypeUnit() 처리해야함
                    }
                    else if (InformationManager.getInstance().enemyRace == Race.Protoss) {
                        // TODO InformationManager.getInstance().getClosestTypeUnit() 처리해야함
                    }
                    else {
                        // TODO InformationManager.getInstance().getClosestTypeUnit() 처리해야함

                        if (ui == null) {
                            // TODO InformationManager.getInstance().getClosestTypeUnit() 처리해야함 (벙커로 처리)
                        }
                    }

                    if (ui != null) {
                        ui.spaceRemaining = Math.min(ui.spaceRemaining + type.spaceRequired(), ui.getType().spaceProvided());
                    }
                }
            }

            hide = true;

            if (type.isBuilding() && !completed) {
                if (--remainingBuildTime <= 0) {
                    completed = true;
                }
            }

            lastEnergy = Math.min((double)Common.Enemy().maxEnergy(type), lastEnergy + 0.03125);

            if (!lastPosition.equals(Position.Unknown)) {
                if (burrowed) {
                    // TODO 터렛, 베슬, 스캔 작동 코드
                }
                else {
                    // Visible 아닌 적이 있는 경우에만 Unknown 처리 한다.
                    if (BasicBotModule.BroodWar.isVisible(lastPosition.toTilePosition())
                            && BasicBotModule.BroodWar.isVisible(vPosition.toTilePosition())) {
                        lastPosition = Position.Unknown;
                    }
                }
            }
        }

        gatheringMinerals = false;
    }

    public void initFrame() {
        if (!enemiesTargetMe.isEmpty()) {
            enemiesTargetMe.clear();
            avgEnemyPosition = Position.None;
            veryFrontEnemyUnit = null;
        }
    }

    // TODO State (유닛 매니저 및 상태 관련 클래스 개발 후 진행)

    public Unit getUnit() {
        return unit;
    }

    public UnitType getType() {
        return type;
    }

    public Position getPos() {
        return lastPosition;
    }

    public Position getvPos() {
        return vPosition;
    }

    public Position getvPos(int frame) {
        int lastX = lastPosition.x;
        int lastY = lastPosition.y;

        Position newPos = new Position((int)(unit.getVelocityX() * frame), (int)(unit.getVelocityY() * frame));
        return new Position(lastX + newPos.x, lastY + newPos.y);
    }

    public Player getPlayer() {
        return player;
    }

    public int getID() {
        return unitID;
    }

    public int getHp() {
        return lastHealth + lastShields;
    }

    public int getShield() {
        return lastShields;
    }

    public boolean isCompleted() {
        return completed;
    }

    public int getRemainingBuildTime() {
        return remainingBuildTime;
    }

    public boolean isMorphing() {
        return morphing;
    }

    public boolean isBurrowed() {
        return burrowed;
    }

    public void setFrame(int frame) {
        this.lastCommandFrame = BasicBotModule.BroodWar.getFrameCount() + frame;
    }

    public int getFrame() {
        return lastCommandFrame;
    }

    public int getExpectedDamage() {
        return expectedDamage;
    }

    public boolean isBeingRepaired() {
        return beingRepaired;
    }

    public int getKillMe() {
        return killMe;
    }

    public boolean isPowered() {
        return powered;
    }

    public int getMarineInBunker() {
        return marineInBunker;
    }

    public void reduceEnergy(int energy) {
        this.lastEnergy -= Math.min(this.lastEnergy, (double) energy);
    }

    public int getEnergy() {
        return (int) lastEnergy;
    }

    // Attacker 가 this unit 을 공격할 때 마다 damage 를 계산 및 누적
    public void setDamage(Unit attacker) {
        // TODO BasicUtil 개발 후 호출
    }

    // Building 전용
    public void setLift(boolean lift) {
        this.lifted = lift;
    }

    public boolean getLift() {
        return lifted;
    }

    public boolean isDangerMine() {
        return dangerMine;
    }

    // enemy 전용
    public boolean isHide() {
        return hide;
    }

    public ArrayList<Unit> getEnemiesTargetMe() {
        return enemiesTargetMe;
    }

    public Position getAvgEnemyPosition() {
        return avgEnemyPosition;
    }

    public Unit getVeryFrontEnemyUnit() {
        return veryFrontEnemyUnit;
    }

    public void clearAvgEnemyPos() {
        avgEnemyPosition = Position.None;
    }

    public void	clearVeryFrontEnemyUnit() {
        veryFrontEnemyUnit = null;
    }

    public boolean isGatheringMinerals() {
        return gatheringMinerals;
    }

    public void setGatheringMinerals() {
        gatheringMinerals = true;
    }

    public void setMarineInBunker() {
        marineInBunker++;
    }

    public void	setKillMe(int i) {
        killMe = i;
    }

    public void setKillMe() {
        setKillMe(1);
    }

    public int getLastPositionTime() {
        return lastPositionTime;
    }

    public Position getLastSeenPosition() {
        return lastSeenPosition;
    }

    public int getSpaceRemaining() {
        return spaceRemaining;
    }

    public void addSpaceRemaining(int space) {
        spaceRemaining += space;
    }

    public void	delSpaceRemaining(int space) {
        spaceRemaining -= space;
    }

    public void initSpaceRemaining() {
        spaceRemaining = unit.getSpaceRemaining();
    }

    public int getLastSiegeOrUnSiegeTime() {
        return lastSiegeOrUnSiegeTime;
    }

    public void setLastSiegeOrUnsiegeTime(int lastSiegeOrUnsiegeTime) {
        lastSiegeOrUnSiegeTime = lastSiegeOrUnsiegeTime;
    }

    public int getCompletedTime() {
        return completedTime;
    }

    // TODO State
//    public Position getIdlePos() {
//
//    }

    public boolean isBlocked() {
        return blockedCount > 25;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UnitInfo)) return false;

        UnitInfo that = (UnitInfo) o;

        return this.getID() == that.getID();
    }
}