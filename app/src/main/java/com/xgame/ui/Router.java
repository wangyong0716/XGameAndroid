package com.xgame.ui;

import android.app.Activity;
import android.content.Context;
import android.webkit.URLUtil;

import com.miui.zeus.mario.sdk.MarioSdk;
import com.xgame.R;
import com.xgame.account.UserManager;
import com.xgame.app.XgameApplication;
import com.xgame.base.ClientSettingManager;
import com.xgame.base.model.ClientSettings;
import com.xgame.common.util.ToastUtil;
import com.xgame.ui.activity.CommonWebViewActivity;
import com.xgame.ui.activity.personal.PersonalInfoActivity;
import com.xgame.util.Analytics;

import static android.text.TextUtils.isEmpty;
import static com.xgame.app.XgameApplication.getTopActivity;
import static com.xgame.common.util.LaunchUtils.startActivity;
import static com.xgame.util.UrlUtils.getRequestUrl;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 * <p>
 * Created by jackwang
 * on 18-2-2.
 */


public final class Router {

    public static final String BASE_URL = "xgame://pk.baiwan.com/";

    public static final String DEFAULT_CASH_URL
            = "https://api.chufengnet.com/v1/enterCashMall?"
            + "dbredirect=https%3A%2F%2Fgoods.m.duiba.com.cn%2Fmobile%2Fdetail%3FitemId%3D53";

    public static final String DEFAULT_CASH_RECORD_URL = "https://api.chufengnet.com/v1/enterCashMall?"
            + "dbredirct=http%3a%2f%2ftrade.m.duiba.com.cn%2fcrecord%2frecord";

    public static final String GOLD_MALL_URL = "https://api.chufengnet.com/v1/enterCoinMall";

    private Router() {
    }

    private static Context getContext() {
        Activity a = getTopActivity();
        return a != null ? a : XgameApplication.getApplication();
    }

    public static void toHome() {
        startActivity(getContext(), BASE_URL + "home");
    }

    public static void toPersonal() {
        startActivity(getContext(), PersonalInfoActivity.class);
    }

    public static void toStrangerList() {
        startActivity(getContext(), BASE_URL + "invite/stranger");
    }

    public static void toFriends() {
        startActivity(getContext(), BASE_URL + "invite");
    }

    /**
     * 任务中心
     */
    public static void toTaskCenter() {
        Context c = getContext();
        String adToken = UserManager.getInstance().getAdToken();
        if (c instanceof Activity && !isEmpty(adToken)) {
            MarioSdk.startTaskCenter((Activity) c, adToken);
            //任务列表页面PV
            Analytics.trackPageShowEvent(Analytics.Constans.URL_TITLE_TASK_PAGE, Analytics.Constans.VISIT_TYPE_READY, null);
        }
    }

    /**
     * 输入邀请码
     */
    public static void toInputCode() {
        Context c = getContext();
        String adToken = UserManager.getInstance().getAdToken();
        if (c instanceof Activity && !isEmpty(adToken)) {
            MarioSdk.inputCode((Activity) c, adToken);
            //填写邀请码页面
            Analytics.trackPageShowEvent(Analytics.Constans.URL_TITLE_INVITE_CODE_PAGE, Analytics.Constans.VISIT_TYPE_READY, null);
        }
    }

    /**
     * 打开收徒界面
     */
    public static void toInviteFollower() {
        Context c = getContext();
        String adToken = UserManager.getInstance().getAdToken();
        if (c instanceof Activity && !isEmpty(adToken)) {
            MarioSdk.inviteFollower((Activity) c, adToken);
        }
    }

    /**
     * 提现页
     */
    public static void toWithdrawCash() {
        ClientSettingManager.loadSettings(new ClientSettingManager.OnLoadResult() {
            @Override
            public void onLoaded(ClientSettings settings) {
                String cash;
                if (settings != null && !isEmpty(cash = settings.cash) && isValidUri(cash)) {
                    startMarioWebView(cash);
                    return;
                }
                startMarioWebView(DEFAULT_CASH_URL);
            }

            @Override
            public void onFailure() {
                ToastUtil.showTip(getContext(), R.string.please_relogin);
            }
        });
    }

    /**
     * 提现详情
     */
    public static void toWithdrawCashRecord() {
        ClientSettingManager.loadSettings(new ClientSettingManager.OnLoadResult() {
            @Override
            public void onLoaded(ClientSettings settings) {
                String cashRecord;
                if (settings != null && !isEmpty(cashRecord = settings.cashRecord) && isValidUri(cashRecord)) {
                    startMarioWebView(cashRecord);
                    return;
                }
                startMarioWebView(DEFAULT_CASH_RECORD_URL);
            }

            @Override
            public void onFailure() {
                ToastUtil.showTip(getContext(), R.string.please_relogin);
            }
        });
    }

    public static boolean isValidUri(String uri) {
        return uri != null && (URLUtil.isValidUrl(uri) || uri.startsWith("xgame://"));
    }

    /**
     * 金币商城
     */
    public static void toGoldCoinMall() {
        ClientSettingManager.loadSettings(new ClientSettingManager.OnLoadResult() {
            @Override
            public void onLoaded(ClientSettings settings) {
                String mall;
                if (settings != null && !isEmpty(mall = settings.mall) && isValidUri(mall)) {
                    startMarioWebView(mall);
                    return;
                }
                startMarioWebView(GOLD_MALL_URL);
            }

            @Override
            public void onFailure() {
                ToastUtil.showTip(getContext(), R.string.please_relogin);
            }
        });
    }

    private static void startMarioWebView(String url) {
        Context c = getContext();
        if (c instanceof Activity) {
            CommonWebViewActivity.startWeb(c, getRequestUrl(url));
          //  MarioSdk.startWebView((Activity) c, getRequestUrl(url));
        }
    }

}
