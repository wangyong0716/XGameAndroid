package com.xgame.home.model;

import android.support.annotation.IntDef;

@IntDef({
        ItemType.TYPE_GAME_GRID_ITEM,
        ItemType.TYPE_IMAGE_BAR,
        ItemType.TYPE_IMAGE_BOX,
        ItemType.TYPE_MSG_BAR,
        ItemType.TYPE_GAME_ITEM_BAR
})
public @interface ItemType {

    int TYPE_GAME_GRID_ITEM = 1;

    int TYPE_IMAGE_BAR = 2;

    int TYPE_IMAGE_BOX = 3;

    int TYPE_MSG_BAR = 4;

    int TYPE_GAME_ITEM_BAR = 5;
}
