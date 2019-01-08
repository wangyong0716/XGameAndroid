package com.xgame.social.share.instance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;

import com.xgame.social.R;
import com.xgame.social.share.ImageDecoder;
import com.xgame.social.share.ShareImageObject;
import com.xgame.social.share.ShareListener;

import java.io.File;

/**
 * Created by shaohui on 2016/11/18.
 */

public class DefaultShareInstance extends ShareInstance {

    @Override
    public void shareText(int platform, String text, Activity activity, ShareListener listener) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        activity.startActivity(Intent.createChooser(sendIntent,
                activity.getResources().getString(R.string.vista_share_title)));
    }

    @Override
    public void shareMedia(int platform, String title, String targetUrl, String summary,
                           ShareImageObject shareImageObject, Activity activity, ShareListener listener) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, String.format("%s %s", title, targetUrl));
        sendIntent.setType("text/plain");
        activity.startActivity(Intent.createChooser(sendIntent,
                activity.getResources().getString(R.string.vista_share_title)));
    }

    @Override
    public void shareImage(int platform, final ShareImageObject shareImageObject,
                           final Activity activity, final ShareListener listener) {
        new AsyncTask<Object, Void, Uri>() {
            @Override
            protected Uri doInBackground(Object[] objects) {
                try {
                    Uri uri =
                            Uri.fromFile(new File(ImageDecoder.decode(activity, shareImageObject)));
                    return uri;
                } catch (Exception e) {
                    onFailed(activity, listener, e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Uri uri) {
                try {
                    listener.shareRequest();
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    shareIntent.setType("image/jpeg");
                    activity.startActivity(Intent.createChooser(shareIntent,
                            activity.getResources().getText(R.string.vista_share_title)));

                } catch (Throwable e) {
                    onFailed(activity, listener ,e);
                }
            }
        }.execute();
    }

    @Override
    public void handleResult(Intent data) {
        // Default share, do nothing
    }

    @Override
    public boolean isInstall(Context context) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        return context.getPackageManager()
                .resolveActivity(shareIntent, PackageManager.MATCH_DEFAULT_ONLY) != null;
    }

    @Override
    public void recycle() {

    }
}
