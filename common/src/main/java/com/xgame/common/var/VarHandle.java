package com.xgame.common.var;

import android.support.annotation.Nullable;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-24.
 */


public abstract class VarHandle<T> implements IDestructor, IVarHandle<T> {

    @Override
    public final T get() {
        T ret;
        if ((ret = peek()) != null) {
            return ret;
        }
        synchronized (this) {
            if ((ret = peek()) != null) {
                return ret;
            }
            ret = onConstructor();
            if (ret == null) {
                throw new NullPointerException("call onConstructor return null.");
            }
            notifyAll();
            return ret;
        }
    }

    @Override
    public synchronized void destructor() {
    }

    protected abstract T constructor();

    protected abstract T onConstructor();

    @Nullable
    protected abstract T peek();
}
