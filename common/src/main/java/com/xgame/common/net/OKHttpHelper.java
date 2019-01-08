package com.xgame.common.net;

import com.xgame.common.BuildConfig;
import com.xgame.common.util.LogUtil;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by wuyanzhi on 2018/1/24.
 */

public class OKHttpHelper {
    public static final String TAG = "XGOKHttpHelper";

    public static class HttpLogInterceptor implements Interceptor {
        private static final int MAX_LOG_LENGTH = 400;
        private final HttpLoggingInterceptor mHttpLoggingInterceptor;

        private final String mTag;

        public HttpLogInterceptor() {
            this(TAG);
        }

        public HttpLogInterceptor(String tag) {
            mTag = tag;
            mHttpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    if (LogUtil.DEBUG) {
                        // Split by linemeiy , then ensure each line can fit into Log's maximum length.
                        for (int i = 0, length = message.length(); i < length; i++) {
                            int newline = message.indexOf('\n', i);
                            newline = newline != -1 ? newline : length;
                            do {
                                int end = Math.min(newline, i + MAX_LOG_LENGTH);
                                LogUtil.v(mTag, message.substring(i, end));
                                i = end;
                            } while (i < newline);
                        }
                    }
                }
            });
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            mHttpLoggingInterceptor.setLevel(LogUtil.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
            return mHttpLoggingInterceptor.intercept(chain);
        }
    }

    public static class NoHttpsInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            if (LogUtil.DEBUG) {
                try {
                    request = removeHttpsRequest(request);
                } catch (Exception e) {

                }
            }
            return chain.proceed(request);
        }

        private Request removeHttpsRequest(Request request) {
//            if (!FileUtils.exists("noHttps")) {
//                return request;
//            }
//
//            String url = request.url().toString();
//            if (url.startsWith(BuildConfig.API_HOST)) {
//                request = request.newBuilder().url(request.url().toString().replaceFirst("https://", "http://")).build();
//            }
            return request;
        }
    }
}
