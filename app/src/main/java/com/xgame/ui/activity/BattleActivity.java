package com.xgame.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.miui.zeus.mario.sdk.MarioSdk;
import com.xgame.R;
import com.xgame.account.UserManager;
import com.xgame.battle.BWBattleManager;
import com.xgame.battle.BattleConstants;
import com.xgame.battle.BattleManager;
import com.xgame.battle.BattleUtils;
import com.xgame.battle.model.BWBattleDetail;
import com.xgame.common.util.LogUtil;
import com.xgame.ui.Router;
import com.xgame.util.Analytics;
import com.xgame.util.dialog.BaiWanAlertDialog;

import org.egret.runtime.launcherInterface.INativePlayer;
import org.egret.egretnativeandroid.EgretNativeAndroid;

import java.lang.ref.WeakReference;

/**
 * 白鹭游戏界面
 *
 * Created by zhanglianyu on 18-1-24.
 */

public class BattleActivity extends Activity {

    private static final String TAG = "BattleActivity";
    private EgretNativeAndroid nativeAndroid;

    private int mBattleType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final Intent intent = getIntent();
        String gameUrl;
        String roomId;
        String token;
        String gameName;
        String clientInfo;
        int gameId;
        int gameType;
        int ruleId;
        String ruleTitle;

        int type = intent.getIntExtra(BattleConstants.BATTLE_TYPE,
                BattleConstants.BATTLE_TYPE_MATCH);
        mBattleType = type;
        LogUtil.i(TAG, "onCreate() : battle type - " + mBattleType);

        BattleManager.getInstance().clearBattleResult();
        gameUrl = BattleManager.getInstance().getGameUrl();
        roomId = BattleManager.getInstance().getRoomId();
//        token = intent.getStringExtra(BattleUtils.EXTRA_SELF_TOKEN);
        token = UserManager.getInstance().getBailuToken();
        gameName = BattleManager.getInstance().getGameName();
        clientInfo = MarioSdk.getClientInfo();

        if (mBattleType == BattleConstants.BATTLE_TYPE_BW) {
            final BWBattleDetail detail = BWBattleManager.getInstance().getBWBattleDetail();
            if (detail == null) {
                LogUtil.i(TAG, "onCreate() : bw detail null");
                gameId = 0;
            } else {
                gameId = (int) detail.getGameId();
                gameName = detail.getTitle();
            }
        } else {
            gameId = BattleManager.getInstance().getGameId();
        }

        gameType = mBattleType;
        ruleId = BattleManager.getInstance().getRuleId();
        ruleTitle = BattleManager.getInstance().getRuleTitle();

        LogUtil.i(TAG, "onCreate() : gameUrl - " + gameUrl);
        LogUtil.i(TAG, "onCreate() : roomId - " + roomId);
        LogUtil.i(TAG, "onCreate() : token - " + token);
        LogUtil.i(TAG, "onCreate() : clientInfo - " + clientInfo);
        LogUtil.i(TAG, "onCreate() : gameId - " + gameId);
        LogUtil.i(TAG, "onCreate() : gameType - " + gameType);
        LogUtil.i(TAG, "onCreate() : ruleId - " + ruleId);

