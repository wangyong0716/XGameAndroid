package com.xgame.battle.model;

import com.xgame.base.api.DataProtocol;

import java.util.Arrays;

/**
 * Created by wangyong on 18-2-27.
 */

public class BWBattleUserInfo implements DataProtocol {
    private long uId;
    private BWBattlePlayer[] list;

    public long getuId() {
        return uId;
    }

    public void setuId(long uId) {
        this.uId = uId;
    }

    public BWBattlePlayer[] getList() {
        return list;
    }

    public void setList(BWBattlePlayer[] list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "BWBattleUserInfo{" +
                "uId=" + uId +
                ", list=" + Arrays.toString(list) +
                '}';
    }
}
