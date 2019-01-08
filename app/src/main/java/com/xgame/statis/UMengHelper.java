
package com.xgame.statis;

import android.app.Activity;

import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.xgame.app.AppConfig;
import com.xgame.common.application.ActivityState;
import com.xgame.common.application.ApplicationStatus;

public class UMengHelper {
    /**
     * add method before ApplicationStatus.initialize
     */
    public static void init() {
        UMConfigure.init(ApplicationStatus.getApplicationContext(), null, AppConfig.getChannel(),
                UMConfigure.DEVICE_TYPE_PHONE, "");
        ApplicationStatus.registerStateListenerForAllActivities(
                new ApplicationStatus.ActivityStateListener() {
                    @Override
                    public void onActivityStateChange(Activity activity, int newState) {
                        if (newState == ActivityState.RESUMED) {
                            MobclickAgent.onResume(activity);
                        } else if (newState == ActivityState.PAUSED) {
                            MobclickAgent.onPause(activity);
                        }
                    }
                });
    }
}
