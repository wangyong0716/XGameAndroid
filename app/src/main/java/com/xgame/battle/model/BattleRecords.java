package com.xgame.battle.model;

import android.support.annotation.Keep;

import java.util.List;

@Keep
public class BattleRecords {

    private int count;
    private List<ServerBattleRecord> recordList;

    public List<ServerBattleRecord> getBattleList() {
        return recordList;
    }
}
