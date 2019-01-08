
package com.xgame.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.xgame.account.UserManager;
import com.xgame.base.ClientSettingManager;
import com.xgame.common.util.SharePrefUtils;
import com.xgame.ui.activity.home.HomePageActivity;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkJump();
    }

    @Override
    protected void loadClientSettings() {
        ClientSettingManager.initSettings();
        ClientSettingManager.reloadSettings();
    }

    private void checkJump() {
        if (UserManager.getInstance().isLogin()) {
            startActivity(new Intent(this, HomePageActivity.class));
        } else if (!SharePrefUtils.getBoolean(this, GuideActivity.KEY_SHOW_GUIDE, false)) {
            startActivity(new Intent(this, GuideActivity.class));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish();
    }
}
