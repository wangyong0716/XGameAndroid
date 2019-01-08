package com.xgame.common.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Created by wuyanzhi on 2018/1/24.
 */

public class NetworkUtil {
    private static final String LOGTAG = NetworkUtil.class.getName();
    public static final String UNKNOWN = "unknown";
    public static final String GSM_MOBILE = "ChinaMobile";//中国移动
    public static final String GSM_UNICOM = "ChinaUnicom";//中国联通
    public static final String GSM_TELECOM = "ChinaTelecom";//中国电信
    public static final String GSM_NONE = "unknown";//无卡模式

    public static final String SIM_SIM = "sim";
    public static final String SIM_USIM = "wcdma";
    public static final String SIM_UIM = "cdma";
    public static final String SIM_NONE = "unknown";

    public interface NetworkType {
        int Network_WIFI = 0;
        int Network_2G = 1;
        int Network_3G = 2;
        int Network_4G = 3;
        int Network_Mobile = 4;
        int Network_None = 5;
    }

    private NetworkUtil() {
    }

    /**
     * get the type of sim:mobile、uincom、telecom
     */
    public static String getMobileType(Context context) {
        if (context == null) {
            return GSM_NONE;
        }
        TelephonyManager tm = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        String iNumeric = tm.getSimOperator();
        if (iNumeric != null && iNumeric.length() > 0) {
            if (iNumeric.equals("46000") || iNumeric.equals("46002") || iNumeric.equals("46007")) {
                // China mobile
                return GSM_MOBILE;
            } else if (iNumeric.equals("46001")) {
                // China Unicom
                return GSM_UNICOM;
            } else if (iNumeric.equals("46003")) {
                // China Telecom
                return GSM_TELECOM;
            }
        }
        return GSM_NONE;
    }


    /**
     * 得到sim的运营商信息
     * 对于双卡双待手机，暂时只考虑mtk芯片
     *
     * @param context
     * @return
     */
    public static String getCarrierOperator(Context context) {
        if (context == null) {
            return UNKNOWN;
        }
        Method method = null;
        Object result_0 = null;
        Object result_1 = null;
        StringBuilder operator = new StringBuilder();
        TelephonyManager tm = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        try {
            method = TelephonyManager.class.getMethod("getSimOperatorGemini", new Class[]{int.class});
            result_0 = method.invoke(tm, new Object[]{Integer.valueOf(0)});
            result_1 = method.invoke(tm, new Object[]{Integer.valueOf(1)});
        } catch (Exception e) {
            if (LogUtil.DEBUG) {
                LogUtil.e(LOGTAG, "get sim operator exception : " + e.toString());
            }
            String ret = tm.getSimOperator();
            if (TextUtils.isEmpty(ret)) {
                ret = UNKNOWN;
            }
            return ret;
        }

        if (TextUtils.isEmpty(result_0.toString())) {
            operator.append(UNKNOWN).append(",");
        } else {
            operator.append(result_0.toString()).append(",");
        }

        if (TextUtils.isEmpty(result_1.toString())) {
            operator.append(UNKNOWN);
        } else {
            operator.append(result_1.toString());
        }
        return operator.toString();
    }

    public static String getSIMType(Context context) {
        if (context == null) {
            return SIM_NONE;
        }
        //获得SIMType
        String simType = SIM_NONE;
        //获得系统服务，从而取得sim数据
        TelephonyManager tm = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        //获得手机SIMType
        int type = tm.getNetworkType();//判断类型值，并且命名
        if (type == TelephonyManager.NETWORK_TYPE_UMTS) {
            simType = SIM_USIM;//类型为UMTS定义为wcdma的USIM卡
        } else if (type == TelephonyManager.NETWORK_TYPE_GPRS) {
            simType = SIM_SIM;//类型为GPRS定义为GPRS的SIM卡
        } else if (type == TelephonyManager.NETWORK_TYPE_EDGE) {
            simType = SIM_SIM;//类型为EDGE定义为EDGE的SIM卡
        } else {
            simType = SIM_UIM;//类型为unknown定义为cdma的UIM卡
        }
        return simType;
    }

