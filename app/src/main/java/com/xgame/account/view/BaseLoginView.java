package com.xgame.account.view;

import com.xgame.account.base.BaseView;
import com.xgame.account.presenter.LoginPresenter;

/**
 * Created by wuyanzhi on 2018/1/28.
 */

public interface BaseLoginView extends BaseView<LoginPresenter> {
    public void onLoginSuccess(int type);
    public void onLoginFailed(int type, String msg);
    public void onThirdAppLoginStart(int type);
    public void onThirdAppLoginEnd(int type);
}
