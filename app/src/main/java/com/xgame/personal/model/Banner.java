package com.xgame.personal.model;

import com.xgame.base.api.DataProtocol;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by dingning1
 * on 18-1-29.
 */


public class Banner implements DataProtocol {

    public String img;
    public String bg;
    public String title;
    public String subTitle;
    public String icon;
    public String extension;

    @Override
    public String toString() {
        return "Banner{" +
                "img='" + img + '\'' +
                ", bg='" + bg + '\'' +
                ", title='" + title + '\'' +
                ", subTitle='" + subTitle + '\'' +
                ", icon='" + icon + '\'' +
                ", extension='" + extension + '\'' +
                '}';
    }
}
