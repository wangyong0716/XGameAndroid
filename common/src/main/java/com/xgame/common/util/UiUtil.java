package com.xgame.common.util;

import android.graphics.Rect;
import android.os.Looper;
import android.view.TouchDelegate;
import android.view.View;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-27.
 */


public final class UiUtil {

    private static final int DP_EXTRA = 50;

    private UiUtil() {
    }
    public static boolean isInMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static void checkInMainThread() {
        if (!isInMainThread()) {
            throw new IllegalStateException("Must run in main thread!!");
        }
    }

    public static <T> T getTag(View view, int tagKey) {
        Object tag = tagKey > 0 ? view.getTag(tagKey) : view.getTag();
        return (T) tag;
    }

    public static void expandHitArea(final View view) {
        final View parent = (View) view.getParent();
        parent.post(new Runnable() {
            @Override
            public void run() {
                Rect touchRect = new Rect();
                view.getHitRect(touchRect);
                int extra = PixelUtil.dip2px(view.getContext(), DP_EXTRA);
                touchRect.inset(-extra, -extra);
                parent.setTouchDelegate(new TouchDelegate(touchRect, view));
            }
        });
    }
}
