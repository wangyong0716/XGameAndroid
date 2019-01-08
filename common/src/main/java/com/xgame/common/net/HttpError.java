package com.xgame.common.net;

import android.text.TextUtils;

public class HttpError {
    private String source;
    private int code;
    private String msg;

    public HttpError(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public HttpError(String source, int code, String msg) {
        this.source = source;
        this.code = code;
        this.msg = msg;
    }

    public String getSource() {
        return source;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public String getMsg(String... sources) {
        if (validSource(sources)) {
            return msg;
        }
        return "";
    }

    private boolean validSource(String... sources) {
        if (sources == null || sources.length == 0) {
            return true;
        }

        for (String s : sources) {
            if (!TextUtils.isEmpty(s) && s.equals(this.source)) {
                return true;
            }
        }

        return false;
    }
}
