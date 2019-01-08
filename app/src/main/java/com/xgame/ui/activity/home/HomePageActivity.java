package com.xgame.ui.activity.home;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.xgame.R;
import com.xgame.account.AccountConstants;
import com.xgame.account.UserManager;
import com.xgame.account.model.User;
import com.xgame.app.GlideApp;
import com.xgame.base.ClientSettingManager;
import com.xgame.base.ServiceFactory;
import com.xgame.common.api.OnCallback;
import com.xgame.common.util.IntentParser;
import com.xgame.common.util.LaunchUtils;
import com.xgame.common.util.LocationUtil;
import com.xgame.common.util.LogUtil;
import com.xgame.common.var.LazyVarHandle;
import com.xgame.common.var.VarHandle;
import com.xgame.home.model.TaskStatus;
import com.xgame.personal.model.PersonalInfoModel;
import com.xgame.personal.model.UserProfile;
import com.xgame.push.PushConstants;
import com.xgame.push.event.FriendPassEvent;
import com.xgame.push.event.FriendVerifyEvent;
import com.xgame.push.event.InvitationEvent;
import com.xgame.ui.Router;
import com.xgame.ui.activity.BaseActivity;
import com.xgame.ui.activity.home.transform.HolderRegister;
import com.xgame.ui.activity.home.transform.SimpleHolder;
import com.xgame.uisupport.OnSelectedListener;
import com.xgame.update.UpdateHelper;
import com.xgame.util.Analytics;
import com.xgame.util.PermissionUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.xgame.common.util.ExecutorHelper.runInBackground;
import static com.xgame.common.util.Priority.HIGH;
import static com.xgame.common.util.ToastUtil.showTip;
import static com.xgame.common.util.ToastUtil.showToast;
import static com.xgame.home.model.ItemType.TYPE_GAME_GRID_ITEM;
import static com.xgame.home.model.ItemType.TYPE_GAME_ITEM_BAR;
import static com.xgame.home.model.ItemType.TYPE_IMAGE_BAR;
import static com.xgame.home.model.ItemType.TYPE_IMAGE_BOX;
import static com.xgame.home.model.ItemType.TYPE_MSG_BAR;
import static com.xgame.home.model.XGameItem.EXTRA_GOLD_COIN;
import static com.xgame.ui.activity.home.TabFragment.Tab.ARENA;
import static com.xgame.ui.activity.home.TabFragment.Tab.BATTLE;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 * <p>
 * Created by jackwang
 * on 18-1-27.
 */


public class HomePageActivity extends BaseActivity implements MailBox, OnSelectedListener {

    public static final int TAB_BATTLE_POS = 0;

    public static final int TAB_ARENA_POS = 1;

    public static final int TAB_HISTORY_POS = 2;

    public static final int TAB_COUNT = 3;

    public static final int MSG_REFRESH_USER = 1;

    public static final int MAIL_HIDE_HISTORY_RED_POINT = 1;

    private static final String TAG = HomePageActivity.class.getSimpleName();

    private final Handler mHandler = new InnerHandler(this);

    private long mFirstClick;

    private IndicatorWrapper mIndicator;

    private ViewPager mTabViewPager;

    private final RedirectRoute mHostRouter = new RedirectRoute() {
        @Override
        protected void onHandleIntent(@NonNull Intent redirect) {
            redirect.putExtra(EXTRA_GOLD_COIN, getGoldCoin());
            LaunchUtils.startActivity(HomePageActivity.this, redirect);
        }

        @Override
        public void onDispatchAnchorAction(String anchor) {
            if ("0".equalsIgnoreCase(anchor)) {
                mIndicator.setSelect(TAB_BATTLE_POS);
            } else if ("1".equalsIgnoreCase(anchor)) {
                mIndicator.setSelect(TAB_ARENA_POS);
            } else if ("2".equalsIgnoreCase(anchor)) {
                mIndicator.setSelect(TAB_HISTORY_POS);
            }
        }
    };

    private TabPageAdapter mTabPageAdapter;

    private User mUser;

    private ImageView mAvatarView;

    private TextView mTaskCenter;

    private TextView mTotalGoldCoin;

    private TextView mNickname;

    private TextView mTvHistoryRedPoint;

    private View mTaskRedPoint;

    private int mGoldCoin = -1;

