package com.xgame.account.base;

/**
 * Created by wuyanzhi on 2018/1/28.
 */

public interface BaseView <P extends BasePresenter> {
    void setPresenter(P presenter);
}
