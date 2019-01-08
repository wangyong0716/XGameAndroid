package com.xgame.common.net;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

/**
 * 根据request请求参数生成校验码。
 * 目前生成规则
 * 1. 将request中key值为s的参数对去掉
 * 2. 按照key值的字母顺序给request中的参数对排序
 * test3. 生成url_encoded格式的请求字符串，然后取md5值。
 */
public class SaltUtil {
    private static final String TAG = SaltUtil.class.getSimpleName();

    public static String getSign(Map<String, String> map, String uuid) {
        map = getSortMap(map);
        StringBuilder keyBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (keyBuilder.length() > 0) {
                keyBuilder.append("&");
            }
            keyBuilder.append(String.format("%s=%s", entry.getKey(), entry.getValue()));
        }

        keyBuilder.append("&").append(uuid);

        String key = keyBuilder.toString();
        Log.i(TAG, "key :" + key);
        String md5 = getMd5Digest(key);
        Log.i(TAG, "md5 :" + md5);
        return md5;
    }


    public static Map getSortMap(Map<String, String> map) {
        if (map == null) throw new IllegalArgumentException("map must not be null.");

        TreeMap<String, String> storeMap = new TreeMap<>();
        storeMap.putAll(map);
        return storeMap;
    }

    private static byte[] getBytes(String s) {
        try {
            return s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return s.getBytes();
        }
    }

    private static String getMd5Digest(String pInput) {
        try {
            MessageDigest lDigest = MessageDigest.getInstance("MD5");
            lDigest.update(getBytes(pInput));
            BigInteger lHashInt = new BigInteger(1, lDigest.digest());
            return String.format("%1$032X", lHashInt);
        } catch (NoSuchAlgorithmException lException) {
            throw new RuntimeException(lException);
        }
    }

    public static final String UUID = "b00e7a36-510a-4552-a3b9-0338ec18b55a";
}
