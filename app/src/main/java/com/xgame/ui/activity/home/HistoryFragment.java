package com.xgame.ui.activity.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.xgame.R;
import com.xgame.app.GlideApp;
import com.xgame.app.XgameApplication;
import com.xgame.base.GameProvider;
import com.xgame.base.GameProvider.GameProfile;
import com.xgame.common.api.OnCallback;
import com.xgame.common.util.LogUtil;
import com.xgame.home.model.MessageSession;
import com.xgame.ui.Router;
import com.xgame.ui.activity.home.transform.ViewAdapter;
import com.xgame.ui.activity.home.transform.ViewStream;
import com.xgame.ui.activity.home.transform.ViewStreamRecyclerAdapter;
import com.xgame.ui.activity.home.view.AbsRefreshLoadLayout.RefreshLoadListener;
import com.xgame.ui.activity.home.view.AlertViewStubWrapper;
import com.xgame.ui.activity.home.view.PageFragment;
import com.xgame.ui.activity.home.view.RefreshLoadLayout;
import com.xgame.ui.activity.home.view.RefreshLoadWrapper;
import com.xgame.util.Analytics;

import static com.xgame.base.ServiceFactory.homeService;
import static com.xgame.common.util.ExecutorHelper.runInBackground;
import static com.xgame.common.util.ExecutorHelper.runInUIThread;
import static com.xgame.common.util.TaggedTextParser.setTaggedText;
import static com.xgame.common.util.ToastUtil.showToast;
import static com.xgame.home.model.MessageSession.MSG_DRAW;
import static com.xgame.home.model.MessageSession.MSG_FRIEND_PASS;
import static com.xgame.home.model.MessageSession.MSG_INVITATION_CANCEL;
import static com.xgame.home.model.MessageSession.MSG_INVITATION_ING;
import static com.xgame.home.model.MessageSession.MSG_INVITATION_REJECT;
import static com.xgame.home.model.MessageSession.MSG_LOSE;
import static com.xgame.home.model.MessageSession.MSG_WIN;
import static com.xgame.ui.activity.ChatActivity.createChatIntent;
import static com.xgame.ui.activity.home.HomePageActivity.MAIL_HIDE_HISTORY_RED_POINT;
import static com.xgame.util.CalendarUtil.parseToDateString;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 * <p>
 * Created by jackwang
 * on 18-1-27.
 */

public class HistoryFragment extends PageFragment {

    private static final String TAG = HistoryFragment.class.getSimpleName();

    private static final String ARG_PARAM1 = "param1";

    private static final String ARG_PARAM2 = "param2";

    private String mParam1;

    private String mParam2;

    private RecyclerView mRecyclerView;

    private RefreshLoadWrapper mRefreshLoadWrapper;

    private View mFlAddFriend;

    private View mFlStrangerGroup;

    private long mLoadTag;

    private LinearLayoutManager mLayoutManager;

    private long mRefreshTag;

    private ViewStream<MessageSession> mViewStream;

    private ViewStreamRecyclerAdapter mRecyclerAdapter;

    private boolean mIsManualRefresh;

    private AlertViewStubWrapper mAlertView;

    private AlertViewStubWrapper.OnAlertListener mOnAlertShow
            = new AlertViewStubWrapper.OnAlertListener() {
        @Override
        public void onAlert(View alertView, Object showData) {
            alertView.setOnClickListener(mOnClick);
        }
    };

    private RefreshLoadListener mOnRefreshLoad = new RefreshLoadListener() {
        @Override
        public void onRefresh() {
            mIsManualRefresh = true;
            refreshRecord(mRefreshTag);
        }

        @Override
        public void onLoad() {
            mIsManualRefresh = true;
            loadRecord(mLoadTag);
        }
    };

    private SparseArray<String> mMsgMode;

    private ViewAdapter<MessageSession> mBo2VoAdapter = new ViewAdapter<MessageSession>() {
        @Override
        public int viewType(MessageSession messageSession) {
            return MessageSession.viewType;
        }

        @Override
        public String title(MessageSession messageSession, TextView tv) {
            return messageSession.otherName();
        }

        @Override
        public String subTitle(MessageSession mr, TextView tv) {
            postSetSubTitle(tv, mr.messageDetailType, mr.gameId);
            return null;
        }

        @Override
        public String image(MessageSession messageSession, View iv) {
            String avatar = messageSession.otherAvatar();
            if (iv != null && iv instanceof ImageView) {
                GlideApp.with(XgameApplication.getApplication())
                        .load(avatar)
                        .transform(new CircleCrop())
                        .error(R.drawable.default_avatar)
                        .into((ImageView) iv);
            }
            return null;
        }

        @Override
        public boolean hasRemind(MessageSession messageSession, TextView tv) {
            return messageSession.needRemind();
        }

        @Override
        public String stamp(MessageSession mr, TextView tv) {
            return parseToDateString(XgameApplication.getApplication(), mr.createTime);
        }

        @Override
        public Intent extension(MessageSession mr) {
            MessageSession.Other ui = mr.otherUserInfo;
            if (ui == null) {
                return createChatIntent(mr.otherUserId, 0, 0, "", "");
            } else {
                return createChatIntent(mr.otherUserId, ui.sex, ui.age, ui.nickname, ui.headimgurl);
            }
        }
    };

