
package com.xgame.util;

import com.xgame.R;
import com.xgame.app.XgameApplication;
import com.xgame.common.util.AppUtils;
import com.xgame.common.util.ToastUtil;

public class ThirdAppUtil {

    public static final boolean isQQInstalled(boolean showUnInstallToast) {
        boolean installed = AppUtils.isAppInstalled(XgameApplication.getApplication(),
                "com.tencent.mobileqq");
        if (!installed && showUnInstallToast) {
            ToastUtil.showToast(XgameApplication.getApplication(), R.string.app_not_installed,false);
        }
        return installed;
    }

    public static final boolean isWXInstalled(boolean showUnInstallToast) {
        boolean installed = AppUtils.isAppInstalled(XgameApplication.getApplication(),
                "com.tencent.mm");
        if (!installed && showUnInstallToast) {
            ToastUtil.showToast(XgameApplication.getApplication(), R.string.app_not_installed, false);
        }
        return installed;
    }

    public static final boolean isWeiboInstalled(boolean showUnInstallToast) {
        boolean installed = AppUtils.isAppInstalled(XgameApplication.getApplication(),
                "com.sina.weibo");
        if (!installed && showUnInstallToast) {
            ToastUtil.showToast(XgameApplication.getApplication(), R.string.app_not_installed, false);
        }
        return installed;
    }
}
