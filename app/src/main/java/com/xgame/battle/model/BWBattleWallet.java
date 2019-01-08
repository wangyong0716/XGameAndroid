package com.xgame.battle.model;

import com.xgame.base.api.DataProtocol;

/**
 * Created by zhanglianyu on 18-2-9.
 */

public class BWBattleWallet implements DataProtocol {
    public long getTotalBonus() {
        return totalBonus;
    }

    public void setTotalBonus(long totalBonus) {
        this.totalBonus = totalBonus;
    }

    public long getCashBonus() {
        return cashBonus;
    }

    public void setCashBonus(long cashBonus) {
        this.cashBonus = cashBonus;
    }

    private long totalBonus; // long, 累积金额，单位人民币分
    private long cashBonus; // long, 可提现金额，单位人民币分

    @Override
    public String toString() {
        return "BWBattleWallet{" +
                "totalBonus=" + totalBonus +
                ", cashBonus=" + cashBonus +
                '}';
    }
}
