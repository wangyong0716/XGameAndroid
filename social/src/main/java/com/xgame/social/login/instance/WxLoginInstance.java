package com.xgame.social.login.instance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.xgame.social.ShareLogger;
import com.xgame.social.SocialConstants;
import com.xgame.social.login.LoginListener;
import com.xgame.social.login.LoginPlatform;
import com.xgame.social.login.LoginResult;
import com.xgame.social.login.result.BaseToken;
import com.xgame.social.login.result.WxToken;
import com.xgame.social.login.result.WxUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by shaohui on 2016/12/1.
 */

public class WxLoginInstance extends LoginInstance {

    private static final String SCOPE_USER_INFO = SocialConstants.WX.SCOPE;
    private static final String STATE = SocialConstants.WX.STATE;
    private static final String SCOPE_BASE = "snsapi_base";

    private IWXAPI mIWXAPI;

    private LoginListener mLoginListener;

    private OkHttpClient mClient;

    private boolean fetchUserInfo;

    public WxLoginInstance(Activity activity, LoginListener listener, boolean fetchUserInfo) {
        super(activity, listener, fetchUserInfo);
        mLoginListener = listener;
        mIWXAPI = WXAPIFactory.createWXAPI(activity.getApplicationContext(), SocialConstants.WX.APP_ID);
        mIWXAPI.registerApp(SocialConstants.WX.APP_ID);
        mClient = new OkHttpClient();
        this.fetchUserInfo = fetchUserInfo;
    }

    @Override
    public void doLogin(Activity activity, LoginListener listener, boolean fetchUserInfo) {
        final SendAuth.Req req = new SendAuth.Req();
        req.scope = SCOPE_USER_INFO;
        req.state = STATE;
        mIWXAPI.sendReq(req);
    }

    private void getToken(final String code) {
        new AsyncTask<Object, Void, WxToken>() {
            @Override
            protected WxToken doInBackground(Object... objects) {
                Request request = new Request.Builder().url(buildTokenUrl(code)).build();
                try {
                    Response response = mClient.newCall(request).execute();
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    WxToken token = WxToken.parse(jsonObject);
                    return token;
                } catch (IOException | JSONException e) {
                    onFailed(null, mLoginListener, e);
                }
                return null;
            }


            @Override
            protected void onPostExecute(WxToken wxToken) {
                try {
                    if (wxToken == null) {
                        onFailed(null, mLoginListener, new NullPointerException("token null"));
                        return;
                    }
                    if (fetchUserInfo) {
                        mLoginListener.beforeFetchUserInfo(wxToken);
                        fetchUserInfo(wxToken);
                    } else {
                        mLoginListener.loginSuccess(new LoginResult(LoginPlatform.WX, wxToken));
                    }
                } catch (Throwable e) {
                    onFailed(null, mLoginListener, e);
                }
            }
        }.execute();
    }

    @Override
    public void fetchUserInfo(final BaseToken token) {
        new AsyncTask<Object, Void, WxUser>() {
            @Override
            protected WxUser doInBackground(Object... objects) {
                try {
                    Request request = new Request.Builder().url(buildUserInfoUrl(token)).build();
                    Response response = mClient.newCall(request).execute();
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    WxUser user = WxUser.parse(jsonObject);
                    return user;
                } catch (IOException | JSONException e) {
                    onFailed(null, mLoginListener, e);
                }
                return null;
            }


            @Override
            protected void onPostExecute(WxUser wxUser) {
                try {
                    mLoginListener.loginSuccess(
                            new LoginResult(LoginPlatform.WX, token, wxUser));
                } catch (Throwable e) {
                    onFailed(null, mLoginListener, e);
                }
            }
        }.execute();
    }

    @Override
    public void handleResult(int requestCode, int resultCode, Intent data) {
        mIWXAPI.handleIntent(data, new IWXAPIEventHandler() {
            @Override
            public void onReq(BaseReq baseReq) {
            }

            @Override
            public void onResp(BaseResp baseResp) {
                if (baseResp instanceof SendAuth.Resp && baseResp.getType() == 1) {
                    SendAuth.Resp resp = (SendAuth.Resp) baseResp;
                    switch (resp.errCode) {
                        case BaseResp.ErrCode.ERR_OK:
                            getToken(resp.code);
                            break;
                        case BaseResp.ErrCode.ERR_USER_CANCEL:
                            mLoginListener.loginCancel();
                            break;
                        case BaseResp.ErrCode.ERR_SENT_FAILED:
                            mLoginListener.loginFailure(new Exception(ShareLogger.INFO.WX_ERR_SENT_FAILED));
                            break;
                        case BaseResp.ErrCode.ERR_UNSUPPORT:
                            mLoginListener.loginFailure(new Exception(ShareLogger.INFO.WX_ERR_UNSUPPORT));
                            break;
                        case BaseResp.ErrCode.ERR_AUTH_DENIED:
                            mLoginListener.loginFailure(new Exception(ShareLogger.INFO.WX_ERR_AUTH_DENIED));
                            break;
                        default:
                            mLoginListener.loginFailure(new Exception(ShareLogger.INFO.WX_ERR_AUTH_ERROR));
                    }
                }
            }
        });
    }

    @Override
    public boolean isInstall(Context context) {
        return mIWXAPI.isWXAppInstalled();
    }

    @Override
    public void recycle() {
        if (mIWXAPI != null) {
            mIWXAPI.detach();
        }
    }

    private String buildTokenUrl(String code) {
        return SocialConstants.WX.BASE_URL_USER_INFO
                + "sns/oauth2/access_token?appid="
                + SocialConstants.WX.APP_ID
                + "&secret="
                + SocialConstants.WX.SECRET
                + "&code="
                + code
                + "&grant_type=" + SocialConstants.WX.GRANT_TYPE;
    }

    private String buildUserInfoUrl(BaseToken token) {
        if (token == null) {
            return null;
        }
        return SocialConstants.WX.BASE_URL_USER_INFO
                + "sns/userinfo?access_token="
                + token.getAccessToken()
                + "&openid="
                + token.getOpenid();
    }
}
