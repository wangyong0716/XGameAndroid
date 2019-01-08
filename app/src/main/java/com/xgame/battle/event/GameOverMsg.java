package com.xgame.battle.event;

/**
 * Created by wangyong on 18-2-5.
 */

public class GameOverMsg {
    public long userId;
    public String sessionId;
    public String gameResult;

    public GameOverMsg(long userId, String sessionId, String gameResult) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.gameResult = gameResult;
    }
}
