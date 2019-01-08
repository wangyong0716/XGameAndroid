package com.xgame.common.util;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import java.io.Serializable;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

public class IntentParser {

    public static final int INVALID_LONG_VALUE = 0;

    static final String TAG = "IntentParser";

    static final Pattern ARGS_PATTERN = Pattern.compile("([0-9a-zA-Z_-]+)=([^&]*)");

    static final int NAME = 1;

    static final int VALUE = 2;

    static final String MARK_QUESTION = "?";

    static final char AMPERSAND = '&';

    static final char MARK_EQUAL = '=';

    static final char MARK_SHARP = '#';

    private static final Pattern HTTP_PATTERN = Pattern.compile("^https?://");
    private static final Pattern XGAME_PATTERN = Pattern.compile("^xgame://");

    private IntentParser() {
        // Intentional blank.
    }

    public static String getString(Intent intent, String key) {
        if (checkInValid(intent, key)) {
            return null;
        }
        String value = intent.getStringExtra(key);
        if (TextUtils.isEmpty(value)) {
            value = intent.getStringExtra(key.toLowerCase());
        }
        if (TextUtils.isEmpty(value)) {
            value = getString(intent.getDataString(), key);
        }
        return value;
    }

    @Nullable
    public static String getString(String uri, String key) {
        String value = null;
        final Map<String, String> map = parseUrlArgs(uri);
        if (map != null && !map.isEmpty()) {
            value = map.get(key);
            value = value == null ? map.get(key.toLowerCase()) : value;
        }
        return value;
    }

    private static boolean checkInValid(Intent intent, String key) {
        return intent == null || TextUtils.isEmpty(key);
    }

    public static int getInt(Intent intent, String key, int def) {
        if (checkInValid(intent, key)) {
            return def;
        }
        Bundle ext = intent.getExtras();
        if (ext != null) {
            Object v = ext.get(key);
            if (v == null) {
                v = ext.get(key.toLowerCase());
            }
            if (v != null) {
                try {
                    return (Integer) v;
                } catch (Throwable t) {
                    // invalid continue parse dataString
                }
            }
        }
        String v = getString(intent.getDataString(), key);
        return parseInt(v, def);
    }

    private static int parseInt(Object v, int def) {
        if (v == null) {
            return def;
        }
        try {
            return (Integer) v;
        } catch (Throwable t) {
            // invalid
        }
        return def;
    }

    public static <T extends Serializable> T getSerializable(Intent intent, String key) {
        if (checkInValid(intent, key)) {
            return null;
        }
        Serializable value = intent.getSerializableExtra(key);
        if (value == null) {
            value = intent.getSerializableExtra(key.toLowerCase());
        }
        return (T) value;
    }

    public static Map<String, String> parseUrlArgs(String urlOrArgs) {
        if (TextUtils.isEmpty(urlOrArgs)) {
            return Collections.emptyMap();
        }
        String strArgs;
        try {
            strArgs = URLDecoder.decode(urlOrArgs, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return Collections.emptyMap();
        }
        if (strArgs != null) {
            if (HTTP_PATTERN.matcher(strArgs).find()) {
                int qIndex = strArgs.indexOf(MARK_QUESTION);
                if (qIndex >= 0) {
                    strArgs = qIndex < strArgs.length() - 1 ? strArgs.substring(qIndex + 1) : "";
                }
            } else if (XGAME_PATTERN.matcher(strArgs).find()) {
                int qIndex = strArgs.indexOf(MARK_QUESTION);
                if (qIndex >= 0) {
                    strArgs = qIndex < strArgs.length() - 1 ? strArgs.substring(qIndex + 1) : "";
                }
            }
        }
        Map<String, String> retMap = new ArrayMap<>();
        if (!TextUtils.isEmpty(strArgs)) {
            int start = 0, end, idx;
            do {
                idx = strArgs.indexOf(MARK_EQUAL, start);
                if (idx == -1) {
                    break;
                }
                String key = strArgs.substring(start, idx);
                if ("url".equals(key)
                        && "http".equals(strArgs.substring(idx + 1, idx + 5))
                        && containQuestionOrSharpMark(strArgs, idx + 1)) {
                    end = strArgs.length();
                } else {
                    int amperIdx = strArgs.indexOf(AMPERSAND, start);
                    end = amperIdx == -1 ? strArgs.length() : amperIdx;
                }
                String value = strArgs.substring(idx + 1, end);
                retMap.put(key, value);
                start = end + 1;
            } while (end < strArgs.length());
        }
        return retMap;
    }

    private static boolean containQuestionOrSharpMark(String strArgs, int start) {
        return strArgs.indexOf(MARK_QUESTION, start) > 0 || strArgs.indexOf(MARK_SHARP, start) > 0;
    }

}

