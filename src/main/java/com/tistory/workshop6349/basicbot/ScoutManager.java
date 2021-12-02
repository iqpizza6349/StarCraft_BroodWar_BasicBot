package com.tistory.workshop6349.basicbot;

public class ScoutManager {

    private static final ScoutManager manager = new ScoutManager();
    public static ScoutManager getInstance() {
        return manager;
    }


}
