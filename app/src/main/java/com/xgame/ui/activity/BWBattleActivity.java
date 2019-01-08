package com.xgame.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.xgame.R;
import static com.xgame.base.ServiceFactory.battleService;

import com.xgame.account.UserManager;
import com.xgame.account.view.LoadingDialog;
import com.xgame.battle.BWBattleManager;
import com.xgame.battle.BattleManager;
import com.xgame.battle.BattleUtils;
import com.xgame.battle.model.BWBattleDetail;
import com.xgame.battle.model.BWBattleMatchResult;
import com.xgame.battle.model.BWBattleWallet;
import com.xgame.battle.model.BWOnlineNumbers;
import com.xgame.common.api.OnCallback;
import com.xgame.common.util.GlobalGson;
import com.xgame.common.util.IntentParser;
import com.xgame.common.util.LogUtil;
import com.xgame.common.util.NetworkUtil;
import com.xgame.push.event.BWBattleMatchResultEvent;
import com.xgame.ui.Router;
import com.xgame.ui.fragment.BWBattleFragment1;
import com.xgame.ui.fragment.BWBattleFragment2;
import com.xgame.ui.fragment.BWBattleFragment3;
import com.xgame.util.Analytics;
import com.xgame.util.dialog.BaiWanAlertDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Random;

/**
 * Created by zhanglianyu on 18-1-28.
 */

public class BWBattleActivity extends FragmentActivity {

    private static final String TAG = "BWBattleActivity";
    private static final String TAG_WD = "BWBattleActivity_WD";

    private BWBattleFragment1 mBWTattleFragment1;
    private BWBattleFragment2 mBWTattleFragment2;
    private BWBattleFragment3 mBWTattleFragment3;

    // 用户处于浏览百万对战状态，尚未参与活动
    private static final int STATE_BROWSING_BWBATTLE = 0;
    // 已经参与活动
    private static final int STATE_IN_BWBATTLE = 1;
    // 已经参与活动并且处于播放串场视频
    private static final int STATE_IN_VIDEO = 2;

    private int mState = STATE_BROWSING_BWBATTLE;

    private SoundPool mSoundPool;
    private static final int SOUND_BACKGROUND_1 = 1;
    private WatchDog mWatchDog;

    private long mStartTime;

    private boolean mWaitJoin = false;
    private long mWaitJoinTime = 0L;

    private boolean mHasMatch = false;
    private boolean mWatchDogMatch = false;

    private boolean mWaitOnlinesResult = false;
    private long mOnlineMin = 0;
    private long mOnlineMax = 0;
    private long mOnlineCurrent = 0;
    private long mCurrentCallOnlinesTime = 0;
    private long mNextCallOnlinesTime = 0;
    private LoadingDialog mLoadingDialog;

