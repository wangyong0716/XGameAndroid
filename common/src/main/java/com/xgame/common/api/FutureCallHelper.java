package com.xgame.common.api;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-2-3.
 */


public final class FutureCallHelper {

    private FutureCallHelper() {

    }

    public static <T> T get(FutureCall<T> call) {
        return get(call, 10);
    }

    public static <T> T get(FutureCall<T> call, long sec) {
        return get(call, sec, TimeUnit.SECONDS);
    }

    public static <T> T get(FutureCall<T> call, long timeout, TimeUnit unit) {
        try {
            return call.get(timeout, unit);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
