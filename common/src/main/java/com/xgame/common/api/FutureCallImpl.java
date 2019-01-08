package com.xgame.common.api;

import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import retrofit2.Response;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-27.
 */


class FutureCallImpl<T> extends AbsFutureCall<T> {

    private final FutureResponseCall<T> mDelegate;

    FutureCallImpl(FutureResponseCall<T> responseCall) {
        this.mDelegate = responseCall;
    }

    @Override
    public FutureCall<T> submit() {
        mDelegate.submit();
        return this;
    }

    @Override
    public FutureCall<T> enqueue(final OnCallback<T> callback) {
        checkIsExecuted(mDelegate);
        if (callback == null) {
            throw new NullPointerException("callback is null");
        }
        mDelegate.enqueue(new OnCallback<Response<T>>() {
            @Override
            public void onResponse(Response<T> result) {
                callback.onResponse(result.body());
            }

            @Override
            public void onFailure(Response<T> result) {
                callback.onFailure(result.body());
            }
        });
        return this;
    }

    @Nullable
    @Override
    public T get(long timeout, TimeUnit unit, boolean throwIfTimeout)
            throws IOException, InterruptedException, TimeoutException {
        Response<T> resp = mDelegate.get(timeout, unit, throwIfTimeout);
        return resp != null ? resp.body() : null;
    }

    @Override
    public boolean isExecuted() {
        return mDelegate.isExecuted();
    }

    @Override
    public void cancel() {
        mDelegate.cancel();
    }

    @Override
    public boolean isCanceled() {
        return mDelegate.isCanceled();
    }

    @Override
    public Object clone() {
        return new FutureCallImpl(mDelegate);
    }
}