package com.xgame.common.api;

import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-25.
 */


public interface FutureCall<T> extends Cloneable {

    /**
     * 提交任务到后台线程并开始请求网络
     * @return this.
     */
    FutureCall<T> submit();

    /**
     * 提交任务到后台线程并开始请求网络，返回结果以后会回调callback
     * @param callback response callback.
     * @return this.
     */
    FutureCall<T> enqueue(OnCallback<T> callback);

    /**
     * 这个方法是阻塞的！直到获取到结果。
     * @return success data,other null, if failure.
     * @throws IOException
     * @throws InterruptedException
     */
    T get() throws IOException, InterruptedException;

    /**
     * 这个方法是阻塞的！直到获取到结果或者超时。如果超时不会抛出异常，而是返回null
     * @param timeout
     * @param unit
     * @return success data,other null, if failure.
     * @throws IOException
     * @throws InterruptedException
     */
    T get(long timeout, TimeUnit unit) throws IOException, InterruptedException;

    /**
     * 这个方法是阻塞的！直到获取到结果或者超时。
     * @param timeout
     * @param unit
     * @param throwIfTimeout true if set, when timeout throw TimeoutException
     * @return success data,other null, if failure.
     * @throws IOException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    @Nullable
    T get(long timeout, TimeUnit unit, boolean throwIfTimeout)
            throws IOException, InterruptedException, TimeoutException;

    boolean isExecuted();

    void cancel();

    boolean isCanceled();

    Object clone() throws CloneNotSupportedException;

}
