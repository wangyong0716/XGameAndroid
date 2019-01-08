package com.xgame.personal.model;

import com.google.gson.annotations.SerializedName;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by dingning1
 * on 18-2-3.
 */


public class UpdateInterval {

    @SerializedName("2G") public long network2g;
    @SerializedName("3G") public long network3g;
    @SerializedName("4G") public long network4g;
    @SerializedName("WIFI") public long networkWifi;

    @Override
    public String toString() {
        return "UpdateInterval{" +
                "network2g='" + network2g + '\'' +
                ", network3g='" + network3g + '\'' +
                ", network4g='" + network4g + '\'' +
                ", networkWifi='" + networkWifi + '\'' +
                '}';
    }
}
