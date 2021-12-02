package com.tistory.workshop6349.basicbot;

public class TrainManager {

    private static final TrainManager manager = new TrainManager();
    public static TrainManager getInstance() {
        return manager;
    }

}
