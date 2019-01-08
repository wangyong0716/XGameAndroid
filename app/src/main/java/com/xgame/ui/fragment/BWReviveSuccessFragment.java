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
import android.widget.TextView;

import com.xgame.R;
import com.xgame.ui.activity.BWBattleResultActivity;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by wangyong on 18-2-7.
 */

public class BWReviveSuccessFragment extends Fragment implements BWBattleResultActivity.FragmentBackHandler {
    private static final String TAG = "BWReviveSuccessFragment";
    private static final int SUCCESS_DISPLAY_SECOND = 3;
    private View mRoot;

    @BindView(R.id.tv_title)
    TextView mTitle;
    @BindView(R.id.btn_positive)
    Button mPositiveBtn;

    private Unbinder mUnbinder;

    private CountTimer mCountTimer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_revive_success, container, false);
        mUnbinder = ButterKnife.bind(this, mRoot);
        return mRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setReviveInfo();
        startCountDown();
        playSound(BWBattleResultActivity.AUDIO_REVIVE_SUCCESS);
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
        alertExit();
        return true;
    }

    @Override
    public void exit() {
        if (mCountTimer != null) {
            mCountTimer.cancel();
        }
    }

    private void setReviveInfo() {
        mTitle.setText(R.string.bw_revive_success);
        mPositiveBtn.setText(R.string.bw_game_continue);
        mPositiveBtn.setOnClickListener(mLister);
    }

    private void startCountDown() {
        mCountTimer = new CountTimer(this, SUCCESS_DISPLAY_SECOND * 1000, 1000);
        mCountTimer.start();
    }

    public void handleTick(int tick) {
        mPositiveBtn.setText(getCountString(tick));
    }

    public void finishTick() {
        next();
    }

    protected String getCountString(int countSecond) {
        if (getContext() == null) {
            return null;
        }
        return getContext().getString(R.string.bw_game_positive, countSecond);
    }

    private View.OnClickListener mLister = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_positive:
                    next();
                    break;
                default:
                    break;
            }
        }
    };

    private void next() {
        mCountTimer.cancel();
        stopSound();
        if (isActivityActive()) {
            ((BWBattleResultActivity) getActivity()).next(BWBattleResultActivity.INTERVAL_STATUS.MATCHING);
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

    private void alertExit() {
        if (isActivityActive()) {
            ((BWBattleResultActivity) getActivity()).alertExit();
        }
    }

    private boolean isActivityActive() {
        return getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed() && isAdded();
    }

    private static class CountTimer extends CountDownTimer{
        private WeakReference<BWReviveSuccessFragment> mWeakReference;
        public CountTimer(BWReviveSuccessFragment fragment, long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            mWeakReference = new WeakReference<BWReviveSuccessFragment>(fragment);
        }

        @Override
        public void onTick(long l) {
            BWReviveSuccessFragment fragment = mWeakReference.get();
            if (fragment != null) {
                fragment.handleTick((int)l/1000);
            } else {
                cancel();
            }
        }

        @Override
        public void onFinish() {
            BWReviveSuccessFragment fragment = mWeakReference.get();
            if (fragment != null) {
                fragment.finishTick();
            } else {
                cancel();
            }
        }
    }
}
