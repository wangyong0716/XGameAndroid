package com.xgame.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xgame.R;
import com.xgame.account.UserManager;
import com.xgame.account.model.User;
import com.xgame.app.GlideApp;
import com.xgame.base.ServiceFactory;
import com.xgame.battle.BWBattleManager;
import com.xgame.battle.BattleManager;
import com.xgame.battle.model.BWBattleBonusResult;
import com.xgame.battle.model.BWBattleDetail;
import com.xgame.battle.model.BWBattleMatchResult;
import com.xgame.common.api.OnCallback;
import com.xgame.common.os.WeakHandler;
import com.xgame.common.util.GlobalGson;
import com.xgame.common.util.LogUtil;
import com.xgame.push.event.BWBattleMatchResultEvent;
import com.xgame.push.event.BWBonusEvent;
import com.xgame.ui.activity.BWBattleResultActivity;
import com.xgame.ui.view.BattleProgressBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by wangyong on 18-2-7.
 */

public class BWIntervalFragment extends Fragment implements BWBattleResultActivity.FragmentBackHandler {
    private static final String TAG = "BWIntervalFragment";
    private static int PROGRESS_PRELOADING_DURATION = 27000;
    private static int PROGRESS_FINISH_DURATION = 3000;
    private static final int REQUEST_MATCH = 1;
    private static final int REQUEST_BONUS = 2;
    private static final int GOTO_MATCH = 3;
    private static final int GOTO_BONUS = 4;
    private static final int MAX_REQUEST_COUNT = 3;

    private View mRoot;
    private BWBattleMatchResult mMatchResult;
    private BWBattleDetail mBattleDetail;

    @BindView(R.id.avatar)
    CircleImageView mAvatar;
    @BindView(R.id.result_title)
    TextView mTitle;
    @BindView(R.id.result_des)
    TextView mDesc;
    @BindView(R.id.progress_bar)
    BattleProgressBar mProgressBar;

    private Unbinder mUnbinder;

    private long mLastRound;
    private int mTotalRound;

    private boolean mHasMatch = false;
    private boolean mHasBonus = false;

    private static final int REQUEST_ADVANCED_DURATION = 10000;
    private static final int REQUEST_INTERVAL = 5000;
    private static final int MATCH_DISPLAY = 7000;

    private int mRequestCount = 0;

