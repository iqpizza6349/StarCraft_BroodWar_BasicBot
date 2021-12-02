package com.tistory.workshop6349.basicbot;

public class ConstructionManager {

    private static final ConstructionManager manager = new ConstructionManager();
    public static ConstructionManager getInstance() {
        return manager;
    }


}
