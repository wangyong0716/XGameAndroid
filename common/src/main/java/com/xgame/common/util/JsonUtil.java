package com.xgame.common.util;


import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by wuyanzhi on 2018/1/24.
 */
public class JsonUtil {

    public static Map<String, String> json2Map(JSONObject jsonObject) {
        Map<String, String> map = new HashMap<>();
        if (jsonObject != null) {
            Iterator<String> iterator = jsonObject.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                map.put(key, String.valueOf(jsonObject.opt(key)));
            }
        }
        return map;
    }
}
