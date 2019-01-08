package com.xgame.battle.model;

import com.xgame.base.api.DataProtocol;

/**
 * Created by zhanglianyu on 18-2-3.
 */

public class BWBattleBonusResult implements DataProtocol {
    private int status; //奖金下发模式，1表示立即下发，2表示延迟下发
    private long bonus; // long, 分得的奖金，单位人民币分
    private String shareBtnText; // string，分享button文案，可能不需要
    private String shareContentText; // string, 分享内容文案，可能不需要
    private long online; // long, 当前在线人数

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getOnline() {
        return online;
    }

    public void setOnline(long online) {
        this.online = online;
    }

    public long getBonus() {
        return bonus;
    }

    public void setBonus(long bonus) {
        this.bonus = bonus;
    }

    public String getShareBtnText() {
        return shareBtnText;
    }

    public void setShareBtnText(String shareBtnText) {
        this.shareBtnText = shareBtnText;
    }

    public String getShareContentText() {
        return shareContentText;
    }

    public void setShareContentText(String shareContentText) {
        this.shareContentText = shareContentText;
    }

    @Override
    public String toString() {
        return "BWBattleBonusResult{" +
                "status=" + status +
                ", bonus=" + bonus +
                ", shareBtnText='" + shareBtnText + '\'' +
                ", shareContentText='" + shareContentText + '\'' +
                ", online=" + online +
                '}';
    }
}
