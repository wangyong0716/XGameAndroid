package com.xgame.social.share.instance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.xgame.social.share.ShareImageObject;
import com.xgame.social.share.ShareListener;

/**
 * Created by shaohui on 2016/11/18.
 */

public abstract class ShareInstance {

    public abstract void shareText(int platform, String text, Activity activity, ShareListener listener);

    public abstract void shareMedia(int platform, String title, String targetUrl, String summary,
                    ShareImageObject shareImageObject, Activity activity, ShareListener listener);

    public abstract void shareImage(int platform, ShareImageObject shareImageObject, Activity activity,
                    ShareListener listener);

    public abstract void handleResult(Intent data);

    public abstract boolean isInstall(Context context);

    public abstract void recycle();

    protected void onFailed(Activity activity, ShareListener listener, Throwable e) {
        if (listener != null) {
            listener.shareFailure(new Exception(e));
        }
        if (activity != null && !activity.isFinishing()) {
            activity.finish();
        }
    }
}
