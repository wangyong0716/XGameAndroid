package com.xgame.ui.activity.home.transform;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-2-8.
 */


public interface IViewAdapter<T> {

    int viewType(T t);

    String title(T t, TextView tv);

    String subTitle(T t, TextView tv);

    String image(T t, View tv);

    String stamp(T t, TextView tv);

    Intent extension(T t);

    boolean hasRemind(T t, TextView tv);
}
