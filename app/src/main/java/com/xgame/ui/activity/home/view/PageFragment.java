package com.xgame.ui.activity.home.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import com.xgame.common.util.LogUtil;
import com.xgame.ui.activity.home.MailBox;
import com.xgame.ui.activity.home.MailBox.MailMessage;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-28.
 */


public class PageFragment extends Fragment {

    private static final String TAG = PageFragment.class.getSimpleName();

    protected MailBox mMailBox;

    private boolean isViewCreated;

    private boolean mDeferredVisibleToUser;

    private boolean mIsVisibleToUser;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.isViewCreated = true;
        if (mDeferredVisibleToUser) {
            mDeferredVisibleToUser = false;
            mIsVisibleToUser = true;
            onVisibleToUser();
        } else {
            mIsVisibleToUser = false;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (isViewCreated) {
                mIsVisibleToUser = true;
                onVisibleToUser();
            } else {
                mIsVisibleToUser = false;
                mDeferredVisibleToUser = true;
            }
        } else {
            mIsVisibleToUser = false;
            mDeferredVisibleToUser = false;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MailBox) {
            mMailBox = (MailBox) context;
        } else {
            LogUtil.w(TAG, context.toString() +
                    " should implement MailBox for communication");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMailBox = null;
    }

    protected void onVisibleToUser() {
    }

    public boolean isVisibleToUser() {
        return this.mIsVisibleToUser;
    }

    protected void postMail(MailMessage msg) {
        if (mMailBox != null) {
            mMailBox.onMailReceive(msg);
        }
    }
}