        if (TextUtils.isEmpty(gameUrl) || TextUtils.isEmpty(token) || TextUtils.isEmpty(roomId)
                || TextUtils.isEmpty(clientInfo)) {
            LogUtil.i(TAG, "onCreate() : param is empty");
            Toast.makeText(getApplicationContext(), "param empty", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // onGameOver(BattleUtils.GAME_OVER_RESULT_WIN);

        final Uri.Builder builder = Uri.parse(gameUrl).buildUpon();
        builder.appendQueryParameter(BattleUtils.URL_PARAM_TOKEN, token);
        builder.appendQueryParameter(BattleUtils.URL_PARAM_ROOMID, roomId);
        builder.appendQueryParameter(BattleUtils.URL_PARAM_CLIENTINFO, clientInfo);
        builder.appendQueryParameter(BattleUtils.URL_PARAM_GAMEID, String.valueOf(gameId));
        builder.appendQueryParameter(BattleUtils.URL_PARAM_GAMETYPE, String.valueOf(gameType));
        builder.appendQueryParameter(BattleUtils.URL_PARAM_RULEID, String.valueOf(ruleId));

        String url = builder.build().toString();

        nativeAndroid = new EgretNativeAndroid(this);
        if (!nativeAndroid.checkGlEsVersion()) {
            Toast.makeText(this, "This device does not support OpenGL ES 2.0.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        nativeAndroid.config.showFPS = false;
        nativeAndroid.config.fpsLogTime = 30;
        nativeAndroid.config.disableNativeRender = false;
        nativeAndroid.config.clearCache = false;

        // runtime超时配置，单位秒，0为默认值：不设超时
        // 这个超时时间是指runtime加载进游戏首页的最长等待时间，如果超时，回调onError：{"error":"load"}
        if (mBattleType == BattleConstants.BATTLE_TYPE_BW) {
            // 百万场考虑到时间线问题，超时时间设置略短
            nativeAndroid.config.homePageTimeout = 20;
        } else {
            nativeAndroid.config.homePageTimeout = 30;
        }

        setExternalInterfaces();

        LogUtil.i(TAG, "onCreate() : url - " + url);
        if (!nativeAndroid.initialize(url)) {
            LogUtil.i(TAG, "bailu nativeAndroid.initialize() fail!");
            onRuntimeError("init fail");
            return;
        } else {
            LogUtil.i(TAG, "bailu nativeAndroid.initialize() success!");
        }
        trackEvent(gameId, gameType, ruleId, ruleTitle, gameName);
        setContentView(nativeAndroid.getRootFrameLayout());
        initQuitButton();
    }

    private void trackEvent(int gameId, int gameType, int ruleId, String ruleTitle, String gameName) {
        try {
            //匹配成功页 进入游戏
            JsonObject json = new JsonObject();
            String battleType = "";
            if (gameType == BattleConstants.BATTLE_TYPE_MATCH) {
                battleType = Analytics.Constans.CUSTOM_GAME_TYPE_BATTLE;
            } else if (gameType == BattleConstants.BATTLE_TYPE_COIN) {
                battleType = Analytics.Constans.CUSTOM_GAME_TYPE_ARENA;
                json.addProperty("rule_title", ruleTitle);
            } else if (gameType == BattleConstants.BATTLE_TYPE_BW) {
                battleType = Analytics.Constans.CUSTOM_GAME_TYPE_BW;
                json.addProperty("bw_id",  Long.toString(BWBattleManager.getInstance().getBWId()));
            }
            json.addProperty("game_id", Integer.toString(gameId));
            json.addProperty("battle_type", battleType);
            json.addProperty("play_mode", Analytics.Constans.CUSTOM_GAMME_MODE_PK);
            json.addProperty("rule_id", Integer.toString(ruleId));
            json.addProperty("game_name", gameName);
            LogUtil.d(TAG, "json: " + json);
            Analytics.trackCustomEvent(Analytics.Constans.ACTION_PATH_MATCH_SUCCESS, Analytics.Constans.ACTION_TYPE_PLAY_GAME,
                    "", json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initQuitButton() {
        final ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.ic_quit_game);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askIfLeaveGame();
            }
        });
        final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams
                (FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(16, 36, 0, 0);
        addContentView(imageView, params);
    }

    private void askIfLeaveGame() {
        BaiWanAlertDialog.Builder builder = new BaiWanAlertDialog.Builder(this);
        if (mBattleType == BattleConstants.BATTLE_TYPE_COIN) {
            builder.setMessage(getString(R.string.battle_back_title_coin));
        } else if (mBattleType == BattleConstants.BATTLE_TYPE_BW) {
            builder.setMessage(getString(R.string.battle_back_title_bw));
        } else {
            builder.setMessage(getString(R.string.battle_back_title));
        }
        builder.setPositiveButton(R.string.battle_back_yes, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveGame();
            }
        });
        builder.setNegativeButton(R.string.battle_back_no, null);
        builder.create().show();
    }

    private void leaveGame() {

        LogUtil.i(TAG, "leaveGame() : type - " + mBattleType);

        // solution 1
        // handleLeaveGame1();

        // solution 2
        handleLeaveGame2();

        // common logic
        if (mBattleType == BattleConstants.BATTLE_TYPE_BW) {
            LogUtil.i(TAG, "leaveGame() : in bw, set quit true.");
            BWBattleManager.getInstance().setBWQuit(true);
        }
    }

    private void handleLeaveGame1() {
        if (nativeAndroid != null) {
            nativeAndroid.callExternalInterface("CallJsLeaveGame", "");
        }
    }

    private void handleLeaveGame2() {
        final BattleManager battleManager = BattleManager.getInstance();
        battleManager.setBattleResult(BattleUtils.GAME_OVER_RESULT_LOSE);
        battleManager.setPeerWin(battleManager.getPeerWin() + 1);
        BattleUtils.showResult(BattleActivity.this, mBattleType);
        if (nativeAndroid != null) {
            LogUtil.i(TAG, "leaveGame, call bailu nativeAndroid.exitGame() ... ");
            nativeAndroid.exitGame();
        }
        finish();
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent keyEvent) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            Log.i(TAG, "back key, call bailu nativeAndroid.exitGame() ... ");
//            if (nativeAndroid != null) {
//                nativeAndroid.exitGame();
//            }
//        }

        return super.onKeyDown(keyCode, keyEvent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nativeAndroid != null) {
            nativeAndroid.resume();
        }
    }

