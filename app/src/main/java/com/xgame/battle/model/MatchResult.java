package com.xgame.battle.model;

import com.xgame.base.api.DataProtocol;

/**
 * Created by wangyong on 18-1-30.
 */

public class MatchResult implements DataProtocol{
    private int gameType;
    private int ruleId;
    private int matchStatus;
    private String roomId;
    private String gameId;
    private String gameUrl;
    private ServerPlayer peer;

    public int getMatchStatus() {
        return matchStatus;
    }

    public void setMatchStatus(int matchStatus) {
        this.matchStatus = matchStatus;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getGameUrl() {
        return gameUrl;
    }

    public void setGameUrl(String gameUrl) {
        this.gameUrl = gameUrl;
    }

    public ServerPlayer getPeer() {
        return peer;
    }

    public void setPeer(ServerPlayer peer) {
        this.peer = peer;
    }

    public int getGameType() {
        return gameType;
    }

    public void setGameType(int gameType) {
        this.gameType = gameType;
    }

    public int getRuleId() {
        return ruleId;
    }

    public void setRuleId(int ruleId) {
        this.ruleId = ruleId;
    }

    @Override
    public String toString() {
        return "MatchResult{" +
                "gameType=" + gameType +
                ", ruleId=" + ruleId +
                ", matchStatus=" + matchStatus +
                ", roomId='" + roomId + '\'' +
                ", gameId='" + gameId + '\'' +
                ", gameUrl='" + gameUrl + '\'' +
                ", peer=" + peer +
                '}';
    }
}
