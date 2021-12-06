package com.tistory.workshop6349.basicbot.worker;

import bwapi.Position;

public class WorkerMoveData {

    private final int mineralsNeeded;
    private final int gasNeeded;
    private final Position position;

    public WorkerMoveData(int m, int g, Position p) {
        mineralsNeeded = m;
        gasNeeded = g;
        position = p;
    }

    public int getMineralsNeeded() {
        return mineralsNeeded;
    }

    public int getGasNeeded() {
        return gasNeeded;
    }

    public Position getPosition() {
        return position;
    }
}
