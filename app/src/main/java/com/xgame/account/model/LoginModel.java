package com.xgame.account.model;

import android.app.Activity;

import java.util.Map;

/**
 * Created by wuyanzhi on 2018/1/28.
 */

public interface LoginModel {
    public void getVerificationCode(String phoneNum);
    public void onPhoneLogin(String phoneNum, String verificationCode);
    public void onPhoneBind(String phoneNum, String verificationCode);
    public void onThirdAppLogin(Activity activity, int loginType);
    public void onThirdAppBind(Activity activity, int loginType);
    public void register();
    public void modifyUserInfo(Map<String, String> map);
    public void completeUserInfo(Map<String, String> map);
    public void uploadAvatar(String photoBase64String, Map<String, String> map);
    public void refreshToken();
    public void logout();
}
