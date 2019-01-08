package com.xgame.statistic;

import android.content.Intent;

import com.xgame.base.ServiceFactory;
import com.xgame.common.api.OnCallback;
import com.xgame.common.util.LogUtil;
import com.xgame.social.share.SharePlatform;
import com.xgame.statistic.api.StatisticResult;

/**
 * Created by Albert
 * on 18-2-5.
 */

public class ShareStatHelper {

    private static final String TAG = "ShareStatManager";

    private boolean mIsNeedStat;

    private TaskShareParams mParams;

    public void setup(Intent intent) {
        mIsNeedStat = checkShareStat(intent);
    }

    private boolean checkShareStat(Intent intent) {
        if (intent == null) {
            return false;
        }
        mParams = new TaskShareParams();
        return true;
    }

    public boolean isNeedStat() {
        return mIsNeedStat;
    }

    public void statTaskShare(@SharePlatform.Platform int platform) {
        if (mParams == null) {
            LogUtil.d(TAG, "task share stat: params empty");
            return;
        }
        if (!isNeedStat()) {
            LogUtil.d(TAG, "task share stat: no need");
            return;
        }
        mParams.shareChannel = getPlatformStr(platform);
        ServiceFactory.statisticService().taskShareStat(mParams.gameId, mParams.shareChannel).enqueue(new OnCallback<StatisticResult>() {
            @Override
            public void onResponse(StatisticResult result) {
                LogUtil.d(TAG, "task share stat: result = " + result);
            }

            @Override
            public void onFailure(StatisticResult result) {
                LogUtil.d(TAG, "task share stat failed: " + result);
            }
        });
    }

    public String getPlatformStr(@SharePlatform.Platform int platform) {
        switch (platform) {
            case SharePlatform.QQ:
                return "QQ";
            case SharePlatform.WX_TIMELINE:
                return "WX_TIMELINE";
            case SharePlatform.WX:
                return "WX";
            case SharePlatform.DEFAULT:
            case SharePlatform.QZONE:
            case SharePlatform.WEIBO:
            default:
                return "";
        }
    }

    public static class TaskShareParams {
        public int gameId;
        public String shareChannel;
    }
}
