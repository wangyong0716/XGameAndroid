
package com.xgame.app;

import com.meituan.android.walle.WalleChannelReader;
import com.xgame.common.util.LogUtil;

public class AppConfig {
    private static final String TAG = AppConfig.class.getSimpleName();
    private static String sChannel = null;

    public static String getChannel() {
        if (sChannel == null) {
            sChannel = WalleChannelReader.getChannel(XgameApplication.getApplication());
            LogUtil.d(TAG, "channel: " + sChannel);
        }
        return sChannel;
    }
}
