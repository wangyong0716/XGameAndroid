package com.xgame.ui.activity.home.transform;

import android.widget.TextView;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-30.
 */

public abstract class ViewAdapter<T> implements IViewAdapter<T> {

    @Override
    public boolean hasRemind(T t, TextView tv){
        return false;
    }
}
