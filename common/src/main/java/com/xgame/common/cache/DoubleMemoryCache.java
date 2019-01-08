package com.xgame.common.cache;

import android.util.LruCache;

import com.xgame.common.os.SoftHashMap;
import com.xgame.common.var.LazyVarHandle;


/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-25.
 */


class DoubleMemoryCache<K, V> implements CacheFactory.ICache<K, V> {

    private static final int DEFAULT_KEEP_SIZE = 7;

    private static final CacheProfile DEFAULT_CACHE_PROFILE = new CacheProfile() {
        @Override
        public int sizeOf(Object key, Object value) {
            return 1;
        }

        @Override
        public int maxSize() {
            return DEFAULT_KEEP_SIZE;
        }
    };

    private final CacheProfile mCacheProfile;

    private LruCache<K, V> mDelegate;

    DoubleMemoryCache(CacheProfile<K, V> profile) {
        this.mCacheProfile = profile != null ? profile : DEFAULT_CACHE_PROFILE;
        mDelegate = obtainDelegate(profile);
    }

    private static <K, V> LruCache<K, V> obtainDelegate(final CacheProfile<K, V> profile) {
        return new ExtLruCache<>(profile);
    }

    private static <K, V> int getValidMaxCacheSize(CacheProfile<K, V> profile) {
        final int size = profile.maxSize();
        if (profile.sizeOf(null, null) == 1) {
            return size <= 0 ? DEFAULT_KEEP_SIZE : size;
        } else if (size <= 0) {
            throw new IllegalStateException("cache size must > 0");
        } else {
            return size;
        }
    }

    @Override
    public CacheProfile getProfile() {
        return mCacheProfile;
    }

    @Override
    public V put(K key, V val) {
        return mDelegate.put(key, val);
    }

    @Override
    public V get(K key) {
        return mDelegate.get(key);
    }

    @Override
    public void clean() {
        mDelegate.evictAll();
    }

    @Override
    public int size() {
        return mDelegate.size();
    }

    @Override
    public V remove(K key) {
        return mDelegate.remove(key);
    }

    private static class ExtLruCache<K, V> extends LruCache<K, V> {

        private final LazyVarHandle<SoftHashMap<K, V>> mCacheVar;

        private final CacheProfile<K, V> mProfile;

        ExtLruCache(CacheProfile<K, V> profile) {
            super(getValidMaxCacheSize(profile));
            mProfile = profile;
            mCacheVar = new LazyVarHandle<SoftHashMap<K, V>>() {
                @Override
                protected SoftHashMap<K, V> constructor() {
                    return new SoftHashMap<>();
                }
            };
        }

        @Override
        public int sizeOf(K key, V value) {
            return mProfile.sizeOf(key, value);
        }

        @Override
        protected V create(K key) {
            SoftHashMap<K, V> c = mCacheVar.peek();
            return c != null ? c.get(key) : null;
        }

        @Override
        protected void entryRemoved(boolean evicted, K key, V oldValue, V newValue) {
            super.entryRemoved(evicted, key, oldValue, newValue);
            SoftHashMap<K, V> m = mCacheVar.get();
            if (evicted) {
                m.put(key, newValue);
            } else {
                m.remove(key);
            }
        }

        @Override
        public void trimToSize(int maxSize) {
            if (maxSize <= 0) {
                mCacheVar.destructor();
            }
            super.trimToSize(maxSize);
        }
    }
}
