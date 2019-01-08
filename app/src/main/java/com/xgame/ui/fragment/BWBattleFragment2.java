package com.xgame.ui.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.xgame.R;
import com.xgame.battle.BWBattleManager;
import com.xgame.battle.model.BWBattleDetail;
import com.xgame.common.util.LogUtil;
import com.xgame.ui.activity.BWBattleActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by zhanglianyu on 18-1-28.
 */

public class BWBattleFragment2 extends Fragment {


    @BindView(R.id.txt_timer)
    TextView mTimer;

    @BindView(R.id.txt_online_num)
    TextView mOnlineNum;

    @BindView(R.id.view_video)
    SimpleExoPlayerView mPlayerView;

    @BindView(R.id.txt_des)
    TextView mDes;

    private static final String TAG = "BWBattleFragment2";
    private Unbinder mUnbinder;
    private CountDownTimer mCountDownTimer;

    private SimpleExoPlayer mPlayer;

    private long mPlaybackPosition;
    private int mCurrentWindow;
    private boolean mPlayWhenReady = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bwbattle2, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        initView();
        initAndStartTimer();
        return view;
    }

    private void initView() {
        final BWBattleDetail bwBattleDetail = BWBattleManager.getInstance().getBWBattleDetail();
        if (bwBattleDetail != null) {
            mDes.setText(bwBattleDetail.getSubTitle());
        }
    }

    private void updateOnlineNum(final long num) {
        if (num > 0) {
            mOnlineNum.setText(getContext().getString(R.string.bw_battle_online_num, num));
        } else {
            mOnlineNum.setText("");
        }
    }

    private void initAndStartTimer() {

        final FragmentActivity activity = getActivity();
        if (activity == null || !(activity instanceof BWBattleActivity)) {
            mTimer.setText(R.string.bw_battle_count_down_0);
            return;
        }

        final long timeToCountDown = ((BWBattleActivity) activity).getTimeToStart();
        if (timeToCountDown <= 0L) {
            mTimer.setText(R.string.bw_battle_count_down_0);
            return;
        }

        mCountDownTimer = new CountDownTimer(timeToCountDown, 1000L) {
            @Override
            public void onTick(long l) {
                if (mTimer == null) {
                    return;
                }
                long seconds = l / 1000;
                final long minutes = seconds / 60;
                seconds = seconds - 60 * minutes;

                if (minutes == 0 && seconds <= 10) {
                    mTimer.setText("00:10");
                    return;
                }

                final String mStr = minutes < 10 ? "0" + minutes : String.valueOf(minutes);
                final String sStr = seconds < 10 ? "0" + seconds : String.valueOf(seconds);
                mTimer.setText(String.format("%s:%s", mStr, sStr));
            }

            @Override
            public void onFinish() {
                LogUtil.i(TAG, "finish");
            }
        };
        mCountDownTimer.start();
    }

    private void cancelTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
    }

    private void playBackgroundSound() {
        final FragmentActivity activity = getActivity();
        if (activity != null && activity instanceof BWBattleActivity) {
            ((BWBattleActivity) activity).playBackgroundSound();
        }
    }

    private void stopBackgroundSound() {
        final FragmentActivity activity = getActivity();
        if (activity != null && activity instanceof BWBattleActivity) {
            ((BWBattleActivity) activity).stopBackgroundSound();
        }
    }

    @Override
    public void onDestroyView() {
        cancelTimer();
        mUnbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        if ((Util.SDK_INT <= 23 || mPlayer == null)) {
            initializePlayer();
        }
        playBackgroundSound();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
        stopBackgroundSound();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateOnlineNum(BWBattleActivity.EventUpdateOnlines
                                        eventUpdateOnlines) {
        LogUtil.i(TAG, "onUpdateOnlineNum() : " + eventUpdateOnlines.getNumbers());
        updateOnlineNum(eventUpdateOnlines.getNumbers());
    }

    private void initializePlayer() {
        final BWBattleDetail bwBattleDetail = BWBattleManager.getInstance().getBWBattleDetail();
        if (bwBattleDetail == null) {
            LogUtil.i(TAG, "initializePlayer() : bwBattleDetail null");
            return;
        }

        if (mPlayer == null) {
            mPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(getContext()),
                    new DefaultTrackSelector(), new DefaultLoadControl());
            mPlayerView.setKeepScreenOn(true);
            mPlayerView.setPlayer(mPlayer);
            mPlayer.addListener(new Player.DefaultEventListener() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    super.onPlayerStateChanged(playWhenReady, playbackState);
                    LogUtil.i(TAG, "onPlayerStateChanged() : playWhenReady - " + playWhenReady
                            + " , playbackState - " + playbackState);
                    if (playWhenReady) {
                        mPlayerView.hideController();
                    } else {
                        mPlayerView.showController();
                    }
                    mPlayWhenReady = playWhenReady;
                    if (playbackState == Player.STATE_ENDED) {
                        mPlayer.seekToDefaultPosition();
                        mPlayer.setPlayWhenReady(false);
                    }
                }
            });
            mPlayer.setPlayWhenReady(mPlayWhenReady);
            mPlayer.seekTo(mCurrentWindow, mPlaybackPosition);
        }
        String url = bwBattleDetail.getVideoRule();
        MediaSource mediaSource = buildMediaSource(Uri.parse(url));
        mPlayer.prepare(mediaSource, true, false);
        mPlayerView.showController();
    }

    private void releasePlayer() {
        if (mPlayer != null) {
            mPlaybackPosition = mPlayer.getCurrentPosition();
            mCurrentWindow = mPlayer.getCurrentWindowIndex();
            mPlayWhenReady = mPlayer.getPlayWhenReady();
            mPlayer.release();
            mPlayer = null;
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource.Factory(new DefaultHttpDataSourceFactory("exoplayer-codelab"))
                .createMediaSource(uri);
    }
}