    private OnCallback<List<MessageSession>> mOnLoadCallback
            = new OnCallback<List<MessageSession>>() {
        @Override
        public void onResponse(List<MessageSession> result) {
            mRefreshLoadWrapper.setLoading(false);
            if (result != null && result.size() > 0) {
                final long newLoadTag = result.get(result.size() - 1).createTime;
                if (newLoadTag >= mLoadTag) {
                    LogUtil.d(TAG, "load tag is same or invalid %s", newLoadTag);
                    return;
                }
                mLoadTag = newLoadTag;
                if (mRefreshTag == 0) {
                    mRefreshTag = result.get(0).createTime;
                }
                initViewStreamIfNeed();
                final int size = mViewStream.size();
                mViewStream.appendAll(result);
                mRecyclerAdapter.notifyItemRangeInserted(size, result.size());
                mRecyclerView.scrollToPosition(size);
            } else {
                LogUtil.d(TAG, "no history data");
            }
            mIsManualRefresh = false;
        }

        @Override
        public void onFailure(List<MessageSession> result) {
            doFailure();
        }
    };

    private OnCallback<List<MessageSession>> mOnRefreshCallback
            = new OnCallback<List<MessageSession>>() {
        @Override
        public void onResponse(List<MessageSession> result) {
            mRefreshLoadWrapper.setRefreshing(false);
            if (result != null && result.size() > 0) {
                long newRefreshTag = result.get(0).createTime;
                if (newRefreshTag <= mRefreshTag) {
                    LogUtil.d(TAG, "tag is same or invalid %s", newRefreshTag);
                    return;
                }
                mRefreshTag = newRefreshTag;
                if (mLoadTag == 0) {
                    mLoadTag = result.get(result.size() - 1).createTime;
                }
                initViewStreamIfNeed();
                mViewStream.refresh(result);
                mRecyclerAdapter.notifyDataSetChanged();
                mRecyclerView.scrollToPosition(0);
            } else {
                // no new data
                LogUtil.d(TAG, "no new data");
            }
            mIsManualRefresh = false;
            mAlertView.hideAlertView();
        }

        @Override
        public void onFailure(List<MessageSession> result) {
            doFailure();
        }
    };

    private boolean mDeferredMsgFriendVerify;

    private View mFriendRedPoint;

    private View.OnClickListener mOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == mFlAddFriend) {
                onAddFriendClick();
            } else if (v == mFlStrangerGroup) {
                onStrangerGroupClick();
            } else if (v.getId() == R.id.alert_layout) {
                resetAndRefreshNewData();
            } else {
                LogUtil.d(TAG, "view click: " + v);
            }
        }
    };

    private boolean mIsTrackedShowPV;

    public HistoryFragment() {
        // Required empty public constructor
    }

    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private void postSetSubTitle(TextView tv, final int type, final int gameId) {
        runInBackground(new SetSubTitleRun(gameId, type, tv, mMsgMode));
    }

    private void inflateMsgString() {
        mMsgMode = new SparseArray<>();
        String template = "<span color='#e14055'>%s</span>";
        mMsgMode.put(MSG_INVITATION_ING, String.format(template, getString(R.string.msg_invite_ing)));
        mMsgMode.put(MSG_INVITATION_CANCEL, getString(R.string.msg_invite_cancel));
        mMsgMode.put(MSG_INVITATION_REJECT, getString(R.string.msg_invite_reject));
        mMsgMode.put(MSG_WIN, getString(R.string.msg_win));
        mMsgMode.put(MSG_LOSE, getString(R.string.msg_lose));
        mMsgMode.put(MSG_DRAW, getString(R.string.msg_draw));
        mMsgMode.put(MSG_FRIEND_PASS, getString(R.string.msg_friend_pass));
    }

    private void resetAndRefreshNewData() {
        mIsManualRefresh = true;
        if (mViewStream != null) {
            mViewStream.clear();
        }
        mLoadTag = 0;
        mRefreshTag = 0;
        refreshRecord(mRefreshTag);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.home_frag_history, container, false);
        mFlAddFriend = inflate.findViewById(R.id.fl_add_friend);
        mFriendRedPoint = inflate.findViewById(R.id.friend_red_point);
        mFlStrangerGroup = inflate.findViewById(R.id.fl_group);
        mFlAddFriend.setOnClickListener(mOnClick);
        mFlStrangerGroup.setOnClickListener(mOnClick);
        RefreshLoadLayout loadLayout = inflate.findViewById(R.id.refresh_load_layout);
