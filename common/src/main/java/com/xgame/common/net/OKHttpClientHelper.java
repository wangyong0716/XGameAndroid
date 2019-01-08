package com.xgame.common.net;

import java.util.concurrent.TimeUnit;

import com.xgame.common.BuildConfig;
import com.xgame.common.util.LogUtil;

import okhttp3.OkHttpClient;

public class OKHttpClientHelper {

    // public static final String TAG = OKHttpClientHelper.class.getSimpleName();
    public static final String TAG = "OKHttpClientHelper";

    private static final long CACHE_MAX_SIZE = 20 * 1024 * 1024;//20M
    private static final int DEFAULT_CONNECT_TIMEOUT = 20;
    private static final int DEFAULT_READ_TIMEOUT = 15;
    private static final int DEFAULT_WRITE_TIMEOUT = 15;

    private static final OkHttpClient sOkHttpClient = createInternal();


    public static OkHttpClient create() {
        return sOkHttpClient;
    }

    private static OkHttpClient createInternal() {
        //CommonInterceptor commonInterceptor = new CommonInterceptor();

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true).connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS).writeTimeout(DEFAULT_WRITE_TIMEOUT, TimeUnit.SECONDS);
                //.addInterceptor(commonInterceptor);
        if (LogUtil.DEBUG) {
            builder.addInterceptor(new OKHttpHelper.HttpLogInterceptor(TAG));
            builder.addInterceptor(new OKHttpHelper.NoHttpsInterceptor());
        }
        return builder.build();
    }
}
