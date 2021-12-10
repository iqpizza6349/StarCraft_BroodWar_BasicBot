package com.tistory.workshop6349.examplebot;

public class Main {

    /**
     * has problem in attacking
     * @see ExampleBot base bugs
     * @see UnitInfo bugs
     */

    public static void main(String[] args) {
        try {
            new ExampleBot().run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
