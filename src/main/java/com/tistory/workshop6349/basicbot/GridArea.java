package com.tistory.workshop6349.basicbot;

import bwapi.TilePosition;
import bwem.Area;

import java.util.Vector;

public class GridArea {

    enum AreaStatus {
        UnknownArea,
        NeutralArea,
        SelfArea,
        EnemyArea,
        CombatArea
    }   // 모름(0), 중립(1), 내 진영(2), 적 진영(3), 전투 진영(4)

    static class GridAreaCell {

        private TilePosition topLeft = new TilePosition(1000, 1000);
        private TilePosition bottomRight = new TilePosition(-1000, -1000);
        private int reservedMineCount = 0;
        private int mineCount = 0;
        private int myUnitCount = 0;
        private int myBuildingCount = 0;
        private int enemyUnitCount = 0;
        private int enemyBuildingCount = 0;

        AreaStatus status = AreaStatus.UnknownArea;

        public GridAreaCell() {

        }

        boolean isValid() {
            return width() > 0 && height() > 0;
        }

        int width() {
            return bottomRight.x - topLeft.x;
        }

        int height() {
            return bottomRight.y - topLeft.y;
        }

        int size() {
            return Math.min(width(), height());
        }

        TilePosition center() {
            int topLeftX = topLeft.x;
            int topLeftY = topLeft.y;
            int bottomRightX = bottomRight.x;
            int bottomRightY = bottomRight.y;

            TilePosition newTile = new TilePosition(topLeftX + bottomRightX, topLeftY + bottomRightY);
            return new TilePosition(newTile.x / 2, newTile.y / 2);
        }

        TilePosition getTopLeft() {
            return topLeft;
        }

        void setTopLeft(TilePosition topLeft) {
            this.topLeft = topLeft;
        }

        TilePosition getBottomRight() {
            return bottomRight;
        }

        void setBottomRight(TilePosition bottomRight) {
            this.bottomRight = bottomRight;
        }

        int getMineCount() {
            return mineCount;
        }

        void setMineCount(int count) {
            this.mineCount = count;
        }

        AreaStatus areaStatus() {
            return status;
        }

        void setAreaStatus(AreaStatus as) {
            this.status = as;
        }

        int getReservedMineCount() {
            return reservedMineCount;
        }

        void setReservedMineCount(int count) {
            this.reservedMineCount = count;
        }

        void addReservedMineCount() {
            this.reservedMineCount++;
        }

        public int getMyUnitCount() {
            return myUnitCount;
        }

        public void setMyUnitCount(int myUnitCount) {
            this.myUnitCount = myUnitCount;
        }

        public int getMyBuildingCount() {
            return myBuildingCount;
        }

        public void setMyBuildingCount(int myBuildingCount) {
            this.myBuildingCount = myBuildingCount;
        }

        public int getEnemyUnitCount() {
            return enemyUnitCount;
        }

        public void setEnemyUnitCount(int enemyUnitCount) {
            this.enemyUnitCount = enemyUnitCount;
        }

        public int getEnemyBuildingCount() {
            return enemyBuildingCount;
        }

        public void setEnemyBuildingCount(int enemyBuildingCount) {
            this.enemyBuildingCount = enemyBuildingCount;
        }
    }

    Vector<Integer> X, Y;
    Vector<Vector<GridAreaCell>> sArea;
    private boolean isMyBaseRight = false;

    public GridArea(Area wholeArea, int N) {
        TilePosition topLeft = wholeArea.getTopLeft();
        TilePosition bottomRight = wholeArea.getBottomRight();

        int width = bottomRight.x - topLeft.x;
        int height = bottomRight.y - topLeft.y;

        if (N <= 0) {
            N = 1;
        }

        if (N > Math.min(width, height) / 3) {
            N = Math.min(width, height) / 3;
        }

        sArea.setSize(N);

        for (int i = 0; i < N; i++) {
            sArea.get(i).setSize(N);
        }

        int wBase = (bottomRight.x - topLeft.x) / N;
        int wRemain = bottomRight.x - topLeft.x - N * wBase;

        TilePosition myBase = Common.Self().getStartLocation();

        if (myBase.x <= (topLeft.x + bottomRight.x) / 2) {
            isMyBaseRight = false;
            X.add(topLeft.x);

            for (int i = 1; i <= N; i++) {
                X.add(X.get(i-1) + wBase - (wRemain >= 0 ? 1 : 0));
                wRemain--;
            }

        }
        else {
            isMyBaseRight = true;

            X.add(bottomRight.x);
            for (int i = 1; i <= N; i++) {
                X.add(X.get(i-1) - wBase - (wRemain >= 0 ? 1 : 0));
                wRemain--;
            }
        }

        int hBase = (bottomRight.y - topLeft.y) / N;
        int hRemain = bottomRight.y - topLeft.y - N * hBase;

        Y.add(topLeft.y);

        for (int i = 0; i <= N; i++) {
            Y.add(Y.get(i-1) + hBase + (hRemain >= 0 ? 1 : 0));
            hRemain--;
        }

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                TilePosition tl = new TilePosition(X.get(i), Y.get(j));
                TilePosition br = new TilePosition(X.get(i+1), Y.get(j+1));

                sArea.get(i).get(j).setTopLeft(tl);
                sArea.get(i).get(j).setBottomRight(br);
            }
        }
    }

    public int getDist(TilePosition base, GridAreaCell gac) {
        TilePosition mid = gac.center();
        return (int)Math.sqrt((mid.x - base.x) * (mid.x - base.x) + (mid.y - base.y) * (mid.y - base.y));
    }

//    public Vector<GridAreaCell> getEnemyBoundary(int margin) {
//
//    }












}
