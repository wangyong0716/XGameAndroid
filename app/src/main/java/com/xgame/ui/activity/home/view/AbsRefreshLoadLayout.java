package com.xgame.ui.activity.home.view;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.xgame.R;
import com.xgame.common.var.LazyVarHandle;
import com.xgame.common.var.VarHandle;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.xgame.common.util.UiUtil.isInMainThread;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 * <p>
 * Created by jackwang
 * on 18-1-28.
 */


public abstract class AbsRefreshLoadLayout extends SwipeRefreshLayout {

    protected boolean isLoading;

    private int mTouchSlop;

    private float mDownY;

    private float mUpY;

    private RefreshLoadListener mRefreshLoadListener;

    private View mContainer;

    private VarHandle<View> mLoadingViewVar = new LazyVarHandle<View>() {
        @Override
        protected View constructor() {
            return onCreateLoadingView(getContext());
        }
    };

    private OnRefreshListener onRefresh = new OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (mRefreshLoadListener != null) {
                mRefreshLoadListener.onRefresh();
            }
        }
    };

    public AbsRefreshLoadLayout(Context context) {
        super(context);
        init(context, null);
    }

    public AbsRefreshLoadLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    protected final void init(Context context, AttributeSet attrs) {
        super.setOnRefreshListener(onRefresh);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        addContainer(context);
        onInit(context, attrs);
    }

    protected final void addContainer(Context context) {
        View container = inflateContainer(context);
        if (container != null) {
            addView(container, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
        }
        this.mContainer = container;
    }

    @Override
    @Deprecated
    public final void setOnRefreshListener(OnRefreshListener listener) {
        throw new IllegalStateException("Deprecated, instead of setRefreshLoadListener");
    }

    public void setRefreshLoadListener(RefreshLoadListener l) {
        this.mRefreshLoadListener = l;
    }

    public boolean isLoading() {
        return this.isLoading;
    }

    public void setLoading(final boolean loading) {
        if (!isInMainThread()) {
            post(new Runnable() {
                @Override
                public void run() {
                    setLoading(loading);
                }
            });
            return;
        }
        if (isLoading == loading) {
            return;
        }
        isLoading = loading;
        if (isLoading) {
            onAddLoadingView(mLoadingViewVar.get());
        } else {
            onRemoveLoadingView(mLoadingViewVar.get());
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mUpY = 0;
                mDownY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                postLoadIfNeed();
                break;
            case MotionEvent.ACTION_UP:
                mUpY = getY();
                postLoadIfNeed();
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean ret = super.onInterceptTouchEvent(ev);
        return canRefresh() && ret;
    }

    private void postLoadIfNeed() {
        post(new Runnable() {
            @Override
            public void run() {
                doLoadIfNeed();
            }
        });
    }

    protected void doLoadIfNeed() {
        if (mRefreshLoadListener != null) {
            if ((mDownY - mUpY) >= mTouchSlop && !isLoading && canLoad()) {
                setLoading(true);
                mRefreshLoadListener.onLoad();
            }
        }
    }

    protected View onCreateLoadingView(Context context) {
        return LayoutInflater.from(context).inflate(R.layout.footer_load_list_view, this, false);
    }

    protected View getContainerView() {
        return this.mContainer;
    }

    protected abstract void onInit(Context context, AttributeSet attrs);

    protected abstract View inflateContainer(Context context);

    protected abstract boolean canLoad();

    protected abstract void onRemoveLoadingView(View loadView);

    protected abstract void onAddLoadingView(View loadView);

    protected boolean canRefresh() {
        return true;
    }

    public interface RefreshLoadListener {

        void onRefresh();

        void onLoad();
    }

    public static abstract class RefreshCallbacks implements RefreshLoadListener {

        @Override
        public void onLoad() {
            // Do nothing, just need refresh.
        }
    }
}
