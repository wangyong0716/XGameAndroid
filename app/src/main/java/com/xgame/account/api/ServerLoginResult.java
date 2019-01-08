
package com.xgame.account.api;

import com.google.gson.annotations.SerializedName;
import com.xgame.account.model.User;
import com.xgame.base.api.DataProtocol;
import com.xgame.common.api.IProtocol;
import com.xgame.common.net.Result;
import com.xgame.common.util.LogUtil;

public class ServerLoginResult implements IProtocol {
    @SerializedName("code")
    private int code = -1;
    @SerializedName("token")
    private String token;
    @SerializedName("isRegister")
    private boolean isRegister;
    @SerializedName("refreshtoken")
    private String refreshtoken;
    @SerializedName("errmsg")
    private String errmsg;
    @SerializedName("msg")
    private String msg;
    @SerializedName("userInfo")
    private User user;
    @SerializedName("sign")
    private String sign;
    @SerializedName("adServiceToken")
    private String adServiceToken;
    @SerializedName("bailuToken")
    private String bailuToken;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean getRegister() {
        return isRegister;
    }

    public void setRegister(boolean register) {
        isRegister = register;
    }

    public String getRefreshtoken() {
        return refreshtoken;
    }

    public void setRefreshtoken(String refreshtoken) {
        this.refreshtoken = refreshtoken;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getAdServiceToken() {
        return adServiceToken;
    }

    public void setAdServiceToken(String adServiceToken) {
        this.adServiceToken = adServiceToken;
    }

    public String getBailuToken() {
        return bailuToken;
    }

    public void setBailuToken(String bailuToken) {
        this.bailuToken = bailuToken;
    }

    @Override
    public String toString() {
        if (LogUtil.DEBUG) {
            StringBuilder builder = new StringBuilder();
            builder.append("code: ").append(code).append(", token: ").append(token)
                    .append(", adToken: ").append(adServiceToken).append(", bailuToken: ")
                    .append(bailuToken).append(", isRegister: ").append(isRegister)
                    .append(", errmsg: ").append(errmsg).append(", user: ")
                    .append(user == null ? null : user.toJSONObject());
            return builder.toString();
        }
        return super.toString();
    }
}
