package com.xgame.social.share.instance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Pair;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.xgame.social.ShareUtil;
import com.xgame.social.share.ImageDecoder;
import com.xgame.social.share.ShareImageObject;
import com.xgame.social.share.ShareListener;
import com.xgame.social.share.SharePlatform;


/**
 * Created by shaohui on 2016/11/18.
 */

public class WxShareInstance extends ShareInstance {

    /**
     * 微信分享限制thumb image必须小于32Kb，否则点击分享会没有反应
     */

    private IWXAPI mIWXAPI;

    private static final int THUMB_SIZE = 32 * 1024 * 8;

    private static final int TARGET_SIZE = 200;

    public WxShareInstance(Context context, String appId) {
        mIWXAPI = WXAPIFactory.createWXAPI(context.getApplicationContext(), appId, true);
        mIWXAPI.registerApp(appId);
    }

    @Override
    public void shareText(int platform, String text, Activity activity, ShareListener listener) {
        if (TextUtils.isEmpty(text)) {
            if (listener != null) {
                listener.onCancel();
            }
            return;
        }
        WXTextObject textObject = new WXTextObject();
        textObject.text = text;

        WXMediaMessage message = new WXMediaMessage();
        message.mediaObject = textObject;
        message.description = text;

        sendMessage(platform, message, buildTransaction("text"));
    }

    @Override
    public void shareMedia(
            final int platform, final String title, final String targetUrl, final String summary,
            final ShareImageObject shareImageObject, final Activity activity, final ShareListener listener) {
        new AsyncTask<Object, Void, byte[]>() {
            @Override
            protected byte[] doInBackground(Object[] objects) {
                try {
                    String imagePath = ImageDecoder.decode(activity.getApplicationContext(), shareImageObject);
                    if (!TextUtils.isEmpty(imagePath)) {
                        return ImageDecoder.compress2Byte(imagePath, TARGET_SIZE, THUMB_SIZE);
                    }
                } catch (Exception e) {
                    onFailed(activity, listener, e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(byte[] bytes) {
                try {
                    listener.shareRequest();
                    WXWebpageObject webpageObject = new WXWebpageObject();
                    webpageObject.webpageUrl = targetUrl;

                    WXMediaMessage message = new WXMediaMessage(webpageObject);
                    message.title = title;
                    message.description = summary;
                    message.thumbData = bytes;

                    sendMessage(platform, message, buildTransaction("webPage"));
                } catch (Throwable e) {
                    onFailed(activity, listener, e);
                }
            }
        }.execute();
    }

    @Override
    public void shareImage(final int platform, final ShareImageObject shareImageObject,
                           final Activity activity, final ShareListener listener) {
        new AsyncTask<Object, Void, Pair<Bitmap, byte[]>>() {
            @Override
            protected Pair<Bitmap, byte[]> doInBackground(Object[] objects) {
                try {
                    String imagePath = ImageDecoder.decode(activity, shareImageObject);
                    return Pair.create(BitmapFactory.decodeFile(imagePath),
                            ImageDecoder.compress2Byte(imagePath, TARGET_SIZE, THUMB_SIZE));
                } catch (Exception e) {
                    onFailed(activity, listener, e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Pair<Bitmap, byte[]> pair) {
                try {
                    listener.shareRequest();

                    WXImageObject imageObject = new WXImageObject(pair.first);

                    WXMediaMessage message = new WXMediaMessage();
                    message.mediaObject = imageObject;
                    message.thumbData = pair.second;

                    sendMessage(platform, message, buildTransaction("image"));
                } catch (Throwable e) {
                    onFailed(activity, listener, e);
                }
            }
        }.execute();

    }

    @Override
    public void handleResult(Intent data) {
        mIWXAPI.handleIntent(data, new IWXAPIEventHandler() {
            @Override
            public void onReq(BaseReq baseReq) {
            }

            @Override
            public void onResp(BaseResp baseResp) {
                if (ShareUtil.mShareListener == null) {
                    return;
                }
                switch (baseResp.errCode) {
                    case BaseResp.ErrCode.ERR_OK:
                        ShareUtil.mShareListener.shareSuccess();
                        break;
                    case BaseResp.ErrCode.ERR_USER_CANCEL:
                        ShareUtil.mShareListener.shareCancel();
                        break;
                    default:
                        ShareUtil.mShareListener.shareFailure(new Exception(baseResp.errStr));
                }
            }
        });
    }

    @Override
    public boolean isInstall(Context context) {
        return mIWXAPI.isWXAppInstalled();
    }

    @Override
    public void recycle() {
        mIWXAPI.detach();
    }

    private void sendMessage(int platform, WXMediaMessage message, String transaction) {
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = transaction;
        req.message = message;
        req.scene = platform == SharePlatform.WX_TIMELINE ? SendMessageToWX.Req.WXSceneTimeline
                : SendMessageToWX.Req.WXSceneSession;
        mIWXAPI.sendReq(req);
    }

    private String buildTransaction(String type) {
        return System.currentTimeMillis() + type;
    }

}
