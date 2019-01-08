package com.xgame.ui.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.xgame.R;
import com.xgame.battle.BattleManager;
import com.xgame.battle.BattleUtils;
import com.xgame.common.util.LogUtil;
import com.xgame.common.util.StatusBarUtil;
import com.xgame.social.share.SharePlatform;
import com.xgame.ui.Router;
import com.xgame.ui.activity.invite.util.ShareInvoker;
import com.xgame.util.Analytics;

import java.lang.ref.WeakReference;

/**
 * Created by wangyong on 18-1-26.
 */

public class CoinBattleResultActivity extends BaseActivity implements ShareInvoker.ShareListener {
    private static final String TAG = CoinBattleResultActivity.class.getSimpleName();
    private static final int COUNT_DOWN_MAX = 60;
    private String mResult;
    private TextView mResultTitle;
    private TextView mResultDes;
    private TextView mTryAgainBtn;
    private ImageView mResultIcon;
    private CountTimer mCountTimer;

    private ShareInvoker mShareInvoker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LogUtil.i(TAG, "onCreate()");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_result);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mResultTitle = findViewById(R.id.result_title);
        mResultDes = findViewById(R.id.result_des);
        mTryAgainBtn = findViewById(R.id.try_again_btn);
        mResultIcon = findViewById(R.id.result_icon);
        mResult = BattleManager.getInstance().getBattleResult();
        LogUtil.i(TAG, "onCreate mResult = " + mResult);
        setResult();
        mShareInvoker = new ShareInvoker();
        mShareInvoker.setShareListener(this);
    }

    private void setResult() {
        if (BattleUtils.GAME_OVER_RESULT_WIN.equals(mResult)) {
            mResultTitle.setText(R.string.coin_battle_title_win);
            mResultTitle.setTextColor(getResources().getColor(R.color.coin_win_title_color));
            mResultDes.setText(getString(R.string.coin_result_win, BattleManager.getInstance().getWinCoin()));
            updateTryAgainBtn(COUNT_DOWN_MAX, true);
            mResultIcon.setImageResource(R.drawable.coin_battle_win);
            mCountTimer = new CountTimer(this, COUNT_DOWN_MAX * 1000, 1000);
            mCountTimer.setWinner(true);
            mCountTimer.start();
        } else {
            mResultTitle.setText(R.string.coin_battle_title_lose);
            mResultTitle.setTextColor(getResources().getColor(R.color.black_alpha_80));
            mResultDes.setText(getString(R.string.coin_result_lose, BattleManager.getInstance().getLoseCoin()));
            updateTryAgainBtn(COUNT_DOWN_MAX, false);
            mResultIcon.setImageResource(R.drawable.coin_battle_lose);
            mCountTimer = new CountTimer(this, COUNT_DOWN_MAX * 1000, 1000);
            mCountTimer.setWinner(false);
            mCountTimer.start();
        }
    }

    private void updateTryAgainBtn(int seconds, boolean isWinner) {
        if (isWinner) {
            mTryAgainBtn.setText(getString(R.string.coin_play_again, seconds));
        } else {
            mTryAgainBtn.setText(getString(R.string.battle_again, seconds));
        }
    }

    public void leave() {
        CoinBattleResultActivity.this.finish();
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setTransparent(this);
        StatusBarUtil.setMiuiStatusBarDarkMode(this, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCountTimer != null) {
            mCountTimer.cancel();
            mCountTimer = null;
        }
    }

    public void handleClick(View view) {
        switch (view.getId()) {
            case R.id.try_again_btn:
                tryAgain();
                break;
            case R.id.share_btn:
                share();
                break;
            case R.id.back_btn:
                goBack();
                break;
            default:
                break;
        }
    }

    /**
     * 再来一场
     */
    private void tryAgain() {
        int gameId = BattleManager.getInstance().getGameId();
        String gameUrl = BattleManager.getInstance().getGameUrl();
        int ruleId = BattleManager.getInstance().getRuleId();
        String ruleTitle = BattleManager.getInstance().getRuleTitle();
        int winCoin = BattleManager.getInstance().getWinCoin();
        int loseCoin = BattleManager.getInstance().getLoseCoin();
        String gameName = BattleManager.getInstance().getGameName();
        if (BattleUtils.GAME_OVER_RESULT_WIN.equals(mResult)) {
            //金币场游戏结果页按钮点击 趁热再来一局
            Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_CLICK, Analytics.Constans.STOCK_ID_ARENA_AGAIN,
                    Analytics.Constans.STOCK_NAME_ARENA_AGAIN, Analytics.Constans.STOCK_TYPE_BTN,
                    Analytics.Constans.PAGE_ARENA_RESULT, Analytics.Constans.SECTION_ARENA_RESULT, getExtInfo());
        } else {
            //金币场游戏结果页按钮点击 我要翻盘
            Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_CLICK, Analytics.Constans.STOCK_ID_ARENA_COMEBACK,
                    Analytics.Constans.STOCK_NAME_ARENA_COMEBACK, Analytics.Constans.STOCK_TYPE_BTN,
                    Analytics.Constans.PAGE_ARENA_RESULT, Analytics.Constans.SECTION_ARENA_RESULT, getExtInfo());
        }
        BattleUtils.startMatch(CoinBattleResultActivity.this, gameId, gameName, gameUrl, ruleId, ruleTitle, winCoin, loseCoin);
        CoinBattleResultActivity.this.finish();
    }

    /**
     * 分享
     */
    private void share() {
        Router.toTaskCenter();
        //金币场游戏结果页按钮点击 分享赚金币
        Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_CLICK, Analytics.Constans.STOCK_ID_ARENA_SHARE,
                Analytics.Constans.STOCK_NAME_ARENA_SHARE, Analytics.Constans.STOCK_TYPE_BTN,
                Analytics.Constans.PAGE_ARENA_RESULT, Analytics.Constans.SECTION_ARENA_RESULT, getExtInfo());
    }

    /**
     * 返回
     */
    private void goBack() {
        Router.toHome();
        CoinBattleResultActivity.this.finish();
        //金币场游戏结果页按钮点击 返回游戏大厅
        Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_CLICK, Analytics.Constans.STOCK_ID_ARENA_BACK,
                Analytics.Constans.STOCK_NAME_ARENA_BACK, Analytics.Constans.STOCK_TYPE_BTN,
                Analytics.Constans.PAGE_ARENA_RESULT, Analytics.Constans.SECTION_ARENA_RESULT, getExtInfo());
    }

    @Override
    public void onShareProceed(int platform) {
        if (platform == SharePlatform.WX) {
            //金币场分享分渠道点击 微信好友
            Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_SHARE, Analytics.Constans.STOCK_ID_SHARE_WX,
                    Analytics.Constans.STOCK_NAME_SHARE_WX, Analytics.Constans.STOCK_TYPE_BTN,
                    Analytics.Constans.PAGE_ARENA_RESULT, Analytics.Constans.SECTION_SHARE, getExtInfo());
        } else if (platform == SharePlatform.WX_TIMELINE) {
            //金币场分享分渠道点击 微信朋友圈
            Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_SHARE, Analytics.Constans.STOCK_ID_SHARE_WX_TIMELINE,
                    Analytics.Constans.STOCK_NAME_SHARE_WX_TIMELINE, Analytics.Constans.STOCK_TYPE_BTN,
                    Analytics.Constans.PAGE_ARENA_RESULT, Analytics.Constans.SECTION_SHARE, getExtInfo());
        } else if (platform == SharePlatform.QQ) {
            //金币场分享分渠道点击 QQ好友
            Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_SHARE, Analytics.Constans.STOCK_ID_SHARE_QQ,
                    Analytics.Constans.STOCK_NAME_SHARE_QQ, Analytics.Constans.STOCK_TYPE_BTN,
                    Analytics.Constans.PAGE_ARENA_RESULT, Analytics.Constans.SECTION_SHARE, getExtInfo());
        }
    }

    @Override
    public void onShareFailed(int type, String error) {

    }

    private String getExtInfo() {
        JsonObject json = new JsonObject();
        json.addProperty("game_id", BattleManager.getInstance().getGameId());
        json.addProperty("battle_type", Analytics.Constans.CUSTOM_GAME_TYPE_ARENA);
        json.addProperty("play_mode", Analytics.Constans.CUSTOM_GAMME_MODE_PK);
        json.addProperty("game_name", BattleManager.getInstance().getGameName());
        return json.toString();
    }

    private static class CountTimer extends CountDownTimer{
        private boolean mIsWinner;
        private WeakReference<CoinBattleResultActivity> mReference;

        public CountTimer(CoinBattleResultActivity activity, long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            mReference = new WeakReference<CoinBattleResultActivity>(activity);
        }

        private void setWinner(boolean win) {
            mIsWinner = win;
        }

        @Override
        public void onTick(long l) {
            CoinBattleResultActivity activity = mReference.get();
            if (activity != null) {
                activity.updateTryAgainBtn((int) l / 1000, mIsWinner);
            } else {
                cancel();
            }
        }

        @Override
        public void onFinish() {
            CoinBattleResultActivity activity = mReference.get();
            if (activity != null) {
                activity.leave();
            } else {
                cancel();
            }
        }
    }
}
