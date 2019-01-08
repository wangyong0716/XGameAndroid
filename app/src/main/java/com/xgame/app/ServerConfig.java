package com.xgame.app;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.miui.zeus.mario.sdk.MarioSdk;
import com.xgame.BuildConfig;
import com.xgame.account.TokenHandler;
import com.xgame.account.UserManager;
import com.xgame.base.ClientSettingManager;
import com.xgame.base.api.Pack;
import com.xgame.common.api.ApiServiceManager;
import com.xgame.common.api.FutureCallAdapterFactory;
import com.xgame.common.net.OKHttpClientHelper;
import com.xgame.common.util.GlobalGson;
import com.xgame.common.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 * <p>
 * Created by jackwang
 * on 18-1-26.
 */
public class ServerConfig implements ApiServiceManager.ServerFactory {
    private static final String TAG = "ServerConfig";
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private static final String SERVER_DOMAIN_DEFAULT = "https://api.chufengnet.com";
    private static final String API_VERSION = "/v1/";

    public static String getDefaultDomain() {
        return SERVER_DOMAIN_DEFAULT;
    }

    public static boolean isDomainChanged(String domain) {
        return !TextUtils.equals(getBaseDomain(), domain);
    }

    private static String getDevDomain() {
        return ClientSettingManager.getDomain(SERVER_DOMAIN_DEFAULT);
//        return "http://staging.api.xgame.miui.com";
    }

    private static String getReleaseDomain() {
        return ClientSettingManager.getDomain(SERVER_DOMAIN_DEFAULT);
//        return "http://staging.api.xgame.miui.com";
    }

    public static String getBaseDomain() {
        return BuildConfig.DEBUG ? getDevDomain() : getReleaseDomain();
    }

    private static String getSpecifiedUrl(String domain) {
        return domain + API_VERSION;
    }

    public static String getServerApiUrl() {
        return getSpecifiedUrl(getBaseDomain());
    }

    @Override
    public Retrofit create() {
        return createSpecifiedRetrofit(getBaseDomain());
    }

    public static Retrofit createSpecifiedRetrofit(String domain) {
        return new Retrofit.Builder()
                .baseUrl(getSpecifiedUrl(domain))
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(GlobalGson.get()))
                .addCallAdapterFactory(FutureCallAdapterFactory.create(Pack.class))
                .client(OKHttpClientHelper.create().newBuilder().addInterceptor(new AppInterceptor()).build())
                .validateEagerly(BuildConfig.DEBUG)
                .build();
    }

    public static class AppInterceptor implements Interceptor {

        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request originalRequest = chain.request();
            String token = UserManager.getInstance().getToken();
            boolean emptyToken = TextUtils.isEmpty(token);
            String originAuth = originalRequest.header("Authorization"); //退出时清空了本地token，所以可能拿到空值，用原来的
            HttpUrl newUrl = originalRequest.url().newBuilder()
                    .addQueryParameter("clientInfo", MarioSdk.getClientInfo()).build();
            Request interceptRequest = originalRequest.newBuilder().url(newUrl)
                    .header("Authorization", TextUtils.isEmpty(originAuth)? ("Bearer" + " " + token) : originAuth)
                    .header("clientTime", String.valueOf(System.currentTimeMillis())).build();
            Response response;
            try {
                response = chain.proceed(interceptRequest);
            } catch (Exception e) {
                LogUtil.d(TAG, "<-- HTTP FAILED: " + e);
                throw e;
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IOException("null body");
            }
            long contentLength = responseBody.contentLength();

            if (!HttpHeaders.hasBody(response)) {
            } else if (bodyEncoded(response.headers())) {
            } else {
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE); // Buffer the entire body.
                Buffer buffer = source.buffer();
                Charset charset = UTF8;
                MediaType contentType = responseBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }
                if (contentLength != 0) {
                    String result = buffer.clone().readString(charset);
                    try {
                        JSONObject obj = new JSONObject(result);
                        int code = obj.optInt("code", -1000);
                        if (code == TokenHandler.CODE_TOKEN_FORBIDDEN
                                || (!emptyToken && code >= TokenHandler.CODE_TOKEN_EMPTY)) {
                            TokenHandler.handleTokenCode(code, obj.optString("msg"));
                            LogUtil.d(TAG, "request: " + interceptRequest + ", obj: " + obj );
                            throw new IOException("token wrong or account forbidden, code: " + code);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
            return response;
        }
    }

    private static boolean bodyEncoded(Headers headers) {
        if (headers != null) {
            String contentEncoding = headers.get("Content-Encoding");
            return contentEncoding != null && !contentEncoding.equalsIgnoreCase("identity");
        } else {
            return false;
        }
    }
}
