package com.xgame.util;

import android.content.Context;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.miui.zeus.mario.sdk.MarioSdk;
import com.xgame.account.UserManager;
import com.xgame.app.AppConfig;
import com.xgame.app.ServerConfig;
import com.xgame.common.net.OKHttpClientHelper;
import com.xgame.common.util.AppUtils;
import com.xgame.common.util.Base64;
import com.xgame.common.util.DeviceUtil;
import com.xgame.common.util.LocationUtil;
import com.xgame.common.util.LogUtil;
import com.xgame.common.util.NetworkUtil;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by jiangjianhe on 2/5/18.
 */

public class Analytics {

    private static final String TAG = "Analytics";
    private static final String APP_ID = "3271031517363563185";
    private static Context sContext;
    private static OkHttpClient sOkHttpClient;

    private static ExecutorService sExecutor;

    public static void init(Context context) {
        sContext = context;
        sOkHttpClient = new OkHttpClient();
        sExecutor = Executors.newSingleThreadExecutor();
    }

    private static Map<String, String> getDefaultParams() {
        Map<String, String> params = Maps.newHashMap();
        params.put("app_id", APP_ID);
        params.put("device_id", DeviceUtil.getImeiMd5(sContext));
        params.put("visitTime", Long.toString(System.currentTimeMillis()));
        Location location = LocationUtil.getLocation();
        if (location != null) {
            params.put("longtitude", String.format("%.5f", location.getLongitude()));
            params.put("latitude", String.format("%.5f", location.getLatitude()));
        }
        params.put("model", DeviceUtil.getModel());
        params.put("version", AppUtils.getAppVersionName(sContext));
        params.put("bag", sContext.getPackageName());
        params.put("channel", "launch");
        params.put("d_channel", AppConfig.getChannel());
        params.put("system_version", getOSVersion());
        params.put("network", getNetworkType(sContext));
        params.put("telecoms", NetworkUtil.getCarrierOperator(sContext));
        return params;
    }


