package com.xgame.battle.model;

/**
 * Created by zhanglianyu on 18-1-24.
 */

public class Game {

    private String mName;
    private String mUrl;

    public Game setName(String name) {
        mName = name;
        return this;
    }

    public String getName() {
        return mName;
    }

    public Game setUrl(String url) {
        mUrl = url;
        return this;
    }

    public String getUrl() {
        return mUrl;
    }
}