    private View.OnClickListener mOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == mAvatarView || v == mRlTopLayout) {
                Router.toPersonal();
            } else if (v == mRlTaskCenter) {
                Router.toTaskCenter();
            } else {
                LogUtil.w(TAG, "Unknown click : " + v);
            }
        }
    };

    private View mRlTopLayout;

    private ViewGroup mRlTaskCenter;

    private int mMsgCount;

    private static User obtainCurrentUser() {
        User user = UserManager.getInstance().getUser();
        Parcel desc = Parcel.obtain();
        user.writeToParcel(desc, 0);
        desc.setDataPosition(0);
        return User.CREATOR.createFromParcel(desc);
    }

    private void onResponseUserInfo(User ur) {
        if (ur != null) {
            if (!ur.equals(mUser)) {
                mUser = ur;
                onRefreshUserInfo();
            }
            return;
        }
        mUser = null;
        onInvalidUserInfo();
    }

    private void onInvalidUserInfo() {
        showToast(getApplicationContext(), R.string.please_relogin);
    }

    private void onRefreshUserInfo() {
        final User user = mUser;
        GlideApp.with(getApplication())
                .load(user.getHeadimgurl())
                .transform(new CircleCrop())
                .into(mAvatarView);
        mNickname.setText(user.getReadableNickName());
    }

    private void loadPersonalInfoIfPossible() {
        UserProfile profile = PersonalInfoModel.getProfileCache();
        int gc = profile != null ? profile.coin : 0;
        if (mGoldCoin == -1 || mGoldCoin != gc) {
            mGoldCoin = gc;
            mTotalGoldCoin.setText(String.valueOf(gc));
        }
    }

    int getGoldCoin() {
        return mGoldCoin;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        PersonalInfoModel.init(new UserProfileOnCallback(this));
        registerComponentView();
        setContentView(R.layout.home_act);
        initView();
        UpdateHelper.update(this, false);
        mHostRouter.performRedirectIntent(getIntent());
        if (IntentParser.getInt(getIntent(), AccountConstants.FIRST_REGISTER, -1) == 1) {
            showRegisterDialog();
        }
        PermissionUtil.requestNecessaryPermission(this, false);
    }

    @Override
    protected void loadClientSettings() {
        ClientSettingManager.reloadSettingsIfNeed();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mHostRouter.performRedirectIntent(intent);
    }

    private void initView() {
        mRlTopLayout = findViewById(R.id.rl_top_layout);
        mAvatarView = findViewById(R.id.iv_avatar);
        mTaskCenter = findViewById(R.id.tv_task_center);
        mRlTaskCenter = findViewById(R.id.rl_task_center);
        mTaskRedPoint = findViewById(R.id.task_red_point);
        mNickname = findViewById(R.id.tv_nick_name);
        mTotalGoldCoin = findViewById(R.id.tv_gold_coin);
        mTvHistoryRedPoint = findViewById(R.id.history_red_point);
        mTabViewPager = findViewById(R.id.vp_content);
        mTabPageAdapter = new TabPageAdapter(getSupportFragmentManager());
        mTabViewPager.setAdapter(mTabPageAdapter);
        mIndicator = new IndicatorWrapper((LinearLayout) findViewById(R.id.ll_tab_indicator));
        mIndicator.attachToViewPager(mTabViewPager);
        mIndicator.setSelect(0);
        mIndicator.setSelectedListener(this);
        mAvatarView.setOnClickListener(mOnClick);
        mRlTopLayout.setOnClickListener(mOnClick);
        mRlTaskCenter.setOnClickListener(mOnClick);
        onHistoryRedPointStateChange(false);
    }

    private void onHistoryRedPointStateChange(boolean show) {
        if (show) {
            mMsgCount++;
        } else {
            mMsgCount = 0;
        }
        if (show && mTabViewPager.getCurrentItem() == TAB_HISTORY_POS) {
            HistoryFragment f = mTabPageAdapter.obtainFragment(TAB_HISTORY_POS);
            f.onHistoryDataSetInvalid();
            mMsgCount = 0;
            mTvHistoryRedPoint.setVisibility(View.INVISIBLE);
            return;
        }
        mTvHistoryRedPoint.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        mTvHistoryRedPoint.setText(String.valueOf(mMsgCount < 100 ? mMsgCount : 99));
    }

    private void requestTaskStatus() {
        ServiceFactory.homeService().loadTaskStatus().enqueue(new TaskStatusOnCallback(this));
    }

    private void onTaskStatusChange(boolean show) {
        mTaskRedPoint.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        mTaskCenter.setText(show ? R.string.collect_gold_coin : R.string.make_gold_coin);
    }

    private void onFriendVerify() {
        HistoryFragment f = mTabPageAdapter.obtainFragment(TAB_HISTORY_POS);
        f.onFriendVerify();
    }

    private void showRegisterDialog() {
        final View view = View.inflate(this, R.layout.finish_register_dialog, null);
        final Dialog dlg = new Dialog(this);
        TextView goMissionBtn = view.findViewById(R.id.go_mission_btn);
        goMissionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Router.toTaskCenter();
                dlg.dismiss();
                //登录成功页“去邀请”点击
                Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_CLICK, Analytics.Constans.STOCK_ID_INVITE,
                        Analytics.Constans.STOCK_NAME_LOGIN_INVITE, Analytics.Constans.STOCK_TYPE_LINK,
                        Analytics.Constans.PAGE_LOGIN_SUCCESS, Analytics.Constans.SECTION_LOGIN_SUCCESS_POP, null);
            }
        });
        ImageView close = view.findViewById(R.id.close_btn);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
                //登录成功页关闭弹窗
                Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_CLICK, Analytics.Constans.STOCK_ID_CLOSE,
                        Analytics.Constans.STOCK_NAME_LOGIN_CLOSE, Analytics.Constans.STOCK_TYPE_BTN,
                        Analytics.Constans.PAGE_LOGIN_SUCCESS, Analytics.Constans.SECTION_LOGIN_SUCCESS_POP, null);
            }
        });
        dlg.setContentView(view);

        Window window = dlg.getWindow();
        window.setBackgroundDrawable(null);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        window.setAttributes(wlp);
        dlg.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        PersonalInfoModel.requestUserProfile(new UserProfileOnCallback(this));
        requestUserInfo();
        requestTaskStatus();
        // PermissionUtil.requestPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestUserInfo();
        requestTaskStatus();
        LocationUtil.updateLocation(getApplicationContext());
        PermissionUtil.checkPermissionAgain(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocationUtil.stopUpdateLocation(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterComponentView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - mFirstClick > 2000) {
            mFirstClick = System.currentTimeMillis();
            String msg = getString(R.string.quit_toast) + getString(R.string.app_name);
            showTip(getApplicationContext(), msg);
        } else {
            super.onBackPressed();
        }
    }

    private void requestUserInfo() {
        runInBackground(new RequestUserInfoRun(mHandler), HIGH);
    }

    private void registerComponentView() {
        HolderRegister reg = HolderRegister.get();
        reg.register(TYPE_GAME_GRID_ITEM, SimpleHolder.create(R.layout.home_layout_game_item));
        reg.register(TYPE_IMAGE_BAR, SimpleHolder.create(R.layout.home_layout_img_bar));
        reg.register(TYPE_IMAGE_BOX, SimpleHolder.create(R.layout.home_layout_img_box));
        reg.register(TYPE_MSG_BAR, SimpleHolder.create(R.layout.home_layout_msg_item));
        reg.register(TYPE_GAME_ITEM_BAR, SimpleHolder.create(R.layout.home_layout_game_bar));
    }

    private void unregisterComponentView() {
        HolderRegister reg = HolderRegister.get();
        reg.unregister(TYPE_GAME_GRID_ITEM);
        reg.unregister(TYPE_IMAGE_BAR);
        reg.unregister(TYPE_IMAGE_BOX);
        reg.unregister(TYPE_MSG_BAR);
        reg.unregister(TYPE_GAME_ITEM_BAR);
    }

    @Override
    protected void setStatusBar() {
    }

    @Override
    public void onMailReceive(MailMessage msg) {
        if (msg.what == MAIL_HIDE_HISTORY_RED_POINT) {
            onHistoryRedPointStateChange(false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecvFriendVerify(FriendVerifyEvent e) {
        onHistoryRedPointStateChange(true);
        onFriendVerify();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecvFriendPass(FriendPassEvent e) {
        onHistoryRedPointStateChange(true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecvInvitation(InvitationEvent e) {
        if (e != null && PushConstants.TYPE_GAME_INVITATION.equals(e.getType())) {
            onHistoryRedPointStateChange(true);
        }
    }

    @Override
    public void onSelected(int position, @Nullable Object o) {
        if (position == 0) {
            Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_CLICK, Analytics.Constans.STOCK_ID_BATTLE,
                    Analytics.Constans.STOCK_NAME_BATTLE, Analytics.Constans.STOCK_TYPE_TAB,
                    Analytics.Constans.PAGE_HOME, Analytics.Constans.SECTION_TAB, null);
        } else if (position == 1) {
            Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_CLICK, Analytics.Constans.STOCK_ID_ARENA,
                    Analytics.Constans.STOCK_NAME_ARENA, Analytics.Constans.STOCK_TYPE_TAB,
                    Analytics.Constans.PAGE_HOME, Analytics.Constans.SECTION_TAB, null);
        } else if (position == 2) {
            Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_CLICK, Analytics.Constans.STOCK_ID_HISTORY,
                    Analytics.Constans.STOCK_NAME_HISTORY, Analytics.Constans.STOCK_TYPE_TAB,
                    Analytics.Constans.PAGE_HOME, Analytics.Constans.SECTION_TAB, null);
        }
    }

    private static class TabPageAdapter extends FragmentPagerAdapter {

        private final FragmentManager fragmentManager;

        private VarHandle<SparseArray<Fragment>> mFragmentCacheVar
                = new LazyVarHandle<SparseArray<Fragment>>() {
            @Override
            protected SparseArray<Fragment> constructor() {
                return new SparseArray<>();
            }
        };

        TabPageAdapter(FragmentManager fm) {
            super(fm);
            fragmentManager = fm;
        }

        @Override
        public Fragment getItem(int position) {
            return obtainFragment(position);
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }

        @NonNull
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            fragmentManager.beginTransaction().show(fragment).commit();
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            fragmentManager.beginTransaction().hide((Fragment) object).commit();
        }

        <T extends Fragment> T obtainFragment(int pos) {
            Fragment f = mFragmentCacheVar.get().get(pos);
            if (f != null) {
                return (T) f;
            }
            switch (pos) {
                default:
                case TAB_BATTLE_POS:
                    f = TabFragment.newInstance(BATTLE, 3);
                    break;
                case TAB_ARENA_POS:
                    f = TabFragment.newInstance(ARENA, 1);
                    break;
                case TAB_HISTORY_POS:
                    f = HistoryFragment.newInstance(null, null);
                    break;
            }
            mFragmentCacheVar.get().put(pos, f);
            return (T) f;
        }

    }

    private static class RequestUserInfoRun implements Runnable {

        private Handler mHandler;

        RequestUserInfoRun(Handler h) {
            mHandler = h;
        }

        @Override
        public void run() {
            Message msg = Message.obtain();
            msg.what = MSG_REFRESH_USER;
            msg.obj = obtainCurrentUser();
            mHandler.sendMessage(msg);
        }
    }

    private static class InnerHandler extends Handler {

        private final WeakReference<HomePageActivity> mActRef;

        InnerHandler(HomePageActivity act) {
            mActRef = new WeakReference<>(act);
        }

        @Override
        public void handleMessage(Message msg) {
            HomePageActivity act = mActRef.get();
            if (act == null) {
                removeCallbacksAndMessages(null);
                return;
            }
            if (msg.what == MSG_REFRESH_USER) {
                act.onResponseUserInfo((User) msg.obj);
            }
        }
    }

    private static class UserProfileOnCallback implements OnCallback<UserProfile> {

        private final WeakReference<HomePageActivity> mActRef;

        UserProfileOnCallback(HomePageActivity act) {
            mActRef = new WeakReference<>(act);
        }

        @Override
        public void onResponse(UserProfile result) {
            HomePageActivity act = mActRef.get();
            if (act == null) {
                return;
            }
            act.loadPersonalInfoIfPossible();
        }

        @Override
        public void onFailure(UserProfile result) {
            HomePageActivity act = mActRef.get();
            if (act == null) {
                return;
            }
            showToast(act.getApplicationContext(), R.string.gold_coin_info_load_fail);
        }
    }

    private static class TaskStatusOnCallback implements OnCallback<TaskStatus> {

        private final WeakReference<HomePageActivity> mActRef;

        TaskStatusOnCallback(HomePageActivity act) {
            mActRef = new WeakReference<>(act);
        }

        @Override
        public void onResponse(TaskStatus result) {
            HomePageActivity act = mActRef.get();
            if (act == null || result == null) {
                return;
            }
            act.onTaskStatusChange(result.hasAwardsNotRecv());
        }

        @Override
        public void onFailure(TaskStatus result) {
            HomePageActivity act = mActRef.get();
            if (act == null) {
                return;
            }
            // guess is no award to collect
            act.onTaskStatusChange(false);
        }
    }
}
