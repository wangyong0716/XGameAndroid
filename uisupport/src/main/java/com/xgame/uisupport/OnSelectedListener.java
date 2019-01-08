package com.xgame.uisupport;

import android.support.annotation.Nullable;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 17-9-30.
 */
public interface OnSelectedListener<T> {

    /**
     *
     * @param position
     * @param t null if not adapt
     */
    void onSelected(int position, @Nullable T t);
}
