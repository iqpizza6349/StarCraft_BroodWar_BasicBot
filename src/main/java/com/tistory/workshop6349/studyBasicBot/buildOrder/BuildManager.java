package com.tistory.workshop6349.studyBasicBot.buildOrder;

public class BuildManager {

    public BuildOrderQueue buildOrderQueue = new BuildOrderQueue();
    private static final BuildManager manager = new BuildManager();
    public static BuildManager getInstance() {
        return manager;
    }

    public void sayHighestInfo() {
        BuildOrderItem currentItem = buildOrderQueue.getHighestPriority();
        System.out.println("가장 우선순위가 높은 것");
        System.out.println("종류: " + currentItem.type.getName());
        System.out.println("유니크 아이디: " + currentItem.producerID);
        System.out.println("우선순위: " + currentItem.priority);
        System.out.println("블락킹: " + currentItem.blocking);
        System.out.println("원하는 위치: " + currentItem.desiredPosition);
    }





}
