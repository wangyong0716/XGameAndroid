package com.xgame.app;

import com.miui.zeus.mario.sdk.MarioSdk;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.lang.ref.WeakReference;

import com.squareup.leakcanary.LeakCanary;
import com.umeng.socialize.Config;
import com.xgame.BuildConfig;
import com.xgame.account.TokenHandler;
import com.xgame.chat.BattleManager;
import com.xgame.base.GameProvider;
import com.xgame.common.api.ApiServiceManager;
import com.xgame.common.application.ApplicationStatus;
import com.xgame.common.cache.CacheFactory;
import com.xgame.push.PushManager;
import com.xgame.statis.UMengHelper;
import com.xgame.util.Analytics;

/**
 * Created by wuyanzhi on 2018/1/23.
 */

public class XgameApplication extends Application {

    private static XgameApplication sApplication;

    private int mActiveActivityCount;

    private static final String APP_KEY = "XGAME";
    private static final String APP_TOKEN = "ff180b01e428ed68e281c102e78ce039";

    private WeakReference<Activity> mTopAct;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        ApplicationStatus.initialize(this);
        PushManager.registerPush(getApplicationContext());
        registerActivityLifecycleCallbacks(mLifecycleCallbacks);
        UMengHelper.init();
        MarioSdk.init(this, APP_KEY, APP_TOKEN);
        Analytics.init(getApplicationContext());
        ApiServiceManager.prepare(new ServerConfig());
        BattleManager.init(this);
        Config.DEBUG = false;

        if (BuildConfig.DEBUG) {
            if (LeakCanary.isInAnalyzerProcess(getApplication())) {
                // This process is dedicated to LeakCanary for heap analysis.
                // You should not init your app in this process.
                return;
            }
            LeakCanary.install(getApplication());
        }
        GameProvider.postFetch();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterActivityLifecycleCallbacks(mLifecycleCallbacks);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= Application.TRIM_MEMORY_RUNNING_LOW) {
            CacheFactory.cleanAll();
        }
    }

    public static XgameApplication getApplication() {
        return sApplication;
    }

    public boolean isInForground() {
        return mActiveActivityCount > 0;
    }

    public static Activity getTopActivity() {
        return sApplication.mTopAct != null ? sApplication.mTopAct.get() : null;
    }

    private ActivityLifecycleCallbacks mLifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
            mTopAct = new WeakReference<>(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {
            if (mTopAct == null || mTopAct.get() != activity) {
                mTopAct = new WeakReference<>(activity);
            }
            mActiveActivityCount++;
        }

        @Override
        public void onActivityResumed(Activity activity) {
            if (mTopAct == null || mTopAct.get() != activity) {
                mTopAct = new WeakReference<>(activity);
            }
        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            mActiveActivityCount--;
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            TokenHandler.dismissDialog(activity);
        }
    };

}
