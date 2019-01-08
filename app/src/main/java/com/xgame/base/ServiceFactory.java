package com.xgame.base;

import com.xgame.account.api.PersionService;
import com.xgame.base.api.ClientSettingsService;
import com.xgame.battle.api.BattleService;
import com.xgame.common.api.ApiServiceManager;
import com.xgame.home.api.HomeService;
import com.xgame.invite.api.InviteService;
import com.xgame.personal.api.PersonalInfoService;
import com.xgame.push.api.PushService;
import com.xgame.statistic.api.StatisticService;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 * <p>
 * Created by jackwang
 * on 18-1-29.
 */


public final class ServiceFactory {

    private ServiceFactory() {
    }

    public static HomeService homeService() {
        return ApiServiceManager.obtain(HomeService.class);
    }

    public static ClientSettingsService settingService() {
        return ApiServiceManager.obtain(ClientSettingsService.class);
    }

    public static BattleService battleService() {
        return ApiServiceManager.obtain(BattleService.class);
    }

    public static PersionService persionService() {
        return ApiServiceManager.obtain(PersionService.class);
    }

    public static InviteService inviteService() {
        return ApiServiceManager.obtain(InviteService.class);
    }

    public static PersonalInfoService personalInfoService() {
        return ApiServiceManager.obtain(PersonalInfoService.class);
    }

    public static PushService pushService() {
        return ApiServiceManager.obtain(PushService.class);
    }

    public static StatisticService statisticService() {
        return ApiServiceManager.obtain(StatisticService.class);
    }
}
