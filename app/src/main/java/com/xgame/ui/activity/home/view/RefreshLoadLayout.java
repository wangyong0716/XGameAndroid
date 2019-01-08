package com.xgame.ui.activity.home.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-29.
 */


public class RefreshLoadLayout extends AbsRefreshLoadLayout {

    private LoadCallback mLoadCallback;

    private View mContainerView;

    public RefreshLoadLayout(Context context) {
        super(context);
    }

    public RefreshLoadLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadCallback getLoadCallback() {
        return this.mLoadCallback;
    }

    public <V> void setLoadCallback(LoadCallback<V> callback) {
        this.mLoadCallback = callback;
    }

    @Override
    protected void onInit(Context context, AttributeSet attrs) {
    }

    @Override
    protected View inflateContainer(Context context) {
        return null;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        if (childCount != 2) {
            throw new IllegalStateException("RefreshLoadLayout child must be only one !");
        }
        mContainerView = getChildAt(1);
    }

    private void checkValidContainerCallback() {
        if (mLoadCallback == null) {
            throw new IllegalStateException("must be set LoadCallback first !");
        }
    }

    @Override
    protected boolean canLoad() {
        checkValidContainerCallback();
        return mLoadCallback.canLoad(mContainerView);
    }

    @Override
    protected void onRemoveLoadingView(View loadView) {
        checkValidContainerCallback();
        mLoadCallback.onRemoveLoadingView(mContainerView, loadView);
    }

    @Override
    protected void onAddLoadingView(View loadView) {
        checkValidContainerCallback();
        mLoadCallback.onAddLoadingView(mContainerView, loadView);
    }

    @Override
    protected View onCreateLoadingView(Context context) {
        View view;
        if (null == mLoadCallback ||
                null == (view = mLoadCallback.onCreateLoadingView(context))) {
            return super.onCreateLoadingView(context);
        }
        return view;
    }

    public final View getContainerView() {
        return this.mContainerView;
    }

    public interface LoadCallback<V> {

        boolean canLoad(V containerView);

        void onRemoveLoadingView(V containerView, View loadView);

        void onAddLoadingView(V containerView, View loadView);

        View onCreateLoadingView(Context context);
    }

    public static abstract class AbsLoadCallback<V> implements LoadCallback<V> {

        @Override
        public View onCreateLoadingView(Context context) {
            return null;
        }
    }

}
