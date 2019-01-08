package com.xgame.common.net;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.xgame.common.BuildConfig;
import com.xgame.common.util.JsonUtil;
import com.xgame.common.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

public class CommonInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Map<String, String> commonQueryMap = ApiParamsUtils.getCommonParamsMap();
        Map map = getParamsMap(request.url());
        map.putAll(commonQueryMap);
        String sign = SaltUtil.getSign(map, SaltUtil.UUID);
        if (LogUtil.DEBUG) {
//            File file = FileUtils.getCacheDirectory();
//            File signFile = new File(file, "sign");
//            if (signFile.exists()) {
//                sign = "wg_xk_sign";
//            }

        }
        commonQueryMap.put("sign", sign);
        Headers.Builder headerBuilder = request.headers().newBuilder();
        if (commonQueryMap.size() > 0) {
            Iterator iterator = commonQueryMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String value = (String) entry.getValue();
                value = URLEncoder.encode(value, "UTF-8");
                headerBuilder.add((String) entry.getKey(), value);
            }
        }
      //  headerBuilder.add("Authorization", SharePrefUtils.getString(BaseApplication.getContext(), Constant.APP.TOKEN, Constant.APP.DEF_TOKEN));
        request = request.newBuilder().headers(headerBuilder.build()).build();
        return chain.proceed(request);
    }

    private String getParamsStr(RequestBody requestBody) throws IOException {
        Buffer buffer = new Buffer();
        requestBody.writeTo(buffer);
        Charset charset = Charset.forName("UTF-8");
        MediaType contentType = requestBody.contentType();
        if (contentType != null) {
            charset = contentType.charset();
        }
        return buffer.readString(charset);
    }

    private Map getParamsMap(RequestBody requestBody) {
        JSONObject object = null;
        try {
            object = new JSONObject(getParamsStr(requestBody));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return JsonUtil.json2Map(object);
    }

    private Map getParamsMap(HttpUrl url) {
        Map<String, String> map = new HashMap<>();
        synchronized (map) {
            int size = url.querySize();
            for (int i = 0; i < size; i++) {
                map.put(url.queryParameterName(i), url.queryParameterValue(i));
            }
        }

        return map;
    }

    private Request injectParamsIntoUrl(HttpUrl.Builder httpUrlBuilder, Request.Builder requestBuilder, Map<String, String> paramsMap) {
        if (paramsMap.size() > 0) {
            Iterator iterator = paramsMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                httpUrlBuilder.addQueryParameter((String) entry.getKey(), (String) entry.getValue());
            }
            requestBuilder.url(httpUrlBuilder.build());
            return requestBuilder.build();
        }

        return null;
    }

}

