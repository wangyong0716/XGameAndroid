package com.xgame.personal.model;

import java.util.Arrays;

import com.google.gson.annotations.SerializedName;
import com.xgame.base.api.DataProtocol;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by dingning1
 * on 18-1-29.
 */


public class PersonalMenu implements DataProtocol {
    public Banner banner;
    public PersonalMenuItem[] points;
    @SerializedName("update_interval_minutes") public UpdateInterval updateInterval;
    public long timestamp;

    @Override
    public String toString() {
        return "PersonalMenu{" +
                "banner=" + banner +
                ", points=" + Arrays.toString(points) +
                ", updateInterval=" + updateInterval +
                ", timestamp=" + timestamp +
                '}';
    }
}
