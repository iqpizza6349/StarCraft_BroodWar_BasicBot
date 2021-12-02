package com.tistory.workshop6349.basicbot;

public class BuildManager {

    private static final BuildManager manager = new BuildManager();
    public static BuildManager getInstance() {
        return manager;
    }

}
