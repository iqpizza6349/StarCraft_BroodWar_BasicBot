package com.tistory.workshop6349.tutorial;

import bwapi.*;

import java.util.List;

public class TutorialBot {

    public static MapTools m_mapTools = new MapTools();

    public TutorialBot() {

    }

    public void onStart() {
        Main.game.setLocalSpeed(10);
        Main.game.setFrameSkip(0);

        Main.game.enableFlag(Flag.UserInput);

        m_mapTools.onStart();
    }

    public void onEnd(boolean isWinner) {
        System.out.println("We " + (isWinner ? "Won!" : "lost!"));
    }

    public void onFrame() {
        m_mapTools.onFrame();

        sendIdleWorkersToMinerals();

        trainAdditionalWorkers();

        buildAdditionalSupply();

        Tools.drawUnitHealthBars();

        drawDebugInformation();
    }

    public void sendIdleWorkersToMinerals() {
        final List<Unit> myUnits = Main.game.self().getUnits();

        for (Unit unit : myUnits) {
            if (unit.getType().isWorker() && unit.isIdle()) {
                Unit closestMineral = Tools.getClosestUnitTo(unit, Main.game.getMinerals());

                if (closestMineral != null) {
                    unit.rightClick(closestMineral);
                }
            }
        }
    }

    public void trainAdditionalWorkers() {
        final UnitType workerType = Main.game.self().getRace().getWorker();
        final int workersWanted = 20;
        final int workersOwned = Tools.countUnitsOfType(workerType, Main.game.self().getUnits());

        if (workersOwned < workersWanted) {
            final Unit myDepot = Tools.getDepot();

            if (myDepot != null && !myDepot.isTraining()) {
                myDepot.train(workerType);
            }
        }
    }

    public void buildAdditionalSupply() {
        final int unUsedSupply = Tools.getTotalSupply(true) - Main.game.self().supplyUsed();

        if (unUsedSupply >= 2) {
            return;
        }

        final UnitType supplyProviderType = Main.game.self().getRace().getSupplyProvider();
        final boolean startedBuilding = Tools.buildBuilding(supplyProviderType);
        if (startedBuilding) {
            Main.game.printf("Started Building " + supplyProviderType.toString());
        }
    }

    public void drawDebugInformation() {
        Main.game.drawTextScreen(new Position(10, 10), "Hello, World");
        Tools.drawUnitCommands();
        Tools.drawUnitBoundingBoxes();
    }

    public void onUnitDestroy(Unit unit) {}

    public void onUnitMorph(Unit unit) {}

    public void onSendText(String text) {
        if (text.equals("/map")) {
            m_mapTools.toggleDraw();
        }
    }

    public void onUnitCreate(Unit unit) {}

    public void onUnitComplete(Unit unit) {}

    public void onUnitShow(Unit unit) {}

    public void onUnitHide(Unit unit) {}

    public void onUnitRenegade(Unit unit) {}
}
