package com.xgame.common.util;

import java.lang.reflect.Modifier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xgame.common.var.LazyVarHandle;
import com.xgame.common.var.VarHandle;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-24.
 */


public final class GlobalGson {

    private static final int[] sExcludeModifiers = {
            Modifier.STATIC, Modifier.TRANSIENT
    };

    private static final VarHandle<Gson> sGsonVar = new LazyVarHandle<Gson>() {
        @Override
        protected Gson constructor() {
            return new GsonBuilder()
                    .excludeFieldsWithModifiers(sExcludeModifiers)
                    .create();
        }
    };

    private GlobalGson() {
    }

    public static Gson get() {
        return sGsonVar.get();
    }
}
