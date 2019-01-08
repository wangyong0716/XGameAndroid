package com.xgame.ui.activity.personal;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.xgame.R;
import com.xgame.account.UserManager;
import com.xgame.account.model.User;
import com.xgame.app.GlideApp;
import com.xgame.base.api.Pack;
import com.xgame.common.api.OnCallback;
import com.xgame.common.util.LaunchUtils;
import com.xgame.common.util.LogUtil;
import com.xgame.common.util.StatusBarUtil;
import com.xgame.common.util.ToastUtil;
import com.xgame.personal.model.Banner;
import com.xgame.personal.model.PersonalInfoModel;
import com.xgame.personal.model.PersonalMenu;
import com.xgame.personal.model.PersonalMenuItem;
import com.xgame.personal.model.UserProfile;
import com.xgame.ui.Router;
import com.xgame.ui.activity.BaseActivity;
import com.xgame.ui.activity.SettingActivity;
import com.xgame.ui.activity.personal.dialog.PersonalDialogUtils;
import com.xgame.ui.activity.personal.view.ItemViewHolder;
import com.xgame.ui.activity.personal.view.PersonalToolbarHolder;
import com.xgame.update.UpdateHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import static com.xgame.base.ServiceFactory.personalInfoService;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by dingning1
 * on 18-1-26.
 */


public class PersonalInfoActivity extends BaseActivity {

    private static final String TAG = PersonalInfoActivity.class.getSimpleName();
    private static final String DETAIL_URI = "xgame://xgame.com/personal/detail";
    private static final String BILL_URI = "xgame://xgame.com/personal/bill";

    private static final String FAQ_URI = "https://api.chufengnet.com/act/cms/chufeng/#id=2fe951d1-2962-48e2-8b37-d00e0e63d6fa";

    private static final String TYPE_TASK = "task";
    private static final String TYPE_MALL = "mall";
    private static final String TYPE_UPGRADE = "upgrade";

    private static int TEN_THOUSAND = 10000;
    private static int HUNDRED_THOUSAND = 10 * TEN_THOUSAND;

    @BindView(R.id.header) View mHeader;
    @BindView(R.id.avatar) ImageView mAvatar;
    @BindView(R.id.name) TextView mName;
    @BindView(R.id.baiwan_id) TextView mId;
    @BindView(R.id.coin_value) TextView mCoin;
    @BindView(R.id.coin_link) TextView mCoinLink;
    @BindView(R.id.cash_value) TextView mCash;
    @BindView(R.id.cash_link) TextView mCashLink;

    @BindView(R.id.scroll) ScrollView mScrollView;
    @BindView(R.id.item_banner) View mItemBanner;
    @BindView(R.id.banner_gap) View mBannerGap;
    @BindView(R.id.item_task) View mItemTask;
    @BindView(R.id.item_input_code) View mItemInputCode;
    @BindView(R.id.item_feedback) View mItemFeedback;
    @BindView(R.id.item_mall) View mItemMall;
    @BindView(R.id.item_upgrade) View mItemUpgrade;

    private boolean mRefreshProfile;

    private Map<String, ItemViewHolder> mItemMap = new HashMap<>();

    private View.OnClickListener mOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mRefreshProfile = v == mCoinLink || v == mItemMall || v == mCashLink
                    || v == mItemTask || v == mItemBanner;

