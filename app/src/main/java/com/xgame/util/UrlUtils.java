package com.xgame.util;

import com.miui.zeus.mario.sdk.MarioSdk;

import android.text.TextUtils;
import android.util.ArrayMap;

import java.util.Map;

import com.xgame.account.UserManager;

import okhttp3.HttpUrl;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by dingning1
 * on 18-2-3.
 */


public class UrlUtils {

    private static Map<String, String> sRequestArgs;

    public static String getRequestUrl(String url) {
        return addArgsToUrl(url, getRequestArgs());
    }

    public static String addArgsToUrl(String url, Map<String, String> args) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        if (args == null || args.isEmpty()) {
            return url;
        }
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl != null) {
            HttpUrl.Builder builder = httpUrl.newBuilder();
            for (Map.Entry<String, String> entry : args.entrySet()) {
                builder.addQueryParameter(entry.getKey(), entry.getValue());
            }
            return builder.toString();
        } else {
            return url;
        }
    }

    private static Map<String, String> getRequestArgs() {
        if (sRequestArgs == null) {
            sRequestArgs = new ArrayMap<>();
            sRequestArgs.put("clientInfo", MarioSdk.getClientInfo());
        }
        String token = UserManager.getInstance().getToken();
        if (token != null && token.length() > 0) {
            sRequestArgs.put("token", token);
        }
        return sRequestArgs;
    }
}
