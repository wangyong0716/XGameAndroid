package com.xgame.push;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.xgame.BuildConfig;
import com.xgame.account.UserManager;
import com.xgame.account.model.User;
import com.xgame.app.ServerConfig;
import com.xgame.app.XgameApplication;
import com.xgame.base.api.Pack;
import com.xgame.common.api.FutureCallAdapterFactory;
import com.xgame.common.api.OnCallback;
import com.xgame.common.net.OKHttpClientHelper;
import com.xgame.common.util.GlobalGson;
import com.xgame.common.util.LogUtil;
import com.xgame.common.util.MD5Util;
import com.xgame.common.util.SharePrefUtils;
import com.xgame.push.api.PushService;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.mipush.sdk.MiPushClient;

import org.w3c.dom.Text;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static com.xgame.base.ServiceFactory.pushService;


/**
 * Created by jiangjianhe on 1/27/18.
 */

public class PushManager {

    public static final String TAG = "PushManager";

    private static final String APP_ID = "2882303761517712364";
    private static final String APP_KEY = "5661771255364";

    private static final String REGID_HASH = "regid_hash";

    private static String sRegId;


    public static void registerPush(Context context) {
        if (shouldInit(context)) {
            MiPushClient.registerPush(context, APP_ID, APP_KEY);
            LoggerInterface newLogger = new LoggerInterface() {

                @Override
                public void setTag(String tag) {
                }

                @Override
                public void log(String content, Throwable t) {
                    LogUtil.d(TAG, content, t);
                }

                @Override
                public void log(String content) {
                    LogUtil.d(TAG, content);
                }
            };
            Logger.setLogger(context, newLogger);
        }
    }

    private static boolean shouldInit(Context context) {
        ActivityManager am = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = context.getPackageName();
        int myPid = Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

    public static void setRegId(String regId) {
        sRegId = regId;
        //TODO send regId to server
        registerRegIdToServer();
    }

    public static void registerRegIdToServer() {
        String token = UserManager.getInstance().getToken();
        long userId = UserManager.getInstance().getUserId();
        LogUtil.d(TAG, String.format("registerRegIdToServer token:%s\tuserId:%s\tsRegId:%s", token, Long.toString(userId), sRegId));
        if (TextUtils.isEmpty(token) || TextUtils.isEmpty(sRegId)) {
            return;
        }
        String lastRegHash = SharePrefUtils.getString(XgameApplication.getApplication(), REGID_HASH, "");
        final String regHash = MD5Util.MD5_32(token + sRegId);
        if (regHash.equals(lastRegHash)) {
            return;
        }
        MiPushClient.setAlias(XgameApplication.getApplication(), Long.toString(userId), null);
        pushService().register(sRegId, Long.toString(userId)).enqueue(new OnCallback<String>() {
            @Override
            public void onResponse(String result) {
                try {
                    LogUtil.d(TAG, "registerRegIdToServer onResponse result" + (result == null ? "null" : result));
                    if (TextUtils.isEmpty(result)) {
                        return;
                    }
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(result);
                    //TODO 服务端现在注册成功后还没有返回状态码，所以默认状态码是200
                    int status = jsonNode.path("code").asInt(200);
                    if (status == 200) {
                        SharePrefUtils.putString(XgameApplication.getApplication(), REGID_HASH, regHash);
                    }
                } catch (Exception e) {
                    LogUtil.d(TAG, "registerRegIdToServer onResponse error: " + Throwables.getStackTraceAsString(e));
                }
            }

            @Override
            public void onFailure(String result) {
                LogUtil.d(TAG, "registerRegIdToServer onFailure result"  + (result == null ? "null" : result));
            }
        });
    }

    public static void unRegisterRegIdToServer() {
        long userId = UserManager.getInstance().getUserId();
        if (userId == User.DEFAULT_USER_ID) {
            return;
        }
        LogUtil.d(TAG, String.format("unRegisterRegIdToServer userId:%s\tsRegId:%s", Long.toString(userId), sRegId));
        MiPushClient.unsetAlias(XgameApplication.getApplication(), Long.toString(userId), null);
        SharePrefUtils.putString(XgameApplication.getApplication(), REGID_HASH, "");
        String authHeader = "Bearer" + " " + UserManager.getInstance().getToken();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerConfig.getServerApiUrl())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(FutureCallAdapterFactory.create(Pack.class))
                .client(OKHttpClientHelper.create())
                .validateEagerly(BuildConfig.DEBUG)
                .build();
        PushService pushService = retrofit.create(PushService.class);
        pushService.unRegister(authHeader, "", Long.toString(userId)).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                LogUtil.d(TAG, "registerRegIdToServer onResponse result " + (response == null ? "null" : response.body()));
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                LogUtil.d(TAG, "registerRegIdToServer onFailure "  + Throwables.getStackTraceAsString(t));
            }
        });
    }

}
