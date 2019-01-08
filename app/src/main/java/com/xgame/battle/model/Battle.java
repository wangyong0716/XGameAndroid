package com.xgame.battle.model;

/**
 * Created by zhanglianyu on 18-1-24.
 */

public class Battle {

    public static final String ACTION_BATTLE_OVER = "INTENT_ACTION_BATTLE_OVER";

    public static final int BATTLE_RESULT_UNKNOWM = 0;
    public static final int BATTLE_RESULT_WON = 1;
    public static final int BATTLE_RESULT_LOST = 2;

    private int mResult = BATTLE_RESULT_UNKNOWM;
    private String mPeer;
    private Game mGame;
    private long mTime;

    public Battle setResult(int result) {
        mResult = result;
        return this;
    }

    public int getResult() {
        return mResult;
    }

    public boolean won() {
        return getResult() == BATTLE_RESULT_WON;
    }

    public boolean lost() {
        return getResult() == BATTLE_RESULT_LOST;
    }

    public Battle setPeer(String peer) {
        mPeer = peer;
        return this;
    }

    public String getPeer() {
        return mPeer;
    }

    public Battle setGame(Game game) {
        mGame = game;
        return this;
    }

    public Game getGame() {
        return mGame;
    }

    public Battle setTime(long time) {
        mTime = time;
        return this;
    }

    public long getTime() {
        return mTime;
    }
}
