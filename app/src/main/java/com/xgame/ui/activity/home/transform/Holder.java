package com.xgame.ui.activity.home.transform;

import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-30.
 */


public interface Holder {

    @LayoutRes
    int layout();

    View onInflate(LayoutInflater inflater, ViewGroup parent);
}
