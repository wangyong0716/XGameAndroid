package com.xgame.personal.model;

import java.util.Arrays;

import com.xgame.base.api.DataProtocol;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by dingning1
 * on 18-1-31.
 */


public class BillList implements DataProtocol {

    public BillItem[] details;

    @Override
    public String toString() {
        return "BillList{" +
                "details=" + Arrays.toString(details) +
                '}';
    }
}
