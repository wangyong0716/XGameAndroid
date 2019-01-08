package com.xgame.common.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.xgame.common.util.LogUtil;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import retrofit2.Response;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-26.
 */


class FuturePackableCall<T> extends AbsFutureCall<Packable<T>> {

    private static final String TAG = FuturePackableCall.class.getSimpleName();

    private final FutureResponseCall<Packable<T>> mResponseCall;

    private final Class<? extends Packable> mPackClz;

    FuturePackableCall(FutureResponseCall<Packable<T>> responseCall,
            Class<? extends Packable<T>> packClz) {
        this.mResponseCall = responseCall;
        this.mPackClz = packClz;
    }

    @Override
    public FutureCall<Packable<T>> submit() {
        mResponseCall.submit();
        return this;
    }

    @Override
    public FutureCall<Packable<T>> enqueue(final OnCallback<Packable<T>> callback) {
        checkIsExecuted(mResponseCall);
        if (callback == null) {
            throw new NullPointerException("callback is null");
        }
        mResponseCall.enqueue(new OnCallback<Response<Packable<T>>>() {
            @Override
            public void onResponse(@NonNull Response<Packable<T>> result) {
                Packable<T> packable = result.body();
                if (packable == null) {
                    callback.onResponse(createEmptyPack(mPackClz, result.code()));
                } else if (packable.isFailure()){
                    callback.onFailure(packable);
                } else {
                    callback.onResponse(packable);
                }
            }

            @Override
            public void onFailure(Response<Packable<T>> result) {
                LogUtil.w(TAG, "onFailure: " + String.valueOf(result.message()));
                callback.onFailure(createEmptyPack(mPackClz, result != null ? result.code() : 0));
            }
        });
        return this;
    }

    private static Packable createEmptyPack(Class clz, int httpCode) {
        try {
            Packable packable = (Packable) clz.newInstance();
            packable.setHttpCode(httpCode);
            return packable;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("call newInstance fail:" + clz);
        }
    }

    @Override
    @Nullable
    public Packable<T> get(long timeout, TimeUnit unit, boolean throwIfTimeout)
            throws IOException, InterruptedException, TimeoutException {
        Response<Packable<T>> response = mResponseCall.get(timeout, unit, throwIfTimeout);
        if (response == null || response.code() == FutureResponseCall.CODE_FAIL_REQUEST) {
            return null;
        }
        return response.body();
    }

    @Override
    public boolean isExecuted() {
        return mResponseCall.isExecuted();
    }

    @Override
    public void cancel() {
        mResponseCall.cancel();
    }

    @Override
    public boolean isCanceled() {
        return mResponseCall.isCanceled();
    }

    @Override
    public Object clone() {
        return new FuturePackableCall(
                (FutureResponseCall<Packable>) mResponseCall.clone(),
                mPackClz);
    }
}
