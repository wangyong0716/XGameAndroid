package com.xgame.base.api;

import com.xgame.base.model.ClientSettings;
import com.xgame.common.api.FutureCall;

import retrofit2.http.GET;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 * <p>
 * Created by jackwang
 * on 18-2-3.
 */

public interface ClientSettingsService {

    @GET("settings")
    FutureCall<ClientSettings> loadSettings();
}
