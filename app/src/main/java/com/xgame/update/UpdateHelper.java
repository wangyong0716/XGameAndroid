package com.xgame.update;

import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import com.xgame.R;
import com.xgame.common.application.ApplicationStatus;
import com.xgame.common.util.FileUtils;
import com.xgame.common.util.SharePrefUtils;
import com.xgame.common.util.TimeUtil;
import com.xgame.util.dialog.NormalAlertDialog;
import com.xiaomi.market.sdk.UpdateResponse;
import com.xiaomi.market.sdk.UpdateStatus;
import com.xiaomi.market.sdk.XiaomiUpdateAgent;
import com.xiaomi.market.sdk.XiaomiUpdateListener;

import java.lang.ref.WeakReference;
import java.util.Locale;

public class UpdateHelper {

    private static final String PREF_LAST_UPDATE_TIME = "last_update_time";
    private static final boolean IS_DEBUG = false;

    static {
        XiaomiUpdateAgent.setUpdateAutoPopup(false);
    }


    public static void update(Activity activity, boolean force) {
        if (!force) {
            long lastUpdaetTime = SharePrefUtils.getLong(activity, PREF_LAST_UPDATE_TIME, 0);
            if (TimeUtil.isToday(lastUpdaetTime)) {
                return;
            }
        }
        XiaomiUpdateAgent.setUpdateListener(new XiaomiUpdate(activity, force));
        XiaomiUpdateAgent.update(activity, IS_DEBUG);
    }

    private static class XiaomiUpdate implements XiaomiUpdateListener {
        private WeakReference<Activity> mWeakReference;
        private Boolean mIsForce;

        public XiaomiUpdate(Activity activity, Boolean isForce) {
            mWeakReference = new WeakReference<>(activity);
            mIsForce = isForce;
        }

        @Override
        public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
            switch (updateStatus) {
                case UpdateStatus.STATUS_UPDATE:
                    SharePrefUtils.putLong(ApplicationStatus.getApplicationContext(), PREF_LAST_UPDATE_TIME, System.currentTimeMillis());
                    if (mWeakReference.get() == null) {
                        return;
                    }
                    Activity activity = mWeakReference.get();
                    String msg = String.format(Locale.getDefault(), "发现新版本，推荐您立即升级到最新版本%s版，大小%s", updateInfo.versionName, FileUtils.byte2FitMemorySize(updateInfo.apkSize));
                    new NormalAlertDialog.Builder(activity)
                            .setMessage(msg)
                            .setPositiveButton(R.string.sure_text, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    XiaomiUpdateAgent.arrange();
                                }
                            })
                            .setNegativeButton(R.string.cancel_text, null)
                            .show();
                    break;
                case UpdateStatus.STATUS_NO_UPDATE:
                    SharePrefUtils.putLong(ApplicationStatus.getApplicationContext(), PREF_LAST_UPDATE_TIME, System.currentTimeMillis());
                    if (mIsForce) {
                        Toast.makeText(ApplicationStatus.getApplicationContext(), R.string.no_update, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case UpdateStatus.STATUS_NO_WIFI:
                case UpdateStatus.STATUS_NO_NET:
                case UpdateStatus.STATUS_FAILED:
                    if (mIsForce) {
                        Toast.makeText(ApplicationStatus.getApplicationContext(), R.string.net_error_text, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case UpdateStatus.STATUS_LOCAL_APP_FAILED:
                    if (mIsForce) {
                        Toast.makeText(ApplicationStatus.getApplicationContext(), R.string.local_check_error, Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
