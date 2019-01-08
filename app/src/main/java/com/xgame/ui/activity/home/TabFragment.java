package com.xgame.ui.activity.home;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.xgame.R;
import com.xgame.app.XgameApplication;
import com.xgame.base.GameProvider;
import com.xgame.common.api.FutureCall;
import com.xgame.common.api.OnCallback;
import com.xgame.common.util.LogUtil;
import com.xgame.home.model.ArenaTabPage;
import com.xgame.home.model.BattleTabPage;
import com.xgame.home.model.Item;
import com.xgame.home.model.TabPage;
import com.xgame.ui.activity.home.transform.ViewAdapter;
import com.xgame.ui.activity.home.transform.ViewStream;
import com.xgame.ui.activity.home.transform.ViewStream.Action;
import com.xgame.ui.activity.home.transform.ViewStream.Builder;
import com.xgame.ui.activity.home.transform.ViewStreamRecyclerAdapter;
import com.xgame.ui.activity.home.view.AbsRefreshLoadLayout;
import com.xgame.ui.activity.home.view.AlertViewStubWrapper;
import com.xgame.ui.activity.home.view.HeaderLayoutManager;
import com.xgame.ui.activity.home.view.PageFragment;
import com.xgame.ui.activity.home.view.RefreshLoadLayout;
import com.xgame.ui.activity.home.view.RefreshLoadWrapper;
import com.xgame.util.Analytics;

import static com.xgame.base.ServiceFactory.homeService;
import static com.xgame.common.util.ToastUtil.showTip;
import static com.xgame.common.util.ToastUtil.showToast;
import static com.xgame.ui.activity.home.TabFragment.Tab.ARENA;
import static com.xgame.ui.activity.home.TabFragment.Tab.BATTLE;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-27.
 */

public class TabFragment extends PageFragment {

    private static final String TAG = TabFragment.class.getSimpleName();

    private static final String ARG_CONTENT_SPAN_COUNT = "contentSpanCount";

    private static final String ARG_TAB_TYPE = "tabType";

    private int mContentSpanCount;

    private int mTabType;

    private RefreshLoadWrapper mRefreshLoadWrapper;

    private RecyclerView mRecyclerView;

    private int mBannerCount;

    private int mRecommendCount;

    private HeaderLayoutManager mHeaderLayoutMgr;

    private ViewStream<Item> mViewStream;

    private ViewStreamRecyclerAdapter mRecyclerAdapter;

    private int mSpanCount;

    private boolean mIsManualRefresh;

    private boolean mIsTrackedShowPV;

    private AlertViewStubWrapper mAlertView;

    private ViewAdapter<Item> mVoBoAdapter = new ViewAdapter<Item>() {
        @Override
        public int viewType(Item item) {
            return item.type();
        }

        @Override
        public String title(Item item, TextView tv) {
            return item.title();
        }

        @Override
        public String subTitle(Item item, TextView tv) {
            return item.subTitle();
        }

        @Override
        public String image(Item item, View iv) {
            return item.img();
        }

        @Override
        public String stamp(Item item, TextView tv) {
            return item.stamp();
        }

        @Override
        public Intent extension(Item item) {
            return item.extension();
        }
    };

    private HeaderLayoutManager.Profile mLayoutProfile = new HeaderLayoutManager.Profile() {

        @Override
        public int headerSpanSize(int position) {
            return position < mBannerCount ? mSpanCount : mSpanCount / mRecommendCount;
        }

        @Override
        public int headerCount() {
            return mRecommendCount + mBannerCount;
        }

        @Override
        public int contentSpanSize() {
            return mSpanCount / mContentSpanCount;
        }
    };