    private BroadcastReceiver mNetworkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bwbattle);

        EventBus.getDefault().register(this);

        initFragment();
        // initSoundPool();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        mLoadingDialog = new LoadingDialog(this);

        initReceiver();

        // get and cache bw id
        final Uri uri = getIntent().getData();
        if (uri != null) {
            final String bwIdStr = uri.getQueryParameter(BattleUtils.URL_PARAM_BWID);
            LogUtil.i(TAG, "onCreate() : bwIdStr - " + bwIdStr);
            final long bwId = BattleUtils.getLongBWId(bwIdStr);
            LogUtil.i(TAG, "onCreate() : bwId - " + bwId);
            BWBattleManager.getInstance().setBWId(bwId);
        }
        
        loadBWData();
        //百万对战页PV
        Analytics.trackPageShowEvent(Analytics.Constans.URL_TITLE_BAIWANG_PAGE, Analytics.Constans.VISIT_TYPE_READY, null);
    }

    private void printTimeLine(String pre, long time) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        LogUtil.i("DEBUG_TIME_LINE", pre + " : " + format.format(time));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBWBattleMatchPush(BWBattleMatchResultEvent event) {
        LogUtil.i(TAG, "onBWBattleMatchPush() : " + event);
        if (mHasMatch) {
            LogUtil.i(TAG, "onBWBattleMatchPush() : has match");
            return;
        }
        final BWBattleDetail bwBattleDetail = BWBattleManager.getInstance().getBWBattleDetail();
        if (bwBattleDetail == null) {
            return;
        }

        if (event != null) {
            final String content = event.getContent();
            LogUtil.i(TAG, "onBWBattleMatchPush() content : " + content);
            final BWBattleMatchResult result = GlobalGson.get().fromJson(content,
                    BWBattleMatchResult.class);
            LogUtil.i(TAG, "onBWBattleMatchPush() result : " + result);
            // if (result != null && result.getMatchResult() != null) {
            if (result != null) {
                LogUtil.i(TAG, "onBWBattleMatchPush() result ok, has match");
                result.setClientTime(System.currentTimeMillis());
                mHasMatch = true;
                BWBattleManager.getInstance().setBWBattleMatchResult(result);
                BattleManager.getInstance().setRoomId(result.getRoomId());
                BattleManager.getInstance().setGameUrl(bwBattleDetail.getGameUrl());
            }
        }
    }

    private void initReceiver() {
        final IntentFilter networkFilter = new IntentFilter();
        networkFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mNetworkReceiver = new NetworkReceiver();
        registerReceiver(mNetworkReceiver, networkFilter);
    }

    private class NetworkReceiver extends BroadcastReceiver {
        public NetworkReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            LogUtil.i(TAG, "NetworkReceiver receive broadcast : " + action);
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                int network = NetworkUtil.getActiveNetworkType(getApplicationContext());
                LogUtil.i(TAG, "network type - " + network);
            }
        }
    }

    private void loadBWWallet() {
        LogUtil.i(TAG, "loadBWWallet()");
        final BWBattleDetail bwBattleDetail = BWBattleManager.getInstance().getBWBattleDetail();
        if (bwBattleDetail == null) {
            return;
        }

        battleService().getBWBattleWallet(UserManager.getInstance().getToken(),
                bwBattleDetail.getBwId()).enqueue(new OnCallback<BWBattleWallet>() {
            @Override
            public void onResponse(BWBattleWallet result) {
                LogUtil.i(TAG, "loadBWWallet() : onResponse - " + result);
                if (result != null) {
                    EventBus.getDefault().post(result);
                }
            }

            @Override
            public void onFailure(BWBattleWallet result) {
                LogUtil.i(TAG, "loadBWWallet()");
            }
        });
    }

    private void loadBWMatchResult() {
        final BWBattleDetail bwBattleDetail = BWBattleManager.getInstance().getBWBattleDetail();
        if (bwBattleDetail == null) {
            handleCommonFail();
            return;
        }
        String token = UserManager.getInstance().getToken();
        LogUtil.i(TAG, "loadBWMatchResult() : token - " + token);
        long bwId = bwBattleDetail.getBwId();
        LogUtil.i(TAG, "loadBWMatchResult() : bwId - " + bwId);
        long gameId = bwBattleDetail.getGameId();
        LogUtil.i(TAG, "loadBWMatchResult() : gameId - " + gameId);
        int roundId = 1;
        LogUtil.i(TAG, "loadBWMatchResult() : roundId - " + roundId);

        battleService().getBWBattleMatchResult(token, bwId, gameId, roundId)
                .enqueue(new OnCallback<BWBattleMatchResult>() {
            @Override
            public void onResponse(BWBattleMatchResult result) {
                LogUtil.i(TAG, "loadBWMatchResult() : onResponse - " + result);
                if (result == null) {
                    LogUtil.i(TAG, "loadBWMatchResult() : result null");
                    handleCommonFail();
                    return;
                }
                result.setClientTime(System.currentTimeMillis());
                if (result.getMatchStatus() != MatchActivity.MATCH_SUCCEED) {
                    LogUtil.i(TAG, "loadBWMatchResult() : match fail");
                    handleCommonFail();
                    return;
                }
//                if (result.getMatchResult() == null) {
//                    LogUtil.i(TAG, "loadBWMatchResult() : player is null");
//                    handleCommonFail();
//                    return;
//                }
                LogUtil.i(TAG, "loadBWMatchResult() : result ok, has match");
                mHasMatch = true;
                BWBattleManager.getInstance().setBWBattleMatchResult(result);
                BattleManager.getInstance().setRoomId(result.getRoomId());
                BattleManager.getInstance().setGameUrl(bwBattleDetail.getGameUrl());
            }

            @Override
            public void onFailure(BWBattleMatchResult result) {
                LogUtil.i(TAG, "loadBWMatchResult() : onFailure");
                handleCommonFail();
            }
        });
    }

    private void loadBWData() {
        final long bwId = BWBattleManager.getInstance().getBWId();
        LogUtil.i(TAG, "loadBWData() : bwId - " + bwId);
        final String token = UserManager.getInstance().getToken();
        LogUtil.i(TAG, "loadBWData() : token - " + token);

        if (mLoadingDialog != null) {
            mLoadingDialog.showLoadingDialog(R.string.bw_battle_loading);
        }
        battleService().getBWBattleDetail(token, bwId)
                .enqueue(new OnCallback<BWBattleDetail>() {
                    @Override
                    public void onResponse(BWBattleDetail result) {
                        LogUtil.i(TAG, "loadBWData() : onResponse");
                        if (mLoadingDialog != null) {
                            mLoadingDialog.dismissLoadingDialog();
                        }
                        if (result != null && result.getUserId() > 0) {
                            result.setClientTime(System.currentTimeMillis());
                            BWBattleManager.getInstance().setBWBattleDetail(result);
                            LogUtil.i(TAG, "loadBWData() : " + result);

                            printTimeLine("getServerTime", result.getServerTime());
                            printTimeLine("getOpenDoorTime", result.getOpenDoorTime());
                            printTimeLine("getCloseDoorTime", result.getCloseDoorTime());
                            printTimeLine("getRealStartTime", result.getRealStartTime());
                            printTimeLine("getClientTime", result.getClientTime());

                            if (result.getServerTime() > result.getCloseDoorTime()) {
                                LogUtil.i(TAG, "loadBWData() : over close door time");
                                handleCloseDoor();
                                return;
                            }

                            if (result.getServerTime() > result.getOpenDoorTime()) {
                                LogUtil.i(TAG, "loadBWData() : over open door time");
                                handleJoin(token, bwId, result.getGameId(), 1,
                                        result.getGameUrl());
                            }

                            final long clientTimeStart = result.getClientTime() +
                                    (result.getShowStartTime() - result.getServerTime());
                            startWatchDog(clientTimeStart);
                            EventBus.getDefault().post(new EventBWBattleDetailLoaded());
                        } else {
                            handleCommonFail();
                        }
                    }

                    @Override
                    public void onFailure(BWBattleDetail result) {
                        LogUtil.i(TAG, "loadBWData() : onFailure");
                        if (mLoadingDialog != null) {
                            mLoadingDialog.dismissLoadingDialog();
                        }
                        handleCommonFail();
                    }
                });

    }

    private void startWatchDog(final long startTime) {
        releaseWatchDog();

        mStartTime = startTime;
        mWatchDog = new WatchDog(this);
        mWatchDog.start();
    }

    private void releaseWatchDog() {
        if (mWatchDog != null) {
            mWatchDog.stop();
            mWatchDog = null;
        }
    }

    @Override
    protected void onResume() {
        LogUtil.i(TAG, "onResume()");
        super.onResume();
        updateUI();
        loadBWWallet();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseSoundPool();
        releaseWatchDog();
        EventBus.getDefault().unregister(this);
        if (mNetworkReceiver != null) {
            unregisterReceiver(mNetworkReceiver);
            mNetworkReceiver = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (isInBattle() || isInVideo()) {
            BaiWanAlertDialog.Builder builder = new BaiWanAlertDialog.Builder(this);
            builder.setMessage(getString(R.string.battle_back_title_bw));
            builder.setPositiveButton(R.string.battle_back_yes, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final BWBattleDetail detail = BWBattleManager.getInstance().getBWBattleDetail();
                    if (detail != null) {
                        LogUtil.i(TAG, "onBackPressed() : call cancel BW battle");
                        battleService().cancelBWBattle(detail.getBwId(), detail.getGameId(), 1).enqueue(new OnCallback<Void>() {
                            @Override
                            public void onResponse(Void result) {
                                LogUtil.i(TAG, "onBackPressed() : call cancel, onResponse");
                            }

                            @Override
                            public void onFailure(Void result) {
                                LogUtil.i(TAG, "onBackPressed() : call cancel, onFailure");
                            }
                        });
                    }
                    BWBattleManager.getInstance().clearAll();
                    Router.toHome();
                    finish();
                }
            });
            builder.setNegativeButton(R.string.battle_back_no, null);
            builder.create().show();
            return;
        }

        super.onBackPressed();
    }

    private void handleJoin(final String token, final long bwId, final long gameId,
                            final long roundId, final String gameUrl) {
        LogUtil.i(TAG, "handleJoin()");
        battleService().joinBWBattle(token, bwId, gameId, roundId).enqueue(
                new OnCallback<BWBattleMatchResult>() {
            @Override
            public void onResponse(BWBattleMatchResult result) {
                LogUtil.i(TAG, "handleJoin() : onResponse");
                if (result == null) {
                    LogUtil.i(TAG, "handleJoin() : result null");
                    return;
                }
                result.setClientTime(System.currentTimeMillis());
                LogUtil.i(TAG, "handleJoin() : result - " + result);
                if (result.getMatchStatus() != MatchActivity.MATCH_SUCCEED) {
                    LogUtil.i(TAG, "handleJoin() : match fail");
                    return;
                }
                LogUtil.i(TAG, "handleJoin() : has match");
                mHasMatch = true;
                BWBattleManager.getInstance().setBWBattleMatchResult(result);
                BattleManager.getInstance().setRoomId(result.getRoomId());
                BattleManager.getInstance().setGameUrl(gameUrl);
            }

            @Override
            public void onFailure(BWBattleMatchResult result) {
                LogUtil.i(TAG, "onFailure() : onResponse");
                handleCommonFail();
            }
        });
    }

    private void handleCloseDoor() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BaiWanAlertDialog.Builder builder = new BaiWanAlertDialog.Builder(
                        BWBattleActivity.this);
                builder.setMessage(getString(R.string.bw_battle_door_closed));
