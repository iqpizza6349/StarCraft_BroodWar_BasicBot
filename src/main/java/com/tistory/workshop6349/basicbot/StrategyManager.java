package com.tistory.workshop6349.basicbot;

public class StrategyManager {

    private static final StrategyManager manager = new StrategyManager();
    public static StrategyManager getInstance() {
        return manager;
    }

}
