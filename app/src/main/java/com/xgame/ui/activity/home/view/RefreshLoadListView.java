package com.xgame.ui.activity.home.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-27.
 */


public class RefreshLoadListView extends AbsRefreshLoadLayout {

    private ListAdapter mAdapter;

    private AbsListView.OnScrollListener onScroll = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount) {
            doLoadIfNeed();
        }
    };

    private ListView containerView;

    public RefreshLoadListView(Context context) {
        super(context);
    }

    public RefreshLoadListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onInit(Context context, AttributeSet attrs) {
    }

    @Override
    protected View inflateContainer(Context context) {
        ListView listView = new ListView(context);
        listView.setDivider(null);
        listView.setOnScrollListener(onScroll);
        containerView = listView;
        return listView;
    }

    public void setAdapter(ListAdapter adapter) {
        mAdapter = adapter;
        containerView.setAdapter(adapter);
    }

    @Override
    protected boolean canLoad() {
        ListView v = this.containerView;
        return v.getLastVisiblePosition() == (v.getAdapter().getCount() - 1);
    }

    @Override
    protected void onRemoveLoadingView(View loadView) {
        loadView.setVisibility(INVISIBLE);
    }

    @Override
    protected void onAddLoadingView(View loadView) {
        loadView.setVisibility(VISIBLE);
        ViewParent parent = loadView.getParent();
        if (parent != containerView) {
            containerView.addFooterView(loadView);
        }
    }

    @Override
    public void setLoading(boolean loading) {
        super.setLoading(loading);
        if (mAdapter != null && mAdapter instanceof BaseAdapter) {
            ((BaseAdapter) mAdapter).notifyDataSetChanged();
        }
    }
}
