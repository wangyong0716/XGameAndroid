
package com.xgame.account.model;

import android.app.Activity;
import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;

import com.miui.zeus.mario.sdk.MarioSdk;
import com.xgame.account.AccountConstants;
import com.xgame.account.AccountConstants.AccountKey;
import com.xgame.account.UserManager;
import com.xgame.account.api.AvatarResult;
import com.xgame.account.api.ServerLoginResult;
import com.xgame.account.event.AccountEventController;
import com.xgame.account.event.AccountEventController.LoginEvent;
import com.xgame.account.event.AccountEventController.UploadEvent;
import com.xgame.app.XgameApplication;
import com.xgame.common.api.OnCallback;
import com.xgame.common.util.DeviceUtil;
import com.xgame.common.util.LogUtil;
import com.xgame.social.LoginUtil;
import com.xgame.social.SocialConstants;
import com.xgame.social.login.LoginListener;
import com.xgame.social.login.LoginResult;
import com.xgame.social.login.result.BaseToken;
import com.xgame.social.login.result.BaseUser;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.xgame.base.ServiceFactory.persionService;
import static com.xgame.social.SocialConstants.LOGIN.TYPE_DEFAULT;

//TODO
public class LoginModelImpl implements LoginModel {
    public String TAG = "LoginModelImpl";
    private static final int TYPE_WX = 1;
    private static final int TYPE_QQ = 2;
    private static final int TYPE_PHONE = 3;
    private static final int CODE_SUCCESS = 0;

    public LoginModelImpl() {
    }

    @Override
    public void getVerificationCode(final String phoneNum) {
        Map<String, String> map = getCommonMap(XgameApplication.getApplication(), null, null, null,
                SocialConstants.LOGIN.TYPE_PHONE_NUM_VERIFICATION_CODE);
        map.put(AccountKey.KEY_PHONE, phoneNum);
        persionService().getVerificationCode(map).enqueue(new OnCallback<ServerLoginResult>() {
            @Override
            public void onResponse(ServerLoginResult result) {
                if (result != null && result.getCode() == CODE_SUCCESS) {
                    updateUserByServerLoginResult(result);
                    AccountEventController.onActionLogin(
                            LoginEvent.EVENT_TYPE_GET_VERIFICATION_SUCCESS,
                            SocialConstants.LOGIN.TYPE_PHONE_NUM_VERIFICATION_CODE,
                            result.getErrmsg());
                } else {
                    AccountEventController.onActionLogin(
                            LoginEvent.EVENT_TYPE_GET_VERIFICATION_FAILED,
                            SocialConstants.LOGIN.TYPE_PHONE_NUM_VERIFICATION_CODE,
                            result == null ? null : result.getErrmsg());
                }
            }

            @Override
            public void onFailure(ServerLoginResult result) {
                AccountEventController.onActionLogin(LoginEvent.EVENT_TYPE_GET_VERIFICATION_FAILED,
                        SocialConstants.LOGIN.TYPE_PHONE_NUM_VERIFICATION_CODE,
                        result == null ? null : result.getErrmsg());
            }
        });
    }

    @Override
    public void onPhoneLogin(final String phoneNum, String verificationCode) {
        Map<String, String> map = getCommonMap(XgameApplication.getApplication(), null, null, null,
                SocialConstants.LOGIN.TYPE_PHONE_NUM_VERIFICATION_CODE);
        map.put(AccountConstants.AccountKey.KEY_PHONE, phoneNum);
        map.put(AccountConstants.AccountKey.KEY_VERIFICATION, verificationCode);
        UserManager.getInstance().clearMemoryCache();
        persionService().phoneLogin(map).enqueue(new OnCallback<ServerLoginResult>() {
            @Override
            public void onResponse(ServerLoginResult result) {
                UserManager.getInstance().getUser().setPhone(phoneNum);
                handleLoginOrBindResult(true, false, result,
                        SocialConstants.LOGIN.TYPE_PHONE_NUM_VERIFICATION_CODE);
                if (result != null && result.getRegister()) {
                    pullUser();
                }
            }

            @Override
            public void onFailure(ServerLoginResult result) {
                handleLoginOrBindResult(false, false, result,
                        SocialConstants.LOGIN.TYPE_PHONE_NUM_VERIFICATION_CODE);
            }
        });

    }

