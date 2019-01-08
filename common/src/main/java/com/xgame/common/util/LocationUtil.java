package com.xgame.common.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import com.google.common.base.Throwables;
import java.util.List;

public class LocationUtil {

    private static final String TAG = "LocationUtil";

    private static final String ADDRESS_PREF = "address";
    private static final String LOCATION_PREF = "location";

    private static final long VALID_CACHE_TIME_INTEVAL = 30 * 60;
    private static final long NANOS_TO_SECONDS_DIVISOR = 1000000000;

    //当Geocoder服务请求失败（常遇到），如果获取到的经纬度和上次获取到的Address相对应的经纬度相距10公里内则使用上次获取到的值
    private static final long VAILD_ADDRESS_DISTINCE = 10 * 1000;
    private static final double EARTH_RADIUS = 6378137.0;

    private static Location sLastLocation;
    private static LocationListener sLocationListener;
    private static boolean sIsListeningLocationUpdate;
    private static String sAddress;

    @SuppressLint("MissingPermission")
    public static void updateLocation(Context context) {
        if (checkLocationPermisson(context)) {
            try {
                LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                Location location = manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                //获取系统最近一次位置信息
                if (location != null) {
                    long inteval = (int) ((SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos()) / NANOS_TO_SECONDS_DIVISOR);
                    if (inteval < VALID_CACHE_TIME_INTEVAL) {
                        LogUtil.i(TAG, "updateLocation get loacation from LocationManager.PASSIVE_PROVIDER");
                        sLastLocation = location;
                        geocoderLocation(context, sLastLocation);
                        return;
                    }
                }
                //判断缓存的位置是否有效
                if (sLastLocation != null) {
                    long inteval = (int) ((SystemClock.elapsedRealtimeNanos() - sLastLocation.getElapsedRealtimeNanos()) / NANOS_TO_SECONDS_DIVISOR);
                    if (inteval < VALID_CACHE_TIME_INTEVAL) {
                        LogUtil.i(TAG, "updateLocation get loacation from cached");
                        return;
                    }
                } else { //网络请求位置
                    if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        sLocationListener = new MyLocationListener(context);
                        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,  60 * 1000, 100, sLocationListener);
                        sIsListeningLocationUpdate = true;
                        LogUtil.i(TAG, "updateLocation request new location from LocationManager.NETWORK_PROVIDER");
                    } else {
                        LogUtil.i(TAG, "LocationManager.NETWORK_PROVIDER is not enable");
                    }
                }
            } catch (SecurityException e) {
                LogUtil.i(TAG, "updateLocation error: " + Throwables.getStackTraceAsString(e));
            }
        }
    }

    private static boolean checkLocationPermisson(Context context) {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    @SuppressLint("MissingPermission")
    public static void stopUpdateLocation(Context context) {
        if (sIsListeningLocationUpdate && sLocationListener!= null) {
            try {
                LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                manager.removeUpdates(sLocationListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void geocoderLocation(final Context context, final Location location) {
        if (location == null) {
            return;
        }
        ExecutorHelper.runInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    Geocoder geocoder = new Geocoder(context);
                    List<Address> list = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (list != null && list.size() > 0) {
                        Address address = list.get(0);
                        sAddress = address.getAdminArea() +"," + address.getSubAdminArea() + "," + address.getLocality();
                        SharedPreferences.Editor prefEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                        prefEditor.putString(LOCATION_PREF, location.getLatitude() +"," + location.getLongitude()).apply();
                        prefEditor.putString(ADDRESS_PREF, sAddress).apply();
                        LogUtil.i(TAG, "Geocoder getFromLocation la: " + location.getLatitude() + " lo: " + location.getLongitude() + " result: " + sAddress);
                    } else {
                        LogUtil.i(TAG, "Geocoder getFromLocation is empty, la: " + location.getLatitude() + " lo: " + location.getLongitude());
                    }
                } catch (Exception e) {
                    LogUtil.i(TAG, "Geocoder location failed: " + Throwables.getStackTraceAsString(e));
                    tryLoadAddressFromCache(context, location);
                }
            }
        });
    }

    private static void tryLoadAddressFromCache(Context context, Location curLocation) {
        try {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            String location = pref.getString(LOCATION_PREF, "");
            if (!TextUtils.isEmpty(location)) {
                String[] items = location.split(",");
                if (items.length != 2) {
                    return;
                }
                double lastLatitude = Double.parseDouble(items[0]);
                double lastLongitude = Double.parseDouble(items[1]);
                if (getDistance(curLocation.getLongitude(), curLocation.getLatitude(), lastLongitude, lastLatitude) < VAILD_ADDRESS_DISTINCE) {
                    sAddress = pref.getString(ADDRESS_PREF, "");
                }
            }
        } catch (Exception e) {
            LogUtil.i(TAG, "tryLoadAddressFromCache failed: " + Throwables.getStackTraceAsString(e));
        }
    }

    private static class MyLocationListener implements LocationListener {

        private Context mContext;

        public MyLocationListener(Context context) {
            mContext = context;
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onLocationChanged(Location location) {
            LogUtil.i(TAG, "onLocationChanged: " + location);
            sLastLocation = location;
            LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            geocoderLocation(mContext, sLastLocation);
            manager.removeUpdates(this);
            sIsListeningLocationUpdate = false;
            sLocationListener = null;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }

    public static String getAddress() {
        //不要从Preferences中读取上次保存的地址，可能用户位置已经发生很大偏差
        return sAddress;
    }

    public static Location getLocation() {
        return sLastLocation;
    }

    //返回单位是米
    public static double getDistance(double longitude1, double latitude1, double longitude2, double latitude2) {
        double lat1 = rad(latitude1);
        double lat2 = rad(latitude2);
        double a = lat1 - lat2;
        double b = rad(longitude1) - rad(longitude2);
        double distince = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(b / 2), 2))) * EARTH_RADIUS;
        return distince;
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }
}
