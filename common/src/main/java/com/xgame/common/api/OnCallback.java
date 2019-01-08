package com.xgame.common.api;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-25.
 */


public interface OnCallback<T> {

    void onResponse(T result);

    void onFailure(T result);
}
