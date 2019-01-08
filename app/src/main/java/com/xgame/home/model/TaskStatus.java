package com.xgame.home.model;

import com.xgame.base.api.DataProtocol;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-2-8.
 */


public class TaskStatus implements DataProtocol {

    protected boolean success;

    protected int rewardsToBeReceived;

    protected String summary;

    public boolean hasAwardsNotRecv() {
        return success && rewardsToBeReceived > 0;
    }
}