            if (v == mCoinLink || v == mItemMall) {
                Router.toGoldCoinMall();
            } else if (v == mCashLink){
                Router.toWithdrawCash();
            } else if (v == mItemTask) {
                Router.toTaskCenter();
            } else if (v == mItemInputCode) {
                Router.toInputCode();
            } else if (v == mItemBanner) {
                Router.toInviteFollower();
            } else if (v == mItemFeedback) {
                PersonalDialogUtils.showFeedbackInputDialog(PersonalInfoActivity.this,
                        new PersonalDialogUtils.IDialogCallback<String[]>() {
                            @Override
                            public void onResult(String[] result) {
                                onFeedback(result);
                            }
                        });
            } else if (v == mItemUpgrade) {
                UpdateHelper.update(PersonalInfoActivity.this,true);
            } else {
                LogUtil.w(TAG, "unknown click %s", v);
            }
        }
    };

    static class IconItemViewHolder {
        @BindView(R.id.icon) ImageView icon;
        @BindView(R.id.name) TextView name;
        @BindView(R.id.info) TextView info;
        @BindView(R.id.more) View iconMore;

        IconItemViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_personal);

        ButterKnife.bind(this);

        initToolbar();
        initView();

        loadUserProfile();
        loadMenu();

        refreshUserProfile();
        refreshMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAndRefreshUser();
        if (mRefreshProfile) {
            mRefreshProfile = false;
            refreshUserProfile();
        }
    }

    private void initToolbar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.toolbar_bg));
        StatusBarUtil.setStatusBarDarkMode(getWindow(), false);

        new PersonalToolbarHolder(findViewById(R.id.toolbar))
                .setTitle(R.string.personal_center, R.color.color_white)
                .setBackIcon(R.drawable.icon_back_white, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onBackPressed();
                    }
                }).setToolIcon(R.drawable.icon_settings, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LaunchUtils.startActivity(PersonalInfoActivity.this, SettingActivity.class);
            }
        });
    }

    private void initView() {
        findViewById(R.id.header).setBackgroundResource(R.drawable.bg_personal_header);
        LaunchUtils.setAction(mAvatar, DETAIL_URI);
        mCoinLink.setOnClickListener(mOnClick);
        mCashLink.setOnClickListener(mOnClick);
        initMenuItem();
    }

    private void loadAndRefreshUser() {
        User user = UserManager.getInstance().getUser();
        GlideApp.with(this)
                .load(user.getHeadimgurl())
                .placeholder(R.drawable.default_avatar)
                .into(mAvatar);
        mName.setText(user.getNickname());
        mId.setText(getString(R.string.personal_baiwan_id, user.getUserid()));
    }

    private void loadUserProfile() {
        UserProfile profile = PersonalInfoModel.getProfileCache();
        if (profile != null) {
            updateProfileView(profile);
        }
    }

    private void refreshUserProfile() {
        personalInfoService().getUserProfile()
                .enqueue(new OnCallback<Pack<UserProfile>>() {
                    @Override
                    public void onResponse(Pack<UserProfile> result) {
                        if (result.data != null) {
                            updateProfileView(result.data);
                        } else {
                            onRequestFailed(result);
                        }
                    }

                    @Override
                    public void onFailure(Pack<UserProfile> result) {
                        onRequestFailed(result);
                    }
                });
    }

    private void refreshMenu() {
        if (!PersonalInfoModel.canUpdateMenu(getApplication())) {
            return;
        }
        personalInfoService().getMyData()
                .enqueue(new OnCallback<Pack<PersonalMenu>>() {
                    @Override
                    public void onResponse(Pack<PersonalMenu> result) {
                        if (result.data != null) {
                            updateMenuItems(result.data);
                        } else {
                            onRequestFailed(result);
                        }
                    }

                    @Override
                    public void onFailure(Pack<PersonalMenu> result) {
                        onRequestFailed(result);
                    }
                });
    }

    private void onRequestFailed(Pack result) {
        String msg = getString(R.string.net_error_text);
        if (result != null) {
            String resultMsg = result.msg;
            if (!TextUtils.isEmpty(resultMsg)) {
                msg += ": " + result.code + ", " + result.msg;
            }
        }
        ToastUtil.showToast(getBaseContext(), msg, false);
    }

    private void updateProfileView(UserProfile profile) {
        PersonalInfoModel.cacheUserProfile(getApplication(), profile);

        String coinStr;
        if (profile.coin >= HUNDRED_THOUSAND) {
            coinStr = profile.coin / TEN_THOUSAND + getString(R.string.bw_battle_w);
        } else {
            coinStr = String.valueOf(profile.coin);
        }
        coinStr += getString(R.string.gold_coin);
        mCoin.setText(coinStr);
        String cashStr = profile.cash + getString(R.string.yuan);
        mCash.setText(cashStr);
    }

    private void loadMenu() {
        PersonalMenu cachedData = PersonalInfoModel.getMenuCache(getApplication());
        if (cachedData != null) {
            updateMenuItems(cachedData);
        }
    }

    private void initMenuItem() {
        ItemViewHolder taskHolder = new ItemViewHolder(mItemTask)
                .setName(R.string.personal_item_task)
                .setAction(mOnClick);
        ItemViewHolder mallHolder = new ItemViewHolder(mItemMall)
                .setName(R.string.personal_item_mall)
                .setAction(mOnClick);
        new ItemViewHolder(mItemInputCode)
                .setName(R.string.personal_item_input_code)
                .setAction(mOnClick);
        new ItemViewHolder(findViewById(R.id.item_bill))
                .setName(R.string.personal_item_bill)
                .setAction(BILL_URI);
        new ItemViewHolder(findViewById(R.id.item_faq))
                .setName(R.string.personal_item_faq)
                .setAction(FAQ_URI);
        new ItemViewHolder(mItemFeedback)
                .setName(R.string.personal_item_feedback)
                .setAction(mOnClick);
        ItemViewHolder upgradeHolder = new ItemViewHolder(mItemUpgrade)
                .setName(R.string.personal_item_upgrade)
                .setAction(mOnClick);

        mItemMap.put(TYPE_TASK, taskHolder);
        mItemMap.put(TYPE_MALL, mallHolder);
        mItemMap.put(TYPE_UPGRADE, upgradeHolder);
    }

    private void updateMenuItems(PersonalMenu homeData) {
        if (homeData.banner != null) {
            showBanner(true);
            setBannerItem(mItemBanner, homeData.banner);
        } else {
            showBanner(false);
        }
        if (homeData.points != null && homeData.points.length > 0) {
            for (PersonalMenuItem item : homeData.points) {
                ItemViewHolder holder = mItemMap.get(item.status);
                holder.item = item;
                if (holder != null) {
                    long oldState = PersonalInfoModel.getPointState(getApplication(), item.status);
                    holder.showPromptIcon(item.state > oldState);
                    holder.setInfo(item.msg);
                }
            }
        }
        PersonalInfoModel.cacheHomeMenu(getApplication(), homeData);
    }

    private void setBannerItem(final View itemView, final Banner banner) {
        if (banner == null) {
            showBanner(false);
            return;
        }
        showBanner(true);
        IconItemViewHolder holder = new IconItemViewHolder(itemView);
        if (!TextUtils.isEmpty(banner.img)) {
            holder.iconMore.setVisibility(View.GONE);
            loadBackground(itemView, banner.img);
        } else {
            holder.iconMore.setVisibility(View.VISIBLE);
            GlideApp.with(this)
                    .load(banner.icon)
                    .placeholder(R.drawable.icon_hongbao)
                    .into(holder.icon);
            loadBackground(itemView, banner.bg);
            holder.name.setText(banner.title);
            holder.info.setVisibility(TextUtils.isEmpty(banner.subTitle) ? View.GONE : View.VISIBLE);
            holder.info.setText(banner.subTitle);
        }
        itemView.setOnClickListener(mOnClick);
    }

    private void loadBackground(final View view, String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        GlideApp.with(this)
                .load(url)
                .into(new ViewTarget<View, Drawable>(view) {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource,
                            @Nullable Transition<? super Drawable> transition) {
                        view.setBackground(resource);
                    }
                });
    }

    private void showBanner(boolean show) {
        int visibility = show ? View.VISIBLE : View.GONE;
        mItemBanner.setVisibility(visibility);
        mBannerGap.setVisibility(visibility);
    }

    private void onFeedback(String[] result) {
        if (result == null || result.length < 2) {
            return;
        }
        String feedback = result[0];
        String contact = result[1];

        personalInfoService().postFeedback(feedback, contact)
                .enqueue(new OnCallback<Pack<Object>>() {
                    @Override
                    public void onResponse(Pack result) {
                        if (result.code >= 200 && result.code < 300) {
                            ToastUtil.showToast(getApplication(), R.string.feedback_posted);
                        } else {
                            String errMsg = getString(R.string.feedback_post_fail_format,
                                    result.code, result.msg);
                            ToastUtil.showToast(getApplication(), errMsg);
                        }
                    }

                    @Override
                    public void onFailure(Pack result) {
                        ToastUtil.showToast(getBaseContext(), R.string.feedback_post_fail);
                    }
                });
    }
}