//        loadLayout.setProgressBackgroundColorSchemeColor(Color.TRANSPARENT);
//        loadLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        mRecyclerView = inflate.findViewById(R.id.grid_recycler_view);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRefreshLoadWrapper = new RefreshLoadWrapper(loadLayout, mRecyclerView);
        mRefreshLoadWrapper.setRefreshLoadListener(mOnRefreshLoad);
        mAlertView = new AlertViewStubWrapper((ViewStub) inflate.findViewById(R.id.vs_alert_layout))
                .attachTo(mRecyclerView)
                .setOnAlertListener(mOnAlertShow);
        inflateMsgString();
        return inflate;
    }

    private void initViewStreamIfNeed() {
        if (mViewStream == null) {
            mViewStream = ViewStream.Builder.of(new ArrayList<MessageSession>()).adapt(mBo2VoAdapter)
                    .build();
            mRecyclerAdapter = new ViewStreamRecyclerAdapter(mViewStream);
            mRecyclerView.setAdapter(mRecyclerAdapter);
        }
    }

    private void showFriendRedPoint(boolean show) {
        if (mFriendRedPoint != null) {
            mFriendRedPoint.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    protected void onVisibleToUser() {
        super.onVisibleToUser();
        startRefreshDataQuietlyIfPossible();
        postMail(MailBox.MailMessage.create(MAIL_HIDE_HISTORY_RED_POINT));
        onHandleDeferredMsg();
        if (!mIsTrackedShowPV) {
            //我的对战页PV
            Analytics.trackPageShowEvent(Analytics.Constans.URL_TITLE_HISTORY_PAGE, Analytics.Constans.VISIT_TYPE_READY, null);
            mIsTrackedShowPV = true;
        }
    }

    private void onHandleDeferredMsg() {
        if (mDeferredMsgFriendVerify) {
            mDeferredMsgFriendVerify = false;
            showFriendRedPoint(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (isVisibleToUser()) {
            startRefreshDataQuietlyIfPossible();
        }
    }

    private void startRefreshDataQuietlyIfPossible() {
        if (mViewStream == null && !mAlertView.isShown()) {
            mRefreshLoadWrapper.setRefreshing(true);
        }
        mIsManualRefresh = false;
        refreshRecord(mRefreshTag);
    }

    private void refreshRecord(long refreshTag) {
        homeService().loadRecordHistory(refreshTag, 0, true).enqueue(mOnRefreshCallback);
    }

    private void loadRecord(long loadTag) {
        homeService().loadRecordHistory(0, loadTag, true).enqueue(mOnLoadCallback);
    }

    private void doFailure() {
        mRefreshLoadWrapper.setRefreshing(false);
        mAlertView.showAlertViewIfNeed(null);
        if (mIsManualRefresh) {
            mIsManualRefresh = false;
            showToast(getContext(), R.string.load_fail_refresh_again);
        }
    }

    private void onStrangerGroupClick() {
        Router.toStrangerList();
    }

    private void onAddFriendClick() {
        showFriendRedPoint(false);
        Router.toFriends();
    }

    public void onFriendVerify() {
        boolean userVisible = getUserVisibleHint();
        if (!userVisible) {
            mDeferredMsgFriendVerify = true;
            return;
        }
        showFriendRedPoint(true);
    }

    public void onHistoryDataSetInvalid() {
        startRefreshDataQuietlyIfPossible();
    }

    private static class SetSubTitleRun implements Runnable {

        private final int mGameId;

        private final int mType;

        private final WeakReference<TextView> mTvRef;

        private final SparseArray<String> mMsgMode;

        SetSubTitleRun(int gameId, int type, TextView tv, SparseArray<String> msgMode) {
            mGameId = gameId;
            mType = type;
            mTvRef = new WeakReference<>(tv);
            mMsgMode = msgMode;
        }

        @Override
        public void run() {
            GameProfile ga = GameProvider.get().load(String.valueOf(mGameId));
            final String msg = mMsgMode.get(mType).concat(ga != null ? ga.name : "");
            final TextView t;
            if ((t = mTvRef.get()) != null) {
                runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        setTaggedText(t, msg);
                    }
                });
            }
        }
    }
}