    private WeakHandler mHandler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == REQUEST_MATCH) {
                requestMatch();
            } else if (msg.what == GOTO_MATCH) {
                next(BWBattleResultActivity.INTERVAL_STATUS.MATCHED);
            } else if (msg.what == REQUEST_BONUS) {
                if (mRequestCount < MAX_REQUEST_COUNT) {
                    requestBonus(false);
                    mHandler.sendEmptyMessageDelayed(REQUEST_BONUS, REQUEST_INTERVAL);
                } else {
                    requestBonus(true);
                }
            } else if (msg.what == GOTO_BONUS) {
                next(BWBattleResultActivity.INTERVAL_STATUS.WIN);
            }
            return false;
        }
    });


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_bw_interval, container, false);
        mUnbinder = ButterKnife.bind(this, mRoot);

        return mRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBattleDetail = BWBattleManager.getInstance().getBWBattleDetail();
        mMatchResult = BWBattleManager.getInstance().getBWBattleMatchResult();
        if (mBattleDetail == null || mMatchResult == null) {
            LogUtil.i(TAG, "wrong status -> battleDetail = " + mBattleDetail + ", matchResult = " + mMatchResult);
            leave();
            return;
        }
        mLastRound = mMatchResult.getRoundId();
        mTotalRound = mBattleDetail.getGameTotalRoundNum();

        LogUtil.i(TAG, "onViewCreated -> currentRound = " + (mLastRound + 1) + ", totalRound = " + mTotalRound);
        User user = UserManager.getInstance().getUser();
        GlideApp.with(this).load(user.getHeadimgurl()).placeholder(R.drawable.default_avatar)
                .into(mAvatar);

        long wait;
        if (mLastRound < mTotalRound) {
            wait = getMaxWaitingTime(mMatchResult.getNextRoundMatchOverTime(),
                    mMatchResult.getServerTime(), mMatchResult.getClientTime(), REQUEST_ADVANCED_DURATION);
            wait = wait > 1000 ? wait : 1000;
            mTitle.setText(R.string.bw_round_congratulations);
            mDesc.setText(R.string.bw_round_waiting_prompt);
            mHandler.sendEmptyMessageDelayed(REQUEST_MATCH, wait);
        } else {
            wait = getMaxWaitingTime(mMatchResult.getNextRoundMatchOverTime(),
                    mMatchResult.getServerTime(), mMatchResult.getClientTime(), 0);
            wait = wait > 1000 ? wait : 1000;
            mTitle.setText(R.string.bw_round_win);
            mDesc.setText(R.string.bw_round_win_prompt);
            mHandler.sendEmptyMessageDelayed(REQUEST_BONUS, wait);
        }
        mProgressBar.startProgress(PROGRESS_PRELOADING_DURATION, 90);
        printTimeLine((int) mMatchResult.getRoundId() + 1, mMatchResult.getNextRoundMatchOverTime(), wait);
        playSound(BWBattleResultActivity.AUDIO_WAITING_MATCH);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onBackPressed() {
        alertExit();
        return true;
    }

    @Override
    public void exit() {
        mHandler.removeMessages(REQUEST_MATCH);
        mHandler.removeMessages(GOTO_MATCH);
        mHandler.removeMessages(REQUEST_BONUS);
        mHandler.removeMessages(GOTO_BONUS);
    }

    private long getMaxWaitingTime(long startTime, long serverTime, long acceptTime, long clientObligate) {
        printTime("startTime", startTime);
        printTime("serverTime", serverTime);
        printTime("acceptTime", acceptTime);
        printTime("clientTime", System.currentTimeMillis());
        return startTime - serverTime - System.currentTimeMillis() + acceptTime - clientObligate;
    }

    private void printTime(String pre, long time) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        LogUtil.i("DEBUG_TIME_LINE", pre + ": " + format.format(time));
    }

    private void printTimeLine(int round, long requestMatchTime, long wait) {
        LogUtil.i("DEBUG_TIME_LINE", "request match for round " + round);
        printTimeLine("RMT", requestMatchTime);
        LogUtil.i("DEBUG_TIME_LINE", "should wait for " + wait + " seconds");
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
        if (event != null) {
            final String content = event.getContent();
            LogUtil.i(TAG, "onBWBattleMatchPush() content : " + content);
            final BWBattleMatchResult result = GlobalGson.get().fromJson(content,
                    BWBattleMatchResult.class);
            handleMatchResult(result, false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBWBonusPush(BWBonusEvent event) {
        if (event == null) {
            return;
        }
        LogUtil.i(TAG, "onBWBonusPush() : " + event);
        BWBattleBonusResult bonusResult = GlobalGson.get().fromJson(event.getContent(),
                BWBattleBonusResult.class);
        handleBonus(bonusResult, false);
    }

    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
        super.onDestroyView();
        stopSound();
    }

    public void requestMatch() {
        LogUtil.i(TAG, "requestMatch -> bwId = " + mBattleDetail.getBwId() + ", gameId = "
                + mMatchResult.getGameId() + ", round = " + (mLastRound + 1));
        ServiceFactory.battleService().getBWBattleMatchResult(UserManager.getInstance().getToken(),
                mBattleDetail.getBwId(), mMatchResult.getGameId(), mLastRound + 1)
                .enqueue(new OnCallback<BWBattleMatchResult>() {
                    @Override
                    public void onResponse(BWBattleMatchResult result) {
                        LogUtil.i(TAG, "requestMatch onResponse -> result = " + result);
                        handleMatchResult(result, true);
                    }

                    @Override
                    public void onFailure(BWBattleMatchResult result) {
                        LogUtil.i(TAG, "requestMatch onFailure -> result = " + result);
                        handleException(getString(R.string.match_no_peer), 1000);
                    }
                });
    }

    private void handleMatchResult(BWBattleMatchResult result, boolean lastTry) {
        if (mHasMatch) {
            return;
        }
        if (result == null) {
            if (lastTry) {
                handleException(getString(R.string.match_no_peer), 1000);
            }
            return;
        }
        LogUtil.i(TAG, "handleMatchResult result : " + result);
        mHasMatch = true;
        mHandler.removeMessages(REQUEST_MATCH);
        result.setClientTime(System.currentTimeMillis());
        BWBattleManager.getInstance().setBWBattleMatchResult(result);
        BattleManager.getInstance().setRoomId(result.getRoomId());
        BattleManager.getInstance().setGameUrl(mBattleDetail.getGameUrl());
        long wait = getMaxWaitingTime(result.getCurrentRoundStart(),
                result.getServerTime(), result.getClientTime(), MATCH_DISPLAY);
        wait = wait > 1000 ? wait : 1000;
        mProgressBar.continueProgress(wait);
        mHandler.sendEmptyMessageDelayed(GOTO_MATCH, wait);
    }

    private void requestBonus(final boolean isLastTry) {
        mRequestCount++;
        LogUtil.i(TAG, "bwId = " + mBattleDetail.getBwId() + ", gameId = " + mBattleDetail.getGameId());
        ServiceFactory.battleService().getBWBattleBonus(mBattleDetail.getBwId(), mBattleDetail.getGameId()).enqueue(new OnCallback<BWBattleBonusResult>() {
            @Override
            public void onResponse(BWBattleBonusResult result) {
                LogUtil.i(TAG, "requestBonus onResponse -> result = " + result);
                handleBonus(result, true);
            }

            @Override
            public void onFailure(BWBattleBonusResult result) {
                LogUtil.i(TAG, "requestBonus onFailure -> result = " + result);
                if (isLastTry) {
                    mHasBonus = true;
                    handleException(getString(R.string.bw_request_bonus_failed), 1000);
                }
            }
        });
    }

    private void handleBonus(BWBattleBonusResult bonus, boolean isLastTry) {
        if (mHasBonus) {
            return;
        }
        if (bonus == null && isLastTry) {
            mHasBonus = true;
            handleException(getString(R.string.bw_request_bonus_failed), 1000);
            return;
        }
        mHasBonus = true;
        mHandler.removeMessages(REQUEST_BONUS);
        BWBattleManager.getInstance().setBonus(bonus);
        mProgressBar.continueProgress(PROGRESS_FINISH_DURATION);
        mHandler.sendEmptyMessageDelayed(GOTO_BONUS, PROGRESS_FINISH_DURATION);
    }

    private void handleException(String message, int delay) {
        if (getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed() && isAdded()) {
            ((BWBattleResultActivity) getActivity()).handleException(message, delay);
        }
    }

    private void next(BWBattleResultActivity.INTERVAL_STATUS status) {
        LogUtil.i(TAG, "next -> currentRound = " + (mLastRound + 1) + ", totalRound = " + mTotalRound);
        stopSound();
        if (getActivity() != null && !getActivity().isFinishing() && isAdded()) {
            ((BWBattleResultActivity) getActivity()).next(status);
        }
    }

    private void leave() {
        mHandler.removeMessages(GOTO_MATCH);
        mHandler.removeMessages(GOTO_BONUS);
        stopSound();
        if (isActivityActive()) {
            ((BWBattleResultActivity) getActivity()).leave();
        }
    }

    private void playSound(int sound) {
        if (isActivityActive()) {
            ((BWBattleResultActivity) getActivity()).playBackgroundSound(sound);
        }
    }

    private void stopSound() {
        if (isActivityActive()) {
            ((BWBattleResultActivity) getActivity()).stopBackgroundSound();
        }
    }

    private void alertExit() {
        if (isActivityActive()) {
            ((BWBattleResultActivity) getActivity()).alertExit();
        }
    }

    private boolean isActivityActive() {
        return getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed() && isAdded();
    }
}
