package com.xgame.common.net;

import android.content.Context;

import com.xgame.common.util.LogUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class ParamsInterceptor implements Interceptor {
    private static final String TAG = ParamsInterceptor.class.getSimpleName();

    private WeakReference<Context> contextWeakReference;

    public ParamsInterceptor(Context context) {
        contextWeakReference = new WeakReference<Context>(context);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request oldRequest = chain.request();
      //  Map<String, String> commonQueryMap = ApiParamsUtils.getCommonParamsMap(contextWeakReference.get());
        HttpUrl.Builder authorizedUrlBuilder = oldRequest.url().newBuilder();
     //   injectParamsIntoUrl(authorizedUrlBuilder, commonQueryMap);
        Request newRequest = oldRequest.newBuilder()
                .url(authorizedUrlBuilder.build())
                .build();
        LogUtil.i(TAG, "url :" + URLDecoder.decode(newRequest.url().toString(), "utf-8"));
        return chain.proceed(newRequest);
    }

    private void injectParamsIntoUrl(final HttpUrl.Builder authorizedUrlBuilder, Map<String, String> paramsMap) {
        if (paramsMap.size() > 0) {
            Iterator iterator = paramsMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                authorizedUrlBuilder.addQueryParameter((String) entry.getKey(), (String) entry.getValue());
            }
           // authorizedUrlBuilder.addQueryParameter("uid", UserManager.getUserId());
        }
    }

}