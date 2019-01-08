package com.xgame.common.api;

import com.xgame.common.cache.CacheFactory;
import com.xgame.common.cache.CacheFactory.ICache;
import com.xgame.common.cache.CacheProfile;
import com.xgame.common.util.LogUtil;

import retrofit2.Retrofit;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 * <p>
 * Created by jackwang
 * on 18-1-24.
 */


public final class ApiServiceManager {
    private static final String TAG = "ApiServiceManager";

    private static final CacheProfile<Class, Object> sCacheProfile
            = new CacheProfile<Class, Object>() {
        @Override
        public int sizeOf(Class key, Object value) {
            return 1;
        }

        @Override
        public int maxSize() {
            return 6;
        }
    };

    private static final Object sInstLock = ApiServiceManager.class;

    private static volatile ApiServiceManager sInst;

    private final Retrofit mRetrofit;

    private final ICache<Class, Object> mCache;

    private ApiServiceManager(Retrofit retrofit) {
        mCache = CacheFactory.create(sCacheProfile);
        mRetrofit = retrofit;
    }

    public static void prepare(ServerFactory factory) {
        if (sInst != null) {
            return;
        }
        if (factory == null) {
            throw new NullPointerException("factory is null");
        }
        synchronized (sInstLock) {
            if (sInst != null) {
                return;
            }
            sInst = new ApiServiceManager(factory.create());
            sInstLock.notifyAll();
        }
    }

    public static ApiServiceManager get() {
        if (sInst == null) {
            throw new IllegalStateException("please call prepare first");
        }
        return sInst;
    }

    public static void reset(ServerFactory factory) {
        sInst = new ApiServiceManager(factory.create());
        LogUtil.d(TAG, "reset service");
    }

    public static <T> T obtain(Class<T> clz) {
        final ApiServiceManager mgr = get();
        Object apiService = mgr.mCache.get(clz);
        if (apiService != null) {
            return (T) apiService;
        }
        apiService = mgr.mRetrofit.create(clz);
        mgr.mCache.put(clz, apiService);
        return (T) apiService;
    }

    public interface ServerFactory {

        Retrofit create();
    }

}
