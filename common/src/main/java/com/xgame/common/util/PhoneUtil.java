package com.xgame.common.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;

public final class PhoneUtil {

    private static Boolean sIsGetImeiMD5;
    private static String sImeiMD5;

    private PhoneUtil() {
    }

    /**
     * 判断设备是否是手机
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isPhone(Context context) {
        TelephonyManager tm =
                (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        return tm != null && tm.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
    }

    /**
     * 获取 IMEI 码
     * <p>需添加权限
     * {@code <uses-permission android:name="android.permission.READ_PHONE_STATE" />}</p>
     *
     * @return IMEI 码
     */
    @SuppressLint({"HardwareIds", "MissingPermission"})
    public static String getIMEI(Context context) {
        TelephonyManager tm =
                (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        return tm != null ? tm.getDeviceId() : null;
    }

    /**
     * 获取IMEI的MD5
     *
     * @param context
     * @return
     */
    @SuppressLint({"HardwareIds", "MissingPermission"})
    public static String getIMEIMD5(Context context) {
        if (sIsGetImeiMD5 == null || !sIsGetImeiMD5) {
            TelephonyManager tm =
                    (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            String imei = tm != null ? tm.getDeviceId() : null;
            sImeiMD5 = imei == null ? "" : MD5Util.getBytesMD5(imei.getBytes());
            sIsGetImeiMD5 = imei != null;
        }
        return sImeiMD5;
    }

    /**
     * 获取 IMSI 码
     * <p>需添加权限
     * {@code <uses-permission android:name="android.permission.READ_PHONE_STATE" />}</p>
     *
     * @return IMSI 码
     */
    @SuppressLint({"HardwareIds", "MissingPermission"})
    public static String getIMSI(Context context) {
        TelephonyManager tm =
                (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        return tm != null ? tm.getSubscriberId() : null;
    }

    /**
     * 获取移动终端类型
     *
     * @return 手机制式
     * <ul>
     * <li>{@link TelephonyManager#PHONE_TYPE_NONE } : 0 手机制式未知</li>
     * <li>{@link TelephonyManager#PHONE_TYPE_GSM  } : 1 手机制式为 GSM，移动和联通</li>
     * <li>{@link TelephonyManager#PHONE_TYPE_CDMA } : 2 手机制式为 CDMA，电信</li>
     * <li>{@link TelephonyManager#PHONE_TYPE_SIP  } : 3</li>
     * </ul>
     */
    public static int getPhoneType(Context context) {
        TelephonyManager tm =
                (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        return tm != null ? tm.getPhoneType() : -1;
    }

    /**
     * 判断 sim 卡是否准备好
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isSimCardReady(Context context) {
        TelephonyManager tm =
                (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        return tm != null && tm.getSimState() == TelephonyManager.SIM_STATE_READY;
    }

    /**
     * 获取 Sim 卡运营商名称
     * <p>中国移动、如中国联通、中国电信</p>
     *
     * @return sim 卡运营商名称
     */
    public static String getSimOperatorName(Context context) {
        TelephonyManager tm =
                (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        return tm != null ? tm.getSimOperatorName() : null;
    }

    /**
     * 获取 Sim 卡运营商名称
     * <p>中国移动、如中国联通、中国电信</p>
     *
     * @return 移动网络运营商名称
     */
    public static String getSimOperatorByMnc(Context context) {
        TelephonyManager tm =
                (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        String operator = tm != null ? tm.getSimOperator() : null;
        if (operator == null) return null;
        switch (operator) {
            case "46000":
            case "46002":
            case "46007":
                return "中国移动";
            case "46001":
                return "中国联通";
            case "46003":
                return "中国电信";
            default:
                return operator;
        }
    }

    /**
     * 获取手机状态信息
     * <p>需添加权限
     * {@code <uses-permission android:name="android.permission.READ_PHONE_STATE" />}</p>
     *
     * @return DeviceId(IMEI) = 99000311726612<br>
     * DeviceSoftwareVersion = 00<br>
     * Line1Number =<br>
     * NetworkCountryIso = cn<br>
     * NetworkOperator = 46003<br>
     * NetworkOperatorName = 中国电信<br>
     * NetworkType = 6<br>
     * PhoneType = 2<br>
     * SimCountryIso = cn<br>
     * SimOperator = 46003<br>
     * SimOperatorName = 中国电信<br>
     * SimSerialNumber = 89860315045710604022<br>
     * SimState = 5<br>
     * SubscriberId(IMSI) = 460030419724900<br>
     * VoiceMailNumber = *86<br>
     */
    @SuppressLint({"HardwareIds", "MissingPermission"})
    public static String getPhoneStatus(Context context) {
        TelephonyManager tm =
                (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (tm == null) return "";
        String str = "";
        str += "DeviceId(IMEI) = " + tm.getDeviceId() + "\n";
        str += "DeviceSoftwareVersion = " + tm.getDeviceSoftwareVersion() + "\n";
        str += "Line1Number = " + tm.getLine1Number() + "\n";
        str += "NetworkCountryIso = " + tm.getNetworkCountryIso() + "\n";
        str += "NetworkOperator = " + tm.getNetworkOperator() + "\n";
        str += "NetworkOperatorName = " + tm.getNetworkOperatorName() + "\n";
        str += "NetworkType = " + tm.getNetworkType() + "\n";
        str += "PhoneType = " + tm.getPhoneType() + "\n";
        str += "SimCountryIso = " + tm.getSimCountryIso() + "\n";
        str += "SimOperator = " + tm.getSimOperator() + "\n";
        str += "SimOperatorName = " + tm.getSimOperatorName() + "\n";
        str += "SimSerialNumber = " + tm.getSimSerialNumber() + "\n";
        str += "SimState = " + tm.getSimState() + "\n";
        str += "SubscriberId(IMSI) = " + tm.getSubscriberId() + "\n";
        str += "VoiceMailNumber = " + tm.getVoiceMailNumber() + "\n";
        return str;
    }

    /**
     * 跳至拨号界面
     *
     * @param phoneNumber 电话号码
     */
    public static void dial(Context context, final String phoneNumber) {
        context.startActivity(IntentUtil.getDialIntent(phoneNumber, !(context instanceof Activity)));
    }

    /**
     * 拨打电话
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.CALL_PHONE" />}</p>
     *
     * @param phoneNumber 电话号码
     */
    public static void call(Context context, final String phoneNumber) {
        context.startActivity(IntentUtil.getCallIntent(phoneNumber, !(context instanceof Activity)));
    }

    /**
     * 跳至发送短信界面
     *
     * @param phoneNumber 接收号码
     * @param content     短信内容
     */
    public static void sendSms(Context context, final String phoneNumber, final String content) {
        context.startActivity(IntentUtil.getSendSmsIntent(phoneNumber, content, !(context instanceof Activity)));
    }
}