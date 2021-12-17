package com.tistory.workshop6349.basicbot;

import bwapi.Color;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;

import java.util.ArrayList;
import java.util.Random;

public class MicroUtil {

    public static boolean reachingArea(Unit unit, int[][] map) {
        Position p0 = unit.getPosition();
        Position p1 = BotUtil.estimate_next_pos(unit, 16);
        if (map[p0.y / 8][p0.x / 8] > 0) {
            return true;
        }
        return map[p1.y / 8][p1.x / 8] > 0;
    }

    public static Position getRetreatVector(Position p0, Position p1, int d) {
        Position vr = BotUtil.linear_interpol_abs(p0, StateManager.getInstance().retreatPos, d);
        Position ve = BotUtil.linear_interpol_abs(p0, p1, -d);
        Position vm = new Position((vr.x + ve.x) / 2, (vr.y + ve.y) / 2);
        Position p2 = BotUtil.linear_interpol_abs(p0, vm, d);

        if (p2.x < 16
                || p2.x > (BasicMap.wp - 16)
                || p2.y < 16
                || p2.y > (BasicMap.hp - 48)) {
            vm = BotUtil.linear_interpol_abs(p2, BasicMap.centerPos, d);
            vm = new Position((p2.x + vm.x) / 2, (p2.y + vm.y) / 2);
            p2 = BotUtil.linear_interpol_abs(p0, vm, d);
        }

        return p2;
    }

    public static Unit getTargetFrom(ArrayList<Unit> v, Unit u, Position p, int r) {
        if (u != null) {
            return u;
        }
        u = BotUtil.getClosest(v, p);
        if (u != null && BotUtil.sqDist(p, u.getPosition()) < r) {
            return u;
        }
        return null;
    }

    public static Unit getMaximumTarget(ArrayList<Unit> v, TilePosition t, int r) {
        Unit maxUnit = null;
        int maxHP = 0;

        for (Unit u : v) {
            if (BotUtil.sqDist(u.getTilePosition(), t) <= r
                    && StateManager.getInstance().targetList.contains(u)
                    && maxHP < u.getHitPoints() + u.getShields()) {
                maxHP = u.getHitPoints() + u.getShields();
                maxUnit = u;
            }
        }

        return maxUnit;
    }

    public static Position getMinePosition() {
        int x = 0;
        int y = 0;
        int n = EnemyManager.getInstance().unclaimed_expo_pos.size();

        if (n > 0 && new Random().nextInt(32767) % 3 == 0) {
            Position position = EnemyManager.getInstance().unclaimed_expo_pos.get(new Random().nextInt(32767) % n);
            x = new Random().nextInt(32767) % 12 - 6 + position.x / 32;
            y = new Random().nextInt(32767) % 11 - 6 + position.y / 32;
        }
        else if (BasicBotAI.BroodWar.getFrameCount() < 14400) {
            x = new Random().nextInt(32767) % 32 - 16 + BasicMap.myEntrance.x;
            y = new Random().nextInt(32767) % 32 - 16 + BasicMap.myEntrance.y;
        }
        else {
            x = new Random().nextInt(32767) % BasicMap.wt;
            y = new Random().nextInt(32767) % (BasicMap.ht - 1);
        }
//
//        if (x != 0
//                && y != 0
//                && !BasicBotAI.BroodWar.isVisible(x, y)
//                && BasicMap.)
        // TODO getGroundDist(x, y)

        return Position.None;
    }

    public static Position getCurrentTargetPosition(Unit u) {
        if (u.getTarget() != null
                && u.getTarget().getPosition().isValid(BasicBotAI.BroodWar)) {
            return u.getTarget().getPosition();
        }
        if (u.getTargetPosition().isValid(BasicBotAI.BroodWar)) {
            return u.getTargetPosition();
        }
        return Position.None;
    }

    public static boolean checkDefenseSiegeTile(TilePosition t) {
        if (!StateManager.getInstance().hasNatural && BasicMap.mainDefMap[t.y][t.x]) {
            return true;
        }
        return (StateManager.getInstance().hasNatural && BasicMap.naturalDefMap[t.y][t.x]);
    }

    public static Position checkDefenseSiegeTile() {
        if (StateManager.getInstance().strategy == 4 && StateManager.getInstance().myTime > 600) {
            return getNaturalDefensePos();
        }

        return  (StateManager.getInstance().hasNatural) ? getNaturalDefensePos() : getRandomPos(BasicMap.myMainDefList);
    }

    public static Position getRandomPos(ArrayList<TilePosition> t) {
        if (!t.isEmpty()) {
            int i = new Random().nextInt(32767) % t.size();
            TilePosition tilePosition = t.get(i);
            if (!BotUtil.isNone(tilePosition)
                    && BasicMap.buildMap_Var[tilePosition.y][tilePosition.x]
                    && BasicBotAI.BroodWar.getUnitsOnTile(tilePosition).isEmpty()) {
                return new Position(32 * tilePosition.x + 16, 32 * tilePosition.y + 16);
            }
        }

        return Position.None;
    }

    public static Position getNaturalDefensePos() {
        if (StateManager.getInstance().highGroundDefenseTank + StateManager.getInstance().plannedHighGroundDefense < 4
                && BasicMap.myHighDefList.size() > 3) {
            StateManager.getInstance().plannedHighGroundDefense++;
            return getRandomPos(BasicMap.myHighDefList);
        }
        return getRandomPos(BasicMap.myNaturalDefList);
    }

    public static boolean checkVentureOut(Position pos) {
        int bunkerSqDist = BotUtil.sqDist(pos, BasicMap.myBunkerDefPos);
        return bunkerSqDist <= 25600
                && BotUtil.sqDist(pos, BasicMap.myNatural) >= bunkerSqDist + BasicMap.bunkerNaturalSqrtDist;
    }

    public static void drawArrow(Position p0, Position p1, Color c) {
        if (BotUtil.sqDist(p0, p1) <= 65536) {
            BasicBotAI.BroodWar.drawLineMap(p0, p1, c);
        }
        else {
            BasicBotAI.BroodWar.drawLineMap(p0, BotUtil.linear_interpol_abs(p0, p1, 128), c);
        }
    }

    public static void drawArrow(Unit u, Position p, Color c) {
        drawArrow(u.getPosition(), p, c);
    }

    public static void drawArrow(Unit u0, Unit u1, Color c) {
        drawArrow(u0.getPosition(), u1.getPosition(), c);
    }

}
