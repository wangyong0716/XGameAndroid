
package com.xgame.account;

import java.lang.reflect.Type;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.miui.zeus.mario.sdk.MarioSdk;
import com.xgame.account.event.AccountEventController;
import com.xgame.account.model.LoginModelImpl;
import com.xgame.account.model.User;
import com.xgame.app.XgameApplication;
import com.xgame.common.application.ApplicationStatus;
import com.xgame.common.util.GlobalGson;
import com.xgame.common.util.LogUtil;
import com.xgame.common.util.SharePrefUtils;
import com.xgame.push.PushManager;
import com.xiaomi.mipush.sdk.MiPushClient;

import android.text.TextUtils;

/**
 * Created by wuyanzhi on 2018/1/25.
 */
public class UserManager {
    private static class Holder {
        static final UserManager INSTANCE = new UserManager();
    }

    private static final String TAG = "UserManager";
    private static final String PREF_NAME = "user_info";
    private static final String KEY_USER = "user";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_AD_TOKEN = "ad_token";
    private static final String KEY_BAILU_TOKEN = "bailu_token";
    private static final String KEY_TYPE = "type";

    /**
     * -1 phone_without_passwd; 0 : phone_with_password ,其他四种三方用户
     * {@link com.xgame.social.SocialConstants.LOGIN}
     */
    private int mLoginType = -2;
    private User mUser;
    private String mToken;
    private String mAdToken;
    private String mBailuToken;
    private String mLoginToken; //登录验签

    private boolean mIsLogin;

    private UserManager() {
        init();
    }

    public static UserManager getInstance() {
        return Holder.INSTANCE;
    }

    private void init() {
        mToken = SharePrefUtils.getString(XgameApplication.getApplication(), PREF_NAME, KEY_TOKEN,
                null);
        mLoginType = SharePrefUtils.getInt(XgameApplication.getApplication(), PREF_NAME, KEY_TYPE,
                -2);
        mAdToken = SharePrefUtils.getString(XgameApplication.getApplication(), PREF_NAME, KEY_AD_TOKEN,
                null);
        mBailuToken = SharePrefUtils.getString(XgameApplication.getApplication(), PREF_NAME, KEY_BAILU_TOKEN,
                null);
        getUser(false);
        if (LogUtil.DEBUG) {
            LogUtil.d(TAG, "user: " + getUser().toJSONObject());
        }
    }

    public synchronized void setUser(final User user, boolean write) {
        if (user != null && user.getUserid() != 0) {
            if (mUser == null) {
                mUser = new User();
            }
            if (mUser != user) {
                mUser.copy(user);
            }
            if (write) {
                SharePrefUtils.putString(ApplicationStatus.getApplicationContext(), PREF_NAME,
                        KEY_USER, GlobalGson.get().toJson(mUser));
            }
            if (mUser != null && mUser.getUserid() != User.DEFAULT_USER_ID
                    && !TextUtils.isEmpty(getToken())) {
                mIsLogin = true;
                MarioSdk.setUid(mUser.getUserid());
            }
            PushManager.registerRegIdToServer();
            LogUtil.d(TAG, "setUser UserId:" + user.getUserid() + ",Token;" + getToken());
        }
    }

    /**
     * 注销用户
     */
    public synchronized void clearUser() {
        new LoginModelImpl().logout(); // TODO 应该收到返回后在清除本地token，目前直接清除了
        SharePrefUtils.clear(XgameApplication.getApplication(), PREF_NAME);
        clearMemoryCache();
        AccountEventController.onActionLogOff(AccountEventController.LogoutEvent.EVENT_TYPE_NONE);
        LogUtil.d(TAG, "clearUser");
        // LoginActivity.reLogin(XgameApplication.getApplication());
    }

    /**
     * 获取用户
     * 
     * @return
     */
    public synchronized User getUser() {
        return getUser(false);
    }

    private synchronized User getUser(boolean isClearUser) {
        if (mUser != null && !isClearUser) {
            return mUser;
        }
        if ((isClearUser && isLogin()) || !isClearUser) {
            String userInfo = SharePrefUtils.getString(XgameApplication.getApplication(), PREF_NAME,
                    KEY_USER, null);
            if (!TextUtils.isEmpty(userInfo)) {
                Type user = new TypeToken<User>() {
                }.getType();
                try {
                    mUser = GlobalGson.get().fromJson(userInfo, user);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    mUser = new User();
                    mIsLogin = false;
                }
                if (mUser != null && mUser.getUserid() != User.DEFAULT_USER_ID
                        && !TextUtils.isEmpty(getToken())) {
                    mIsLogin = true;
                    MarioSdk.setUid(mUser.getUserid());
                    MiPushClient.setAlias(XgameApplication.getApplication(), Long.toString(mUser.getUserid()), null);
                }
            } else {
                mUser = new User();
                mIsLogin = false;
            }
        }
        return mUser;

    }

    /**
     * 是否登录
     * 
     * @return
     */
    public synchronized boolean isLogin() {
        return mIsLogin;
    }

    public synchronized void setIsLogin(boolean isLogin) {
        this.mIsLogin = isLogin;
    }

    public synchronized void clearMemoryCache() {
        mUser = new User();
        mToken = null;
        mAdToken = null;
        mBailuToken = null;
        mIsLogin = false;
        mLoginToken = null;
        mLoginType = -2;
        MarioSdk.setUid(0L);
    }

    /**
     * 获取用户id
     * 
     * @return
     */
    public synchronized long getUserId() {
        return getInstance().getUser().getUserid();
    }

    /**
     * 获取token
     * 
     * @return
     */
    public synchronized String getToken() {
        return mToken;
    }

    public synchronized void setToken(String token) {
        if (!TextUtils.isEmpty(token)) {
            mToken = token;
            SharePrefUtils.putString(XgameApplication.getApplication(), PREF_NAME, KEY_TOKEN,
                    token);
        }
    }

    /**
     * 获取AdToken
     *
     * @return
     */
    public synchronized String getAdToken() {
        return mAdToken;
    }

    public synchronized void setAdToken(String token) {
        if (!TextUtils.isEmpty(token)) {
            mAdToken = token;
            SharePrefUtils.putString(XgameApplication.getApplication(), PREF_NAME, KEY_AD_TOKEN,
                    token);
        }
    }

    /**
     * 获取bailuToken
     *
     * @return
     */
    public synchronized String getBailuToken() {
        return mBailuToken;
    }

    public synchronized void setBailuToken(String token) {
        if (!TextUtils.isEmpty(token)) {
            mBailuToken = token;
            SharePrefUtils.putString(XgameApplication.getApplication(), PREF_NAME, KEY_BAILU_TOKEN,
                    token);
        }
    }

    /**
     * 获取登录type
     *
     * @return {@link com.xgame.social.SocialConstants.LOGIN}
     */
    public synchronized int getLoginType() {
        return mLoginType;
    }

    public synchronized void setLoginType(int loginType) {
        mLoginType = loginType;
        SharePrefUtils.putInt(XgameApplication.getApplication(), PREF_NAME, KEY_TYPE, loginType);

    }

    public String getLoginToken() {
        return mLoginToken;
    }

    public void setLoginToken(String sign) {
        mLoginToken = sign;
    }

    public interface Callback {
        public void done();
    }
}
