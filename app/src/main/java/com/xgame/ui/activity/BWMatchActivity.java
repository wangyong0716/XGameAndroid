package com.xgame.ui.activity;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.xgame.R;
import com.xgame.account.UserManager;
import com.xgame.account.model.User;
import com.xgame.base.ServiceFactory;
import com.xgame.battle.BWBattleManager;
import com.xgame.battle.BattleUtils;
import com.xgame.battle.model.BWBattleDetail;
import com.xgame.battle.model.BWBattleMatchResult;
import com.xgame.battle.model.BWBattlePlayer;
import com.xgame.battle.model.BWBattleUserInfo;
import com.xgame.battle.model.Player;
import com.xgame.common.api.OnCallback;
import com.xgame.common.os.WeakHandler;
import com.xgame.common.util.LogUtil;
import com.xgame.common.util.StatusBarUtil;
import com.xgame.common.util.ToastUtil;
import com.xgame.ui.Router;
import com.xgame.ui.view.MatchLoadingView;
import com.xgame.ui.view.MatchView;
import com.xgame.util.dialog.BaiWanAlertDialog;
import com.xgame.util.sign.SignToolUtil;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangyong on 18-1-31.
 */

public class BWMatchActivity extends BaseActivity {
    private static final String TAG = BWMatchActivity.class.getSimpleName();
    private static final int SOUND_BACKGROUND_1 = 1;
    private int COUNT_DOWN_MAX = 10;
    private int COUNT_STAY_NUM = 2;
    private int COUNT_REQUEST_PARTNER_INFO = 5;
    private MatchView mMatchView;
    private TextView mCountDownView;
    private TextView mGameName;
    private BWBattleMatchResult mMatchResult;
    private BWBattleDetail mBattleDetail;
    private BWBattlePlayer mPeerInfo;
    private boolean shouldShowPeerInfo = false;
    private User mUser;
    private TextView mOnlineView;
    private MatchLoadingView mLoadingView;
    private MySoundPool mSoundPool;
    private CountTimer mCountTimer;
    private long mCurrentRound;
    private boolean cancelAble = true;

    private static final int FINISH = 1;
    private WeakHandler mHandler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == FINISH) {
                leave();
            }
            return false;
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bw_match);
        mMatchView = findViewById(R.id.match_view);
        mCountDownView = findViewById(R.id.count_down);
        mOnlineView = findViewById(R.id.online_num);
        mGameName = findViewById(R.id.game_name);
        mLoadingView = findViewById(R.id.loading_view);
        mMatchView.setLoadingMode();
        mMatchResult = BWBattleManager.getInstance().getBWBattleMatchResult();
        mBattleDetail = BWBattleManager.getInstance().getBWBattleDetail();
        mCurrentRound = BWBattleManager.getInstance().getBattleRound() + 1;
        if (mBattleDetail == null || mMatchResult == null) {
            LogUtil.i(TAG, "wrong status -> battleDetail = " + mBattleDetail + ", matchResult = " + mMatchResult);
            handleException("game info lost!", 1000);
            return;
        }
        mUser = UserManager.getInstance().getUser();

        LogUtil.i(TAG, "onCreate -> matchResult = " + mMatchResult);
        startCountDown();
