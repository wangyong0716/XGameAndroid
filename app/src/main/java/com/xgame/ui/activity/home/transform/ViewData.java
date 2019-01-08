package com.xgame.ui.activity.home.transform;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-30.
 */

interface ViewData {

    int viewType();

    String title(TextView tv);

    String subTitle(TextView tv);

    String image(View v);

    String stamp(TextView tv);

    boolean remind(TextView tv);

    Intent extension();
}
