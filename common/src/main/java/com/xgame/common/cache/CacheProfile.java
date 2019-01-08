package com.xgame.common.cache;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-24.
 */

public interface CacheProfile<K, V> {

    int sizeOf(K key, V value);

    int maxSize();
}
