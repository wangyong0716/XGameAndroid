package com.xgame.common.net;


import java.util.HashMap;
import java.util.Map;

public class ApiParamsUtils {

    public static Map<String, String> getCommonParamsMap() {
        Map<String, String> map = new HashMap();
//        String androidId = BaseConfig.getInstance().getAndroidId();
//        if (androidId != null & androidId.length() > 0) {
//            map.put("d", AESUtils.encrypt(androidId));
//        }
//        map.put("osn", "android");
//        map.put("osv", BaseConfig.getInstance().getVersionRelease());
//        map.put("v", BaseConfig.getInstance().getAppVersionName());
//        map.put("n", String.valueOf(NetUtil.getNetType(BaseApplication.getContext())));
//        map.put("lo", AESUtils.encrypt(String.valueOf(
//                SharePrefUtils.getFloat(BaseApplication.getContext(), Constant.LOCATION_LONGITUDE, 0f))));
//        map.put("la", AESUtils.encrypt(String.valueOf(
//                SharePrefUtils.getFloat(BaseApplication.getContext(), Constant.LOCATION_LATITUDE, 0f))));
//        String rg = SharePrefUtils.getString(
//                BaseApplication.getContext(), Constant.LOCATION_REGION, "");
//        if (rg != null && rg.length() > 0) {
//            map.put("rg", AESUtils.encrypt(rg));
//            Crashlytics.setString("rg", rg);
//        }
//        map.put("ac", String.valueOf(
//                SharePrefUtils.getFloat(BaseApplication.getContext(), Constant.LOCATION_ACCURACY, 0f)));
//        map.put("ts", String.valueOf(System.currentTimeMillis()));
//        String imei = ApplicationUtils.getIMEI(BaseApplication.getContext());
//        map.put("im", AESUtils.encrypt(imei));
//        map.put("sw", String.valueOf(BaseConfig.getInstance().getScreenWidth()));
//        map.put("sh", String.valueOf(BaseConfig.getInstance().getScreenHeight()));
//        String lc = BaseConfig.getInstance().getLC();
//        if (lc != null && lc.length() > 0) {
//            map.put("lc", lc);
//        }
//        map.put("lm", AESUtils.encrypt(String.valueOf(SharePrefUtils.getInt(
//                BaseApplication.getContext(), Constant.LOGIN_WITH_PLATFORM, -2))));
//        map.put("di", AESUtils.encrypt(BaseConfig.getInstance().getUniqueID()));
//        map.put("ch", ChannelUtils.getChannelWithCache(BaseApplication.getContext()));
//        map.put("regid", SharePrefUtils.getString(BaseApplication.getContext(), Constant.APP.MI_PUSH_REG_ID_PREF_KEY, ""));
//        map.put("ip", AESUtils.encrypt(IpAdressUtils.getIp(BaseApplication.getContext())));
//        map.put("mk", AESUtils.encrypt(Build.MANUFACTURER));
//        map.put("sm", BaseConfig.getInstance().getBuilderModel());
//        map.put("fio", AppUtils.isFirstInstallStart() ? "1" : "2");

        return map;
    }
}
