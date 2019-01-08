package com.xgame.home.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.xgame.base.api.DataProtocol;

import static java.util.Collections.singletonList;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-26.
 */


public abstract class TabPage implements DataProtocol {

    protected ImageItemBar banner;

    protected ImageItemBox[] recommend;

    protected XGameItem[] items;

    /**
     * 预留字段
     */
    protected int layout;

    /**
     * 预留字段
     */
    protected int itemType;


    @LayoutType
    public int layout() {
        return this.layout;
    }

    @ItemType
    public int itemType() {
        return this.itemType;
    }

    public List<ImageItemBar> banner() {
        return banner != null ? singletonList(banner) : this.<ImageItemBar>emptyList();
    }

    public List<ImageItemBox> recommend() {
        return recommend != null && recommend.length > 0 ?
                Arrays.asList(recommend) : this.<ImageItemBox>emptyList();
    }

    public List<XGameItem> items() {
        if (items != null && items.length > 0) {
            List<XGameItem> ret = new ArrayList<>(items.length);
            for (XGameItem i : items) {
                i.type = itemType();
                ret.add(i);
            }
            return ret;
        }
        return emptyList();
    }

    @NonNull
    private <T> List<T> emptyList() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "TabPage{" +
                "banner=" + banner +
                ", recommend=" + Arrays.toString(recommend) +
                ", items=" + Arrays.toString(items) +
                ", layout=" + layout +
                ", itemType=" + itemType +
                '}';
    }
}
