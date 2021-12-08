package com.tistory.workshop6349.basicbot;

import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.WalkPosition;

import java.util.Random;

public class BotUtil {

    public static int safeSum(int n, int dn) {
        return  (n > -dn) ? n + dn : 0; // call by reference 하고 싶지만 현실적으로 힘들어서 그냥 call by value 를 함
    }

    public static boolean isNone(TilePosition t) {
        return (t.equals(TilePosition.None));
    }

    public static boolean isNone(Position p) {
        return (p.equals(Position.None));
    }

    public static Position linear_interpol_rel(Position p1, Position p2, float f) {
        int x = p1.x + (int)(f*(p2.x - p1.x));
        int y = p1.y + (int)(f*(p2.y - p1.y));
        return new Position(x, y);
    }

    public static Position linear_interpol_abs(Position p1, Position p2, int d) {
        int l = dist(p1, p2);
        if (l != 0) {
            int x = p1.x + (int)(d * (p2.x - p1.x) / l);
            int y = p1.y + (int)(d * (p2.y - p1.y) / l);
            return new Position(x, y);
        }
        return p1;
    }

    public static Position estimate_next_pos(Unit u, int n) {
        int x = u.getPosition().x + (int)(n * u.getVelocityX());
        int y = u.getPosition().y + (int)(n * u.getVelocityY());
        Position pos = new Position(x, y);
        if (pos.isValid(BasicBotAI.BroodWar)) {
            return pos;
        }
        else {
            return u.getPosition();
        }
    }

    public static Position get_random_position() {
        Random random = new Random();
        int x = random.nextInt(32767) % BasicBotAI.BroodWar.mapWidth();
        int y = random.nextInt(32767) % BasicBotAI.BroodWar.mapHeight();
        return new Position(x * 32, y * 32);
    }

    public static int dist(Position p1, Position p2) {
        return (int)(Math.pow(sqDist(p1, p2), 0.5));
    }

    public static int dist(WalkPosition w1, WalkPosition w2) {
        return (int)(Math.pow(sqDist(w1, w2), 0.5));
    }

    public static int sqDist(TilePosition t1, TilePosition t2) {
        return (int)(Math.pow(t1.x - t2.x, 2) + Math.pow(t1.y - t2.y, 2));
    }

    public static int sqDist(WalkPosition w1, WalkPosition w2) {
        return (int)(Math.pow(w1.x - w2.x, 2) + Math.pow(w1.y - w2.y, 2));
    }

    public static int sqDist(Position p1, Position p2) {
        return (int)(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    public static int sqDist(Unit u1, Unit u2) {
        return sqDist(u1.getTilePosition(), u2.getTilePosition());
    }

    public static int sqDist(Unit u, Position p) {
        return sqDist(u.getPosition(), p);
    }

    public static int sqDist(int x1, int y1, int x2, int y2) {
        return (int)(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }







}