    public static int getActiveNetworkType(Context context) {
        if (context == null) {
            return -1;
        }
        int defaultValue = -1;
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return defaultValue;
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) return defaultValue;
        return info.getType();
    }

    public static NetworkInfo getActiveNetwork(final Context context) {
        if (context == null) {
            return null;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return null;
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info;
    }

    public static String getActiveNetworkName(final Context context) {
        if (context == null) {
            return "null";
        }
        String defaultValue = "null";
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return defaultValue;
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) return defaultValue;
        defaultValue = info.getTypeName();
        if (!TextUtils.isEmpty(info.getSubtypeName())) {
            defaultValue = defaultValue + "-" + info.getSubtypeName();
        }
        if (!TextUtils.isEmpty(info.getExtraInfo())) {
            defaultValue = defaultValue + "-" + info.getExtraInfo();
        }
        return defaultValue;
    }

    public static boolean isWifi(Context context) {
        if (context == null) {
            return false;
        }

        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null) {
            switch (info.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                case ConnectivityManager.TYPE_ETHERNET:
                case ConnectivityManager.TYPE_BLUETOOTH:
                    return true;
                default:
                    return false;
            }
        } else
            return false;
    }

    public static boolean hasNetwork(Context context) {
        return (getActiveNetworkType(context) != -1);
    }

    /**
     * check wifi state
     *
     * @param context
     * @return true if current wifi 1.not connected to wifi 2.has opened 3.have
     * available AP, otherwise return false
     */
    public static boolean checkWifiNetworkState(Context context) {
        if (context == null) {
            return false;
        }
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            return false;
        }

        try {
            Thread.sleep(1500);
        } catch (Exception e) {

        }

        if (getWifiScanResultsAvailable(wifiManager) && getWifiIpAddress(wifiManager) == 0) {
            return true;
        }
        return false;
    }

    /**
     * get the SSID of currently connected wifi
     */
    public static String getWifiSSID(Context context) {
        if (context == null) {
            return "";
        }
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        if (info == null)
            return new String("");
        return info.getSSID();
    }

    /**
     * Get wifi ip address
     *
     * @param wifiManager
     * @return
     */
    private static int getWifiIpAddress(WifiManager wifiManager) {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getIpAddress();
    }

    /**
     * Get if has wifi available results
     *
     * @param wifiManager
     * @return
     */
    private static boolean getWifiScanResultsAvailable(WifiManager wifiManager) {
        List<ScanResult> scanResults = wifiManager.getScanResults();
        if (scanResults != null && scanResults.size() > 0) {
            return true;
        }
        return false;
    }

    public static int getNetworkType(Context context) {
        NetworkInfo networkInfo = NetworkUtil.getActiveNetwork(context);
        if (networkInfo == null || !networkInfo.isConnected()) {
            return NetworkType.Network_None;
        }
        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI ||
                networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET ||
                networkInfo.getType() == ConnectivityManager.TYPE_WIMAX) {
            return NetworkType.Network_WIFI;
        }

        if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            int subType = networkInfo.getSubtype();
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return NetworkType.Network_2G;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return NetworkType.Network_3G;
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return NetworkType.Network_4G;
            }

            String subTypeName = networkInfo.getSubtypeName();
            if (subTypeName.equalsIgnoreCase("TD-SCDMA") || subTypeName.equalsIgnoreCase("WCDMA") || subTypeName.equalsIgnoreCase("CDMA2000")) {
                return NetworkType.Network_3G;
            }

            return NetworkType.Network_Mobile;
        }

        return NetworkType.Network_None;
    }

    public static boolean is2GNetwork(Context context) {
        return NetworkType.Network_2G == (getNetworkType(context));
    }

}
