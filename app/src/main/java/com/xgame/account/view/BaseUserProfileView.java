package com.xgame.account.view;

import com.xgame.account.base.BaseView;
import com.xgame.account.presenter.LoginPresenter;

/**
 * Created by wuyanzhi on 2018/1/28.
 */

public interface BaseUserProfileView extends BaseView<LoginPresenter> {
    public void uploadUserInfoSuccess();
    public void uploadUserInfoFailed(String msg);
    public void uploadAvatarSuccess();
    public void uploadAvatarFailed(String msg);
}
