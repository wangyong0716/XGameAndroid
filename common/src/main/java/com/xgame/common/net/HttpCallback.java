package com.xgame.common.net;

import com.xgame.common.Constants;
import com.xgame.common.util.LogUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public abstract class HttpCallback<T extends Result> implements Callback<T>, SmartCallback<T> {
    private static final String TAG = HttpCallback.class.getSimpleName();

    public static final int CODE_SUCCESS = 0;
    public static final int CODE_FAILURE = -101;

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful()) {
            int code = response.body().getCode();
            LogUtil.d(TAG, "code: " + code);
            if (code == CODE_SUCCESS) {
                onResponse(response.body());
            } else if (response.code() == Constants.CODE.NET_ERROR_TOKEN_EXPIRED) {
//                CommonAction action = new CommonAction(Dispatcher.get());
//                action.noAuthorization();
              ///   UserManager.getInstance().clearUser();
                onFailure(Constants.CODE.NET_ERROR_TOKEN_EXPIRED, "登录状态异常");
            } else {
                onFailure(code, response.body().getMsg());
            }
        } else if (response.code() == Constants.CODE.NET_ERROR_UNAUTHORIZED) {
//            CommonAction action = new CommonAction(Dispatcher.get());
//            action.noAuthorization();

            ///UserManager.getInstance().clearUser();
            onFailure(Constants.CODE.NET_ERROR_UNAUTHORIZED, "登录状态异常");
        } else {
            LogUtil.i(TAG, "onResponse code: " + response.code() + ", body: " + response.body() + ", msg: " + response.message());
            onFailure(CODE_FAILURE, "网络异常");
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        onFailure(CODE_FAILURE, "网络异常");
        LogUtil.e(TAG, null, t);
    }

    @Override
    public boolean validResponseForCache(T t) {
        return true;
    }

    public abstract void onResponse(T t);

    public void onFailure(int code, String msg) {
        if (code == CODE_FAILURE) {
            return;
        }
    }
}
