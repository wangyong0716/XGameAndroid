package com.xgame.battle;

import android.os.Handler;
import android.os.Looper;

import com.xgame.battle.model.BWBattleBonusResult;
import com.xgame.battle.model.BWBattleDetail;
import com.xgame.battle.model.BWBattleMatchResult;
import com.xgame.common.util.LogUtil;
import com.xgame.push.event.InvitationEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhanglianyu on 18-1-30.
 */

public class BWBattleManager {

    private static final String TAG = "BWBattleManager";

    private static volatile BWBattleManager instance;

    private Map<String, Object> mCache = new HashMap<>();

    private BWBattleManager() {
    }

    public static BWBattleManager getInstance() {
        if (instance == null) {
            synchronized (BattleManager.class) {
                if (instance == null) {
                    instance = new BWBattleManager();
                }
            }
        }
        return instance;
    }

    public synchronized void setBWId(long bwId) {
        putLong(BattleConstants.BW_ID, bwId);
    }

    public synchronized long getBWId() {
        return getLong(BattleConstants.BW_ID);
    }

    public synchronized void setBWBattleDetail(BWBattleDetail bwBattleDetail) {
        putObject(BattleConstants.BW_BATTLE_DETAIL, bwBattleDetail);
    }

    public synchronized BWBattleDetail getBWBattleDetail() {
        return (BWBattleDetail) getObject(BattleConstants.BW_BATTLE_DETAIL);
    }

    public synchronized void setBWBattleMatchResult(BWBattleMatchResult result) {
        putObject(BattleConstants.BW_BATTLE_MATCH_RESULT, result);
    }

    public synchronized BWBattleMatchResult getBWBattleMatchResult() {
        return (BWBattleMatchResult) getObject(BattleConstants.BW_BATTLE_MATCH_RESULT);
    }

    public synchronized void setBWRestartTime(int time) {
        putObject(BattleConstants.BW_RESTART_TIME, time);
    }

    public synchronized void setBWQuit(boolean quit) {
        putBoolean(BattleConstants.BW_BATTLE_QUIT, quit);
    }

    public synchronized boolean isBWQuit() {
        return getBoolean(BattleConstants.BW_BATTLE_QUIT);
    }

    public synchronized int getBWRestartTime() {
        return getInt(BattleConstants.BW_RESTART_TIME);
    }


    public synchronized void setBonus(BWBattleBonusResult result) {
        putObject(BattleConstants.BW_BONUS, result);
    }

    public synchronized BWBattleBonusResult getBonus() {
        return (BWBattleBonusResult) getObject(BattleConstants.BW_BONUS);
    }

    public synchronized void setBattleRound(long round) {
        putLong(BattleConstants.BW_BATTLE_ROUND, round);
    }

    public synchronized void revive() {
        int reviveCount = getInt(BattleConstants.BW_REVIVE_COUNT);
        putInt(BattleConstants.BW_REVIVE_COUNT, reviveCount + 1);
    }

    public synchronized int getReviveCount() {
        return getInt(BattleConstants.BW_REVIVE_COUNT);
    }

    public synchronized long getBattleRound() {
        return getLong(BattleConstants.BW_BATTLE_ROUND);
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
        LogUtil.i(TAG, "putString -> key = " + key + ", value = " + value);
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
