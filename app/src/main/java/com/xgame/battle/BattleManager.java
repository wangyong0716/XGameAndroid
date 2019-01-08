package com.xgame.battle;

import android.os.Handler;
import android.os.Looper;

import com.xgame.battle.model.Player;
import com.xgame.common.util.LogUtil;
import com.xgame.push.event.InvitationEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhanglianyu on 18-1-30.
 */

public class BattleManager {

    private static final String TAG = "BattleManager";

    private static volatile BattleManager instance;

    private Map<String, Object> mCache = new HashMap<>();

    private BattleManager() {}

    public static BattleManager getInstance() {
        if (instance == null) {
            synchronized (BattleManager.class) {
                if (instance == null) {
                    instance = new BattleManager();
                }
            }
        }
        return instance;
    }

    public synchronized void setPeerPlayer(Player player) {
        putObject(BattleConstants.BATTLE_PLAYER_PEER, player);
    }

    public synchronized Player getPeerPlayer() {
        return (Player) (getObject(BattleConstants.BATTLE_PLAYER_PEER));
    }

    public synchronized void setSelfPlayer(Player player) {
        putObject(BattleConstants.BATTLE_PLAYER_SELF, player);
    }

    public synchronized Player getSelfPlayer() {
        return (Player) (getObject(BattleConstants.BATTLE_PLAYER_SELF));
    }

    public synchronized void setGameId(int gameId) {
        putInt(BattleConstants.BATTLE_GAME_ID, gameId);
    }

    public synchronized int getGameId() {
        return getInt(BattleConstants.BATTLE_GAME_ID);
    }

    public synchronized void setIsFriend(boolean isFriend) {
        putBoolean(BattleConstants.IS_FRIEND, isFriend);
    }

    public synchronized boolean isFriend() {
        return getBoolean(BattleConstants.IS_FRIEND);
    }

    public synchronized void setRuleId(int ruleId) {
        putInt(BattleConstants.BATTLE_RULE_ID, ruleId);
    }

    public synchronized int getRuleId() {
        return getInt(BattleConstants.BATTLE_RULE_ID);
    }

    public synchronized void setRuleTitle(String ruleTitle) {
        putString(BattleConstants.BATTLE_RULE_TITLE, ruleTitle);
    }

    public synchronized String getRuleTitle() {
        return getString(BattleConstants.BATTLE_RULE_TITLE);
    }

    public synchronized int getSelfWin() {
        return getInt(BattleConstants.RESULT_WIN_SELF);
    }

    public synchronized void setSelfWin(int selfWin) {
        putInt(BattleConstants.RESULT_WIN_SELF, selfWin);
    }

    public synchronized int getPeerWin() {
        return getInt(BattleConstants.RESULT_WIN_PEER);
    }

    public synchronized void setPeerWin(int peerWin) {
        putInt(BattleConstants.RESULT_WIN_PEER, peerWin);
    }

    public synchronized void setWinCoin(int winCoin) {
        putInt(BattleConstants.BATTLE_WIN_COIN, winCoin);
    }

    public synchronized int getWinCoin() {
        return getInt(BattleConstants.BATTLE_WIN_COIN);
    }

    public synchronized void setLoseCoin(int loseCoin) {
        putInt(BattleConstants.BATTLE_LOSE_COIN, loseCoin);
    }

    public synchronized int getLoseCoin() {
        return getInt(BattleConstants.BATTLE_LOSE_COIN);
    }

    public synchronized void setGameType(int battleType) {
        putInt(BattleConstants.BATTLE_TYPE, battleType);
    }

    public synchronized int getGameType() {
        return getInt(BattleConstants.BATTLE_TYPE);
    }

    public synchronized void setToken(String token) {
        putString(BattleConstants.TOKEN, token);
    }

    public synchronized String getToken() {
        return getString(BattleConstants.TOKEN);
    }

    public synchronized void setRoomId(String roomId) {
        putString(BattleConstants.BATTLE_ROOM_ID, roomId);
    }