    @Override
    public void onPhoneBind(final String phoneNum, String verificationCode) {
        Map<String, String> map = getCommonMap(XgameApplication.getApplication(), null, null,
                UserManager.getInstance().getToken(), -1);
        map.put(AccountKey.KEY_PHONE, phoneNum);
        map.put(AccountKey.KEY_VERIFICATION, verificationCode);
        persionService().phoneBind(map).enqueue(new OnCallback<ServerLoginResult>() {
            @Override
            public void onResponse(ServerLoginResult result) {
                if (result != null && result.getCode() == CODE_SUCCESS) { // 返回值只有code是否成功, 直接写数据
                    UserManager.getInstance().getUser().setPhone(phoneNum);
                    UserManager.getInstance().setUser(UserManager.getInstance().getUser(), true);
                    AccountEventController.onActionLogin(LoginEvent.EVENT_TYPE_LOGIN_SUCCESS,
                            SocialConstants.LOGIN.TYPE_PHONE_NUM_VERIFICATION_CODE,
                            result.getErrmsg());
                } else {
                    handleLoginOrBindResult(false, true, result,
                            SocialConstants.LOGIN.TYPE_PHONE_NUM_VERIFICATION_CODE);
                }
            }

            @Override
            public void onFailure(ServerLoginResult result) {
                handleLoginOrBindResult(false, true, result,
                        SocialConstants.LOGIN.TYPE_PHONE_NUM_VERIFICATION_CODE);
            }
        });
    }

