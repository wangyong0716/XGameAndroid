package com.xgame.personal.model;

import com.xgame.base.api.DataProtocol;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by dingning1
 * on 18-1-31.
 */


public class BillItem implements DataProtocol {

    public String userId;
    public String infoId;
    public String gameId;
    public int type;
    public long time;
    public float cash;
    public String remark;

    @Override
    public String toString() {
        return "BillItem{" +
                "userId='" + userId + '\'' +
                ", infoId='" + infoId + '\'' +
                ", gameId='" + gameId + '\'' +
                ", type=" + type +
                ", time=" + time +
                ", cash=" + cash +
                ", remark='" + remark + '\'' +
                '}';
    }
}

