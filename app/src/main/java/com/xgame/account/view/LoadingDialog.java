package com.xgame.account.view;

import android.app.Activity;

import java.lang.ref.WeakReference;

import com.xgame.common.widget.CustomLoadingDialog;


public class LoadingDialog {
    private CustomLoadingDialog.Builder customLoadingDialog;
    private WeakReference<Activity> mActivity;

    public LoadingDialog(Activity activity) {
        mActivity = new WeakReference<>(activity);
    }

    /**
     * 展示Loading弹出框
     */
    public void showLoadingDialog(int loadingText) {
        if (mActivity.get() != null) {
            if (customLoadingDialog != null) {
                customLoadingDialog.dismissLoadingDialog();
            }
            customLoadingDialog = new CustomLoadingDialog.Builder(mActivity.get(), loadingText);
            customLoadingDialog.create().show();
        }
    }

    /**
     * 展示Loading弹出框
     */
    public void showLoadingDialog(String loadingText) {
        if (mActivity.get() != null) {
            if (customLoadingDialog != null) {
                customLoadingDialog.dismissLoadingDialog();
            }
            customLoadingDialog = new CustomLoadingDialog.Builder(mActivity.get(), loadingText);
            customLoadingDialog.create().show();
        }
    }

    public void updateLoadingDialog(String loadingText) {
        if (mActivity.get() != null) {
            if (customLoadingDialog != null) {
                customLoadingDialog.updateLoadingMsg(loadingText);
            }
        }
    }
    /**
     * 展示Loading弹出框
     */
    public void showLoadingDialog() {
        if (mActivity.get() != null) {
            if (customLoadingDialog == null) {
                customLoadingDialog = new CustomLoadingDialog.Builder(mActivity.get());
            }
            customLoadingDialog.create().show();
        }
    }

    public void dismissLoadingDialog() {
        if (customLoadingDialog != null) {
            customLoadingDialog.dismissLoadingDialog();
        }
    }
}
