package com.xgame.personal.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.ArrayMap;

import java.util.Map;

import com.xgame.account.UserManager;
import com.xgame.app.XgameApplication;
import com.xgame.base.api.Pack;
import com.xgame.common.api.OnCallback;
import com.xgame.common.util.GlobalGson;
import com.xgame.common.util.MD5Util;
import com.xgame.common.util.NetworkUtil;
import com.xgame.common.util.SharePrefUtils;

import static com.xgame.base.ServiceFactory.personalInfoService;
import static com.xgame.common.util.ExecutorHelper.runInBackground;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by dingning1
 * on 18-1-31.
 */


public class PersonalInfoModel {

    private static final String PREF_MY = "personal_info_pref";
    private static final String KEY_MY_INFO = "home_my";
    private static final String KEY_POINT_CLICK = "_point_click";
    private static final String KEY_UPDATE = "_update";
    private static final String KEY_INVITE_CODE = "my_invite_code";

    private static long sPrevUpdate;
    private static PersonalMenu sMenuCache;
    private static UserProfile sProfileCache;

    private static final Map<String, Long> sPointClickState;

    static {
        sPointClickState = new ArrayMap<>();
    }

    public static void init(final OnCallback<UserProfile> callback) {
        requestUserProfile(callback);
        personalInfoService().getMyData()
                .enqueue(new OnCallback<Pack<PersonalMenu>>() {
                    @Override
                    public void onResponse(Pack<PersonalMenu> result) {
                        if (result.data != null) {
                            cacheHomeMenu(XgameApplication.getApplication(), result.data);
                        }
                    }

                    @Override
                    public void onFailure(Pack<PersonalMenu> result) {
                    }
                });
    }

    public static void requestUserProfile(final OnCallback<UserProfile> callback) {
        personalInfoService().getUserProfile()
                .enqueue(new OnCallback<Pack<UserProfile>>() {
                    @Override
                    public void onResponse(Pack<UserProfile> result) {
                        if (result.data != null) {
                            cacheUserProfile(XgameApplication.getApplication(), result.data);
                            if (callback != null) {
                                callback.onResponse(result.data);
                            }
                        } else {
                            if (callback != null) {
                                callback.onFailure(null);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Pack<UserProfile> result) {
                        if (callback != null) {
                            callback.onFailure(null);
                        }
                    }
                });
    }

    public static String getInviteCode(Context ctx) {
        return SharePrefUtils.getString(ctx, getPrefName(), KEY_INVITE_CODE);
    }

    public static void cacheUserProfile(final Context ctx, final UserProfile profile) {
        sProfileCache = profile;
        runInBackground(new Runnable() {
            @Override
            public void run() {
                SharePrefUtils.putString(ctx, getPrefName(), KEY_INVITE_CODE, profile.inviteCode);
            }
        });
    }

    public static UserProfile getProfileCache() {
        return sProfileCache;
    }

    public static void cacheHomeMenu(final Context ctx, final PersonalMenu menu) {
        if (menu == null || sMenuCache == menu) {
            return;
        }
        sMenuCache = menu;
        sPrevUpdate = System.currentTimeMillis();
        runInBackground(new Runnable() {
            @Override
            public void run() {
                SharePrefUtils.putLong(ctx, getPrefName(), KEY_MY_INFO + KEY_UPDATE, sPrevUpdate);
                SharePrefUtils.putString(ctx, getPrefName(), KEY_MY_INFO, GlobalGson.get().toJson(menu));
            }
        });
    }

    public static boolean canUpdateMenu(Context ctx) {
        if (sPrevUpdate == 0) {
            sPrevUpdate = SharePrefUtils.getLong(ctx, getPrefName(), KEY_MY_INFO + KEY_UPDATE, 0);
        }
        return sPrevUpdate == 0 || System.currentTimeMillis() - sPrevUpdate > getUpdateInterval(ctx);
    }

    public static PersonalMenu getMenuCache(Context ctx) {
        if (sMenuCache == null) {
            String json = SharePrefUtils.getString(ctx, getPrefName(), KEY_MY_INFO, "");
            if (!TextUtils.isEmpty(json)) {
                try {
                    sMenuCache = GlobalGson.get().fromJson(json, PersonalMenu.class);
                } catch (Exception e) {
                }
            }
        }
        return sMenuCache;
    }

    public static long getPointState(Context ctx, String type) {
        PersonalMenu menu = getMenuCache(ctx);
        if (menu != null && menu.points != null && menu.points.length > 0) {
            for (PersonalMenuItem item : menu.points) {
                if (item.status.equals(type)) {
                    long state = getPointClickState(ctx, type);
                    if (state < item.state) {
                        return 0; // user doesn't click this point
                    }
                    return item.state;
                }
            }
        }
        return 0;
    }

    public static void setPointClickState(Context ctx, String type, long state) {
        sPointClickState.put(type, state);
        SharePrefUtils.putLong(ctx, getPrefName(), type + KEY_POINT_CLICK, state);
    }

    private static long getPointClickState(Context ctx, String type) {
        if (!sPointClickState.containsKey(type)) {
            long state = SharePrefUtils.getLong(ctx, getPrefName(), type + KEY_POINT_CLICK, 0);
            sPointClickState.put(type, state);
        }
        return sPointClickState.get(type);
    }

    private static long getUpdateInterval(Context ctx) {
        PersonalMenu menu = getMenuCache(ctx);
        if (menu != null && menu.updateInterval != null) {
            switch(NetworkUtil.getNetworkType(ctx)) {
                case NetworkUtil.NetworkType.Network_2G:
                    return menu.updateInterval.network2g;
                case NetworkUtil.NetworkType.Network_3G:
                    return menu.updateInterval.network3g;
                case NetworkUtil.NetworkType.Network_4G:
                    return menu.updateInterval.network4g;
                case NetworkUtil.NetworkType.Network_WIFI:
                    return menu.updateInterval.networkWifi;
                default:
                    break;
            }
        }
        return 0;
    }

    private static String getPrefName() {
        return PREF_MY + getUserId();
    }

    private static String getUserId() {
        return MD5Util.MD5_32(String.valueOf(UserManager.getInstance().getUserId()));
    }
}
