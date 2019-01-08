package com.xgame.common.api;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-26.
 */


abstract class AbsFutureCall<T> implements FutureCall<T> {

    protected static void checkIsExecuted(FutureCall<?> call) {
        if (call == null) {
            throw new IllegalStateException("FutureCall is null");
        }
        if (call.isExecuted()) {
            throw new IllegalStateException("Already executed.");
        }
    }

    protected static void checkIsExecuted(Call<?> call) {
        if (call == null) {
            throw new IllegalStateException("Call is null");
        }
        if (call.isExecuted()) {
            throw new IllegalStateException("Already executed.");
        }
    }

    @Override
    public T get() throws IOException, InterruptedException {
        return get(-1, TimeUnit.MILLISECONDS);
    }

    @Override
    public T get(long timeout, TimeUnit unit)
            throws IOException, InterruptedException {
        try {
            return get(timeout, unit, false);
        } catch (TimeoutException e) {
            throw new IllegalStateException("not reach", e);
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("is abstract");
    }
}
