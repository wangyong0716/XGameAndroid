package com.xgame.common.var;

import android.support.annotation.NonNull;

import java.lang.ref.Reference;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-3.
 */


abstract class ReferenceVarHandle<T> extends VarHandle<T> {

    private Reference<T> mRef;

    @Override
    protected T onConstructor() {
        final T ret = constructor();
        mRef = buildVarRef(ret);
        return ret;
    }

    @NonNull
    protected abstract Reference<T> buildVarRef(T var);

    @Override
    protected T peek() {
        final Reference<T> ref = mRef;
        T ret;
        if (ref != null && (ret = ref.get()) != null) {
            return ret;
        }
        return null;
    }

    @Override
    public synchronized void destructor() {
        super.destructor();
        mRef = null;
    }
}
