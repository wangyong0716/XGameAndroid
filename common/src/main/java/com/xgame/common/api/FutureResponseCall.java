package com.xgame.common.api;

import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-25.
 */


class FutureResponseCall<R> extends AbsFutureCall<Response<R>> {

    static final int CODE_FAIL_REQUEST = 499;

    private final Executor mCallbackExecutor;

    private final Call<R> mCall;

    private volatile Response<?> mResult;

    private final Object mLock = new Object();

    FutureResponseCall(Call<R> call, Executor callbackExecutor) {
        if (call == null) {
            throw new NullPointerException("call is null");
        }
        this.mCall = call;
        this.mCallbackExecutor = callbackExecutor;
    }
//    private Response<R> execute() throws IOException {
//        return mCall.clone().execute();
//    }

    private void submitIfNeed() {
        if (mCall.isExecuted()) {
            return;
        }
        submit();
    }

    @Override
    public FutureCall<Response<R>> submit() {
        checkIsExecuted(mCall);
        mCall.enqueue(new CallCallback(null));
        return this;
    }

    @Override
    public FutureCall<Response<R>> enqueue(OnCallback<Response<R>> callback) {
        checkIsExecuted(mCall);
        if (callback == null) {
            throw new NullPointerException("callback is null");
        }
        mCall.enqueue(new CallCallback<>(callback));
        return this;
    }

    @Override
    @Nullable
    public Response<R> get(long timeout, TimeUnit unit, boolean throwIfTimeout)
            throws IOException, InterruptedException, TimeoutException {
        if (mResult != null) {
            return (Response<R>) mResult;
        }
        submitIfNeed();
        final long waitTime = timeout > 0 ? unit.toMillis(timeout) : -1;
        final Response<R> ret;
        boolean isTimeOut = false;
        try {
            synchronized (mLock) {
                while (unInterrupted() && mResult == null && !isTimeOut) {
                    if (waitTime < 0) {
                        mLock.wait();
                    } else {
                        mLock.wait(waitTime);
                        isTimeOut = true;
                    }
                }
                ret = (Response<R>) mResult;
                mLock.notifyAll();
            }
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
        if (throwIfTimeout && isTimeOut && ret == null) {
            throw new TimeoutException(String.format("timeout: %s, %s", timeout, unit));
        }
        return ret;
    }

    private boolean unInterrupted() {
        return !Thread.currentThread().isInterrupted();
    }

    @Override
    public boolean isExecuted() {
        return mCall.isExecuted();
    }

    @Override
    public void cancel() {
        mCall.cancel();
    }

    @Override
    public boolean isCanceled() {
        return mCall.isCanceled();
    }

    @Override
    public Object clone() {
        return new FutureResponseCall<>(mCall.clone(), mCallbackExecutor);
    }

    private void execInCallbackExecutor(Runnable task) {
        final Executor e = mCallbackExecutor;
        if (e != null) {
            e.execute(task);
        } else {
            task.run();
        }
    }

    static final class NoContentResponseBody extends ResponseBody {

        private final MediaType contentType;

        private final long contentLength;

        private final Throwable cause;

        NoContentResponseBody(Throwable t) {
            this(null, -1, t);
        }

        NoContentResponseBody(MediaType contentType, long contentLength, Throwable t) {
            this.contentType = contentType;
            this.contentLength = contentLength;
            this.cause = t;
        }

        @Override
        public MediaType contentType() {
            return contentType;
        }

        @Override
        public long contentLength() {
            return contentLength;
        }

        @Override
        public BufferedSource source() {
            throw new IllegalStateException("Cannot read raw response body of a converted body.");
        }

        @Override
        public String toString() {
            return "NoContentResponseBody{" +
                    "contentType=" + contentType +
                    ", contentLength=" + contentLength +
                    ", cause=" + cause +
                    '}';
        }
    }

    private class CallCallback<R> implements Callback<R> {

        private final OnCallback<Response<R>> nOnCallback;

        CallCallback(OnCallback<Response<R>> onCallback) {
            this.nOnCallback = onCallback;
        }

        @Override
        public void onResponse(Call<R> call, final Response<R> response) {
            synchronized (mLock) {
                mResult = response;
                mLock.notifyAll();
            }
            if (nOnCallback != null) {
                execInCallbackExecutor(new Runnable() {
                    @Override
                    public void run() {
                        nOnCallback.onResponse(response);
                    }
                });
            }
        }

        @Override
        public void onFailure(Call<R> call, Throwable t) {
            synchronized (mLock) {
                mResult = Response.error(CODE_FAIL_REQUEST, new NoContentResponseBody(t));
                mLock.notifyAll();
            }
            if (nOnCallback != null) {
                execInCallbackExecutor(new Runnable() {
                    @Override
                    public void run() {
                        nOnCallback.onFailure((Response) mResult);
                    }
                });
            }
        }
    }
}
