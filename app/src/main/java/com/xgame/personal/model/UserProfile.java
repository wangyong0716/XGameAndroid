package com.xgame.personal.model;

import com.xgame.base.api.DataProtocol;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by dingning1
 * on 18-1-31.
 */


public class UserProfile implements DataProtocol {

    public String nickName;
    public long accountId;
    public String avatar;
    public String inviteCode;
    public int coin;
    public double cash;

    @Override
    public String toString() {
        return "UserProfile{" +
                "nickName='" + nickName + '\'' +
                ", accountId=" + accountId +
                ", avatar='" + avatar + '\'' +
                ", inviteCode='" + inviteCode + '\'' +
                ", coin=" + coin +
                ", cash=" + cash +
                '}';
    }
}
