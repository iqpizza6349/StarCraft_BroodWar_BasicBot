package com.tistory.workshop6349.examplebotT;

public class Main {

    /*
    // TODO 벌처 공격 -> 우선 순위에 따른 공격 (본진 바로 공격)
    // TODO 마린 & 메딕 -> 마린 14기 이상, 메딕 5기 이상일 시 러쉬
    // TODO 마린: 사거리 업그레이드 (U-238 bullets)
    // TODO 유닛 생산 및 정찰 개발 필요
     */

    public static void main(String[] args) {
        try {
            new ExampleBot().run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
