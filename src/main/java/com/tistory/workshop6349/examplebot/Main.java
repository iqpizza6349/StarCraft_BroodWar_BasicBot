package com.tistory.workshop6349.examplebot;

public class Main {

    public static void main(String[] args) {
        try {
            new ExampleBot().run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
