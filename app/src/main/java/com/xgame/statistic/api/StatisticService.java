package com.xgame.statistic.api;

import com.xgame.common.api.FutureCall;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface StatisticService {

    @FormUrlEncoded
    @POST("track/data")
    FutureCall<StatisticResult> track(@Field("data") String data);

    @GET("task/share")
    FutureCall<StatisticResult> taskShareStat(@Query("gameId") int gameId, @Query("sharechannel") String channel);
}
