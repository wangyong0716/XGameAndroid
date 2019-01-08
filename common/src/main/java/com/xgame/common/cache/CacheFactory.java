package com.xgame.common.cache;

import java.util.Set;
import java.util.WeakHashMap;

import static com.xgame.common.cache.CacheFactory.ICache.TYPE_DOUBLE_MEMORY;


/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-25.
 */

public final class CacheFactory {

    private static final WeakHashMap<ICache, Object> sHolder = new WeakHashMap<>(7);

    private CacheFactory() {
    }

    public static <K, V> ICache<K, V> create(CacheProfile<K, V> profile) {
        return create(profile, TYPE_DOUBLE_MEMORY);
    }

    public static <K, V> ICache<K, V> create(CacheProfile<K, V> profile, int type) {
        ICache<K, V> cache;
        switch (type) {
            default:
            case TYPE_DOUBLE_MEMORY:
                cache = new DoubleMemoryCache<>(profile);
                break;
        }
        sHolder.put(cache, null);
        return cache;
    }

    public static void cleanAll() {
        Set<ICache> set = sHolder.keySet();
        for (ICache c : set) {
            c.clean();
        }
    }

    public interface ICache<K, V> {

        int TYPE_DOUBLE_MEMORY = 1;

        CacheProfile getProfile();

        V put(K key, V val);

        V get(K key);

        void clean();

        int size();

        V remove(K key);
    }
}
