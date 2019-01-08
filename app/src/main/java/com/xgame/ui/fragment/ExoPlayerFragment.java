package com.xgame.ui.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.xgame.R;
import com.xgame.common.util.LogUtil;
import com.xgame.ui.view.LoadingProgressBar;



/**
 * Created by jiangjianhe on 1/30/18.
 */

public class ExoPlayerFragment extends Fragment implements View.OnClickListener, PlaybackControlView.VisibilityListener {

    private static final String TAG = "ExoPlayer";

    private static final int TIME_PROGRESS_MAX = 200;

    private SimpleExoPlayerView mPlayerView;
    private ImageButton mReplayButton;
    private LoadingProgressBar mLoadingProgressBar;
    private TextView mMediaTitleView;
    private ProgressBar mMiniTimeProgressBar;

    private SimpleExoPlayer mPlayer;
    private View mPauseButton;

    private long mPlaybackPosition;
    private int mCurrentWindow;
    private boolean mPlayWhenReady;
    private boolean mUseController;
    private String mVideoUrl;
    private String mMediaTitle;
    private boolean mIsLiveMode;

    private final Runnable updateProgressAction = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };

    public static ExoPlayerFragment newInstance(String videoUrl) {
        return newInstance(videoUrl, false);
    }

    public static ExoPlayerFragment newInstance(String videoUrl, boolean liveMode) {
        return newInstance(videoUrl, liveMode, 0);
    }

    /**
     * 视频播放
     *
     * @param videoUrl 视频URL
     * @param liveMode 是否是直播模式，直播模式会自动开始播放且没有进度条和播放控制器
     * @param positionMs 视频从positionMs开始播放
     */
    public static ExoPlayerFragment newInstance(String videoUrl, boolean liveMode, long positionMs) {
        ExoPlayerFragment fragment = new ExoPlayerFragment();
        boolean autoPlay = liveMode ? true : false;
        boolean useController = liveMode ? false : true;
        Bundle args = new Bundle();
        args.putString("videoUrl", videoUrl);
        args.putBoolean("autoPlay", autoPlay);
        args.putBoolean("useController", useController);
        args.putLong("positionMs", positionMs);
        args.putBoolean("liveMode", liveMode);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mPlayWhenReady = args.getBoolean("autoPlay", false);
        mVideoUrl = args.getString("videoUrl");
        mUseController = args.getBoolean("useController");
        mPlaybackPosition = args.getLong("positionMs", 0);
        mMediaTitle = args.getString("title");
        mIsLiveMode = args.getBoolean("liveMode", false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mPlayerView = new SimpleExoPlayerView(getContext());
        mPlayerView.setBackgroundResource(R.drawable.bw_battle_rule_play_in_bg);
        mPauseButton = mPlayerView.findViewById(R.id.exo_pause);
        FrameLayout overlayFrameLayout = mPlayerView.getOverlayFrameLayout();
        View view = inflater.inflate(R.layout.exo_overlay_layout, overlayFrameLayout);
        mReplayButton = view.findViewById(R.id.exo_replay);
        mReplayButton.setOnClickListener(this);
        mLoadingProgressBar = view.findViewById(R.id.exo_loading_progress);
        mMiniTimeProgressBar = view.findViewById(R.id.exo_mini_time_bar);
        mMiniTimeProgressBar.setMax(TIME_PROGRESS_MAX);
        mMediaTitleView = view.findViewById(R.id.exo_media_title);
        mMediaTitleView.setText(mMediaTitle);
        view.setVisibility(mUseController ? View.VISIBLE : View.GONE);
        mPlayerView.setControllerVisibilityListener(this);
        return mPlayerView;
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
        if ((Util.SDK_INT <= 23 || mPlayer == null)) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    private void initializePlayer() {
        if (mPlayer == null) {
            mPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(getActivity()),
                    new DefaultTrackSelector(), new DefaultLoadControl());
            mPlayerView.setKeepScreenOn(true);
            mPlayerView.setPlayer(mPlayer);
            mPlayer.setPlayWhenReady(mPlayWhenReady);
            mPlayer.addListener(mEventListener);
            mPlayerView.setUseController(mUseController);
            if (mIsLiveMode) {
                mPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            }
            if (!TextUtils.isEmpty(mVideoUrl)) {
                MediaSource mediaSource = buildMediaSource(Uri.parse(mVideoUrl));
                mPlayer.prepare(mediaSource, true, false);
                mPlayer.seekTo(mCurrentWindow, mPlaybackPosition);
            }
        }
    }

    private void releasePlayer() {
        if (mPlayer != null) {
            mPlaybackPosition = mPlayer.getCurrentPosition();
            mCurrentWindow = mPlayer.getCurrentWindowIndex();
            mPlayWhenReady = mPlayer.getPlayWhenReady();
            mPlayer.removeListener(mEventListener);
            mPlayer.release();
            mPlayer = null;
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        String userAgent = Util.getUserAgent(getContext(), "XGame");
        return new ExtractorMediaSource.Factory(new DefaultHttpDataSourceFactory(userAgent))
                .createMediaSource(uri);
    }

    private Player.EventListener mEventListener = new Player.EventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            LogUtil.d(TAG, "onLoadingChanged: isLoading=" + isLoading + " percentage=" + mPlayer.getBufferedPercentage());
            mLoadingProgressBar.setProgress(mPlayer.getBufferedPercentage());
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            LogUtil.d(TAG, "onPlayerStateChanged: playWhenReady=" + playWhenReady + " playbackState=" + playbackState);
            if (playbackState == Player.STATE_BUFFERING && playWhenReady == true) {
                mLoadingProgressBar.setVisibility(View.VISIBLE);
                mPauseButton.post(new Runnable() {
                    @Override
                    public void run() {
                        mPauseButton.setVisibility(View.GONE);
                    }
                });
            } else {
                mLoadingProgressBar.setVisibility(View.GONE);
            }
            if (playbackState == Player.STATE_ENDED && mUseController) {
                mPlayerView.setUseController(false);
                mReplayButton.setVisibility(View.VISIBLE);
                mMediaTitleView.setVisibility(View.VISIBLE);
                mMiniTimeProgressBar.removeCallbacks(updateProgressAction);
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            LogUtil.d(TAG, "onPlayerError error:" + error);
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }

        @Override
        public void onSeekProcessed() {

        }
    };

    @Override
    public void onClick(View view) {
        if (view == mReplayButton) {
            if (mPlayer == null) {
                return;
            }
            mPlayer.seekTo(mCurrentWindow, 0);
            mPlayer.setPlayWhenReady(true);
            mReplayButton.setVisibility(View.GONE);
            mPlayerView.setUseController(true);
            mMediaTitleView.setVisibility(View.GONE);
            updateProgress();
        }
    }

    @Override
    public void onVisibilityChange(int visibility) {
        if (visibility == View.VISIBLE) {
            mMediaTitleView.setVisibility(View.VISIBLE);
            mMiniTimeProgressBar.removeCallbacks(updateProgressAction);
            mMiniTimeProgressBar.setVisibility(View.GONE);
        } else {
            mMediaTitleView.setVisibility(View.GONE);
            mMiniTimeProgressBar.setVisibility(View.VISIBLE);
            updateProgress();
        }
    }

    private void updateProgress() {
        if (!mUseController && mMiniTimeProgressBar.getVisibility() != View.VISIBLE) {
            return;
        }
        if (mPlayer != null) {
            long duration = mPlayer.getDuration();
            long delayMs =  duration / TIME_PROGRESS_MAX;
            if (delayMs < 100) {
                delayMs = 100;
            }
            long currentPosition = mPlayer.getCurrentPosition();
            int progress = (int) (currentPosition * TIME_PROGRESS_MAX / duration);
            mMiniTimeProgressBar.setProgress(progress);
            mMiniTimeProgressBar.postDelayed(updateProgressAction, delayMs);
        }
    }

    public void pause() {
        if (mPlayer != null) {
            mPlayer.setPlayWhenReady(false);
        }
    }

    public void play() {
        if (mPlayer != null) {
            mPlayer.setPlayWhenReady(true);
        }
    }

    public void seekTo(long positionMs) {
        seekTo(positionMs, false);
    }

    public void seekTo(long positionMs, boolean autoPlay) {
        if (mPlayer != null) {
            mPlayer.seekTo(mCurrentWindow, positionMs);
            if (autoPlay) {
                mPlayer.setPlayWhenReady(true);
            }
        }
    }

}
