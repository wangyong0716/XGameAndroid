package com.xgame.ui.fragment;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.miui.zeus.mario.sdk.MarioSdk;
import com.xgame.R;
import com.xgame.account.UserManager;
import com.xgame.battle.BWBattleManager;
import com.xgame.battle.BattleManager;
import com.xgame.battle.BattleUtils;
import com.xgame.battle.model.BWBattleDetail;
import com.xgame.battle.model.BWBattleWallet;
import com.xgame.ui.Router;
import com.xgame.ui.activity.BWBattleActivity;
import com.xgame.util.Analytics;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by zhanglianyu on 18-1-28.
 */

public class BWBattleFragment1 extends Fragment {

    @BindView(R.id.ic_back)
    ImageView mBack;

    @BindView(R.id.btn_try)
    RelativeLayout mTry;

    @BindView(R.id.txt_try)
    TextView mTxtTry;

    @BindView(R.id.txt_title)
    TextView mTxtTitle;

    @BindView(R.id.txt_time_value)
    TextView mTxtTimeValue;

    @BindView(R.id.txt_bonus_value)
    TextView mTxtBonusValue;

    @BindView(R.id.txt_open_alarm)
    TextView mOpenAlarm;

    @BindView(R.id.txt_learn_rule)
    TextView mLearnRule;

    @BindView(R.id.txt_rmb_val)
    TextView mRmbVal;

    @BindView(R.id.txt_cash_val)
    TextView mCashVal;

    @BindView(R.id.rel_cash)
    RelativeLayout mRelCash;

    @BindView(R.id.view_content)
    ImageView mViewContent;

    @OnClick({R.id.ic_back, R.id.btn_try, R.id.txt_learn_rule, R.id.rel_cash, R.id.rel_invite})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ic_back:
                onBackPressed();
                break;
            case R.id.btn_try:
                goToGame();
                break;
            case R.id.txt_learn_rule:
                goToRule();
                break;
            case R.id.rel_cash:
                goToCash();
                break;
            case R.id.rel_invite:
                goToInvite();
                break;
            default:
                break;
        }
    }

    private Unbinder mUnbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bwbattle1, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        initView();
        return view;
    }

    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        updateView();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInto10Minutes(BWBattleActivity.EventInto10Minutes
                                            eventInto10Minutes) {
        updateView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDetailLoaded(BWBattleActivity.EventBWBattleDetailLoaded
                                            eventBWBattleDetailLoaded) {
        updateView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWalletLoaded(BWBattleWallet bwBattleWallet) {
        updateWallet(bwBattleWallet);
    }

    private void onBackPressed() {
        final FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.onBackPressed();
        }
    }

    private void initView() {
        mOpenAlarm.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mLearnRule.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
    }

    public void updateView() {
        final BWBattleDetail bwBattleDetail = BWBattleManager.getInstance().getBWBattleDetail();
        final boolean hasDetail = bwBattleDetail != null;
        if (!in10Minutes()) {
            mTxtTry.setText(R.string.bw_battle_try);
            if (hasDetail) {
                mTxtTitle.setText(bwBattleDetail.getTitle());
            }
            mLearnRule.setVisibility(View.VISIBLE);
        } else {
            mTxtTry.setText(R.string.bw_battle_enter);
            mTxtTitle.setText(R.string.bw_battle_start_soon);
            mLearnRule.setVisibility(View.INVISIBLE);
        }

        if (hasDetail) {
            mTxtTimeValue.setText(bwBattleDetail.getShowStartTimeStr());
            mTxtBonusValue.setText(bwBattleDetail.getShownBonus());
            Glide.with(this).load(bwBattleDetail.getImageBanner()).into(mViewContent);
            mRmbVal.setText(BattleUtils.getRmbTxt(bwBattleDetail.getTotalBonus()));
            mCashVal.setText(BattleUtils.getRmbTxt(bwBattleDetail.getCashBonus()));
        } else {
            mTxtTimeValue.setText(R.string.bw_battle_start_time_default);
            mTxtBonusValue.setText(R.string.bw_battle_bonus_default);
            mRmbVal.setText(R.string.bw_battle_rmb_default);
            mCashVal.setText(R.string.bw_battle_rmb_default);
        }
    }

    private void updateWallet(BWBattleWallet bwBattleWallet) {
        if (bwBattleWallet != null) {
            mRmbVal.setText(BattleUtils.getRmbTxt(bwBattleWallet.getTotalBonus()));
            mCashVal.setText(BattleUtils.getRmbTxt(bwBattleWallet.getCashBonus()));
        }
    }

    private boolean in10Minutes() {
        final FragmentActivity activity = getActivity();
        if (activity != null && activity instanceof BWBattleActivity) {
            return ((BWBattleActivity) activity).getTimeToStart() < BattleUtils.MINUTE_10;
        }
        return false;
    }

    private void goToGame() {
        if (!in10Minutes()) {
            // go to try bw battle game
            final BWBattleDetail bwBattleDetail = BWBattleManager.getInstance().getBWBattleDetail();
            if (bwBattleDetail != null) {
                BattleUtils.startMatch(getActivity(), (int) bwBattleDetail.getGameId(), bwBattleDetail.getTitle(),
                        String.valueOf(bwBattleDetail.getGameUrl()));
            }
        } else {
            // enter bw battle
            final FragmentActivity activity = getActivity();
            if (activity != null && activity instanceof BWBattleActivity) {
                ((BWBattleActivity) activity).enterBWBattle();
            }
        }
    }

    private void goToRule() {
        BattleUtils.gotoBWBattleRule(getActivity(), "");
    }

    private void goToCash() {
        Router.toWithdrawCash();
        Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_CLICK, Analytics.Constans.STOCK_ID_WITHDRAW,
                Analytics.Constans.STOCK_NAME_WITHDRAW, Analytics.Constans.STOCK_TYPE_LINK,
                Analytics.Constans.PAGE_BW_HOME, Analytics.Constans.SECTION_CASH_INFO, null);
    }

    private void goToInvite() {
        MarioSdk.inviteFollower(getActivity(), UserManager.getInstance().getAdToken());
        Analytics.trackPageShowEvent(Analytics.Constans.URL_TITLE_INVITE_FOLLOWER_PAGE, Analytics.Constans.VISIT_TYPE_READY, null);
    }
}