    @Override
    public void onBackPressed() {
        askIfLeaveGame();
    }

    @Override
    protected void onPause() {
        if (nativeAndroid != null) {
            nativeAndroid.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        LogUtil.i(TAG, "onDestroy()");
//        if (nativeAndroid != null) {
//            Log.i(TAG, "onDestroy, call bailu nativeAndroid.exitGame() ... ");
//            nativeAndroid.exitGame();
//        }
        super.onDestroy();
    }

    private void setExternalInterfaces() {
        if (nativeAndroid != null) {
            nativeAndroid.setExternalInterface(BattleUtils.JS_CALLBACK_GAME_OVER,
                    new GameOverCallBack(this));
            nativeAndroid.setExternalInterface(BattleUtils.RT_CALLBACK_ERROR,
                    new RuntimeErrorCallBack(this));
        }
    }

    public void onTrimMemory(int level) {
        if (nativeAndroid != null) {
            nativeAndroid.onTrimMemory(level);
        }
    }

    private void onRuntimeError(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LogUtil.i(TAG, "onRuntimeError() : " + message);
                BaiWanAlertDialog.Builder builder =
                        new BaiWanAlertDialog.Builder(BattleActivity.this);
                builder.setMessage(getString(R.string.battle_error));
                builder.setPositiveButton(R.string.battle_error_return, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        BattleManager.getInstance().clearAll();
                        if (mBattleType == BattleConstants.BATTLE_TYPE_BW) {
                            BWBattleManager.getInstance().clearAll();
                        }
                        Router.toHome();
                        if (nativeAndroid != null) {
                            LogUtil.i(TAG, "onRuntimeError, call bailu nativeAndroid.exitGame() ... ");
                            nativeAndroid.exitGame();
                        }
                        finish();
                    }
                });
                BaiWanAlertDialog baiWanAlertDialog = builder.create();
                baiWanAlertDialog.setCancelable(false);
                baiWanAlertDialog.show();
            }
        });
    }

    private void onGameOver(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LogUtil.i(TAG, "onGameOver() : " + message);
                String str;
                final BattleManager battleManager = BattleManager.getInstance();
                if (BattleUtils.GAME_OVER_RESULT_WIN.equals(message)) {
                    str = "win";
                    battleManager.setBattleResult(BattleUtils.GAME_OVER_RESULT_WIN);
                    battleManager.setSelfWin(battleManager.getSelfWin() + 1);
                } else if (BattleUtils.GAME_OVER_RESULT_LOSE.equals(message)) {
                    str = "lose";
                    battleManager.setBattleResult(BattleUtils.GAME_OVER_RESULT_LOSE);
                    battleManager.setPeerWin(battleManager.getPeerWin() + 1);
                }else if (BattleUtils.GAME_OVER_RESULT_DOGFALL.equals(message)) {
                    str = "dogfall";
                    battleManager.setBattleResult(BattleUtils.GAME_OVER_RESULT_DOGFALL);
                } else {
                    str = "no result";
                    battleManager.setBattleResult(BattleUtils.GAME_OVER_RESULT_NULL);
                }
                LogUtil.i(TAG, "onGameOver() : " + str + " , battle type - " + mBattleType);
                BattleUtils.showResult(BattleActivity.this, mBattleType);
                if (nativeAndroid != null) {
                    LogUtil.i(TAG, "onGameOver() : call bailu nativeAndroid.exitGame() ... ");
                    nativeAndroid.exitGame();
                }
                finish();
            }
        });
    }
    
    private static class GameOverCallBack implements INativePlayer.INativeInterface {

        private WeakReference<BattleActivity> activityRef;

        public GameOverCallBack(BattleActivity activity) {
            activityRef = new WeakReference<BattleActivity>(activity);
        }

        @Override
        public void callback(final String s) {
            if (activityRef == null) {
                return;
            }
            final BattleActivity activity = activityRef.get();
            if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                return;
            }
            activity.onGameOver(s);
        }
    }

    private static class RuntimeErrorCallBack implements INativePlayer.INativeInterface {

        private WeakReference<BattleActivity> activityRef;

        public RuntimeErrorCallBack(BattleActivity activity) {
            activityRef = new WeakReference<BattleActivity>(activity);
        }

        @Override
        public void callback(final String s) {
            if (activityRef == null) {
                return;
            }
            final BattleActivity activity = activityRef.get();
            if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                return;
            }
            activity.onRuntimeError(s);
        }
    }
}