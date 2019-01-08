package com.xgame.battle.model;

import com.xgame.base.api.DataProtocol;

/**
 * Created by zhanglianyu on 18-2-3.
 */

public class BWBattleReviveResult implements DataProtocol {
    private int status;
    private String msg;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "BWBattleReviveResult{" +
                "status=" + status +
                ", msg='" + msg + '\'' +
                '}';
    }
}