    public static void trackPageShowEvent(final String url_title, final String visit_type, final String ext) {
        sExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = getDefaultParams();
                JsonObject json = mapToJson(params);
                json.addProperty("type", "PV");
                json.addProperty("url_title", url_title);
                json.addProperty("visit_type", visit_type);
                if (!TextUtils.isEmpty(ext)) {
                    json.addProperty("ext", ext);
                }
                uploadEvent(json);
            }
        });
    }

    public static void trackClickEvent(final String subtype, final String stock_id, final String stock_name, final String stock_type,
                                       final String page, final String section, final String ext) {
        sExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = getDefaultParams();
                JsonObject json = mapToJson(params);
                json.addProperty("type", "CLICK");
                JsonArray reachitems = new JsonArray();
                JsonObject item = new JsonObject();
                item.addProperty("subtype", subtype);
                item.addProperty("stock_id", stock_id);
                item.addProperty("stock_name", stock_name);
                item.addProperty("stock_type", stock_type);
                item.addProperty("page", page);
                item.addProperty("section", section);
                if (!TextUtils.isEmpty(ext)) {
                    item.addProperty("ext", ext);
                }
                reachitems.add(item);
                json.add("reachitems", reachitems);
                uploadEvent(json);
            }
        });
    }

    public static void trackCustomEvent(final String action_path, final String action_type, final String action_name, final String ext) {
        sExecutor.submit(new Runnable() {
            @Override
            public void run() {
                Map<String, String> params = getDefaultParams();
                JsonObject json = mapToJson(params);
                json.addProperty("type", "DIY");
                JsonObject event = new JsonObject();
                event.addProperty("action_path", action_path);
                event.addProperty("action_type", action_type);
                event.addProperty("action_name", action_name);
                json.add("event", event);
                if (!TextUtils.isEmpty(ext)) {
                    json.addProperty("ext", ext);
                }
                uploadEvent(json);
            }
        });
    }

    private static JsonObject mapToJson(Map<String, String> params) {
        JsonObject json = new JsonObject();
        Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            json.addProperty(entry.getKey(), entry.getValue());
        }
        return json;
    }

    private static void uploadEvent(JsonObject event) {
        try {
            JsonObject json = new JsonObject();
            JsonArray array = new JsonArray();
            array.add(event);
            json.add("requests", array);
            String data = json.toString();
            LogUtil.i(TAG, "event:" + data);
            String encodeData = Base64.encode(data.getBytes("utf-8"));
            FormBody.Builder builder = new FormBody.Builder();
            builder.add("data", encodeData);
            builder.add("from", "android");
            Request request = new Request.Builder()
                    .url(ServerConfig.getBaseDomain() + "/track/data").addHeader("Authorization", "Bearer" + " " + UserManager.getInstance().getToken())
                    .post(builder.build())
                    .build();
            Response response = sOkHttpClient.newCall(request).execute();
            LogUtil.d(TAG, "response: " + response);
        } catch (Exception e) {
            LogUtil.d(TAG, "uploadEvent error: " + Throwables.getStackTraceAsString(e));
        }
    }

    private static String getNetworkType(Context context) {
        int networkType = NetworkUtil.getNetworkType(context);
        if (networkType == NetworkUtil.NetworkType.Network_2G) {
            return "2g";
        } else if (networkType == NetworkUtil.NetworkType.Network_3G) {
            return "3g";
        } else if (networkType == NetworkUtil.NetworkType.Network_4G) {
            return "4g";
        } else if (networkType == NetworkUtil.NetworkType.Network_Mobile) {
            return "mobile";
        } else if (networkType == NetworkUtil.NetworkType.Network_WIFI) {
            return "wifi";
        } else if (networkType == NetworkUtil.NetworkType.Network_None) {
            return "none";
        }
        return "other";
    }


    private static String getOSVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    public static class Constans {
        public static final String TYPE_PV = "PV";
        public static final String TYPE_CLICK = "CLICK";
        public static final String TYPE_DIY = "DIY";

        public static final String URL_TITLE_LOGIN_PAGE = "登录页";
        public static final String URL_TITLE_BATTLE_PAGE = "真人对战首页";
        public static final String URL_TITLE_ARENA_PAGE = "金币场首页";
        public static final String URL_TITLE_BAIWANG_PAGE = "百万对战页";
        public static final String URL_TITLE_HISTORY_PAGE = "我的对战页";
        public static final String URL_TITLE_RELIVE_PAGE = "购买复活卡弹窗";
        public static final String URL_TITLE_TASK_PAGE = "任务列表页";
        public static final String URL_TITLE_INVITE_FOLLOWER_PAGE = "收徒页";
        public static final String URL_TITLE_INVITE_CODE_PAGE = "填写邀请码页";

        public static final String VISIT_TYPE_READY = "READY";

        public static final String SUBTYPE_CLICK = "CLICK";
        public static final String SUBTYPE_SHARE = "SHARE";

        public static final String STOCK_ID_MOBILE = "mobile";
        public static final String STOCK_ID_QQ = "QQ";
        public static final String STOCK_ID_WECHAT = "wechat";
        public static final String STOCK_ID_NEXT = "next";
        public static final String STOCK_ID_CLOSE = "close";
        public static final String STOCK_ID_INVITE = "invite";
        public static final String STOCK_ID_BATTLE = "ZhenRen";
        public static final String STOCK_ID_ARENA  = "JinBi";
        public static final String STOCK_ID_HISTORY  = "MyDuiZhan";
        public static final String STOCK_ID_BATTLE_AGAIN = "again";
        public static final String STOCK_ID_BATTLE_CHANGE_PEER = "ChangeOppo";
        public static final String STOCK_ID_BATTLE_CHANGE_GAME = "ChangeGame";
        public static final String STOCK_ID_BATTLE_ACCEPT = "accept";
        public static final String STOCK_ID_BATTLE_PEER_CHANGE_GAME = "OppoChangeGame";
        public static final String STOCK_ID_ARENA_AGAIN = "ImmeAgain";
        public static final String STOCK_ID_ARENA_SHARE = "share";
        public static final String STOCK_ID_ARENA_BACK = "BackGameCenter";
        public static final String STOCK_ID_ARENA_COMEBACK = "FanPan";
        public static final String STOCK_ID_WITHDRAW = "withdraw";
        public static final String STOCK_ID_SHARE_WX  = "WechatShare";
        public static final String STOCK_ID_SHARE_WX_TIMELINE  = "MomentShare";
        public static final String STOCK_ID_SHARE_QQ  = "QQShare";

        public static final String STOCK_ID_BW_PURCHASE = "purchase";

        public static final String STOCK_NAME_MOBILE = "手机号登录";
        public static final String STOCK_NAME_QQ = "QQ登录";
        public static final String STOCK_NAME_WECHAT = "微信登录";
        public static final String STOCK_NAME_NEXT = "下一步按钮";
        public static final String STOCK_NAME_LOGIN_CLOSE = "关闭x按钮";
        public static final String STOCK_NAME_LOGIN_INVITE = "去邀请";
        public static final String STOCK_NAME_BATTLE = "真人对战";
        public static final String STOCK_NAME_ARENA  = "金币场";
        public static final String STOCK_NAME_HISTORY  = "我的对战";
        public static final String STOCK_NAME_BATTLE_AGAIN = "再来一局";
        public static final String STOCK_NAME_BATTLE_CHANGE_PEER = "换个对手";
        public static final String STOCK_NAME_BATTLE_CHANGE_GAME = "换个游戏";
        public static final String STOCK_NAME_BATTLE_ACCEPT = "接受邀请";
        public static final String STOCK_NAME_BATTLE_PEER_CHANGE_GAME = "对方想换个游戏";
        public static final String STOCK_NAME_ARENA_AGAIN = "趁热再来一局";
        public static final String STOCK_NAME_ARENA_SHARE = "分享赚金币";
        public static final String STOCK_NAME_ARENA_BACK = "返回游戏大厅";
        public static final String STOCK_NAME_ARENA_COMEBACK = "我要翻盘";
        public static final String STOCK_NAME_WITHDRAW = "去提现";
        public static final String STOCK_NAME_SHARE_WX  = "微信好友";
        public static final String STOCK_NAME_SHARE_WX_TIMELINE  = "微信朋友圈";
        public static final String STOCK_NAME_SHARE_QQ  = "QQ好友";

        public static final String STOCK_NAME_BW_PURCHASE = "购买复活卡";

        public static final String STOCK_TYPE_BTN = "btn";
        public static final String STOCK_TYPE_LINK = "link";
        public static final String STOCK_TYPE_TAB = "tab";
        public static final String STOCK_TYPE_GAME = "game";

        public static final String PAGE_LOGIN = "登录页";
        public static final String PAGE_PROFILE = "资料填写页";
        public static final String PAGE_LOGIN_SUCCESS = "登录成功页";
        public static final String PAGE_HOME = "首页";
        public static final String PAGE_BATTLE_RESULT = "真人对战结果页";
        public static final String PAGE_ARENA_RESULT = "金币场结果页";
        public static final String PAGE_BAIWAN_RESULT = "百万对战结果页";
        public static final String PAGE_BATTLE_HOME = "真人对战首页";
        public static final String PAGE_ARENA_HOME = "金币场首页";
        public static final String PAGE_BW_HOME = "百万对战首页";

        public static final String PAGE_BW_LIVE = "百万对战直播页";
        public static final String PAGE_ADD_FRIENDS = "添加好友页";

        public static final String SECTION_LOGIN = "登录页";
        public static final String SECTION_PROFILE = "资料填写页";
        public static final String SECTION_LOGIN_SUCCESS_POP = "登录成功弹窗";
        public static final String SECTION_TAB = "tab栏";
        public static final String SECTION_BATTLE_RESULT = "真人对战结果页";
        public static final String SECTION_ARENA_RESULT = "金币场结果页";
        public static final String SECTION_SHARE = "分享模块";
        public static final String SECTION_GAME_LIST = "游戏列表";
        public static final String SECTION_CASH_INFO = "现金信息";
        public static final String SECTION_BW_BUY_REVIVE = "购买复活卡弹窗";
        public static final String SECTION_INVITE_FRIENDS = "邀请朋友";

        public static final String CUSTOM_GAME_TYPE_BATTLE = "真人对战";
        public static final String CUSTOM_GAME_TYPE_ARENA = "金币场";
        public static final String CUSTOM_GAME_TYPE_BW = "百万场";

        public static final String CUSTOM_ACTION_NAME_MOBILE_LOGIN = "手机号登录";
        public static final String CUSTOM_ACTION_NAME_QQ_LOGIN = "QQ登录";
        public static final String CUSTOM_ACTION_NAME_WX_LOGIN = "微信登录";
        public static final String CUSTOM_GAMME_MODE_PK  = "对战模式";

        public static final String ACTION_PATH_LOGIN_SUCCESS = "登录成功页";
        public static final String ACTION_PATH_MATCH_SUCCESS = "匹配成功页";
        public static final String ACTION_PATH_INVITE_GAME = "约战页";

        public static final String ACTION_TYPE_LOGIN_SUCCESS = "登录成功";
        public static final String ACTION_TYPE_PLAY_GAME = "进入游戏";
        public static final String ACTION_TYPE_INVITE_GAME = "发起对战";

    }
}