//        initSoundPool();
    }

    private void startCountDown() {
        int countDownDuration = getCountDownDuration(mMatchResult.getCurrentRoundStart(),
                mMatchResult.getServerTime(), mMatchResult.getClientTime());
        countDownDuration = countDownDuration > 1000 ? countDownDuration : 1000;
        printTimeLine((int) mMatchResult.getRoundId(), mMatchResult.getServerTime(), mMatchResult.getClientTime(),
                mMatchResult.getCurrentRoundStart(), mMatchResult.getNextRoundMatchOverTime(),
                mMatchResult.getNextRoundMatchOverTime() - 10000, countDownDuration);
        mMatchView.setUserInfo(mUser);
        mGameName.setText(getString(R.string.bw_interval_round, mMatchResult.getRoundId(), mBattleDetail.getGameTotalRoundNum(), mMatchResult.getOnline()));

        if (countDownDuration > (COUNT_DOWN_MAX + COUNT_STAY_NUM) * 1000) {
            mCountDownView.setVisibility(View.GONE);
            mLoadingView.setVisibility(View.VISIBLE);
            findViewById(R.id.match_succeed).setVisibility(View.GONE);
        } else if (countDownDuration >= COUNT_STAY_NUM * 1000) {
            mMatchView.setLoadingMode();
            mLoadingView.setVisibility(View.GONE);
            mCountDownView.setVisibility(View.VISIBLE);
            findViewById(R.id.match_succeed).setVisibility(View.GONE);
            updateCountView(countDownDuration / 1000 - COUNT_STAY_NUM);
        } else {
            showPeerInfo();
        }

        if (countDownDuration < COUNT_REQUEST_PARTNER_INFO * 1000) {
            requestPartnerInfo();
        }

        mCountTimer = new CountTimer(this, countDownDuration, 1000);
        mCountTimer.start();
    }

    public void handleTick(int tick) {
        if (tick > COUNT_DOWN_MAX + COUNT_STAY_NUM || tick < COUNT_STAY_NUM) {
            return;
        }

        if (tick == COUNT_DOWN_MAX + COUNT_STAY_NUM) {
            mMatchView.setLoadingMode();
            mLoadingView.setVisibility(View.GONE);
            mCountDownView.setVisibility(View.VISIBLE);
            findViewById(R.id.match_succeed).setVisibility(View.GONE);
        }

        if (tick == COUNT_REQUEST_PARTNER_INFO) {
            requestPartnerInfo();
        }

        if (tick >= COUNT_STAY_NUM) {
            updateCountView(tick - COUNT_STAY_NUM);
        }

        if (tick == COUNT_STAY_NUM) {
            showPeerInfo();
        }
    }

    private void finishTick() {
        LogUtil.i(TAG, "countDown finished -> gotoBattle");
        consumeMatch();
        BWBattleManager.getInstance().setBattleRound(mMatchResult.getRoundId());
        BattleUtils.gotoBWBattle(BWMatchActivity.this, UserManager.getInstance().getToken());
        BWMatchActivity.this.finish();
    }

    private void showPeerInfo() {
        if (mPeerInfo == null) {
            shouldShowPeerInfo = true;
            return;
        }
        shouldShowPeerInfo = false;
        mMatchView.setPeerInfo(getPlayer(mPeerInfo));
        mCountDownView.setVisibility(View.GONE);
        findViewById(R.id.match_succeed).setVisibility(View.VISIBLE);
    }

    private Player getPlayer(BWBattlePlayer bwBattlePlayer) {
        Player player = new Player();
        player.setAvatar(bwBattlePlayer.getAvatar());
        player.setName(bwBattlePlayer.getName());
        player.setAge(bwBattlePlayer.getAge());
        player.setGender(bwBattlePlayer.getSex());
        return player;
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setTransparent(this);
        StatusBarUtil.setMiuiStatusBarDarkMode(this, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        releaseSoundPool();
        if (mCountTimer != null) {
            mCountTimer.cancel();
            mCountTimer = null;
        }
    }

    @Override
    public void onBackPressed() {
        BaiWanAlertDialog.Builder builder = new BaiWanAlertDialog.Builder(this);
        builder.setMessage(getString(R.string.battle_back_title_bw));
        builder.setPositiveButton(R.string.battle_back_yes, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cancelAble) {
                    cancelMatch();
                }
                mHandler.removeMessages(FINISH);
                BWBattleManager.getInstance().clearAll();
                BWMatchActivity.super.onBackPressed();
            }
        });
        builder.setNegativeButton(R.string.battle_back_no, null);
        builder.create().show();
    }

    private void initSoundPool() {
        if (mSoundPool == null) {
            mSoundPool = new MySoundPool(this, 1, AudioManager.STREAM_MUSIC, 0);
            mSoundPool.init();
        }
    }

    private void releaseSoundPool() {
        if (mSoundPool != null) {
            mSoundPool.release();
            mSoundPool = null;
        }
    }

    private void printTimeLine(int round, long serverTime, long receiveTime,
                               long currentRoundStartTime, long nextRoundStartTime,
                               long nextRoundRequestTime, long waitBattleDuration) {
        LogUtil.i("DEBUG_TIME_LINE", "matching in round " + round);
        printTimeLine("ST", serverTime);
        printTimeLine("RT", receiveTime);
        printTimeLine("CRST", currentRoundStartTime);
        printTimeLine("NRST", nextRoundStartTime);
        printTimeLine("NRRT", nextRoundRequestTime);
        printTimeLine("WBD", waitBattleDuration);
    }

    private void printTimeLine(String pre, long time) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        LogUtil.i("DEBUG_TIME_LINE", pre + " : " + format.format(time));
    }

    private int getCountDownDuration(long startTime, long serverTime, long acceptTime) {
        printTime("startTime", startTime);
        printTime("serverTime", serverTime);
        printTime("acceptTime", acceptTime);
        printTime("clientTime", System.currentTimeMillis());
        return (int) (startTime - serverTime - System.currentTimeMillis() + acceptTime);
    }

    private void printTime(String pre, long time) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        LogUtil.i(TAG, pre + ": " + format.format(time));
    }

    private void updateCountView(int second) {
        mCountDownView.setText(second + "");
    }

    private void updateOnlineNum(final long num) {
        mOnlineView.setText(getString(R.string.bw_battle_online_num, num >= 0 ? num : 0));
    }

    private void leave() {
        Router.toHome();
        BWMatchActivity.this.finish();
    }

    private static class MySoundPool extends SoundPool {
        private WeakReference<BWMatchActivity> mWeakReference;

        public MySoundPool(BWMatchActivity activity, int maxStreams, int streamType, int srcQuality) {
            super(maxStreams, streamType, srcQuality);
            mWeakReference = new WeakReference<BWMatchActivity>(activity);
        }

        private void init() {
            BWMatchActivity bwMatchActivity = mWeakReference.get();
            if (bwMatchActivity == null) {
                release();
                return;
            }
            load(bwMatchActivity, R.raw.audio_waiting_match, SOUND_BACKGROUND_1);
            setOnLoadCompleteListener(new OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                    BWMatchActivity bwMatchActivity = mWeakReference.get();
                    if (bwMatchActivity == null) {
                        release();
                    } else {
                        play(i, 1f, 1f, 1, -1, 1f);
                    }
                }
            });
        }
    }

    /**
     * 当前保存的matchResult是否是有效的，当轮的则有效，上一轮的则需要重新请求
     *
     * @return
     */
    private boolean isMatchResultValid() {
        return mMatchResult.getRoundId() == mCurrentRound;
    }

    public void handleException(String message, int delay) {
        if (!TextUtils.isEmpty(message)) {
            ToastUtil.showToast(this, message);
        }
        if (delay > 0) {
            mHandler.removeMessages(FINISH);
            mHandler.sendEmptyMessageDelayed(FINISH, delay);
        } else {
            leave();
        }
    }

    private void cancelMatch() {
        LogUtil.i(TAG, "cancelMatch -> bwId = " + mBattleDetail.getBwId() + ", gameId = "
                + mBattleDetail.getGameId() + ", roundId = " + mCurrentRound);
        ServiceFactory.battleService().cancelBWBattle(mBattleDetail.getBwId(), mBattleDetail.getGameId(), mCurrentRound).enqueue(new OnCallback<Void>() {
            @Override
            public void onResponse(Void result) {
                LogUtil.i(TAG, "cancelMatch -> onResponse");
            }

            @Override
            public void onFailure(Void result) {
                LogUtil.i(TAG, "cancelMatch -> onFailure");
            }
        });
    }

    private void consumeMatch() {
        LogUtil.i(TAG, "consumeMatch -> bwId = " + mBattleDetail.getBwId() + ", gameId = "
                + mBattleDetail.getGameId() + ", roundId = " + mCurrentRound);
        ServiceFactory.battleService().startBWBattle(mBattleDetail.getBwId(), mBattleDetail.getGameId(), mCurrentRound).enqueue(new OnCallback<Void>() {
            @Override
            public void onResponse(Void result) {
                LogUtil.i(TAG, "consumeMatch -> onResponse");
            }

            @Override
            public void onFailure(Void result) {
                LogUtil.i(TAG, "consumeMatch -> onFailure");
            }
        });
    }

    public String generateSign() {
        Map<String, String> params = new HashMap<>();
        params.put("roomId", mMatchResult.getRoomId());
        params.put("token", UserManager.getInstance().getBailuToken());
        params.put("appSecret", SignToolUtil.getAppSecret());
        return SignToolUtil.sign2(params);
    }

    private void requestPartnerInfo() {
        cancelAble = false;
        LogUtil.i(TAG, "requestPartnerInfo -> roomId = " + mMatchResult.getRoomId() + ", round = " + mCurrentRound);
        ServiceFactory.battleService().getPartnerInfo(mMatchResult.getRoomId(), generateSign(), UserManager.getInstance().getBailuToken()).enqueue(new OnCallback<BWBattleUserInfo>() {
            @Override
            public void onResponse(BWBattleUserInfo result) {
                LogUtil.i(TAG, "requestPartnerInfo -> onResponse result = " + result);
                if (result == null || result.getList() == null || result.getList().length < 1) {
                    return;
                }
                for (int i = 0; i < result.getList().length; i++) {
                    if (result.getList()[i].getuId() != result.getuId()) {
                        mPeerInfo = result.getList()[i];
                        break;
                    }
                }
                if (shouldShowPeerInfo) {
                    showPeerInfo();
                }
            }

            @Override
            public void onFailure(BWBattleUserInfo result) {
                LogUtil.i(TAG, "requestPartnerInfo -> onFailure result = " + result);
            }
        });
    }

    public static class CountTimer extends CountDownTimer {
        private WeakReference<BWMatchActivity> mWeakReference;

        public CountTimer(BWMatchActivity activity, long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            mWeakReference = new WeakReference<BWMatchActivity>(activity);
        }

        @Override
        public void onTick(long l) {
            BWMatchActivity activity = mWeakReference.get();
            if (activity != null) {
                activity.handleTick((int) l / 1000);
            } else {
                cancel();
            }
        }

        @Override
        public void onFinish() {
            BWMatchActivity activity = mWeakReference.get();
            if (activity != null) {
                activity.finishTick();
            } else {
                cancel();
            }
        }
    }
}
