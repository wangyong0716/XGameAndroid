package com.xgame.personal.api;

import com.xgame.base.api.Pack;
import com.xgame.common.api.FutureCall;
import com.xgame.personal.model.BillList;
import com.xgame.personal.model.PersonalMenu;
import com.xgame.personal.model.UserProfile;

import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by dingning1
 * on 18-1-29.
 */


public interface PersonalInfoService {

    @GET("user/profile")
    FutureCall<Pack<UserProfile>> getUserProfile();

    @GET("home/my")
    FutureCall<Pack<PersonalMenu>> getMyData();

    @GET("user/cash/detail")
    FutureCall<Pack<BillList>> getCashBill();

    @GET("user/coin/detail")
    FutureCall<Pack<BillList>> getCoinBill();

    @POST("user/feedback")
    FutureCall<Pack<Object>> postFeedback(@Query("feedbackContent") String content, @Query("contact") String contact);
}
