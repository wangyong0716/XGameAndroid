package com.xgame.ui.fragment;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.xgame.R;
import com.xgame.base.ServiceFactory;
import com.xgame.battle.BWBattleManager;
import com.xgame.battle.BattleConstants;
import com.xgame.battle.model.BWBattleDetail;
import com.xgame.battle.model.BWBattleMatchResult;
import com.xgame.battle.model.BWBattleReviveResult;
import com.xgame.common.api.OnCallback;
import com.xgame.common.util.LogUtil;
import com.xgame.ui.activity.BWBattleResultActivity;
import com.xgame.util.Analytics;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by wangyong on 18-2-7.
 */

public class BWReviveFragment extends Fragment implements BWBattleResultActivity.FragmentBackHandler {
    private static final String TAG = "BWReviveFragment";
    private static final int MAX_WAIT_REVIVE_SECOND = 10;
    private View mRoot;
    private BWBattleMatchResult mMatchResult;
    private BWBattleDetail mBattleDetail;

    @BindView(R.id.lose_layout)
    LinearLayout mLoseLayout;
    @BindView(R.id.lose_title)
    TextView mLoseTitle;
    @BindView(R.id.lose_btn)
    Button mLoseBtn;
    @BindView(R.id.revive_layout)
    LinearLayout mReviveLayout;
    @BindView(R.id.tv_title)
    TextView mTitle;
    @BindView(R.id.tv_msg)
    TextView mMsg;
    @BindView(R.id.btn_negative)
    Button mNegativeBtn;
    @BindView(R.id.btn_positive)
    Button mPositiveBtn;

    private Unbinder mUnbinder;

