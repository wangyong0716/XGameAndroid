package com.xgame.common;

/**
 * Created by wuyanzhi on 2018/1/24.
 */

public class Constants {

    public interface CODE {
        public static final int ERROR_LOGIN = -2001;
        public static final int NET_ERROR_UNAUTHORIZED = 401;
        public static final int NET_ERROR_TOKEN_EXPIRED = -6001;
    }

    public class APP {
        public static final String YUZHUANG_CTA_ALERT_PREF_KEY = "yuzhuang_cta_alert";
        public static final String APP_CURR_VERSION_PREF_KEY = "app_curr_version";
        public static final String APP_CURR_VERSION_FIRST_OPEN_TIME_PREF_KEY = "app_curr_version_first_open_time";
        public static final String TOKEN = "token";
        public static final String DEF_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ2IjowLCJpYXQiOjE0ODI5MjgxMTYsImQiOnsidWlkIjoiZGVmZTlkYmYtNGVjYS00MzUwLTg5ZDQtNWVmMDYxZjdhNDU2In19.y8tuvKqxuI5EjnUSloqePAGPzTHEELFAU-uFR3TxLkA";
        public static final String MI_PUSH_REG_ID_PREF_KEY = "mi_push_reg_id";
    }

    public static final String APP_KEY = "XGAME";
    public static final String APP_TOKEN = "ff180b01e428ed68e281c102e78ce039";
}
