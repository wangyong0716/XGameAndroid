package com.xgame.common.var;

import android.support.annotation.NonNull;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-3.
 */


public abstract class SoftVarHandle<T> extends ReferenceVarHandle<T> {

    @Override
    @NonNull
    protected Reference<T> buildVarRef(T var) {
        return new SoftReference<>(var);
    }

}