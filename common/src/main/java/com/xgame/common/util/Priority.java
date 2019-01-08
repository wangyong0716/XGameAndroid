package com.xgame.common.util;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({Priority.LOW, Priority.NORMAL, Priority.HIGH})
@Retention(RetentionPolicy.SOURCE)
public @interface Priority {

    int LOW = 0;

    int NORMAL = 1;

    int HIGH = 2;
}