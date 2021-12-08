package com.tistory.workshop6349.basicbot;

public class Main {

    public static void main(String[] args) {
        try {
            new BasicBotAI().run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
