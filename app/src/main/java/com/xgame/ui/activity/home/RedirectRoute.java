package com.xgame.ui.activity.home;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.xgame.BuildConfig;

import static android.text.TextUtils.isEmpty;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * on 17-12-14.
 */


public abstract class RedirectRoute implements IDispatchAnchorAction {

    public final void performRedirectIntent(Intent intent) {
        final Uri data = intent.getData();
        if (data == null) {
            return;
        }
        String anchor = data.getFragment();
        if (isEmpty(anchor)) {
            return;
        }
        if (anchor.startsWith("/")) {
            Uri uri = data.buildUpon().fragment(null).path(anchor).build();
            Intent in = new Intent(Intent.ACTION_VIEW);
            in.setPackage(BuildConfig.APPLICATION_ID);
            in.setData(uri);
            in.putExtras(intent);
            onHandleIntent(in);
        } else {
            onDispatchAnchorAction(anchor);
        }
    }

    protected abstract void onHandleIntent(@NonNull Intent redirectIntent);
}
