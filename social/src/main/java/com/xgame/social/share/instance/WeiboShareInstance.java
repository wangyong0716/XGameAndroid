package com.xgame.social.share.instance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Pair;

import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMessageToWeiboResponse;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.constant.WBConstants;
import com.xgame.social.ShareUtil;
import com.xgame.social.share.ImageDecoder;
import com.xgame.social.share.ShareImageObject;
import com.xgame.social.share.ShareListener;

/**
 * Created by shaohui on 2016/11/18.
 */

public class WeiboShareInstance extends ShareInstance {
    /**
     * 微博分享限制thumb image必须小于2097152，否则点击分享会没有反应
     */

    private IWeiboShareAPI mWeiboShareAPI;

    private static final int TARGET_SIZE = 1024;

    private static final int TARGET_LENGTH = 2097152;

    public WeiboShareInstance(Context context, String appId) {
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(context, appId);
        mWeiboShareAPI.registerApp();
    }

    @Override
    public void shareText(int platform, String text, Activity activity, ShareListener listener) {
        TextObject textObject = new TextObject();
        textObject.text = text;
        WeiboMultiMessage message = new WeiboMultiMessage();
        message.textObject = textObject;

        sendRequest(activity, message);
    }

    @Override
    public void shareMedia(int platform, final String title, final String targetUrl, String summary,
                           ShareImageObject shareImageObject, final Activity activity,
                           final ShareListener listener) {
        String content = String.format("%s %s", title, targetUrl);
        shareTextOrImage(shareImageObject, content, activity, listener);
    }

    @Override
    public void shareImage(int platform, ShareImageObject shareImageObject, Activity activity,
            ShareListener listener) {
        shareTextOrImage(shareImageObject, null, activity, listener);
    }

    @Override
    public void handleResult(Intent intent) {
        if (ShareUtil.mShareListener == null) {
            return;
        }
        SendMessageToWeiboResponse baseResponse =
                new SendMessageToWeiboResponse(intent.getExtras());

        switch (baseResponse.errCode) {
            case WBConstants.ErrorCode.ERR_OK:
                ShareUtil.mShareListener.shareSuccess();
                break;
            case WBConstants.ErrorCode.ERR_FAIL:
                ShareUtil.mShareListener.shareFailure(new Exception(baseResponse.errMsg));
                break;
            case WBConstants.ErrorCode.ERR_CANCEL:
                ShareUtil.mShareListener.shareCancel();
                break;
            default:
                ShareUtil.mShareListener.shareFailure(new Exception(baseResponse.errMsg));
        }
    }

    @Override
    public boolean isInstall(Context context) {
        return mWeiboShareAPI.isWeiboAppInstalled();
    }

    @Override
    public void recycle() {
        mWeiboShareAPI = null;
    }

    private void shareTextOrImage(final ShareImageObject shareImageObject, final String text,
            final Activity activity, final ShareListener listener) {
        new AsyncTask<Object, Void, Pair<String, byte[]>>() {
            @Override
            protected Pair<String, byte[]> doInBackground(Object[] objects) {
                try {
                    String path = ImageDecoder.decode(activity, shareImageObject);
                    return Pair.create(path,
                            ImageDecoder.compress2Byte(path, TARGET_SIZE, TARGET_LENGTH));
                } catch (Exception e) {
                    onFailed(activity, listener, e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Pair<String, byte[]> pair) {
                try {
                    listener.shareRequest();
                    ImageObject imageObject = new ImageObject();
                    imageObject.imageData = pair.second;
                    imageObject.imagePath = pair.first;

                    WeiboMultiMessage message = new WeiboMultiMessage();
                    message.imageObject = imageObject;
                    if (!TextUtils.isEmpty(text)) {
                        TextObject textObject = new TextObject();
                        textObject.text = text;

                        message.textObject = textObject;
                    }

                    sendRequest(activity, message);

                } catch (Throwable e) {
                    onFailed(activity, listener, e);
                }
            }
        }.execute();
    }

    private void sendRequest(Activity activity, WeiboMultiMessage message) {
        if (activity == null || message == null) {
            return;
        }
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = message;
        mWeiboShareAPI.sendRequest(activity, request);
    }
}
