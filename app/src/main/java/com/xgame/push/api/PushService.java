package com.xgame.push.api;



import com.xgame.common.api.FutureCall;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface PushService {


    @FormUrlEncoded
    @POST("register/push")
    FutureCall<String> register(@Field("regid") String regid, @Field("userId") String userId);

    @FormUrlEncoded
    @POST("register/push")
    Call<String> unRegister(@Header("Authorization") String auth, @Field("regid") String regid, @Field("userId") String userId);
}