    private CountTimer mCountTimer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_revive, container, false);
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
        }
        int reviveCount = BWBattleManager.getInstance().getReviveCount();
        setLayout(reviveCount <= 0 && !isLastRound() && !isQuit(), 0);
    }

    private boolean isLastRound() {
        return mMatchResult.getRoundId() >= mBattleDetail.getGameTotalRoundNum();
    }

    private boolean isQuit() {
        return BWBattleManager.getInstance().isBWQuit();
    }

    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
        super.onDestroyView();
        if (mCountTimer != null) {
            mCountTimer.cancel();
            mCountTimer = null;
        }
        stopSound();
    }

    @Override
    public boolean onBackPressed() {
        if (mLoseLayout.getVisibility() == View.VISIBLE) {
            leave();
        } else {
            alertExit();
        }
        return true;
    }

    @Override
    public void exit() {
        if (mCountTimer != null) {
            mCountTimer.cancel();
        }
    }

    private void setLayout(boolean canRevive, int stringId) {
        if (mCountTimer != null) {
            mCountTimer.cancel();
        }
        if (canRevive) {
            mReviveLayout.setVisibility(View.VISIBLE);
            mLoseLayout.setVisibility(View.GONE);
            setReviveInfo(mBattleDetail.getRestartCoins());
            startCountDown();
            Analytics.trackPageShowEvent(Analytics.Constans.URL_TITLE_RELIVE_PAGE, Analytics.Constans.VISIT_TYPE_READY, getExtra());
        } else {
            if (mCountTimer != null) {
                mCountTimer.cancel();
            }
            mLoseLayout.setVisibility(View.VISIBLE);
            mReviveLayout.setVisibility(View.GONE);
            setLoseInfo(stringId);
            playSound(BWBattleResultActivity.AUDIO_LOSE);
        }
    }

    private void setReviveInfo(int coin) {
        mTitle.setText(R.string.bw_revive_title);
        mMsg.setText(getResources().getString(R.string.bw_revive_coin, coin));
        mPositiveBtn.setText(R.string.bw_revive_buy);
        mPositiveBtn.setOnClickListener(mLister);
        mNegativeBtn.setText(R.string.bw_revive_negative);
        mNegativeBtn.setOnClickListener(mLister);
    }

    private void setLoseInfo(int stringId) {
        if (stringId <= 0) {
            stringId = R.string.bw_lose;
        }
        mLoseTitle.setText(stringId);
        mLoseBtn.setOnClickListener(mLister);
    }

    private void startCountDown() {
        mCountTimer = new CountTimer(this, MAX_WAIT_REVIVE_SECOND * 1000, 1000);
        mCountTimer.start();
    }

    public void handleTick(int tick) {
        mPositiveBtn.setText(getCountString(tick));
    }

    public void finishTick() {
        setLayout(false, 0);
    }

    protected String getCountString(int countSecond) {
        if (getContext() == null) {
            return null;
        }
        return getContext().getString(R.string.bw_revive_positive, countSecond);
    }

    private void requestRevive() {
        LogUtil.i(TAG, "requestRevive -> gameId = " + mMatchResult.getGameId()
                + ", bwId = " + mBattleDetail.getBwId() + ", roundId = " + mMatchResult.getRoundId());
        ServiceFactory.battleService().requestRevive(mMatchResult.getGameId(),
                mBattleDetail.getBwId(), mMatchResult.getRoundId()).enqueue(new OnCallback<BWBattleReviveResult>() {
            @Override
            public void onResponse(BWBattleReviveResult result) {
                LogUtil.i(TAG, "requestRevive -> onResponse = " + result);
                handleReviveResult(result);
            }

            @Override
            public void onFailure(BWBattleReviveResult result) {
                LogUtil.i(TAG, "requestRevive -> onFailure = " + result);
                setLayout(false, R.string.bw_revive_fail);
            }
        });
    }


    private void handleReviveResult(BWBattleReviveResult result) {
        if (result == null) {
            handleException(getString(R.string.bw_revive_fail), 1000);
            return;
        }
        switch (result.getStatus()) {
            case BattleConstants.BW_REVIVE_SUCCESS:
                BWBattleManager.getInstance().revive();
                next(BWBattleResultActivity.INTERVAL_STATUS.REVIVED);
                break;
            case BattleConstants.BW_REVIVE_COIN_LACK:
                handleException(getString(R.string.match_coin_lack), 1000);
                break;
            case BattleConstants.BW_REVIVE_COUNT_LIMITATION:
                handleException(getString(R.string.bw_revive_count_limitation), 1000);
                break;
            case BattleConstants.BW_REVIVE_ROUND_LIMITATION:
                handleException(getString(R.string.bw_revive_round_limitation), 1000);
                break;
            default:
                setLayout(false, R.string.bw_revive_fail);
                break;
        }
    }

    private View.OnClickListener mLister = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_positive:
                    mCountTimer.cancel();
                    mPositiveBtn.setEnabled(false);
                    mNegativeBtn.setEnabled(false);
                    requestRevive();
                    //百万对战购买复活卡弹窗购买点击
                    Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_CLICK, Analytics.Constans.STOCK_ID_BW_PURCHASE,
                            Analytics.Constans.STOCK_NAME_BW_PURCHASE, Analytics.Constans.STOCK_TYPE_BTN,
                            Analytics.Constans.PAGE_BW_LIVE, Analytics.Constans.SECTION_BW_BUY_REVIVE, getExtra());
                    break;
                case R.id.btn_negative:
                    setLayout(false, 0);
                    break;
                case R.id.lose_btn:
                    stopSound();
                    leave();
                    break;
                default:
                    break;
            }
        }
    };

    private void handleException(String message, int delay) {
        if (isActivityActive()) {
            ((BWBattleResultActivity) getActivity()).handleException(message, delay);
        }
    }

    private void next(BWBattleResultActivity.INTERVAL_STATUS status) {
        mCountTimer.cancel();
        stopSound();
        if (isActivityActive()) {
            ((BWBattleResultActivity) getActivity()).next(status);
        }
    }

    private void leave() {
        stopSound();
        if (isActivityActive()) {
            ((BWBattleResultActivity) getActivity()).leave();
        }
    }

    private void alertExit() {
        if (isActivityActive()) {
            ((BWBattleResultActivity) getActivity()).alertExit();
        }
    }

    private void playSound(int sound) {
        if (isActivityActive()) {
            ((BWBattleResultActivity) getActivity()).playBackgroundSound(sound, 0);
        }
    }

    private void stopSound() {
        if (isActivityActive()) {
            ((BWBattleResultActivity) getActivity()).stopBackgroundSound();
        }
    }

    private boolean isActivityActive() {
        return getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed() && isAdded();
    }

    private String getExtra() {
        JsonObject json = new JsonObject();
        if (mMatchResult != null) {
            json.addProperty("round", mMatchResult.getRoundId());
        }
        if (mBattleDetail != null) {
            json.addProperty("game_id", mBattleDetail.getGameId());
            json.addProperty("game_name", mBattleDetail.getTitle());
            json.addProperty("battle_type", Analytics.Constans.CUSTOM_GAME_TYPE_BW);
            json.addProperty("bw_id", mBattleDetail.getBwId());
        }
        return json.toString();
    }

    private static class CountTimer extends CountDownTimer {
        private WeakReference<BWReviveFragment> mWeakReference;

        public CountTimer(BWReviveFragment fragment, long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            mWeakReference = new WeakReference<BWReviveFragment>(fragment);
        }

        @Override
        public void onTick(long l) {
            BWReviveFragment fragment = mWeakReference.get();
            if (fragment != null) {
                fragment.handleTick((int) l / 1000);
            } else {
                cancel();
            }
        }

        @Override
        public void onFinish() {
            BWReviveFragment fragment = mWeakReference.get();
            if (fragment != null) {
                fragment.finishTick();
            } else {
                cancel();
            }
        }
    }
}
