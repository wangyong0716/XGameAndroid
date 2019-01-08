package com.xgame.battle.model;

import com.xgame.base.api.DataProtocol;

/**
 * Created by zhanglianyu on 18-2-3.
 */

public class BWBattleMatchResult implements DataProtocol {

    private int matchStatus; // int, 匹配状态，1匹配成功，2匹配中, 3规则id无效，4金币不足
    private String roomId; // string, 游戏房间id (这个比较特殊，用string来表示roomId)
    private long gameId; // long，游戏id
    private long roundId; // long， 第几轮
    private long nextRoundMatchOverTime; // long，本轮游戏结束后等待下一轮匹配结果的最长时间，单位：毫秒
    private long currentRoundStart; // long， 当轮游戏开始时间的服务器时间
    private long serverTime; // long，服务当前时间
    private long clientTime;
    private long online; // long, 当前在线人数
    // private BWBattlePlayer matchResult;

    public long getOnline() {
        return online;
    }

    public void setOnline(long online) {
        this.online = online;
    }

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

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public long getRoundId() {
        return roundId;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }

    public long getNextRoundMatchOverTime() {
        return nextRoundMatchOverTime;
    }

    public void setNextRoundMatchOverTime(long nextRoundMatchOverTime) {
        this.nextRoundMatchOverTime = nextRoundMatchOverTime;
    }

    public long getCurrentRoundStart() {
        return currentRoundStart;
    }

    public void setCurrentRoundStart(long currentRoundStart) {
        this.currentRoundStart = currentRoundStart;
    }

    public long getServerTime() {
        return serverTime;
    }

    public void setServerTime(long serverTime) {
        this.serverTime = serverTime;
    }

//    public BWBattlePlayer getMatchResult() {
//        return matchResult;
//    }
//
//    public void setMatchResult(BWBattlePlayer matchResult) {
//        this.matchResult = matchResult;
//    }

    public long getClientTime() {
        return clientTime;
    }

    public void setClientTime(long clientTime) {
        this.clientTime = clientTime;
    }

//    @Override
//    public String toString() {
//        return "BWBattleMatchResult{" +
//                "matchStatus=" + matchStatus +
//                ", roomId='" + roomId + '\'' +
//                ", gameId=" + gameId +
//                ", roundId=" + roundId +
//                ", nextRoundMatchOverTime=" + nextRoundMatchOverTime +
//                ", currentRoundStart=" + currentRoundStart +
//                ", serverTime=" + serverTime +
//                ", clientTime=" + clientTime +
//                ", online=" + online +
//                ", matchResult=" + matchResult +
//                '}';
//    }


    @Override
    public String toString() {
        return "BWBattleMatchResult{" +
                "matchStatus=" + matchStatus +
                ", roomId='" + roomId + '\'' +
                ", gameId=" + gameId +
                ", roundId=" + roundId +
                ", nextRoundMatchOverTime=" + nextRoundMatchOverTime +
                ", currentRoundStart=" + currentRoundStart +
                ", serverTime=" + serverTime +
                ", clientTime=" + clientTime +
                ", online=" + online +
                '}';
    }
}
