package com.xgame.ui.activity;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import com.xgame.R;
import com.xgame.base.ServiceFactory;
import com.xgame.battle.BWBattleManager;
import com.xgame.battle.BattleManager;
import com.xgame.battle.BattleUtils;
import com.xgame.battle.model.BWBattleDetail;
import com.xgame.battle.model.BWBattleMatchResult;
import com.xgame.common.api.OnCallback;
import com.xgame.common.os.WeakHandler;
import com.xgame.common.util.LogUtil;
import com.xgame.common.util.StatusBarUtil;
import com.xgame.common.util.ToastUtil;
import com.xgame.ui.Router;
import com.xgame.ui.fragment.BWIntervalFragment;
import com.xgame.ui.fragment.BWReviveFragment;
import com.xgame.ui.fragment.BWReviveSuccessFragment;
import com.xgame.ui.fragment.BWWinFragment;
import com.xgame.util.dialog.BaiWanAlertDialog;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wangyong on 18-1-31.
 */

public class BWBattleResultActivity extends BaseActivity {
    private static final String TAG = BWBattleResultActivity.class.getSimpleName();

    private static final int FINISH = 1;
    private BWBattleMatchResult mMatchResult;
    private BWBattleDetail mBattleDetail;

    private boolean mIsLiving;
    private SoundManager mSoundManager;

    public static final int AUDIO_WAITING_MATCH = 1;
    public static final int AUDIO_REVIVE_SUCCESS = 2;
    public static final int AUDIO_LOSE = 3;
    public static final int AUDIO_FINAL_WIN = 4;

    public enum INTERVAL_STATUS {
        REVIVING, REVIVED, MATCHING, MATCHED, WIN
    }

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

        LogUtil.i(TAG, "onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bw_battle_result);

        mBattleDetail = BWBattleManager.getInstance().getBWBattleDetail();
        mMatchResult = BWBattleManager.getInstance().getBWBattleMatchResult();
        mIsLiving = BattleManager.getInstance().isWinner();
        mIsLiving = mIsLiving && hasBattled();
        LogUtil.i(TAG, "isLiving = " + mIsLiving);
        LogUtil.i(TAG, "onCreate -> mBattleDetail = " + mBattleDetail);
        LogUtil.i(TAG, "onCreate -> mMatchResult = " + mMatchResult);
        INTERVAL_STATUS status;
        if (!mIsLiving) {
            status = INTERVAL_STATUS.REVIVING;
        } else {
            status = INTERVAL_STATUS.MATCHING;
        }
        next(status);
        mSoundManager = new SoundManager(this);
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setTransparent(this);
        StatusBarUtil.setMiuiStatusBarDarkMode(this, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private boolean hasBattled() {
        return BWBattleManager.getInstance().getBattleRound() == mMatchResult.getRoundId();
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

    public void leave() {
        LogUtil.i(TAG, "leave back to home!");
        Router.toHome();
        BWBattleResultActivity.this.finish();
    }

    public void gotoMatch() {
        BattleUtils.startMatch(this);
        BWBattleResultActivity.this.finish();
    }

    public void next(INTERVAL_STATUS status) {
        String fragmentTag;
        Fragment fragment;
        switch (status) {
            case REVIVING:
                fragment = new BWReviveFragment();
                fragmentTag = "bw_revive_fragment";
                break;
            case REVIVED:
                fragment = new BWReviveSuccessFragment();
                fragmentTag = "bw_revive_success_fragment";
                break;
            case MATCHING:
                fragment = new BWIntervalFragment();
                fragmentTag = "bw_interval_fragment";
                break;
            case WIN:
                fragment = new BWWinFragment();
                fragmentTag = "bw_win_fragment";
                break;
            case MATCHED:
                gotoMatch();
            default:
                return;

        }
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.container, fragment, fragmentTag);
        transaction.commitAllowingStateLoss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseSoundPool();
    }

    @Override
    public void onBackPressed() {
        if (!BackHandlerHelper.handleBackPress(this)) {
            super.onBackPressed();
        }
    }

    public void alertExit() {
        BaiWanAlertDialog.Builder builder = new BaiWanAlertDialog.Builder(this);
        builder.setMessage(getString(R.string.battle_back_title_bw));
        builder.setPositiveButton(R.string.battle_back_yes, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BackHandlerHelper.handleExit(BWBattleResultActivity.this);
                cancelMatch();
                BWBattleManager.getInstance().clearAll();
                BWBattleResultActivity.super.onBackPressed();
            }
        });
        builder.setNegativeButton(R.string.battle_back_no, null);
        BaiWanAlertDialog baiWanAlertDialog = builder.create();
        baiWanAlertDialog.show();
    }

    private void releaseSoundPool() {
        mSoundManager.release();
    }

    public void playBackgroundSound(int sound) {
        mSoundManager.playSound(sound);
    }

    public void playBackgroundSound(int sound, int repeatCount) {
        mSoundManager.playSound(sound, repeatCount);
    }

    public void stopBackgroundSound() {
        mSoundManager.stopSound();
    }

    public static class SoundManager {
        private WeakReference<BWBattleResultActivity> mWeakReference;
        private SoundPool mSoundPool;
        private int mPlayingId;
        private PlayTask mPlayTask;

        private Map<Integer, SoundModel> sounds = new HashMap<>();
        private Map<Integer, Integer> loadIds = new HashMap<>();

