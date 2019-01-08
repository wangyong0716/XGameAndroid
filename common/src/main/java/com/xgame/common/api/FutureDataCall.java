package com.xgame.common.api;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-26.
 */


class FutureDataCall<T> extends AbsFutureCall<T> {

    private final FuturePackableCall<T> mPackCall;

    FutureDataCall(FuturePackableCall<T> packCall) {
        checkIsExecuted(packCall);
        this.mPackCall = packCall;
    }

    @Override
    public FutureCall<T> submit() {
        mPackCall.submit();
        return this;
    }

    @Override
    public FutureCall<T> enqueue(final OnCallback<T> callback) {
        checkIsExecuted(mPackCall);
        if (callback == null) {
            throw new NullPointerException("callback is null");
        }
        mPackCall.enqueue(new OnCallback<Packable<T>>() {
            @Override
            public void onResponse(@NonNull Packable<T> result) {
                callback.onResponse(result.data());
            }

            @Override
            public void onFailure(Packable<T> result) {
                callback.onFailure(null);
            }

        });
        return this;
    }

    @Override
    public T get(long timeout, TimeUnit unit, boolean throwIfTimeout)
            throws IOException, InterruptedException, TimeoutException {
        Packable<T> pack = mPackCall.get(timeout, unit, throwIfTimeout);
        if (pack == null) {
            return null;
        }
        return pack.data();
    }

    @Override
    public boolean isExecuted() {
        return mPackCall.isExecuted();
    }

    @Override
    public void cancel() {
        mPackCall.cancel();
    }

    @Override
    public boolean isCanceled() {
        return mPackCall.isCanceled();
    }

    @Override
    public Object clone() {
        return new FutureDataCall<>((FuturePackableCall<Object>) mPackCall.clone());
    }
}
