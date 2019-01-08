package com.xgame.home.api;

import java.util.List;

import com.xgame.common.api.FutureCall;
import com.xgame.home.model.ArenaTabPage;
import com.xgame.home.model.BattleTabPage;
import com.xgame.home.model.MessageSession;
import com.xgame.home.model.TaskStatus;

import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-25.
 */


public interface HomeService {


    @GET("home/battle")
    FutureCall<BattleTabPage> loadBattleTab(@Query("loadTag") long loadTag,
            @Query("refreshTag") long refreshTag);

    @GET("home/arena")
    FutureCall<ArenaTabPage> loadArenaTab(@Query("loadTag") long loadTag,
            @Query("refreshTag") long refreshTag);

    @GET("record/battle")
    FutureCall<List<MessageSession>> loadRecordHistory(@Query("startTime") long startTime,
            @Query("endTime") long endTime, @Query("isFriend") boolean isFriend);

    @GET("home/summary")
    FutureCall<TaskStatus> loadTaskStatus();

}
