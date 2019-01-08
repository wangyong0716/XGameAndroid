package com.xgame.common.util;

import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.Toast;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-29.
 */


public final class ToastUtil {

    private ToastUtil() {
    }

    public static void showTip(Context cnt, @StringRes int msgId) {
        showToast(cnt, msgId, false);
    }

    public static void showTip(Context cnt, String msg) {
        showToast(cnt, msg, false);
    }

    public static void showToast(Context cnt, @StringRes int msgId) {
        showToast(cnt, msgId, true);
    }

    public static void showToast(Context cnt, String msg) {
        showToast(cnt, msg, true);
    }

    public static void showToast(Context cnt, String msg, boolean isLong) {
        if (cnt == null) {
            return;
        }
        Toast.makeText(cnt.getApplicationContext(), msg, isLong ? LENGTH_LONG : LENGTH_SHORT)
                .show();
    }

    public static void showToast(Context cnt, int msgId, boolean isLong) {
        if (cnt == null) {
            return;
        }
        Toast.makeText(cnt.getApplicationContext(), msgId, isLong ? LENGTH_LONG : LENGTH_SHORT)
                .show();
    }
}