    public synchronized String getRoomId() {
        return getString(BattleConstants.BATTLE_ROOM_ID);
    }

    public synchronized void setGameUrl(String gameUrl) {
        putString(BattleConstants.BATTLE_GAME_URL, gameUrl);
    }

    public synchronized String getGameUrl() {
        return getString(BattleConstants.BATTLE_GAME_URL);
    }

    public synchronized void setBattleResult(String result) {
        putString(BattleConstants.BATTLE_RESULT, result);
    }

    public synchronized String getBattleResult() {
        return getString(BattleConstants.BATTLE_RESULT);
    }

    public synchronized void clearBattleResult() {
        putString(BattleConstants.BATTLE_RESULT, null);
    }

    public synchronized boolean isWinner() {
        return BattleUtils.GAME_OVER_RESULT_WIN.equals(getBattleResult());
    }

    public synchronized void setGameName(String gameName) {
        putString(BattleConstants.BATTLE_GAME_NAME, gameName);
    }

    public synchronized String getGameName() {
        return getString(BattleConstants.BATTLE_GAME_NAME);
    }

    public synchronized void setSessionId(String sessionId) {
        putString(BattleConstants.SESSION_ID, sessionId);
    }

    public synchronized String getSessionId() {
        return getString(BattleConstants.SESSION_ID);
    }

    public synchronized void setMatchFrom(String matchFrom) {
        putString(BattleConstants.MATCH_FROM, matchFrom);
    }

    public synchronized String getMatchFrom() {
        return getString(BattleConstants.MATCH_FROM);
    }

    public synchronized void clearAll() {
        LogUtil.i(TAG, "clearAll()");
        mCache.clear();
    }

    // 退出，给首页发个 event ，让其更新消息列表的数据
    public synchronized void sendPendingCancelEvent() {
        final InvitationEvent cancel = new InvitationEvent(
                InvitationEvent.PUSH_EVENT_INVITATION_CANCEL, null);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(cancel);
            }
        }, 1200);
    }


    private void putBoolean(String key, boolean value) {
        LogUtil.i(TAG, "putBoolean -> key = " + key + ", value = " + value);
        mCache.put(key, value);
    }

    private void putString(String key, String value) {
        LogUtil.i(TAG, "putString -> key = " + key + ", value = " + value);
        mCache.put(key, value);
    }

    private void putInt(String key, int value) {
        LogUtil.i(TAG, "putInt -> key = " + key + ", value = " + value);
        mCache.put(key, value);
    }

    private void putLong(String key, long value) {
        LogUtil.i(TAG, "putLong -> key = " + key + ", value = " + value);
        mCache.put(key, value);
    }

    private void putObject(String key, Object object) {
        LogUtil.i(TAG, "putObject -> key = " + key + ", value = " + (object == null ? null : object.toString()));
        mCache.put(key, object);
    }

    private String getString(String key) {
        String value = (String) (mCache.get(key));
        LogUtil.i(TAG, "getString -> key = " + key + ", value = " + value);
        return value;
    }

    private boolean getBoolean(String key) {
        boolean value;
        Object object = mCache.get(key);
        if (object == null) {
            value = false;
        } else {
            value = (boolean) object;
        }
        LogUtil.i(TAG, "getBoolean -> key = " + key + ", value = " + value);
        return value;
    }

    private int getInt(String key) {
        int value;
        Object object = mCache.get(key);
        if (object == null) {
            value = 0;
        } else {
            value = (Integer) object;
        }
        LogUtil.i(TAG, "getInt -> key = " + key + ", value = " + value);
        return value;
    }

    private long getLong(String key) {
        long value;
        Object object = mCache.get(key);
        if (object == null) {
            value = 0;
        } else {
            value = (Long) object;
        }
        LogUtil.i(TAG, "getInt -> key = " + key + ", value = " + value);
        return value;
    }

    private Object getObject(String key) {
        Object object = mCache.get(key);
        LogUtil.i(TAG, "getObject -> key = " + key + ", value = " + (object == null ? null : object.toString()));
        return object;
    }

}
