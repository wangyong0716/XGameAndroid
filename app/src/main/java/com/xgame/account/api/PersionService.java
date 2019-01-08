
package com.xgame.account.api;

import java.util.Map;

import com.xgame.common.api.FutureCall;

import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by wuyanzhi on 2018/1/25.
 */

public interface PersionService {
    @FormUrlEncoded
    @POST("login/phone")
    FutureCall<ServerLoginResult> phoneLogin(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("login/bind/phone")
    FutureCall<ServerLoginResult> phoneBind(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("login/verification/push")
    FutureCall<ServerLoginResult> getVerificationCode(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("login/token/refresh/")
    FutureCall<ServerLoginResult> refreshPhoneToken(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("login/token/extend/")
    FutureCall<ServerLoginResult> extendToken(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("login/logout")
    FutureCall<ServerLoginResult> logout(@Header("Authorization") String auth, @FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("login/login")
    FutureCall<ServerLoginResult> thirdAppLogin(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("login/bind")
    FutureCall<ServerLoginResult> thirdAppBind(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("login/refresh")
    FutureCall<ServerLoginResult> refreshToken(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("login/add_user_info")
    FutureCall<ServerLoginResult> completeUserInfo(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("login/modify_user_info")
    FutureCall<ServerLoginResult> modifyUserInfo(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("user/upload_img")
    FutureCall<AvatarResult> uploadAvatar(@FieldMap Map<String, String> map);

    @FormUrlEncoded
    @POST("login/get_user_info")
    FutureCall<ServerLoginResult> pullUserInfo(@FieldMap Map<String, String> map);
}
