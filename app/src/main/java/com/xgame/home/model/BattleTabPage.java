package com.xgame.home.model;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-26.
 */


public class BattleTabPage extends TabPage {

    @Override
    public int layout() {
        return LayoutType.LAYOUT_GRID;
    }

    @Override
    public int itemType() {
        return ItemType.TYPE_GAME_GRID_ITEM;
    }
}