    private OnCallback<TabPage> mOnLoadData = new OnCallback<TabPage>() {
        @Override
        public void onResponse(TabPage tab) {
            mRefreshLoadWrapper.setRefreshing(false);
            if (tab == null) {
                return;
            }
            mBannerCount = tab.banner().size();
            mRecommendCount = tab.recommend().size();
            mSpanCount = calcSpanCount(mRecommendCount, mContentSpanCount);
            mHeaderLayoutMgr.setSpanCount(mSpanCount);
            List<Item> items = new ArrayList<>();
            items.addAll(tab.banner());
            items.addAll(tab.recommend());
            items.addAll(tab.items());
            if (mViewStream == null) {
                mViewStream = Builder.of(items).adapt(mVoBoAdapter).build();
                mRecyclerAdapter = new ViewStreamRecyclerAdapter(mViewStream);
                mRecyclerView.setAdapter(mRecyclerAdapter);
            } else {
                mViewStream
                        .diffUpdate(items)
                        .then(new Action() {
                            @Override
                            public void run(ViewStream.State s) {
                                if (s.hasUpdate()) {
                                    mRecyclerAdapter.notifyDataSetChanged();
                                } else if (mIsManualRefresh) {
                                    showTip(getContext(), R.string.already_the_newest_data);
                                } else {
                                    LogUtil.d(TAG, "Quietly update,data not change.");
                                }
                            }
                        });
            }
            mIsManualRefresh = false;
            mAlertView.hideAlertView();
            syncGameProvider(tab);
        }

        @Override
        public void onFailure(TabPage tab) {
            mRefreshLoadWrapper.setRefreshing(false);
            mAlertView.showAlertViewIfNeed(null);
            if (mIsManualRefresh) {
                mIsManualRefresh = false;
                showToast(getContext(), R.string.load_fail_refresh_again);
            }
        }
    };

    private AbsRefreshLoadLayout.RefreshLoadListener mOnRefreshLoad
            = new AbsRefreshLoadLayout.RefreshLoadListener() {
        @Override
        public void onRefresh() {
            manualRefreshData();
        }

        @Override
        public void onLoad() {
            mRefreshLoadWrapper.setLoading(false);
            LogUtil.d(TAG, "no data to load.");
        }
    };

