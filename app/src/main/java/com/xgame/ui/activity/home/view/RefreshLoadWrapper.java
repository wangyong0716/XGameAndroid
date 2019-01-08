package com.xgame.ui.activity.home.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.xgame.ui.activity.home.view.AbsRefreshLoadLayout.RefreshLoadListener;
import com.xgame.ui.activity.home.view.RefreshLoadLayout.AbsLoadCallback;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-29.
 */


public class RefreshLoadWrapper {

    private static final String TAG = RefreshLoadWrapper.class.getSimpleName();

    private final RefreshLoadLayout mLayout;

    private final RecyclerView mRecyclerView;

    private final RefreshLoadLayout.LoadCallback<RecyclerView> mLoadCallbackStub
            = new AbsLoadCallback<RecyclerView>() {
        @Override
        public boolean canLoad(RecyclerView rv) {
            postCanLoadCheck();
            return false;
        }

        @Override
        public void onRemoveLoadingView(RecyclerView rv, View loadView) {
        }

        @Override
        public void onAddLoadingView(RecyclerView rv, View loadView) {
        }
    };

    private RefreshLoadListener mRefreshLoadListener;

    private final RefreshLoadListener mRefreshLoadStub
            = new RefreshLoadListener() {
        @Override
        public void onRefresh() {
            if (mRefreshLoadListener != null) {
                mRefreshLoadListener.onRefresh();
            } else {
                mLayout.setRefreshing(false);
            }
        }

        @Override
        public void onLoad() {
            if (mRefreshLoadListener != null) {
                mRefreshLoadListener.onLoad();
            } else {
                mLayout.setLoading(false);
            }
        }
    };

    private RefreshLoadWrapper.mDeferredCanLoadCheck
            mDeferredCanLoadCheck;

    public RefreshLoadWrapper(RefreshLoadLayout loadLayout,
            RecyclerView recyclerView) {
        this.mLayout = loadLayout;
        this.mRecyclerView = recyclerView;
        loadLayout.setLoadCallback(mLoadCallbackStub);
        loadLayout.setRefreshLoadListener(mRefreshLoadStub);
    }

    private boolean checkCanLoad(RecyclerView rv) {
        return !rv.canScrollVertically(1);
    }

    private void postCanLoadCheck() {
        if (mDeferredCanLoadCheck == null) {
            mDeferredCanLoadCheck = new mDeferredCanLoadCheck();
        } else {
            mRecyclerView.removeCallbacks(mDeferredCanLoadCheck);
        }
        mRecyclerView.postDelayed(mDeferredCanLoadCheck, 350);
    }

    public void setLoading(boolean loading) {
        this.mLayout.setLoading(loading);
    }

    public void setRefreshing(boolean refreshing) {
        this.mLayout.setRefreshing(refreshing);
    }

    public void setRefreshLoadListener(RefreshLoadListener l) {
        this.mRefreshLoadListener = l;
    }

    private class mDeferredCanLoadCheck implements Runnable {

        @Override
        public void run() {
            if (mRecyclerView.getScrollState() == SCROLL_STATE_IDLE
                    && checkCanLoad(mRecyclerView)) {
                setLoading(true);
                mRefreshLoadStub.onLoad();
            }
        }
    }
}
