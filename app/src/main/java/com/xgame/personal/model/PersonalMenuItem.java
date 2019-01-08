package com.xgame.personal.model;

import com.xgame.base.api.DataProtocol;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by dingning1
 * on 18-1-29.
 */


public class PersonalMenuItem implements DataProtocol {

    public String status;
    public long state;
    public String msg;
    public String extension;

    @Override
    public String toString() {
        return "PersonalMenuItem{" +
                "status='" + status + '\'' +
                ", state=" + state +
                ", msg='" + msg + '\'' +
                ", extension='" + extension + '\'' +
                '}';
    }
}
