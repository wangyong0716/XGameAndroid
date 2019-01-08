package com.xgame.common.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;

import com.xgame.common.R;

import static android.text.TextUtils.isEmpty;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by dingning1
 * on 18-1-30.
 */

public class LaunchUtils {

    private static final String SCHEME_ROOT = "://";

    private static final String INTENT_SCHEME = "intent:";

    public static void setAction(final View view, final String uri) {
        if (view == null || TextUtils.isEmpty(uri)) {
            return;
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context cnt = view.getContext();
                LaunchUtils.startActivity(cnt, uri, cnt.getPackageName());
            }
        });
    }

    public static void startActivity(Context ctx, String uri) {
        if (ctx == null) {
            return;
        }
        startActivity(ctx, uri, ctx.getPackageName());
    }

    public static void startActivity(Context ctx, String uri, String defPkg) {
        if (TextUtils.isEmpty(uri) || ctx == null) {
            return;
        }
        Intent intent;
        if (uri.startsWith(INTENT_SCHEME)) {
            try {
                intent = Intent.parseUri(uri, Intent.URI_INTENT_SCHEME);
            } catch (Exception e) {
                LogUtil.w("XGame.LaunchUtils", "parse intent failed, " + e + ", uri = " + uri);
                ToastUtil.showToast(ctx, ctx.getString(R.string.lauch_fail, e));
                return;
            }
        } else if (uri.contains(SCHEME_ROOT)) {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(uri.trim()));
        } else {
            intent = new Intent();
            intent.setAction(uri);
        }
        startActivity(ctx, intent, defPkg);
    }

    public static void startActivity(Context ctx, Class<? extends Activity> actClass) {
        if (ctx == null) {
            return;
        }
        startActivity(ctx, new Intent(ctx, actClass), null);
    }

    public static void startActivity(Context ctx, Intent intent) {
        if (ctx == null) {
            return;
        }
        startActivity(ctx, intent, ctx.getPackageName());
    }

    public static void startActivity(Context ctx, Intent intent, String defTarget) {
        if (ctx == null) {
            return;
        }
        try {
            String target = IntentParser.getString(intent, "target");
            if (!isEmpty(target)) {
                intent.setPackage(target);
            } else if (!isEmpty(defTarget)) {
                intent.setPackage(defTarget);
            }
            if (!(ctx instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            ctx.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            ToastUtil.showToast(ctx, ctx.getString(R.string.lauch_fail, e));
        }
    }
}
