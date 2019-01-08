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


public class SimpleHolder implements Holder {

    private final int layout;

    private SimpleHolder(int layout) {
        this.layout = layout;
    }

    public static Holder create(@LayoutRes int layout) {
        return new SimpleHolder(layout);
    }

    @Override
    public int layout() {
        return this.layout;
    }

    /**
     * do custom inflate
     */
    @Override
    public View onInflate(LayoutInflater inflater, ViewGroup parent) {
        return null;
    }
}
