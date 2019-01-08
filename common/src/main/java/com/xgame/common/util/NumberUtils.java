package com.xgame.common.util;

import android.text.TextUtils;

/**
 * Created by cox
 * on 16-6-22.
 */
public class NumberUtils {
    public static final String TAG = "NumberUtils";

    private NumberUtils() {
    }

    public static int parseInt(String s, int defaultValue) {
        if (TextUtils.isEmpty(s)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static long parseLong(String s, long defaultValue) {
        if (TextUtils.isEmpty(s)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(s);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static float parseFloat(String s, float defaultValue) {
        if (TextUtils.isEmpty(s)) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(s);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static boolean parseBoolean(String s) {
        if (TextUtils.isEmpty(s)) {
            return false;
        }
        try {
            return Boolean.parseBoolean(s);
        } catch (Exception e) {
            return false;
        }
    }


    public static String getFloatString(float value) {
        int intValue = (int) value;
        if (intValue == value) {
            return String.valueOf(intValue);
        } else {
            return String.valueOf(value);
        }
    }

    public static String getFloatString(String valueString) {
        float value = parseFloat(valueString, 0);
        int intValue = (int) value;
        if (intValue == value) {
            return String.valueOf(intValue);
        } else {
            return String.valueOf(value);
        }
    }
}
