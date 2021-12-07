package com.tistory.workshop6349.studyBasicBot.buildOrder;

import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.UnitType;
import bwapi.UpgradeType;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public class BuildOrderQueue {

    // 빌드 오더 큐 (deque)를 만들어 값을 넣거나 뺄 수 있음
    public Deque<BuildOrderItem> orderQueue = new ArrayDeque<>();

    private int highestPriority;
    private int lowestPriority;
    private final int defaultPrioritySpacing;

    private int numSkippedItems;

    public BuildOrderQueue() {
        highestPriority = 0;
        lowestPriority = 0;
        numSkippedItems = 0;
        defaultPrioritySpacing = 10;
    }

    public void clearAll() {
        orderQueue.clear();

        highestPriority = 0;
        lowestPriority = 0;
    }

    public BuildOrderItem getHighestPriority() {
        numSkippedItems = 0;

        return orderQueue.getFirst();
    }

    public BuildOrderItem getNextItem() {
        Object[] tempArr = orderQueue.toArray();

        return (BuildOrderItem) tempArr[numSkippedItems];
    }

    public int getItemCount(MetaType queryType, TilePosition queryTilePosition) {
        // queryTilePosition 을 입력한 경우, 거리의 maxRange. 타일단위
        int maxRange = 16;

        int itemCount = 0;

        int reps = orderQueue.size();

        Object[] tempArr = orderQueue.toArray();

        // for each unit in the orderQueue
        for (int i = 0; i<reps; i++) {

            final MetaType item = ((BuildOrderItem)tempArr[orderQueue.size() - 1 - i]).type;
            TilePosition itemPosition = ((BuildOrderItem)tempArr[orderQueue.size() - 1 - i]).desiredPosition;
            if(queryTilePosition == null) {
                queryTilePosition = TilePosition.None;
            }

            if (queryType.isUnit() && item.isUnit()) {
                //if (item.getUnitType().getID() == queryType.getUnitType().getID()) {
                if (item.getUnitType() == queryType.getUnitType()) {
                    if (queryType.getUnitType().isBuilding() && queryTilePosition != TilePosition.None) {
                        if (itemPosition.getDistance(queryTilePosition) <= maxRange) {
                            itemCount++;
                        }
                    }
                    else {
                        itemCount++;
                    }
                }
            }
            else if (queryType.isTech() && item.isTech()) {
                //if (item.getTechType().getID() == queryType.getTechType().getID()) {
                if (item.getTechType() == queryType.getTechType()) {
                    itemCount++;
                }
            }
            else if (queryType.isUpgrade() && item.isUpgrade()) {
                //if (type.getUpgradeType().getID() == queryType.getUpgradeType().getID()) {
                if (item.getUpgradeType() == queryType.getUpgradeType()) {
                    itemCount++;
                }
            }
        }
        return itemCount;
    }

    public int getItemCount(MetaType queryType) {
        return getItemCount(queryType, null);
    }
    
    public int getItemCount(UnitType unitType, TilePosition queryTilePosition) {
        return getItemCount(new MetaType(unitType), queryTilePosition);
    }

    public int getItemCount(UnitType unitType) {
        return getItemCount(new MetaType(unitType), null);
    }

    public int getItemCount(TechType techType) {
        return getItemCount(new MetaType(techType), null);
    }

    public int getItemCount(UpgradeType upgradeType) {
        return getItemCount(new MetaType(upgradeType), null);
    }

    public void skipCurrentItem() {
        // make sure we can skip
        if (canSkipCurrentItem()) {
            // skip it
            numSkippedItems++;
        }
    }

    public boolean canSkipCurrentItem() {
        // does the orderQueue have more elements
        boolean bigEnough = orderQueue.size() > (1 + numSkippedItems);

        if (!bigEnough) {
            return false;
        }

        // is the current highest priority item not blocking a skip
        Object[] tempArr = orderQueue.toArray();

        // this tells us if we can skip
        return !((BuildOrderItem)tempArr[numSkippedItems]).blocking;
    }

    /// orderQueues something with a given priority
    public void orderQueueItem(BuildOrderItem b) {
        // if the orderQueue is empty, set the highest and lowest priorities
        if (orderQueue.isEmpty()) {
            highestPriority = b.priority;
            lowestPriority = b.priority;
        }

        // push the item into the orderQueue
        if (b.priority <= lowestPriority) {
            orderQueue.addLast(b);
        }
        else {
            orderQueue.addFirst(b);
        }

        // if the item is somewhere in the middle, we have to sort again
        if ((orderQueue.size() > 1) && (b.priority < highestPriority) && (b.priority > lowestPriority)) {
            // sort the list in ascending order, putting highest priority at the top
            Object[] tempArr = orderQueue.toArray();
            Arrays.sort(tempArr);
            orderQueue.clear();
            for (Object o : tempArr) {
                orderQueue.add((BuildOrderItem) o);
            }
        }

        // update the highest or lowest if it is beaten
        highestPriority = Math.max(b.priority, highestPriority);
        lowestPriority  = Math.min(b.priority, lowestPriority);
    }

    public void addHighestPriority(MetaType type, int producerID, boolean blocking) {
        orderQueueItem(new BuildOrderItem(type, producerID, getNewPriority(), blocking));
    }

    public void addHighestPriority(MetaType type, TilePosition desiredPosition, boolean blocking) {
        orderQueueItem(new BuildOrderItem(type, -1, getNewPriority(), blocking, desiredPosition));
    }

    public void addHighestPriority(UnitType type, int producerID, boolean blocking) {
        orderQueueItem(new BuildOrderItem(new MetaType(type), producerID, getNewPriority(), blocking));
    }

    public void addHighestPriority(UnitType type, TilePosition desiredPosition, boolean blocking) {
        orderQueueItem(new BuildOrderItem(new MetaType(type), -1, getNewPriority(), blocking, desiredPosition));
    }

    public void addHighestPriority(UpgradeType type, int producerID, boolean blocking) {
        orderQueueItem(new BuildOrderItem(new MetaType(type), producerID, getNewPriority(), blocking));
    }

    public void addHighestPriority(UpgradeType type, TilePosition desiredPosition, boolean blocking) {
        orderQueueItem(new BuildOrderItem(new MetaType(type), -1, getNewPriority(), blocking, desiredPosition));
    }

    public void addHighestPriority(TechType type, int producerID, boolean blocking) {
        orderQueueItem(new BuildOrderItem(new MetaType(type), producerID, getNewPriority(), blocking));
    }

    public void addHighestPriority(TechType type, TilePosition desiredPosition, boolean blocking) {
        orderQueueItem(new BuildOrderItem(new MetaType(type), -1, getNewPriority(), blocking, desiredPosition));
    }

    public void addLowestPriority(MetaType type, int producerID, boolean blocking) {
        int newPriority = lowestPriority - defaultPrioritySpacing;
        if (newPriority < 0) {
            newPriority = 0;
        }

        orderQueueItem(new BuildOrderItem(type, producerID, newPriority, blocking));
    }

    public void addLowestPriority(UnitType type, int producerID, boolean blocking) {
        addLowestPriority(new MetaType(type), producerID, blocking);
    }

    public void addLowestPriority(UpgradeType type, int producerID, boolean blocking) {
        addLowestPriority(new MetaType(type), producerID, blocking);
    }

    public void addLowestPriority(TechType type, int producerID, boolean blocking) {
        addLowestPriority(new MetaType(type), producerID, blocking);
    }





    /// removes the highest priority item
    public void removeHighestPriorityItem() {
        // remove the back element of the vector
        // queue.pop_back();
        orderQueue.removeFirst();

        // if the list is not empty, set the highest accordingly
        // highestPriority = queue.isEmpty() ? 0 : queue.back().priority;
        highestPriority = orderQueue.isEmpty() ? 0 : orderQueue.getLast().priority;
        lowestPriority  = orderQueue.isEmpty() ? 0 : lowestPriority;
    }

    /// skippedItems 다음의 item 을 제거합니다
    public void removeCurrentItem() {
        // remove the back element of the vector

        Object[] tempArr = orderQueue.toArray();
        BuildOrderItem currentItem = (BuildOrderItem)tempArr[numSkippedItems];
        orderQueue.remove(currentItem);

        // if the list is not empty, set the highest accordingly
        highestPriority = orderQueue.isEmpty() ? 0 : orderQueue.getFirst().priority;
        lowestPriority  = orderQueue.isEmpty() ? 0 : lowestPriority;
    }

    /// returns the size of the queue
    public int size() {
        return orderQueue.size();
    }

    public boolean isEmpty() {
        return (orderQueue.size() == 0);
    }

    /// overload the bracket operator for ease of use
    public BuildOrderItem operator(int i) {
        Object[] tempArr = orderQueue.toArray();
        return (BuildOrderItem)tempArr[i];
    }

    public Deque<BuildOrderItem> getQueue() {
        return orderQueue;
    }

    public int getNewPriority() {
        return highestPriority + defaultPrioritySpacing;
    }

}