    @Override
    public void onThirdAppLogin(Activity activity, int loginType) {
        if (activity != null) {
            switch (loginType) {
                case SocialConstants.LOGIN.TYPE_QQ:
                case SocialConstants.LOGIN.TYPE_WECHAT:
                    LoginUtil.login(activity, loginType, getThirdAppLoginListener());
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onThirdAppBind(Activity activity, int loginType) {
        onThirdAppLogin(activity, loginType);
    }

    private LoginListener mThirdAppLoginListener;

    private LoginListener getThirdAppLoginListener() {
        if (mThirdAppLoginListener == null) {
            mThirdAppLoginListener = new LoginListener() {
                @Override
                public void loginSuccess(LoginResult result) {
                    if (result != null) {
                        int type = result.getPlatform();
                        AccountEventController.onActionLogin(
                                LoginEvent.EVENT_TYPE_THIRD_APP_LOGIN_SUCCESS, type, null);
                        BaseToken token = result.getToken();
                        BaseUser user = result.getUserInfo();
                        if (!UserManager.getInstance().isLogin()) {
                            updateUserByThirdAppLogin(user, token, result.getPlatform());
                            if (token != null) {
                                try {
                                    thirdAppServerLogin(XgameApplication.getApplication(), type, token.getOpenid(),
                                            token.getAccessToken());
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            if (token != null) {
                                try {
                                    thirdAppServerBind(XgameApplication.getApplication(), type, token.getOpenid(),
                                            token.getAccessToken());
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        LogUtil.d(TAG, "third app phoneLogin success");
                    }
                }

                @Override
                public void loginFailure(Exception e) {
                    LogUtil.d(TAG, "third app phoneLogin fail");
                }

                @Override
                public void loginCancel() {
                    LogUtil.d(TAG, "third app phoneLogin cancel");
                }
            };
        }
        return mThirdAppLoginListener;
    }

    private void updateUserByThirdAppLogin(BaseUser user, BaseToken token, int type) {
        User gameUser = UserManager.getInstance().getUser();
        if (user != null) {
            gameUser.setNickname(user.getNickname());
            gameUser.setSex(user.getSex());
            String largeHeadImageUrl = user.getHeadImageUrlLarge();
            gameUser.setHeadimgurl(TextUtils.isEmpty(largeHeadImageUrl) ? user.getHeadImageUrl()
                    : largeHeadImageUrl);
            UserManager.getInstance().setLoginType(type);
        }
        if (token != null) {
            switch (type) {
                case SocialConstants.LOGIN.TYPE_QQ:
                    gameUser.setQqNickname(user.getNickname());
                    gameUser.setQqOpenid(token.getOpenid());
                    break;
                case SocialConstants.LOGIN.TYPE_WECHAT:
                    gameUser.setWxNickname(user.getNickname());
                    gameUser.setWxOpenid(token.getOpenid());
                    break;
                default:
                    break;
            }
            UserManager.getInstance().setLoginToken(token.getAccessToken());
        }
        UserManager.getInstance().setUser(gameUser, false);
    }

    private void thirdAppServerLogin(Context context, final int type, String openId,
            String access_token) {
        if (context == null || openId == null || access_token == null) {
            AccountEventController.onActionLogin(LoginEvent.EVENT_TYPE_LOGIN_FAILED, type, "null");
            return;
        }
        Map<String, String> map = getCommonMap(context, openId, access_token, null, type);
        persionService().thirdAppLogin(map).enqueue(new OnCallback<ServerLoginResult>() {
            @Override
            public void onResponse(ServerLoginResult result) {
                UserManager.getInstance().getUser().setPhone(null);
                handleLoginOrBindResult(true, false, result, type);
            }

            @Override
            public void onFailure(ServerLoginResult result) {
                handleLoginOrBindResult(false, false, result, type);

            }
        });
    }

    private void thirdAppServerBind(Context context, final int type, String openId,
            String access_token) {
        if (context == null || openId == null || access_token == null) {
            AccountEventController.onActionLogin(LoginEvent.EVENT_TYPE_LOGIN_FAILED, type, "null");
            return;
        }
        Map<String, String> map = getCommonMap(context, openId, access_token,
                UserManager.getInstance().getToken(), type);
        persionService().thirdAppBind(map).enqueue(new OnCallback<ServerLoginResult>() {
            @Override
            public void onResponse(ServerLoginResult result) {
                handleLoginOrBindResult(true, true, result, type);
            }

            @Override
            public void onFailure(ServerLoginResult result) {
                handleLoginOrBindResult(false, false, result, type);

            }
        });

    }

    private void handleLoginOrBindResult(boolean success, boolean bind, ServerLoginResult result,
            int loginType) {
        if (success && result != null && result.getCode() == CODE_SUCCESS) {
            if (!bind) {
                UserManager.getInstance().setLoginType(loginType);
            }
            if (!bind && loginType == SocialConstants.LOGIN.TYPE_PHONE_NUM_VERIFICATION_CODE) {
                UserManager.getInstance().setLoginToken(result.getSign());
            }
            updateUserByServerLoginResult(result);
            AccountEventController.onActionLogin(LoginEvent.EVENT_TYPE_LOGIN_SUCCESS, loginType,
                    result.getErrmsg());

        } else {
            AccountEventController.onActionLogin(LoginEvent.EVENT_TYPE_LOGIN_FAILED, loginType,
                    result == null ? null : result.getErrmsg());

        }
    }

    @Override
    public void register() {

    }

    @Override
    public void completeUserInfo(Map<String, String> map) {
        appendProfileInfo(map, true);
        persionService().completeUserInfo(map).enqueue(new OnCallback<ServerLoginResult>() {
            @Override
            public void onResponse(ServerLoginResult result) {
                if (result != null && result.getCode() == CODE_SUCCESS) {
                    updateUserByServerLoginResult(result);
                    AccountEventController.onActionUpload(UploadEvent.EVENT_TYPE_UPLOAD_SUCCESS,
                            UploadEvent.UPLOAD_TYPE_INFO, null);
                } else {
                    AccountEventController.onActionUpload(UploadEvent.EVENT_TYPE_UPLOAD_FAILED,
                            UploadEvent.UPLOAD_TYPE_INFO,
                            result == null ? null : result.getErrmsg());
                }
            }

            @Override
            public void onFailure(ServerLoginResult result) {
                AccountEventController.onActionUpload(UploadEvent.EVENT_TYPE_UPLOAD_FAILED,
                        UploadEvent.UPLOAD_TYPE_INFO, result == null ? null : result.getErrmsg());
            }
        });
    }

    @Override
    public void modifyUserInfo(Map<String, String> map) {
        appendProfileInfo(map, false);
        persionService().modifyUserInfo(map).enqueue(new OnCallback<ServerLoginResult>() {
            @Override
            public void onResponse(ServerLoginResult result) {
                if (result != null && result.getCode() == CODE_SUCCESS) {
                    updateUserByServerLoginResult(result);
                    AccountEventController.onActionUpload(UploadEvent.EVENT_TYPE_UPLOAD_SUCCESS,
                            UploadEvent.UPLOAD_TYPE_INFO, null);
                } else {
                    AccountEventController.onActionUpload(UploadEvent.EVENT_TYPE_UPLOAD_FAILED,
                            UploadEvent.UPLOAD_TYPE_INFO,
                            result == null ? null : result.getErrmsg());
                }
            }

            @Override
            public void onFailure(ServerLoginResult result) {
                AccountEventController.onActionUpload(UploadEvent.EVENT_TYPE_UPLOAD_FAILED,
                        UploadEvent.UPLOAD_TYPE_INFO, result == null ? null : result.getErrmsg());
            }
        });
    }

    @Override
    public void uploadAvatar(String photoBase64String, final Map<String, String> oriMap) {
        if (TextUtils.isEmpty(photoBase64String)) {
            AccountEventController.onActionUpload(UploadEvent.EVENT_TYPE_UPLOAD_FAILED,
                    UploadEvent.UPLOAD_TYPE_AVATAR, "");
            return;
        }
        final Map<String, String> map = new ArrayMap<>();
        map.put(AccountKey.KEY_FILE, photoBase64String);
        persionService().uploadAvatar(map).enqueue(new OnCallback<AvatarResult>() {
            @Override
            public void onResponse(AvatarResult result) {
                if (result != null && !TextUtils.isEmpty(result.getImageUrl())) {
                    // 需要再次上传到登录接口..
                    String imageUrl = result.getImageUrl();
                    if (UserManager.getInstance().isLogin()) {
                        android.util.ArrayMap<String, String> map = new android.util.ArrayMap<>();
                        map.put(AccountKey.KEY_HEAD_IMG_URL, imageUrl);
                        modifyUserInfo(map);
                    } else {
                        if (oriMap != null) {
                            oriMap.put(AccountKey.KEY_HEAD_IMG_URL, imageUrl);
                            completeUserInfo(oriMap);
                        }
                    }

                } else {
                    AccountEventController.onActionUpload(UploadEvent.EVENT_TYPE_UPLOAD_FAILED,
                            UploadEvent.UPLOAD_TYPE_AVATAR, null);
                }
            }

            @Override
            public void onFailure(AvatarResult result) {
                AccountEventController.onActionUpload(UploadEvent.EVENT_TYPE_UPLOAD_FAILED,
                        UploadEvent.UPLOAD_TYPE_AVATAR, result == null ? null : result);
            }
        });

    }

    /**
     * 从服务端获取最新用户信息
     */
    public void pullUser() {
        android.util.ArrayMap<String, String> map = new android.util.ArrayMap<>();
        map.put(AccountConstants.AccountKey.KEY_TOKEN, UserManager.getInstance().getToken());
        persionService().pullUserInfo(map).enqueue(new OnCallback<ServerLoginResult>() {
            @Override
            public void onResponse(ServerLoginResult result) {
                if (result != null && result.getCode() == CODE_SUCCESS) {
                    User user = result.getUser();
                    if (user != null && (user.getUserid() == UserManager.getInstance().getUserId()
                            || !UserManager.getInstance().isLogin())) {
                        UserManager.getInstance().setUser(user, true);
                        AccountEventController.onUserChangeEvent();
                    }
                }
            }

            @Override
            public void onFailure(ServerLoginResult result) {

            }
        });
    }

    @Override
    public void logout() {
        String token = UserManager.getInstance().getToken();
        Map<String, String> map = getCommonMap(XgameApplication.getApplication(), null, null, token,
                TYPE_DEFAULT);
        persionService().logout("Bearer" + " " + token, map).enqueue(new OnCallback<ServerLoginResult>() {
            @Override
            public void onResponse(ServerLoginResult result) {
                if (result != null && result.getCode() == CODE_SUCCESS) {
                    LogUtil.d(TAG, "logout success");
//                    AccountEventController
//                            .onActionLogOff(AccountEventController.LogoutEvent.EVENT_TYPE_NONE);
                }
            }

            @Override
            public void onFailure(ServerLoginResult result) {

            }
        });
    }

    @Override
    public void refreshToken() {
        android.util.ArrayMap<String, String> map = new android.util.ArrayMap<>();
        map.put(AccountConstants.AccountKey.KEY_TOKEN, UserManager.getInstance().getToken());
        persionService().refreshToken(map).enqueue(new OnCallback<ServerLoginResult>() {
            @Override
            public void onResponse(ServerLoginResult result) {
                if (result != null && result.getCode() == CODE_SUCCESS) {
                    updateUserByServerLoginResult(result);
                    LogUtil.d(TAG, "refreshToekn success");
//                    AccountEventController
//                            .onActionLogOff(AccountEventController.LogoutEvent.EVENT_TYPE_NONE);
                } else {
                    LogUtil.d(TAG, "refreshToekn failed");
                }
            }

            @Override
            public void onFailure(ServerLoginResult result) {
                LogUtil.d(TAG, "refreshToekn failed");
            }
        });
    }

    private void updateUserByServerLoginResult(ServerLoginResult result) {
        if (result != null) {
            LogUtil.d(TAG, "server result: " + result);
            if (!TextUtils.isEmpty(result.getToken())) {
                UserManager.getInstance().setToken(result.getToken());
            }
            if (!TextUtils.isEmpty(result.getAdServiceToken())) {
                UserManager.getInstance().setAdToken(result.getAdServiceToken());
            }
            if (!TextUtils.isEmpty(result.getBailuToken())) {
                UserManager.getInstance().setBailuToken(result.getBailuToken());
            }
            User user = result.getUser();
            if (user != null && user.getUserid() != 0
                    && (user.getUserid() == UserManager.getInstance().getUserId()
                            || !UserManager.getInstance().isLogin())) {
                UserManager.getInstance().setUser(user, true);
            }
        }
    }

    private Map<String, String> getCommonMap(Context context, String openId, String access_token,
            String token, int type) {
        int serverType = -1;
        switch (type) {
            case SocialConstants.LOGIN.TYPE_QQ:
                serverType = TYPE_QQ;
                break;
            case SocialConstants.LOGIN.TYPE_WECHAT:
                serverType = TYPE_WX;
                break;
            case SocialConstants.LOGIN.TYPE_PHONE_NUM_VERIFICATION_CODE:
                serverType = TYPE_PHONE;
                break;
        }
        Map<String, String> map = new ArrayMap<>();
        map.put(AccountKey.KEY_IMEI, DeviceUtil.getImeiMd5(context));
        map.put(AccountKey.KEY_ANDROID_ID, DeviceUtil.getAndroidID(context));
        map.put(AccountKey.KEY_CLIENT_INFO, MarioSdk.getClientInfo());
        if (!TextUtils.isEmpty(access_token)) {
            map.put(AccountKey.KEY_ACCESS_TOKEN, access_token);
        }
        if (!TextUtils.isEmpty(openId)) {
            map.put(AccountKey.KEY_OPEN_ID, openId);
        }
        if (!TextUtils.isEmpty(token)) {
            map.put(AccountKey.KEY_TOKEN, token);
        }
        map.put(AccountKey.KEY_TYPE, serverType + "");
        return map;
    }

    private void appendProfileInfo(Map map, boolean loginStep) {
        map.put(AccountKey.KEY_CLIENT_INFO, MarioSdk.getClientInfo());
        String loginToken = UserManager.getInstance().getLoginToken();
        String key = null;
        int loginType = UserManager.getInstance().getLoginType();
        switch (loginType) {
            case SocialConstants.LOGIN.TYPE_QQ:
                map.put(AccountKey.KEY_TYPE, TYPE_QQ);
                map.put(AccountKey.KEY_OPEN_ID, UserManager.getInstance().getUser().getQqOpenid());
                map.put(AccountKey.KEY_THIRD_NAME,
                        UserManager.getInstance().getUser().getQqNickname());
                key = AccountKey.KEY_ACCESS_TOKEN;
                break;
            case SocialConstants.LOGIN.TYPE_WECHAT:
                map.put(AccountKey.KEY_TYPE, TYPE_WX);
                map.put(AccountKey.KEY_OPEN_ID, UserManager.getInstance().getUser().getWxOpenid());
                map.put(AccountKey.KEY_THIRD_NAME,
                        UserManager.getInstance().getUser().getWxNickname());
                key = AccountKey.KEY_ACCESS_TOKEN;
                break;
            case SocialConstants.LOGIN.TYPE_PHONE_NUM_VERIFICATION_CODE:
                map.put(AccountKey.KEY_TYPE, TYPE_PHONE);
                map.put(AccountKey.KEY_OPEN_ID, UserManager.getInstance().getUser().getPhone());
                key = AccountKey.KEY_TOKEN;
                break;
            default:

                break;
        }
        // 登录时需要传对应的token,第三方和手机参数不一致...
        if (loginStep && loginToken != null && key != null) {
            map.put(key, loginToken);
        }
        if (!loginStep) {
            map.put(AccountKey.KEY_TOKEN, UserManager.getInstance().getToken());
        }

    }

    private Map<String, String> removeNullValue(Map<String, String> map) {
        Set set = map.keySet();
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext()) {
            String value = iterator.next();
            if (map.get(value) == null) {
                iterator.remove();
            }
        }
        return map;
    }
}
