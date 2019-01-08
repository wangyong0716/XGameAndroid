package com.xgame.base;

import android.content.Context;
import android.text.TextUtils;

import com.xgame.app.ServerConfig;
import com.xgame.app.XgameApplication;
import com.xgame.base.api.ClientSettingsService;
import com.xgame.base.model.ClientSettings;
import com.xgame.common.api.ApiServiceManager;
import com.xgame.common.api.FutureCallHelper;
import com.xgame.common.util.GlobalGson;
import com.xgame.common.util.SharePrefUtils;
import com.xgame.util.StringUtil;

import static com.xgame.common.util.ExecutorHelper.runInUIThread;
import static com.xgame.common.util.ExecutorHelper.runInWorkerThread;
import static com.xgame.common.util.UiUtil.isInMainThread;

/**
 * Created by Albert
 * on 18-2-4.
 */

public class ClientSettingManager {

    private static final String PREF_NAME = "client_settings";

    private static final String KEY_SETTINGS = "settings";

    private static volatile ClientSettings sSettings;

    public static boolean hasDomain() {
        return sSettings != null && sSettings.hasDomain();
    }

    public static boolean hasIps() {
        return sSettings != null && sSettings.hasIps();
    }

    public static String getDomain(String defaultDomain) {
        return hasDomain() ? sSettings.domain : defaultDomain;
    }

    public static void reloadSettingsIfNeed() {
        if (sSettings == null) {
            reloadSettings();
        }
    }

    public static void reloadSettings() {
        reloadSettings(null, false);
    }

    /**
     * 向服务端重新请求Settings
     * @param result
     * @param domainChecked 是否已经使用过domain，setting先从domain请求，失败后再逐次从ip请求
     */
    public static void reloadSettings(final OnLoadResult result, final boolean domainChecked) {

        if (isInMainThread()) {
            runInWorkerThread(new Runnable() {
                @Override
                public void run() {
                    reloadSettings(result, domainChecked);
                }
            });
            return;
        }

        String domain = null;
        if (hasDomain() && !domainChecked) {
            domain = sSettings.domain;
        } else if (hasIps()) {
            domain = sSettings.getNextIp();
        }
        if (StringUtil.isEmpty(domain)) {
            if (sSettings != null) {
                sSettings.reset();
            }
            requestSettings(ServerConfig.getDefaultDomain(), result, false);
        } else {
            requestSettings(domain, result, true);
        }
    }

    private static void requestSettings(String domain, OnLoadResult result, boolean retry) {
        final ClientSettings newSetting =
                FutureCallHelper.get(ServerConfig.createSpecifiedRetrofit(domain)
                        .create(ClientSettingsService.class).loadSettings().submit());
        if (newSetting != null) {
            notifyOnLoad(result, newSetting);
            saveToPref(newSetting);
            String preDomain = sSettings == null ? null : sSettings.domain;
            sSettings = newSetting;
            if (!TextUtils.equals(preDomain, newSetting.domain)) {
                reloadApiService();
            }
        } else if (retry && !ServerConfig.isDomainChanged(domain)) {
            reloadSettings(result, true);
        } else {
            notifyOnFailure(result);
        }
    }

    private static void reloadApiService() {
        ApiServiceManager.reset(new ServerConfig());
    }

    private static void notifyOnFailure(final OnLoadResult result) {
        if (result == null) {
            return;
        }
        if (!isInMainThread()) {
            runInUIThread(new Runnable() {
                @Override
                public void run() {
                    notifyOnFailure(result);
                }
            });
            return;
        }
        result.onFailure();
    }

    private static void notifyOnLoad(final OnLoadResult result,
                                     final ClientSettings setting) {
        if (result == null) {
            return;
        }
        if (!isInMainThread()) {
            runInUIThread(new Runnable() {
                @Override
                public void run() {
                    notifyOnLoad(result, setting);
                }
            });
            return;
        }
        result.onLoaded(setting);
    }

    public static void loadSettings(final OnLoadResult result) {
        if (tryLoadSettings(result)) {
            return;
        }
        doLoadSettings(result);
    }

    private static void doLoadSettings(final OnLoadResult result) {
        if (isInMainThread()) {
            runInWorkerThread(new Runnable() {
                @Override
                public void run() {
                    doLoadSettings(result);
                }
            });
            return;
        }
        if (tryLoadSettings(result)) {
            return;
        }
        Context context = XgameApplication.getApplication();
        String json = SharePrefUtils.getString(context, PREF_NAME, "");
        boolean needNotify = true;
        if (!StringUtil.isEmpty(json)) {
            final ClientSettings ss = GlobalGson.get().fromJson(json, ClientSettings.class);
            if (ss != null) {
                notifyOnLoad(result, ss);
                needNotify = false;
            }
        }
        reloadSettings(needNotify ? result : null, false);
    }

    public static void initSettings() {
        Context context = XgameApplication.getApplication();
        String json = SharePrefUtils.getString(context, PREF_NAME, "");
        if (!StringUtil.isEmpty(json)) {
            ClientSettings ss = null;
            try {
                ss = GlobalGson.get().fromJson(json, ClientSettings.class);
            } catch (Exception e) {

            }
            if (ss != null) {
                sSettings = ss;
            }
        }
    }

    private static boolean tryLoadSettings(OnLoadResult result) {
        ClientSettings settings = sSettings;
        if (settings != null) {
            notifyOnLoad(result, settings);
            return true;
        }
        return false;
    }

    private static void saveToPref(ClientSettings settings) {
        if (settings == null) {
            return;
        }
        Context context = XgameApplication.getApplication();
        String settingsJson = GlobalGson.get().toJson(settings);
        SharePrefUtils.putString(context, PREF_NAME, KEY_SETTINGS, settingsJson);
    }

    public interface OnLoadResult {

        void onLoaded(ClientSettings settings);

        void onFailure();
    }
}
