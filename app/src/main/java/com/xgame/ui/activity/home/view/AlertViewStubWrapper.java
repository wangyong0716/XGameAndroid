package com.xgame.ui.activity.home.view;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-2-2.
 */


public class AlertViewStubWrapper {

    private final ViewStub mAlertViewStub;

    private OnAlertListener mOnAlert;

    private View mContainerView;

    private Intercept mInterceptShowAlert;

    private Object mShowData;

    private View mAlertView;

    public AlertViewStubWrapper(ViewStub alertView) {
        if (alertView == null) {
            throw new NullPointerException("view is null");
        }
        this.mAlertViewStub = alertView;
        showAlertView(false, false);
    }

    public OnAlertListener getOnAlertListener() {
        return this.mOnAlert;
    }

    public <T> AlertViewStubWrapper setOnAlertListener(OnAlertListener<T> onAlert) {
        this.mOnAlert = onAlert;
        return this;
    }

    private void showAlertView(boolean show, boolean notify) {
        if (show && mAlertView == null) {
            mAlertView = mAlertViewStub.inflate();
        }
        if (mAlertView == null) {
            return;
        }
        mAlertView.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show && notify && mOnAlert != null) {
            //noinspection SingleStatementInBlock
            mOnAlert.onAlert(mAlertView, mShowData);
        }
        mShowData = null;
    }

    public <T extends View> AlertViewStubWrapper attachTo(T t) {
        return attachTo(t, null);
    }

    public <T extends View> AlertViewStubWrapper attachTo(T t, Intercept<T> intercept) {
        mContainerView = t;
        mInterceptShowAlert = intercept;
        return this;
    }

    public void showAlertViewIfNeed(Object showData) {
        if (!containerHasData()) {
            mShowData = showData;
            showAlertView(true, true);
        }
    }

    public void hideAlertView() {
        showAlertView(false, false);
    }

    public boolean isShown() {
        return mAlertView != null && mAlertView.isShown();
    }

    private boolean containerHasData() {
        View cv = mContainerView;
        if (mInterceptShowAlert != null) {
            //noinspection unchecked
            return mInterceptShowAlert.interceptShowAlert(cv);
        } else if (cv != null && cv instanceof ViewGroup) {
            return ((ViewGroup) cv).getChildCount() > 0;
        } else {
            throw new IllegalStateException("you need set Intercept");
        }
    }

    public interface OnAlertListener<T> {

        void onAlert(View alertView, T showData);
    }

    public interface Intercept<T> {

        boolean interceptShowAlert(T t);
    }
}
