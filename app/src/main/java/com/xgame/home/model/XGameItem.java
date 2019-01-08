package com.xgame.home.model;

import android.content.Intent;
import android.net.Uri;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 * 主要用于添加Game类型的默认处理
 * Created by jackwang
 * on 18-1-30.
 */


public class XGameItem extends Item {

    public static final int PLAY_TYPE_DOUBLE = 1; // 双人

    public static final int PLAY_TYPE_MULTI = 2; // 多人

    public static final int GAME_TYPE_MATCH = 1; // 匹配场

    public static final int GAME_TYPE_COIN = 2; // 金币场

    public static final int GAME_TYPE_BW = 3; // 百万场

    public static final Uri REDIRECT_BATTLE_MATCH = Uri.parse("xgame://pk.baiwan.com/home#/battle/match");

    public static final Uri REDIRECT_BATTLE_COIN = Uri.parse("xgame://pk.baiwan.com/home#/coinbattle/detail");

    private static final Uri REDIRECT_BATTLE_BW = Uri.parse("xgame://pk.baiwan.com/home#/bwbattle");

    public static final String EXTRA_GAME_ID = "gameId";

    public static final String EXTRA_GAME_NAME = "gameName";

    public static final String EXTRA_GAME_URL = "gameUrl";

    public static final String EXTRA_GAME_TYPE = "gameType";

    public static final String EXTRA_PLAY_TYPE = "playType";

    public static final String EXTRA_GOLD_COIN = "goldCoin";

    /**
     * 游戏图标
     */
    protected String icon;

    protected String gameId;

    protected String gameUrl;

    protected int gameType; // 1匹配，2金币场，3百万场

    protected int playType;

    protected String onlineCount; // 在线人数描述

    private static void inflateExtra(Intent in, XGameItem item) {
        in.putExtra(EXTRA_GAME_ID, item.gameId());
        in.putExtra(EXTRA_GAME_NAME, item.title());
        in.putExtra(EXTRA_GAME_URL, item.gameUrl());
        in.putExtra(EXTRA_GAME_TYPE, item.gameType());
        in.putExtra(EXTRA_PLAY_TYPE, item.playType());
    }

    public String gameId() {
        return gameId;
    }

    public String gameUrl() {
        return gameUrl;
    }

    public int gameType() {
        return gameType;
    }

    public int playType() {
        return playType;
    }

    public String icon() {
        return icon;
    }

    @Override
    public String stamp() {
        return onlineCount;
    }

    @Override
    public Intent extension() {
        Intent in = super.extension();
        if (in != null) {
            inflateExtra(in, this);
            return in;
        }
        if (gameType == GAME_TYPE_MATCH) {
            in = new Intent(Intent.ACTION_VIEW, REDIRECT_BATTLE_MATCH);
            inflateExtra(in, this);
        } else if (gameType == GAME_TYPE_COIN) {
            in = new Intent(Intent.ACTION_VIEW, REDIRECT_BATTLE_COIN);
            inflateExtra(in, this);
        } else if (gameType == GAME_TYPE_BW) {
            in = new Intent(Intent.ACTION_VIEW, REDIRECT_BATTLE_BW);
            inflateExtra(in, this);
        } else {
            in = null;
        }
        return in;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof XGameItem)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final XGameItem xGameItem = (XGameItem) o;
        if (gameType != xGameItem.gameType) {
            return false;
        }
        if (playType != xGameItem.playType) {
            return false;
        }
        if (icon != null ? !icon.equals(xGameItem.icon) : xGameItem.icon != null) {
            return false;
        }
        if (gameId != null ? !gameId.equals(xGameItem.gameId) : xGameItem.gameId != null) {
            return false;
        }
        return gameUrl != null ? gameUrl.equals(xGameItem.gameUrl) : xGameItem.gameUrl == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (icon != null ? icon.hashCode() : 0);
        result = 31 * result + (gameId != null ? gameId.hashCode() : 0);
        result = 31 * result + (gameUrl != null ? gameUrl.hashCode() : 0);
        result = 31 * result + gameType;
        result = 31 * result + playType;
        return result;
    }
}
