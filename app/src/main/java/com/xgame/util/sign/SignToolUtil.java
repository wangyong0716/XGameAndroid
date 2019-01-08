package com.xgame.util.sign;

import com.google.common.collect.Lists;

import java.security.MessageDigest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SignToolUtil {

    private static final String appSecret = "3MiOiJYR0FNRSIsImF1ZCI6IlhHQU1FX1VTRVIiLCJzdWIiOiJYR0FNRV9UT0t";

    public SignToolUtil() {
    }

    private static String toHexValue(byte[] messageDigest) {
        if(messageDigest == null) {
            return "";
        } else {
            StringBuilder hexValue = new StringBuilder();
            byte[] arr$ = messageDigest;
            int len$ = messageDigest.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                byte aMessageDigest = arr$[i$];
                int val = 255 & aMessageDigest;
                if(val < 16) {
                    hexValue.append("0");
                }

                hexValue.append(Integer.toHexString(val));
            }

            return hexValue.toString();
        }
    }

    public static String getAppSecret() {
        return appSecret;
    }

    public static boolean signVerify2(String appSecret, Map<String, String> params) {
        Map<String, String> map = new ConcurrentHashMap();
        map.put("appSecret", appSecret);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!entry.getKey().equalsIgnoreCase("sign")) {
                map.put(entry.getKey(), entry.getValue());
            }
        }

        String sign1 = sign2(map);
        if(sign1.equals(params.get("sign"))) {
            return true;
        } else {
            return false;
        }
    }

    public static String sign2(Map<String, String> params) {
        List<String> keys = Lists.newArrayList(params.keySet());
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            sb.append(key).append("=").append(params.get(key)).append("&");
        }
        try {
            return toHexValue(encryptMD5(sb.toString().getBytes()));
        } catch (Exception var5) {
            var5.printStackTrace();
            throw new RuntimeException("md5 error");
        }
    }

    private static byte[] encryptMD5(byte[] data) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(data);
        return md5.digest();
    }

    public static void main(String[] args) {

        String appSecret = "znb";
        // 请求参数
        Map<String, String> znb = new HashMap<>();
        znb.put("appSecret", appSecret);
        znb.put("result", "asdfasdfasdfsadf");
        // 生成签名方式
        znb.put("sign", sign2(znb));

        // 验证签名
        System.out.println(signVerify2(appSecret, znb));

    }
}
