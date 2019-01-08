package com.xgame.common.api;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-27.
 */


public interface Packable<T> {

    /**
     * entity type must be {@link Data}, {@link Data} Array, {@link Data} Collection.
     *
     * @return such as Business data.
     */
    T data();

    void setHttpCode(int httpCode);

    boolean isFailure();
//    void onIntercept();
}