    private View.OnClickListener mOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.alert_layout) {
                manualRefreshData();
            }
        }
    };

    private AlertViewStubWrapper.OnAlertListener mOnAlertShow
            = new AlertViewStubWrapper.OnAlertListener() {
        @Override
        public void onAlert(View alertView, Object showData) {
            alertView.setOnClickListener(mOnClick);
        }
    };

    public TabFragment() {
        // Required empty public constructor
    }

    private static int calcSpanCount(int recommend, int content) {
        if (recommend == content) {
            return recommend;
        }
        final int max = Math.max(recommend, content);
        final int min = Math.min(recommend, content);
        if (min <= 0) {
            return max;
        }
        return max % min == 0 ? max : max * min;
    }

    public static TabFragment newInstance(@Tab int tabType, int spanCount) {
        TabFragment fragment = new TabFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CONTENT_SPAN_COUNT, spanCount);
        args.putInt(ARG_TAB_TYPE, tabType);
        fragment.setArguments(args);
        return fragment;
    }

    private void syncGameProvider(TabPage tab) {
        if (tab instanceof BattleTabPage) {
            GameProvider.get().postUpdate(tab.items());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mContentSpanCount = args.getInt(ARG_CONTENT_SPAN_COUNT, 1);
            mTabType = args.getInt(ARG_TAB_TYPE, BATTLE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.home_frag_tab, container, false);
        RefreshLoadLayout loadLayout = inflate.findViewById(R.id.refresh_load_layout);
//        loadLayout.setProgressBackgroundColorSchemeColor(Color.TRANSPARENT);
//        loadLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        mRecyclerView = inflate.findViewById(R.id.grid_recycler_view);
        ViewStub alertLayout = inflate.findViewById(R.id.vs_alert_layout);
        mAlertView = new AlertViewStubWrapper(alertLayout)
                .attachTo(mRecyclerView)
                .setOnAlertListener(mOnAlertShow);
        mRefreshLoadWrapper = new RefreshLoadWrapper(loadLayout, mRecyclerView);
        mRefreshLoadWrapper.setRefreshLoadListener(mOnRefreshLoad);
        Context context = XgameApplication.getApplication();
        mHeaderLayoutMgr = new HeaderLayoutManager(context, mContentSpanCount);
        mHeaderLayoutMgr.setHeaderProfile(mLayoutProfile);
        mRecyclerView.setLayoutManager(mHeaderLayoutMgr);
        Resources res = getResources();
        int v = res.getDimensionPixelOffset(R.dimen.dp_4);
        int h = res.getDimensionPixelOffset(R.dimen.dp_13);
        mRecyclerView.addItemDecoration(new InnerItemDecoration(v, h));
        return inflate;
    }

    @Override
    protected void onVisibleToUser() {
        super.onVisibleToUser();
        startLoadDataQuietlyIfPossible();
        trackShowPVIfNeed(false);
    }

    private void trackShowPVIfNeed(boolean force) {
        if (force || !mIsTrackedShowPV) {
            if (mTabType == BATTLE) {
                //真人对战首页PV
                Analytics.trackPageShowEvent(Analytics.Constans.URL_TITLE_BATTLE_PAGE, Analytics.Constans.VISIT_TYPE_READY, null);
            } else if (mTabType == ARENA) {
                //金币场首页PV
                Analytics.trackPageShowEvent(Analytics.Constans.URL_TITLE_ARENA_PAGE, Analytics.Constans.VISIT_TYPE_READY, null);
            }
            mIsTrackedShowPV = true;
        }
    }

    private void startLoadDataQuietlyIfPossible() {
        if (mViewStream == null && !mAlertView.isShown()) {
            mRefreshLoadWrapper.setRefreshing(true);
        }
        mIsManualRefresh = false;
        startLoadData();
    }

    private void manualRefreshData() {
        mIsManualRefresh = true;
        startLoadData();
        trackShowPVIfNeed(true);
    }

    private void startLoadData() {
        switch (mTabType) {
            default:
            case BATTLE: {
                final FutureCall<BattleTabPage> call = homeService().loadBattleTab(0, 0);
                call.enqueue(new BattleTabPageOnCallback(mOnLoadData));

            }
            break;
            case ARENA: {
                final FutureCall<ArenaTabPage> call = homeService().loadArenaTab(0, 0);
                call.enqueue(new ArenaTabPageOnCallback(mOnLoadData));
            }
            break;
        }
    }

    @IntDef({
            BATTLE,
            ARENA
    })
    public @interface Tab {

        int BATTLE = 0;

        int ARENA = 1;

    }

    private class InnerItemDecoration extends RecyclerView.ItemDecoration {

        private final int verticalSpace;

        private final int horizontalSpace;

        InnerItemDecoration(int verticalSpace, int horizontalSpace) {
            this.verticalSpace = verticalSpace;
            this.horizontalSpace = horizontalSpace;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                RecyclerView.State state) {
            int pos = parent.getChildAdapterPosition(view);
            final int headCount = mBannerCount + mRecommendCount;
            final int halfV = (verticalSpace + 1) / 2;
            if (pos < mBannerCount) {
                outRect.set(halfV, 0, halfV, horizontalSpace);
            } else if (pos < headCount - 1) {
                outRect.set(halfV, 0, halfV, horizontalSpace);
            } else if (pos == headCount - 1) {
                outRect.set(halfV, 0, halfV, horizontalSpace);
            } else if ((pos - mBannerCount - mRecommendCount + 1) % mContentSpanCount == 0) {
                outRect.set(halfV, 0, halfV, verticalSpace);
            } else {
                outRect.set(halfV, 0, halfV, verticalSpace);
            }
        }
    }

    private static class ArenaTabPageOnCallback implements OnCallback<ArenaTabPage> {

        private WeakReference<OnCallback<TabPage>> mOnLoadDataRef;

        private ArenaTabPageOnCallback(OnCallback<TabPage> onLoadData) {
            mOnLoadDataRef = new WeakReference<>(onLoadData);
        }

        @Override
        public void onResponse(ArenaTabPage tab) {
            OnCallback<TabPage> callback = mOnLoadDataRef.get();
            if (callback == null) {
                return;
            }
            callback.onResponse(tab);
        }

        @Override
        public void onFailure(ArenaTabPage tab) {
            OnCallback<TabPage> callback = mOnLoadDataRef.get();
            if (callback == null) {
                return;
            }
            callback.onFailure(tab);
        }
    }

    private static class BattleTabPageOnCallback implements OnCallback<BattleTabPage> {

        private final WeakReference<OnCallback<TabPage>> mOnLoadDataRef;

        BattleTabPageOnCallback(OnCallback<TabPage> onLoadData) {
            mOnLoadDataRef = new WeakReference<>(onLoadData);
        }
        @Override
        public void onResponse(BattleTabPage tab) {
            OnCallback<TabPage> callback = mOnLoadDataRef.get();
            if (callback == null) {
                return;
            }
            callback.onResponse(tab);
        }

        @Override
        public void onFailure(BattleTabPage tab) {
            OnCallback<TabPage> callback = mOnLoadDataRef.get();
            if (callback == null) {
                return;
            }
            callback.onFailure(tab);
        }
    }
}
