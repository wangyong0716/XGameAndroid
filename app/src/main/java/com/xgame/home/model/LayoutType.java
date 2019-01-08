package com.xgame.home.model;

import android.support.annotation.IntDef;

@IntDef({
        LayoutType.LAYOUT_GRID,
        LayoutType.LAYOUT_LIST,
})
public @interface LayoutType {

    int LAYOUT_GRID = 1;

    int LAYOUT_LIST = 2;


}
