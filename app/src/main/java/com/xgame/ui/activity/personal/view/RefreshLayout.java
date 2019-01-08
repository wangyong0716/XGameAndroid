package com.xgame.ui.activity.personal.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import com.xgame.R;
import com.xgame.ui.activity.home.view.AbsRefreshLoadLayout;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 * <p>
 * Created by dingning1
 * on 18-1-29.
 */

public class RefreshLayout extends AbsRefreshLoadLayout {

    public interface IRefreshChecker {
        boolean canRefresh();
    }

    private View mContainer;
    private IRefreshChecker mChecker;

    public RefreshLayout(Context context) {
        super(context);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RefreshLayout(Context context, View container) {
        super(context);
        mContainer = container;
        addContainer(context);
    }

    public RefreshLayout(Context context, int layoutId) {
        super(context);
        mContainer = View.inflate(context, layoutId, null);
        addContainer(context);
    }

    @Override
    protected void onInit(Context context, AttributeSet attrs) {
        parseAttrs(context, attrs);
    }

    private void parseAttrs(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RefreshLayout);
        int layoutId = ta.getResourceId(R.styleable.RefreshLayout_container, 0);
        if (layoutId != 0) {
            mContainer = View.inflate(context, layoutId, null);
            addContainer(context);
        }
        ta.recycle();
    }

    public void setChecker(IRefreshChecker checker) {
        mChecker = checker;
    }

    @Override
    protected View inflateContainer(Context context) {
        return mContainer;
    }

    @Override
    protected boolean canLoad() {
        return false;
    }

    @Override
    protected void onRemoveLoadingView(View loadView) {
        // do nothing
    }

    @Override
    protected void onAddLoadingView(View loadView) {
        // do nothing
    }

    @Override
    protected boolean canRefresh() {
        return mChecker == null || mChecker.canRefresh();
    }
}