        private SoundManager(BWBattleResultActivity activity) {
            mWeakReference = new WeakReference<BWBattleResultActivity>(activity);
            init();
        }

        private void init() {
            sounds.put(AUDIO_WAITING_MATCH, new SoundModel(R.raw.audio_waiting_match));
            sounds.put(AUDIO_REVIVE_SUCCESS, new SoundModel(R.raw.audio_revive_success));
            sounds.put(AUDIO_LOSE, new SoundModel(R.raw.audio_lose));
            sounds.put(AUDIO_FINAL_WIN, new SoundModel(R.raw.audio_final_win));

            mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
            mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                    BWBattleResultActivity activity = mWeakReference.get();
                    if (activity == null) {
                        stopSound();
                        release();
                        return;
                    }
                    if (!loadIds.containsKey(i)) {
                        return;
                    }
                    int sId = loadIds.get(i);
                    SoundModel sound = sounds.get(sId);
                    if (sound == null) {
                        return;
                    }
                    sound.loadId = i;
                    sound.loaded = true;
                    if (mPlayTask != null && mPlayTask.soundId == sId) {
                        playSound(mPlayTask.soundId, mPlayTask.repeatCount);
                        mPlayTask = null;
                    }
                }
            });

        }

        private void playSound(int soundId) {
            playSound(soundId, -1);
        }

        private void playSound(int soundId, int repeatCount) {
            BWBattleResultActivity activity = mWeakReference.get();
            if (activity == null) {
                stopSound();
                release();
                return;
            }
            stopSound();
            if (!sounds.containsKey(soundId)) {
                return;
            }
            SoundModel sound = sounds.get(soundId);
            if (sound.loaded) {
                sound.playId = mSoundPool.play(sound.loadId, 1f, 1f,
                        1, repeatCount, 1f);
                mPlayingId = soundId;
            } else {
                sound.loadId = mSoundPool.load(activity, sound.soundResId, 1);
                loadIds.put(sound.loadId, soundId);
                if (mPlayTask == null) {
                    mPlayTask = new PlayTask(soundId, repeatCount);
                } else {
                    mPlayTask.reset(soundId, repeatCount);
                }
            }
        }

        private void stopSound() {
            if (mPlayingId <= 0) {
                return;
            }
            if (!sounds.containsKey(mPlayingId)) {
                return;
            }
            SoundModel sound = sounds.get(mPlayingId);
            mSoundPool.stop(sound.playId);
            mPlayingId = 0;
        }

        private void release() {
            mSoundPool.release();
            mSoundPool = null;
        }

        private class SoundModel {
            int loadId;
            int soundResId;
            int playId;
            boolean loaded;

            private SoundModel(int soundResId) {
                this.soundResId = soundResId;
            }
        }

        private class PlayTask {
            int soundId;
            int repeatCount;

            private PlayTask(int soundId, int repeatCount) {
                this.soundId = soundId;
                this.repeatCount = repeatCount;
            }

            private void reset(int soundId, int repeatCount) {
                this.soundId = soundId;
                this.repeatCount = repeatCount;
            }
        }
    }

    public interface FragmentBackHandler {
        boolean onBackPressed();

        void exit();
    }

    public static class BackHandlerHelper {
        public static boolean handleBackPress(FragmentManager fragmentManager) {
            List<Fragment> fragments = fragmentManager.getFragments();
            if (fragments == null) return false;
            for (int i = fragments.size() - 1; i >= 0; i--) {
                Fragment child = fragments.get(i);
                if (isFragmentSelected(child)) {
                    return child instanceof FragmentBackHandler && ((FragmentBackHandler) child).onBackPressed();
                }
            }

            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
                return true;
            }
            return false;
        }

        public static boolean handleBackPress(Fragment fragment) {
            return handleBackPress(fragment.getChildFragmentManager());
        }

        public static boolean handleBackPress(AppCompatActivity fragmentActivity) {
            return handleBackPress(fragmentActivity.getSupportFragmentManager());
        }

        public static boolean isFragmentSelected(Fragment fragment) {
            return fragment != null
                    && fragment.isVisible()
                    && fragment.getUserVisibleHint(); //for ViewPager
        }

        public static void handleExit(AppCompatActivity fragmentActivity) {
            handleExit(fragmentActivity.getSupportFragmentManager());
        }

        public static void handleExit(FragmentManager fragmentManager) {
            List<Fragment> fragments = fragmentManager.getFragments();
            if (fragments == null) return;
            for (int i = fragments.size() - 1; i >= 0; i--) {
                Fragment child = fragments.get(i);
                if (isFragmentSelected(child)) {
                    if (child instanceof FragmentBackHandler) {
                        ((FragmentBackHandler) child).exit();
                    }
                    return;
                }
            }
        }
    }

    private void cancelMatch() {
        LogUtil.i(TAG, "cancelMatch -> bwId = " + mBattleDetail.getBwId()
                + ", gameId = " + mBattleDetail.getGameId() + ", roundId = " + (mMatchResult.getRoundId() + 1));
        ServiceFactory.battleService().cancelBWBattle(mBattleDetail.getBwId(), mBattleDetail.getGameId(),
                mMatchResult.getRoundId() + 1).enqueue(new OnCallback<Void>() {
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
}
