package com.xgame.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.xgame.R;
import com.xgame.account.UserManager;
import com.xgame.app.XgameApplication;
import com.xgame.base.ClientSettingManager;
import com.xgame.common.util.StatusBarUtil;

/**
 * Created by wuyanzhi on 2018/1/24.
 */

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isLogin() && needLoadClientSettings()) {
            loadClientSettings();
        }
    }

    protected boolean isLogin() {
        return UserManager.getInstance().isLogin();
    }

    private boolean needLoadClientSettings() {
        return !ClientSettingManager.hasDomain() && XgameApplication.getTopActivity() == this;
    }

    protected void loadClientSettings() {
        ClientSettingManager.loadSettings(null);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        setStatusBar();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setStatusBar();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        setStatusBar();
    }

    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.colorPrimary));
        StatusBarUtil.setMiuiStatusBarDarkMode(this, true);
    }
}
