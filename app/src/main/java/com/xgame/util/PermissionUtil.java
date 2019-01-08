
package com.xgame.util;

import java.util.Iterator;
import java.util.List;

import com.xgame.R;
import com.xgame.util.dialog.NormalAlertDialog;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;
import com.yanzhenjie.permission.SettingService;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.ArraySet;
import android.view.View;

/**
 * Created by wuyanzhi on 2018/1/28.
 */

public class PermissionUtil {

    private static final int REQUEST_CODE_PERMISSION_MUST = 1000;
    private static final String[] REQUEST_MUST_PERMISSIONS = new String[] {
            Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private static Boolean sShowSettingDialog;

    /**
     * 申请必须权限，onCreate调用
     * @param activity
     * @param checkRationale
     */
    public static void requestNecessaryPermission(final Activity activity, boolean checkRationale) {
        // 申请权限。
        sShowSettingDialog = null;
        AndPermission.with(activity).requestCode(REQUEST_CODE_PERMISSION_MUST)
                .permission(REQUEST_MUST_PERMISSIONS).callback(new PermissionListener() {
                    @Override
                    public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                        switch (requestCode) {
                            case REQUEST_CODE_PERMISSION_MUST: {
                                break;
                            }
                        }
                        sShowSettingDialog = false;
                    }

                    @Override
                    public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                        if (!AndPermission.hasPermission(activity, deniedPermissions)) {
                            // 用户否勾选了不再提示并且拒绝了权限，那么提示用户到设置中授权。
                            if (AndPermission.hasAlwaysDeniedPermission(activity,
                                    deniedPermissions)) {
                                sShowSettingDialog = true;
                                showSettingDialog(activity, deniedPermissions);
                            } else {
                                // 继续请求
                                requestNecessaryPermission(activity, true);
                            }
                        }

                    }
                }).rationale(new RationaleListener() {
                    @Override
                    public void showRequestPermissionRationale(int requestCode,
                            final Rationale rationale) {
                        List<String> denyPermissions = AndPermission.getDeniedPermissions(activity,
                                REQUEST_MUST_PERMISSIONS);
                        new NormalAlertDialog.Builder(activity)
                                .setMessage(convertPermissionToString(activity, denyPermissions)).setCancelable(false)
                                .setPositiveButton(activity.getString(android.R.string.ok),
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                rationale.resume();
                                            }
                                        })
                                .show();

                    }
                }).checkRationale(checkRationale).start();

    }

    /**
     * 检查必须权限是否授予，onResume调用
     * @param activity
     */
    public static void checkPermissionAgain(Activity activity) {
        List<String> denyPermissions = AndPermission.getDeniedPermissions(activity,
                REQUEST_MUST_PERMISSIONS);
        if (denyPermissions.size() > 0
                && AndPermission.hasAlwaysDeniedPermission(activity, denyPermissions)) {
            showSettingDialog(activity, denyPermissions);
        }
    }

    private static void showSettingDialog(Activity activity, List<String> denyPermissions) {
        if (sShowSettingDialog != null && sShowSettingDialog) {
            final SettingService settingHandle = AndPermission.defineSettingDialog(activity,
                    REQUEST_CODE_PERMISSION_MUST);
            new NormalAlertDialog.Builder(activity)
                    .setMessage(convertPermissionToString(activity, denyPermissions)).setCancelable(false)
                    .setPositiveButton(activity.getString(R.string.go_setting), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            settingHandle.execute();
                            sShowSettingDialog = true;
                        }
                    }).show();
            sShowSettingDialog = false;
        }
    }

    private static String convertPermissionToString(Context context, List<String> denyPermissions) {
        if (denyPermissions != null && denyPermissions.size() > 0) {
            ArraySet<String> toastSet = new ArraySet<>();
            for(String deny : denyPermissions) {
                if (Manifest.permission.READ_PHONE_STATE.equalsIgnoreCase(deny)) {
                    toastSet.add(context.getString(R.string.grant_permission_read_phone));
                } else if (Manifest.permission.READ_EXTERNAL_STORAGE.equalsIgnoreCase(deny) || Manifest.permission.WRITE_EXTERNAL_STORAGE.equalsIgnoreCase(deny)) {
                    toastSet.add(context.getString(R.string.grant_permission_write_storage));
                }
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(context.getString(R.string.grant_permission_start));
            Iterator iterator = toastSet.iterator();
            while (iterator.hasNext()) {
                stringBuilder.append(iterator.next());
                if (iterator.hasNext()) {
                    stringBuilder.append("、");
                }
            }
            stringBuilder.append(context.getString(R.string.grant_permission_end));
            return stringBuilder.toString();
        }

        return "";
    }

    public static void requestPermission(final Activity activity, String... permissions) {
        AndPermission.with(activity).permission(permissions).start();
    }
}
