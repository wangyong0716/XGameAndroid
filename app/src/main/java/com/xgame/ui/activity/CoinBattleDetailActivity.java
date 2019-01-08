package com.xgame.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.miui.zeus.mario.sdk.MarioSdk;
import com.xgame.R;
import com.xgame.account.UserManager;
import com.xgame.account.model.User;
import com.xgame.app.GlideApp;
import com.xgame.base.ServiceFactory;
import com.xgame.battle.BattleConstants;
import com.xgame.battle.model.ServerCoinGame;
import com.xgame.battle.model.ServerCoinItem;
import com.xgame.common.api.OnCallback;
import com.xgame.common.util.IntentParser;
import com.xgame.common.util.LogUtil;
import com.xgame.common.util.StatusBarUtil;
import com.xgame.common.util.ToastUtil;
import com.xgame.home.model.XGameItem;
import com.xgame.ui.fragment.CoinMatchBattleFragment;
import com.xgame.ui.fragment.CoinMultiBattleFragment;

/**
 * Created by wangyong on 18-1-26.
 */

public class CoinBattleDetailActivity extends BaseActivity {
    private static final String TAG = CoinBattleDetailActivity.class.getSimpleName();
    private int mGameId;
    private String mGameUrl;
    private String mGameTitle;
    private int mGameType;
    private int mCoin;
    private User mUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_battle_detail);

        keepValues(getIntent());

        setTitleView();
        mUser = UserManager.getInstance().getUser();

        updateHeader(mUser.getNickname(), mUser.getHeadimgurl(), null, mCoin);
    }

    private void keepValues(Intent intent) {
        String gameId = IntentParser.getString(intent, XGameItem.EXTRA_GAME_ID);
        if (!TextUtils.isEmpty(gameId)) {
            mGameId = Integer.valueOf(gameId);
        } else {
            ToastUtil.showToast(this, R.string.game_get_fail);
            finish();
        }
        mGameUrl = IntentParser.getString(intent, XGameItem.EXTRA_GAME_URL);
        mGameType = IntentParser.getInt(intent, XGameItem.EXTRA_GAME_TYPE, -1);
        mGameTitle = IntentParser.getString(intent, XGameItem.EXTRA_GAME_NAME);
        mCoin = IntentParser.getInt(intent, XGameItem.EXTRA_GOLD_COIN, 0);
        LogUtil.i(TAG, "onCreate -> gameId = " + mGameId + ", gameUrl = "
                + mGameUrl + ", gameTitle = " + mGameTitle + ", coin = " + mCoin);
    }

    public void updateHeader(String name, String avatar, String imgUrl, int coin) {
        if (isFinishing()) {
            return;
        }
        if (!TextUtils.isEmpty(name)) {
            ((TextView) findViewById(R.id.player_name)).setText(name);
        }
        if (!TextUtils.isEmpty(avatar)) {
            GlideApp.with(this).load(avatar).placeholder(R.drawable.default_avatar)
                    .into((ImageView) findViewById(R.id.player_avatar));
        }
        if (!TextUtils.isEmpty(imgUrl)) {
            GlideApp.with(this).load(imgUrl).into((ImageView) findViewById(R.id.game_img));
        }
        mCoin = coin;
        if (mCoin >= 0) {
            String coinS = coin + "";
            SpannableString coinSS = new SpannableString(coinS + getResources().getString(R.string.gold_coin));
            coinSS.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.gold_coin_color)), 0, coinS.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ((TextView) findViewById(R.id.player_account)).setText(coinSS);
        }
    }

    public void getGame() {
        LogUtil.i(TAG, "gameId = " + mGameId + ", gameTitle = " + mGameTitle + ", userId = " + mUser.getUserid());
        ServiceFactory.battleService().getCoinGameDetail(mGameId, MarioSdk.getClientInfo()).enqueue(new OnCallback<ServerCoinGame>() {
            @Override
            public void onResponse(ServerCoinGame game) {
                LogUtil.i(TAG, "onResponse -> game = " + game);
                if (game == null) {
                    return;
                }

                updateHeader(null, null, game.getImgUrl(), game.getCoin());
                showGameDetail(game.getPlayType(), game.getRuleDesc(), game.getItems());
            }

            @Override
            public void onFailure(ServerCoinGame game) {
                LogUtil.i(TAG, "onFailure -> game = " + game);

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getGame();
    }

    private void setTitleView() {
        TextView titleView = findViewById(R.id.title);
        titleView.setText(mGameTitle);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setTransparent(this);
        StatusBarUtil.setMiuiStatusBarDarkMode(this, false);
    }

    private void showGameDetail(int type, String ruleDesc, ServerCoinItem[] items) {
        String fragmentTag;
        Fragment fragment;
        if (type == BattleConstants.COIN_BATTLE_PLAYTYPE_MULTI) {
            fragment = new CoinMultiBattleFragment();
            fragmentTag = "multi_battle_fragment";
        } else {
            fragment = new CoinMatchBattleFragment();
            fragmentTag = "match_battle_fragment";
        }
        Bundle bundle = new Bundle();
        if (items != null) {
            bundle.putSerializable("coin_items", Lists.newArrayList(items));
        }
        bundle.putInt(XGameItem.EXTRA_GAME_ID, mGameId);
        bundle.putInt(XGameItem.EXTRA_GAME_TYPE, mGameType);
        bundle.putInt(XGameItem.EXTRA_GOLD_COIN, mCoin);
        bundle.putString(XGameItem.EXTRA_GAME_URL, mGameUrl);
        bundle.putString(XGameItem.EXTRA_GAME_NAME, mGameTitle);
        bundle.putString(BattleConstants.BATTLE_RULE_DESC, ruleDesc);
        fragment.setArguments(bundle);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.detail_content, fragment, fragmentTag);
        transaction.commitAllowingStateLoss();
    }

}
