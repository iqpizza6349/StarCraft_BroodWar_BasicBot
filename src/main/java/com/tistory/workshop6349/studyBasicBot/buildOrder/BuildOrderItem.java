package com.tistory.workshop6349.studyBasicBot.buildOrder;

import bwapi.TilePosition;

public class BuildOrderItem {

    // Order 작업하는 영역

    // 우선 순위 등 여러 조건을 거쳐 작업함
    public final MetaType type;
    public final int priority;
    public final int producerID;
    public final boolean blocking;
    public final TilePosition desiredPosition;

    public BuildOrderItem(MetaType type, int producerID, int priority, boolean blocking, TilePosition desiredPosition) {
        this.type = type;
        this.producerID = producerID;
        this.priority = priority;
        this.blocking = blocking;
        this.desiredPosition = desiredPosition;
    }

    public BuildOrderItem(MetaType type, int producerID, int priority, boolean blocking) {
        this(type, producerID, priority, blocking, TilePosition.None);
    }

    public BuildOrderItem(MetaType type, int producerID, int priority) {
        this(type, producerID, priority, false, TilePosition.None);
    }

    public BuildOrderItem(MetaType type, int producerID) {
        this(type, producerID, 0, false, TilePosition.None);
    }

    public BuildOrderItem(MetaType type, int producerID, boolean blocking) {
        this(type, producerID, 0, blocking, TilePosition.None);
    }

    /// equals override
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BuildOrderItem)) return false;

        BuildOrderItem that = (BuildOrderItem) obj;
        if (this.type != null && that.type != null) {
            if (this.type.equals(that)) {
                if (this.priority == that.priority
                        && this.blocking == that.blocking
                        && this.producerID == that.producerID) {
                    if (this.desiredPosition != null) {
                        return this.desiredPosition.equals(that.desiredPosition);
                    }
                    else {
                        return that.desiredPosition == null;
                    }
                }
            }
        }

        return false;
    }






}
