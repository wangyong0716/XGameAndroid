package com.xgame.battle.model;

import com.xgame.base.api.DataProtocol;

/**
 * Created by wangyong on 18-2-2.
 */

public class BWOnlineNumbers implements DataProtocol {

    public long getBwId() {
        return bwId;
    }

    public void setBwId(long bwId) {
        this.bwId = bwId;
    }

    public long getOffline() {
        return offline;
    }

    public void setOffline(long offline) {
        this.offline = offline;
    }

    public long getOnline() {
        return online;
    }

    public void setOnline(long online) {
        this.online = online;
    }

    public long getNextTime() {
        return nextTime;
    }

    public void setNextTime(long nextTime) {
        this.nextTime = nextTime;
    }

    public long getServerTime() {
        return serverTime;
    }

    public void setServerTime(long serverTime) {
        this.serverTime = serverTime;
    }

    public long getClientTime() {
        return clientTime;
    }

    public void setClientTime(long clientTime) {
        this.clientTime = clientTime;
    }

    private long bwId; // long， 百万场活动id
    private long offline; // long, 人数区间下限
    private long online; // long，人数区间上限
    private long nextTime; // long, 下次过来轮循的时间
    private long serverTime; // long，服务器返回结果时的时间
    private long clientTime;

    @Override
    public String toString() {
        return "BWOnlineNumbers{" +
                "bwId=" + bwId +
                ", offline=" + offline +
                ", online=" + online +
                ", nextTime=" + nextTime +
                ", serverTime=" + serverTime +
                ", clientTime=" + clientTime +
                '}';
    }
}
