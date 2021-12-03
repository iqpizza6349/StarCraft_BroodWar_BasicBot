package com.tistory.workshop6349.basicbot;

import bwapi.Unit;
import bwapi.UnitType;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class UnitData {

    /// Unit 과 UnitInfo 를 Map 형태로 저장하는 자료구조
    Map<Integer, UnitInfo> unitAndUnitInfoMap = new HashMap<>();

    /// UnitType 별 파괴/사망한 유닛 숫자 누적값
    Map<String, Integer> numDeadUnits = new HashMap<>();

    /// UnitType 별 건설/훈련했던 유닛 숫자 누적값
    Map<String, Integer> numCreatedUnits = new HashMap<>();

    /// UnitType 별 존재하는 유닛 숫자 카운트. 적군 유닛의 경우 식별된 유닛 숫자 카운트
    Map<String, Integer> numUnits = new HashMap<>();

    /// 사망한 유닛을 생산하는데 소요되었던 Mineral 의 누적값 (얼마나 손해를 보았는가 계산하기 위함임)
    private int mineralsLost = 0;
    /// 사망한 유닛을 생산하는데 소요되었던 Gas 의 누적값 (얼마나 손해를 보았는가 계산하기 위함임)
    private int gasLost = 0;

    /// unitAndUnitInfoMap 에서 제거해야할 데이터들
    Vector<Integer> badUnitsToRemove = new Vector<>();

    public UnitData() {
    }

    /// 유닛의 상태정보를 업데이트합니다
    public void updateUnitInfo(Unit unit) {
        if (unit == null) {
            return;
        }

        boolean firstSeen = false;
        if (!unitAndUnitInfoMap.containsKey(unit.getID())) {
            firstSeen = true;
            unitAndUnitInfoMap.put(unit.getID(), new UnitInfo());
        }

        UnitInfo ui = unitAndUnitInfoMap.get(unit.getID());
        ui.setUnit(unit);
        ui.setPlayer(unit.getPlayer());
        ui.setLastPosition(unit.getPosition());
        ui.setLastHealth(unit.getHitPoints());
        ui.setLastShields(unit.getShields());
        ui.setUnitID(unit.getID());
        ui.setType(unit.getType());
        ui.setCompleted(unit.isCompleted());

        if (firstSeen) {
            if (!numCreatedUnits.containsKey(unit.getType().toString())) {
                numCreatedUnits.put(unit.getType().toString(), 1);
            } else {
                numCreatedUnits.put(unit.getType().toString(), numCreatedUnits.get(unit.getType().toString()) + 1);
            }
            if (!numUnits.containsKey(unit.getType().toString())) {
                numUnits.put(unit.getType().toString(), 1);
            } else {
                numUnits.put(unit.getType().toString(), numUnits.get(unit.getType().toString()) + 1);
            }
        }
    }

    /// 파괴/사망한 유닛을 자료구조에서 제거합니다
    public void removeUnit(Unit unit) {
        if (unit == null) {
            return;
        }

        if (numUnits.get(unit.getType().toString()) == null) {
            numUnits.put(unit.getType().toString(), 0);
            return;
        }

        mineralsLost += unit.getType().mineralPrice();
        gasLost += unit.getType().gasPrice();
        if (numUnits.get(unit.getType().toString()) == 1) {
            numUnits.remove(unit.getType().toString());
        } else {
            numUnits.put(unit.getType().toString(), numUnits.get(unit.getType().toString()) - 1);
        }
        if (!numDeadUnits.containsKey(unit.getType().toString())) {
            numDeadUnits.put(unit.getType().toString(), 1);
        } else {
            numDeadUnits.put(unit.getType().toString(), numDeadUnits.get(unit.getType().toString()) + 1);
        }

        unitAndUnitInfoMap.remove(unit.getID());
    }

    /// 포인터가 null 이 되었거나, 파괴되어 Resource_Vespene_Geyser 로 돌아간 Refinery, 예전에는 건물이 있었던
    /// 걸로 저장해두었는데 지금은 파괴되어 없어진 건물 (특히, 테란의 경우 불타서 소멸한 건물) 데이터를 제거합니다
    public void removeBadUnits() {

        for (Integer integer : unitAndUnitInfoMap.keySet()) {
            UnitInfo ui = unitAndUnitInfoMap.get(integer);
            if (isBadUnitInfo(ui)) {
                Unit unit = ui.getUnit();
                if (numUnits.get(unit.getType().toString()) != null) {
                    numUnits.put(unit.getType().toString(), numUnits.get(unit.getType().toString()) - 1);
                }

                badUnitsToRemove.add(unit.getID());
            }
        }

        if (badUnitsToRemove.size() > 0) {
            for (Integer i : badUnitsToRemove) {
                unitAndUnitInfoMap.remove(i);
            }
            badUnitsToRemove.clear();
        }
    }

    public final boolean isBadUnitInfo(final UnitInfo ui) {
        if (ui.getUnit() == null) {
            return false;
        }

        // Cull away any refineries / assimilators / extractors that were destroyed and
        // reverted to vespene geysers
        if (ui.getUnit().getType() == UnitType.Resource_Vespene_Geyser) {
            return true;
        }

        // If the unit is a building, and we can currently see its position, and it is not
        // there
        return ui.getType().isBuilding()
                && BasicBotModule.BroodWar.isVisible(ui.getLastPosition().getX() / 32, ui.getLastPosition().getY() / 32)
                && !ui.getUnit().isVisible();
    }

    /// 사망한 유닛을 생산하는데 소요되었던 Gas 의 누적값 (얼마나 손해를 보았는가 계산하기 위함임)
    public final int getGasLost() {
        return gasLost;
    }

    /// 사망한 유닛을 생산하는데 소요되었던 Mineral 의 누적값 (얼마나 손해를 보았는가 계산하기 위함임) 을 리턴합니다
    public final int getMineralsLost() {
        return mineralsLost;
    }

    /// 해당 UnitType 의 식별된 Unit 숫자를 리턴합니다
    public final int getNumUnits(String t) {
        if (numUnits.get(t) != null) {
            return numUnits.get(t);
        } else {
            return 0;
        }
    }

    /// 해당 UnitType 의 식별된 Unit 파괴/사망 누적값을 리턴합니다
    public final int getNumDeadUnits(String t) {
        if (numDeadUnits.get(t) != null) {
            return numDeadUnits.get(t);
        } else {
            return 0;
        }
    }

    /// 해당 UnitType 의 식별된 Unit 건설/훈련 누적값을 리턴합니다
    public final int getNumCreatedUnits(String t) {
        if (numCreatedUnits.get(t) != null) {
            return numCreatedUnits.get(t);
        } else {
            return 0;
        }
    }

    public final Map<Integer, UnitInfo> getUnitAndUnitInfoMap() {
        return unitAndUnitInfoMap;
    }

    public Map<String, Integer> getNumDeadUnits() {
        return numDeadUnits;
    }

    public Map<String, Integer> getNumCreatedUnits() {
        return numCreatedUnits;
    }

    public Map<String, Integer> getNumUnits() {
        return numUnits;
    }

}
