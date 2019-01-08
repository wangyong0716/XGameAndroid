
package com.xgame.account;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.View;

import com.xgame.R;
import com.xgame.app.XgameApplication;
import com.xgame.common.util.ExecutorHelper;
import com.xgame.push.PushManager;
import com.xgame.ui.activity.LoginActivity;
import com.xgame.util.dialog.NormalAlertDialog;

public class TokenHandler {
    public static final int CODE_TOKEN_EMPTY = 60000;
    public static final int CODE_TOKEN_INVALID = 60001;
    public static final int CODE_TOKEN_EXPIRED = 60002;
    public static final int CODE_TOKEN_UPDATE = 60003;
    public static final int CODE_TOKEN_FORBIDDEN = 65000; // 可能未登录也会返回


    private static NormalAlertDialog mDialog;

    public static void handleTokenCode(final int code, final String msg) {
        switch (code) {
            case CODE_TOKEN_EMPTY:
            case CODE_TOKEN_INVALID:
            case CODE_TOKEN_EXPIRED:
                PushManager.unRegisterRegIdToServer();
                ExecutorHelper.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        showDialog(code, XgameApplication.getApplication().getResources()
                                .getString(R.string.token_invalid_and_relogin));
                    }
                });
                break;
            case CODE_TOKEN_UPDATE:
                ExecutorHelper.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        showDialog(code, XgameApplication.getApplication().getResources()
                                .getString(R.string.token_login_on_another_device));
                    }
                });
                break;
            case CODE_TOKEN_FORBIDDEN:
                ExecutorHelper.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        showDialog(code,
                                TextUtils.isEmpty(msg)
                                        ? XgameApplication.getApplication().getResources()
                                                .getString(R.string.account_forbid)
                                        : msg);
                    }
                });
                break;
            default:
                break;
        }
    }

    private static void showDialog(final int code, String message) {
        Activity activity = XgameApplication.getTopActivity();
        if (activity == null) {
            return;
        }
        if (mDialog != null && mDialog.isShowing()) {
            try {
                mDialog.dismiss();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        mDialog = new NormalAlertDialog.Builder(activity).setCancelable(false).setMessage(message)
                .setPositiveButton(android.R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBtnClick(code, v.getContext());
                        mDialog = null;
                    }
                }).show();
    }

    private static void onBtnClick(int code, Context context) {
        switch (code) {
            case CODE_TOKEN_EMPTY:
            case CODE_TOKEN_INVALID:
            case CODE_TOKEN_EXPIRED:
            case CODE_TOKEN_UPDATE:
                UserManager.getInstance().clearUser();
                LoginActivity.reLogin(context);
                break;
            case CODE_TOKEN_FORBIDDEN:
                android.os.Process.killProcess(android.os.Process.myPid());
                break;
            default:
                break;
        }
    }

    public static void dismissDialog(Activity activity) {
        if (mDialog != null && mDialog.isShowing()) {
            Context context = mDialog.getContext();
            if (context instanceof ContextThemeWrapper) {
                context = ((ContextThemeWrapper) context).getBaseContext();
            }
            if (context == activity) {
                try {
                    mDialog.dismiss();
                    mDialog = null;
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
