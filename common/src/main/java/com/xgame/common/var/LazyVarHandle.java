package com.xgame.common.var;

import android.support.annotation.Nullable;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-24.
 */


public abstract class LazyVarHandle<T> extends VarHandle<T> {

    private volatile T mSingle;

    @Override
    protected T onConstructor() {
        T ret = constructor();
        mSingle = ret;
        return ret;
    }

    @Override
    @Nullable
    public T peek() {
        return mSingle;
    }

    @Override
    public synchronized void destructor() {
        super.destructor();
        mSingle = null;
        notifyAll();
    }

}
