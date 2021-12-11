package com.tistory.workshop6349.examplebot;

public class Main {

    // 현재 인텔리제이 깃허브가 오류가 나서 고치는 중입니다.

    public static void main(String[] args) {
        try {
            new ExampleBot().run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
