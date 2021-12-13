package com.tistory.workshop6349.examplebotT;

import bwapi.Unit;

import java.util.ArrayList;

public class Vulture {
    
    public Unit vulture;

    public Vulture(Unit vulture) {
        this.vulture = vulture;
    }

    public void update() {
        ArrayList<Unit> workers = ExampleUtil.getTypeUnitsInRadius(
                ExampleBot.BroodWar.enemy().getRace().getWorker(),
                ExampleBot.BroodWar.enemy(),
                vulture.getPosition(),
                15 * 32,
                true
        );

        for (Unit w : workers) {
            if (!vulture.isInWeaponRange(w)) {
                continue;
            }

            ExampleUtil.kiting(vulture, w, vulture.getDistance(w), 2 * 32);
        }

        Unit closestWorker = ExampleUtil.getClosestTypeUnit(
                ExampleBot.BroodWar.enemy(),
                vulture,
                ExampleBot.BroodWar.enemy().getRace().getWorker(),
                0,
                true,
                true,
                false
        );

        if (closestWorker != null) {
            ExampleUtil.kiting(vulture, closestWorker, vulture.getDistance(closestWorker), 2 * 32);
        }
        else {
            // 본진 공격
        }
    }

    public void actionExecute() {

        if (ExampleBot.BroodWar.getFrameCount() % 6 != 0) {
            return;
        }

        update();
    }
    
    
}
