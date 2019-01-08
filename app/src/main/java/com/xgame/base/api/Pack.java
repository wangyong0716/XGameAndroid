package com.xgame.base.api;

import com.xgame.common.api.IProtocol;
import com.xgame.common.api.Packable;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-25.
 */


public class Pack<T> implements Packable<T>, IProtocol {

    public static final int CODE_UNKNOWN = 0;

    public static final int CODE_SUCC = 200;

    public static final int CODE_PARAM_ERROR = 400;

    public static final int CODE_INVALID_AUTH = 401;

    public static final int CODE_NOT_AUTHORIZED = 403;

    public static final int CODE_ENDPOINT_ERROR = 404;

    public static final int CODE_METHOD_ERROR = 405;

    public static final int CODE_SERVER_ERROR = 500;

    public static final int CODE_SERVER_DOWNGRADE = 700;

    public int code;

    public T data;

    public String msg;

    private int httpCode;

    public Pack() {
        // don't remove
    }

    @Override
    public T data() {
        return this.data;
    }

    @Override
    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
        code = (httpCode >= 200 && httpCode < 300) ? CODE_SUCC : CODE_UNKNOWN;
    }

    @Override
    public boolean isFailure() {
        return this.code != Pack.CODE_SUCC;
    }

    @Override
    public String toString() {
        return "Pack{" +
                "code=" + code +
                ", data=" + data +
                ", msg='" + msg + '\'' +
                '}';
    }
}