//                builder.setPositiveButton(R.string.bw_battle_door_closed_alarm,
//                        new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                BWBattleManager.getInstance().clearAll();
//                                BWBattleActivity.super.onBackPressed();
//                            }
//                        });
//                builder.setNegativeButton(R.string.bw_battle_door_closed_home,
//                        new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                BWBattleManager.getInstance().clearAll();
//                                BWBattleActivity.super.onBackPressed();
//                            }
//                        });

                builder.setPositiveButton(R.string.bw_battle_door_closed_home,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                BWBattleManager.getInstance().clearAll();
                                Router.toHome();
                                finish();
                            }
                        });

                BaiWanAlertDialog dialog = builder.create();
                dialog.setCancelable(false);
                dialog.show();
            }
        });
    }

    private void handleCommonFail() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                releaseWatchDog();
                BaiWanAlertDialog.Builder builder = new BaiWanAlertDialog.Builder(
                        BWBattleActivity.this);
                builder.setMessage(getString(R.string.bw_battle_fail_common));
                builder.setPositiveButton(R.string.bw_battle_fail_common_yes,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                BWBattleManager.getInstance().clearAll();
                                Router.toHome();
                                finish();
                            }
                        });
                BaiWanAlertDialog dialog = builder.create();
                dialog.setCancelable(false);
                dialog.show();
            }
        });
    }


    private void initSoundPool() {
        if (mSoundPool == null) {
            mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC,0);
            mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                    if (isInBattle()) {
                        playBackgroundSound();
                    }
                }
            });
            mSoundPool.load(this, R.raw.audio_bwbattle_bg, SOUND_BACKGROUND_1);
        }
    }

    private void releaseSoundPool() {
        if (mSoundPool != null) {
            mSoundPool.release();
            mSoundPool = null;
        }
    }

    public void playBackgroundSound() {
        if (mSoundPool != null) {
            mSoundPool.play(SOUND_BACKGROUND_1, 1f, 1f,
                    1,-1,1f);
        }
    }

    public void stopBackgroundSound() {
        if (mSoundPool != null) {
            mSoundPool.stop(SOUND_BACKGROUND_1);
        }
    }

    private void initFragment() {
        mBWTattleFragment1 = new BWBattleFragment1();
        mBWTattleFragment2 = new BWBattleFragment2();
        mBWTattleFragment3 = new BWBattleFragment3();
    }

    public void showBattleFragment1() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.content, mBWTattleFragment1);
        transaction.commit();
    }

    public void showBattleFragment2() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.content, mBWTattleFragment2);
        transaction.commit();
    }

    public void showBattleFragment3() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.content, mBWTattleFragment3);
        transaction.commit();
    }

    // 得到距离当前本场百万对战开始的时间。时间为毫秒。
    // 开始时间是进入到串场视频的时间，并非真正开始对战的时间。
    public long getTimeToStart() {
        final long current = System.currentTimeMillis();
        LogUtil.i(TAG_WD, "getTimeToStart() : current - " + current +
                " , mStartTime - " + mStartTime);
        return mStartTime - current;
    }

    public void enterBWBattle() {
        updateState(STATE_IN_BWBATTLE);
        waitJoinAndComputeJoinTime();
    }

    private void waitJoinAndComputeJoinTime() {
        final BWBattleDetail bwBattleDetail = BWBattleManager.getInstance().getBWBattleDetail();
        if (bwBattleDetail == null) {
            return;
        }

        if (mWaitJoin) {
            return;
        }

        mWaitJoin = true;
        mWaitJoinTime = bwBattleDetail.getClientTime() +
                (bwBattleDetail.getOpenDoorTime() - bwBattleDetail.getServerTime())
                + (new Random().nextInt(50)) * BattleUtils.SECOND_1;

        LogUtil.i(TAG, "waitJoinAndComputeJoinTime() : mWaitJoin - " + mWaitJoin
                + ", mWaitJoinTime - " + mWaitJoinTime);
    }

    private boolean isBrowsing() {
        return mState == STATE_BROWSING_BWBATTLE;
    }

    private boolean isInBattle() {
        return mState == STATE_IN_BWBATTLE;
    }

    private boolean isInVideo() {
        return mState == STATE_IN_VIDEO;
    }

    private boolean beforeBrowsing() {
        return mState <= STATE_BROWSING_BWBATTLE;
    }

    private boolean beforeInBattle() {
        return mState <= STATE_IN_BWBATTLE;
    }

    private boolean beforeInVideo() {
        return mState <= STATE_IN_VIDEO;
    }

    private void updateState(int state) {
        if (state != STATE_BROWSING_BWBATTLE && state != STATE_IN_BWBATTLE
                && state != STATE_IN_VIDEO) {
            return;
        }
        if (mState != state) {
            mState = state;
            updateUI();
        }
    }

    private void updateUI() {
        if (isBrowsing()) {
            showBattleFragment1();
        } else if (isInBattle()) {
            showBattleFragment2();
        } else if (isInVideo()) {
            showBattleFragment3();
        }
    }

    private void onWatchDog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LogUtil.i(TAG_WD, "onWatchDog");
                // update state and ui
                onWatchDogHandleUpdateStateAndUI();
                // handle join bw battle
                onWatchDogHandleJoinBW();
                // handle match
                onWatchDogHandleMatch();
                // handle game start
                onWatchDogHandleGameStart();
                // handle online numbers
                onWatchDogHandleOnlines();
            }
        });
    }

    private void onWatchDogHandleOnlines() {
        onWatchDogHandleOnlinesUpdateUI();
        onWatchDogHandleOnlinesLoadData();
    }

    private void onWatchDogHandleOnlinesUpdateUI() {
        if (isBrowsing()) {
            return;
        }
        final long time = System.currentTimeMillis();
        if (mOnlineMax > mOnlineMin && mNextCallOnlinesTime > time
                && mCurrentCallOnlinesTime < time) {
            mOnlineCurrent = mOnlineMin +
                    (mOnlineMax - mOnlineMin) * (time - mCurrentCallOnlinesTime) /
                            (mNextCallOnlinesTime - mCurrentCallOnlinesTime);
        } else {
            mOnlineCurrent = mOnlineMax;
        }
        LogUtil.i(TAG_WD, "onWatchDogHandleOnlinesUpdateUI() : post, mOnlineCurrent - "
                + mOnlineCurrent);
        EventBus.getDefault().post(new EventUpdateOnlines(mOnlineCurrent));
    }

    private void onWatchDogHandleOnlinesLoadData() {
        final BWBattleDetail bwBattleDetail = BWBattleManager.getInstance().getBWBattleDetail();
        if (bwBattleDetail == null) {
            return;
        }
        if (mWaitOnlinesResult) {
            return;
        }
        final long time = System.currentTimeMillis();
        if (time > mNextCallOnlinesTime) {
            mWaitOnlinesResult = true;
            LogUtil.i(TAG, "onWatchDogHandleOnlinesLoadData()");
            battleService().getOnlineNum(UserManager.getInstance().getToken(),
                    bwBattleDetail.getBwId(), bwBattleDetail.getGameId()).enqueue(
                            new OnCallback<BWOnlineNumbers>() {
                @Override
                public void onResponse(BWOnlineNumbers result) {
                    LogUtil.i(TAG, "onWatchDogHandleOnlinesLoadData() : onResponse");
                    if (result == null) {
                        LogUtil.i(TAG, "onWatchDogHandleOnlinesLoadData() : result null");
                        mNextCallOnlinesTime = time + BattleUtils.SECOND_30;
                    } else {
                        result.setClientTime(System.currentTimeMillis());
                        LogUtil.i(TAG, "onWatchDogHandleOnlinesLoadData() : " + result);
                        mOnlineMin = result.getOffline();
                        mOnlineMax = result.getOnline();

//                        mOnlineMin = 20000;
//                        mOnlineMax = 30000;

                        mCurrentCallOnlinesTime = result.getClientTime();
                        final long duration = result.getNextTime() - result.getServerTime();
                        if (duration > 0) {
                            mNextCallOnlinesTime = result.getClientTime() + duration;
                        } else {
                            mNextCallOnlinesTime = result.getClientTime() + BattleUtils.SECOND_30;
                        }
                    }
                    mWaitOnlinesResult = false;
                }

                @Override
                public void onFailure(BWOnlineNumbers result) {
                    LogUtil.i(TAG, "onWatchDogHandleOnlinesLoadData() : onFailure");
                    mNextCallOnlinesTime = time + BattleUtils.SECOND_30;
                    mWaitOnlinesResult = false;
                }
            });
        }
    }

    private void onWatchDogHandleUpdateStateAndUI() {
        final long timeToStart = getTimeToStart();
        final BWBattleDetail bwBattleDetail = BWBattleManager.getInstance().getBWBattleDetail();
        if (bwBattleDetail == null) {
            return;
        }
        if (timeToStart > BattleUtils.MINUTE_10) {
            LogUtil.i(TAG_WD, "onWatchDogHandleUpdateStateAndUI : over 10 minutes");
            if (beforeBrowsing()) {
                updateState(STATE_BROWSING_BWBATTLE);
            }
        } else if (timeToStart > BattleUtils.MINUTE_1) {
            LogUtil.i(TAG_WD, "onWatchDogHandleUpdateStateAndUI : in 10 minutes");
            if (beforeBrowsing()) {
                updateState(STATE_BROWSING_BWBATTLE);
                EventBus.getDefault().post(new EventInto10Minutes());
            }
        } else if (timeToStart + BattleUtils.SECOND_1 > BattleUtils.SECOND_10) {
            LogUtil.i(TAG_WD, "onWatchDogHandleUpdateStateAndUI : in 1 minutes");
            if (beforeInBattle()) {
                updateState(STATE_IN_BWBATTLE);
                waitJoinAndComputeJoinTime();
            }
        } else if (timeToStart > 0L) {
            LogUtil.i(TAG_WD, "onWatchDogHandleUpdateStateAndUI : in 10 seconds");
            if (beforeInVideo()) {
                updateState(STATE_IN_VIDEO);
                waitJoinAndComputeJoinTime();
            }
        } else if (timeToStart >
                -1 * (bwBattleDetail.getOpenDoorTime() - bwBattleDetail.getShowStartTime())) {
            LogUtil.i(TAG_WD, "onWatchDogHandleUpdateStateAndUI : before open door");
            if (beforeInVideo()) {
                updateState(STATE_IN_VIDEO);
                waitJoinAndComputeJoinTime();
            }
        } else {
            LogUtil.i(TAG_WD, "onWatchDogHandleUpdateStateAndUI : over time");
            if (beforeInVideo()) {
                updateState(STATE_IN_VIDEO);
            }
        }
    }

    private void onWatchDogHandleJoinBW() {
        final BWBattleDetail bwBattleDetail = BWBattleManager.getInstance().getBWBattleDetail();
        if (bwBattleDetail == null) {
            return;
        }
        if (!mWaitJoin) {
            LogUtil.i(TAG_WD, "onWatchDogHandleJoinBW() : not wait join, return.");
            return;
        }
        if (System.currentTimeMillis() >= mWaitJoinTime) {
            LogUtil.i(TAG_WD, "onWatchDogHandleJoinBW() : call join, mWaitJoinTime - "
                    + mWaitJoinTime);
            handleJoin(UserManager.getInstance().getToken(), bwBattleDetail.getBwId(),
                    bwBattleDetail.getGameId(), 1, bwBattleDetail.getGameUrl());
            mWaitJoin = false;
        }
    }

    private void onWatchDogHandleMatch() {
        final BWBattleDetail bwBattleDetail = BWBattleManager.getInstance().getBWBattleDetail();
        if (bwBattleDetail == null) {
            return;
        }
        if (mHasMatch) {
            LogUtil.i(TAG_WD, "onWatchDogHandleMatch : has match, return");
            return;
        }

        if (mWatchDogMatch) {
            LogUtil.i(TAG_WD, "onWatchDogHandleMatch : has try watch dog match, return");
            return;
        }

        final long realStartGameTime = bwBattleDetail.getClientTime() +
                (bwBattleDetail.getRealStartTime() - bwBattleDetail.getServerTime());
        final int seconds = 12;
        if (System.currentTimeMillis() + BattleUtils.SECOND_1 * seconds >= realStartGameTime) {
            LogUtil.i(TAG_WD, "onWatchDogHandleMatch : match over time, load match");
            mWatchDogMatch = true;
            loadBWMatchResult();
        }
    }

    private void onWatchDogHandleGameStart() {
        final BWBattleDetail bwBattleDetail = BWBattleManager.getInstance().getBWBattleDetail();
        if (bwBattleDetail == null) {
            return;
        }
        final long realStartGameTime = bwBattleDetail.getClientTime() +
                (bwBattleDetail.getRealStartTime() - bwBattleDetail.getServerTime());
        // 提前时间
        final int seconds = 2;
        if (System.currentTimeMillis() + BattleUtils.SECOND_1 * seconds >= realStartGameTime) {
            if (mHasMatch) {
                LogUtil.i(TAG_WD, "onWatchDogHandleGameStart() : has match, goto match screen and finish");
                BattleUtils.startMatch(this);
                finish();
            } else {
                // 到此时仍然没有匹配信息，退出
                LogUtil.i(TAG_WD, "onWatchDogHandleGameStart() : has not match, goto quit");
                handleCommonFail();
            }
        }
    }
    

    private static class WatchDog extends Handler {

        private static final int MSG_WATCH_DOG = 1000;
        private static final long TIME_WATCH_DOG = 1000L;

        private WeakReference<BWBattleActivity> activityRef;

        public WatchDog(BWBattleActivity bwBattleActivity) {
            activityRef = new WeakReference<BWBattleActivity>(bwBattleActivity);
        }

        public void start() {
            // run onWatchDog firstly
            doOnce();
        }

        public void stop() {
            removeMessages(MSG_WATCH_DOG);
        }

        @Override
        public void handleMessage(Message msg) {
            doOnce();
        }

        private void doOnce() {
            if (activityRef == null) {
                return;
            }
            final BWBattleActivity activity = activityRef.get();
            if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                return;
            }

            activity.onWatchDog();
            sendEmptyMessageDelayed(MSG_WATCH_DOG, TIME_WATCH_DOG);
        }
    }

    public static final class EventBWBattleDetailLoaded {}
    public static final class EventInto10Minutes {}

    public static final class EventUpdateOnlines {
        private long numbers;

        public EventUpdateOnlines(long numbers) {
            this.numbers = numbers;
        }

        public long getNumbers() {
            return numbers;
        }
    }
}
