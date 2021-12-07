package com.tistory.workshop6349.studyBasicBot.buildOrder;

import bwapi.Race;
import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;

public class MetaType {

    private final int type;
    private final Race race;

    private UnitType unitType;
    private TechType techType;
    private UpgradeType upgradeType;

    private enum MetaTypes {Unit, Tech, Upgrade, Default}

    public MetaType() {
        type = MetaTypes.Default.ordinal();
        race = Race.None;
    }

    public MetaType (UnitType t) {
        unitType = t;
        type = MetaTypes.Unit.ordinal();
        race = t.getRace();
    }

    public MetaType (TechType t) {
        techType = t;
        type = MetaTypes.Tech.ordinal();
        race = t.getRace();
    }

    public MetaType (UpgradeType t) {
        upgradeType = t;
        type = MetaTypes.Upgrade.ordinal();
        race = t.getRace();
    }

    public final int type() {
        return type;
    }

    public final boolean isUnit() {
        return type == MetaTypes.Unit.ordinal();
    }

    public final boolean isTech() {
        return type == MetaTypes.Tech.ordinal();
    }

    public final boolean isUpgrade() {
        return type == MetaTypes.Upgrade.ordinal();
    }

    public final Race getRace() {
        return race;
    }

    public final boolean isBuilding() {
        return type == MetaTypes.Unit.ordinal() && unitType.isBuilding();
    }

    public final boolean isRefinery() {
        return isBuilding() && unitType.isRefinery();
    }

    public final UnitType getUnitType() {
        return unitType;
    }

    public final TechType getTechType() {
        return techType;
    }

    public final UpgradeType getUpgradeType() {
        return upgradeType;
    }

    public int supplyRequired() {
        if (isUnit()) {
            return unitType.supplyRequired();
        }
        else {
            return 0;
        }
    }

    public final int mineralPrice() {
        return isUnit() ? unitType.mineralPrice() : (isTech() ? techType.mineralPrice() : upgradeType.mineralPrice());
    }

    public final int gasPrice() {
        return isUnit() ? unitType.gasPrice() : (isTech() ? techType.gasPrice() : upgradeType.gasPrice());
    }

    public final UnitType whatBuilds() {
        return isUnit() ? unitType.whatBuilds().getFirst() : (isTech() ? techType.whatResearches() : upgradeType.whatUpgrades());
    }

    public final String getName() {
        if (isUnit()) {
            return unitType.toString();
        }
        else if (isTech()) {
            return techType.toString();
        }
        else if (isUpgrade()) {
            return upgradeType.toString();
        }
        else {
            return "LOL";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetaType)) return false;

        MetaType that = (MetaType) o;

        return this.type == that.type;
    }
}
