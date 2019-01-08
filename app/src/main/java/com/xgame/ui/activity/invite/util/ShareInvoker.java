
package com.xgame.ui.activity.invite.util;

import android.content.Context;
import android.view.View;

import com.xgame.R;
import com.xgame.app.XgameApplication;
import com.xgame.common.util.ToastUtil;
import com.xgame.social.share.SharePlatform;
import com.xgame.ui.activity.invite.view.ShareDialog;
import com.xgame.util.ThirdAppUtil;

import butterknife.internal.DebouncingOnClickListener;

/**
 * Created by Albert
 * on 18-2-6.
 */

public class ShareInvoker {

    private ShareDialog mShareDialog;
    private ShareViewRenderer mShareRenderer;
    private ShareListener mShareListener;

    public ShareInvoker() {
        mShareRenderer = new ShareViewRenderer();
    }

    public void setShareView(View shareView) {
        mShareRenderer.initView(shareView);
    }

    public void shareInDialog() {
        Context context = XgameApplication.getTopActivity();
        if (context == null) {
            return;
        }
        if (mShareDialog == null) {
            ShareDialog.Builder builder = new ShareDialog.Builder(context);
            builder.setTitle(context.getString(R.string.share_to)).setPositiveButton(
                    context.getString(R.string.cancel_text), new DebouncingOnClickListener() {
                        @Override
                        public void doClick(View v) {
                            mShareDialog.dismiss();
                        }
                    }).setCancelable(true);
            mShareDialog = builder.create();
            mShareRenderer.initView(mShareDialog.findViewById(R.id.share_layout));
        }
        mShareDialog.initShareLayout(this);
        mShareDialog.show();
    }

    public void onInviteThirdUser(View v) {
        if (v == null) {
            return;
        }
        int platform = -1;
        switch (v.getId()) {
            case R.id.share_qq:
                platform = SharePlatform.QQ;
                break;
            case R.id.share_wechat:
                platform = SharePlatform.WX;
                break;
            case R.id.share_timeline:
                platform = SharePlatform.WX_TIMELINE;
                break;
        }
        if (platform != -1) {
            inviteThirdUser(platform);
            if (mShareListener != null) {
                mShareListener.onShareProceed(platform);
            }
        }
    }

    private void inviteThirdUser(@SharePlatform.Platform int platform) {
        boolean installed = false;
        switch (platform) {
            case SharePlatform.QQ:
                installed = ThirdAppUtil.isQQInstalled(true);
                break;
            case SharePlatform.WX:
            case SharePlatform.WX_TIMELINE:
                installed = ThirdAppUtil.isWXInstalled(true);
                break;
            case SharePlatform.WEIBO:
                installed = ThirdAppUtil.isWeiboInstalled(true);
                break;
            case SharePlatform.DEFAULT:
                break;
            case SharePlatform.QZONE:
                break;
        }
        if (installed) {
            mShareRenderer.prepareToShare(platform);
        }
    }

    public void setShareListener(ShareListener listener) {
        mShareListener = listener;
    }

    public interface ShareListener {
        void onShareProceed(@SharePlatform.Platform int platform);

        void onShareFailed(int type, String error);
    }
}
