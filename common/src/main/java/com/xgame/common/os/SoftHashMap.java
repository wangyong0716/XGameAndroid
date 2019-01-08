package com.xgame.common.os;

import android.support.annotation.NonNull;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 17-12-8.
 */


public class SoftHashMap<K, V> implements Map<K, V> {

    private final HashMap<K, SoftValueRef<K, V>> mMap;

    private ReferenceQueue<? super V> mQueue;

    public SoftHashMap() {
        mMap = new HashMap<K, SoftValueRef<K, V>>();
        mQueue = new ReferenceQueue<>();
    }

    @Override
    public int size() {
        removeNull();
        return mMap.size();
    }

    @Override
    public boolean isEmpty() {
        removeNull();
        return mMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        removeNull();
        for (SoftValueRef<K, V> val : mMap.values()){
            V v = val.get();
            if (v != null && v.equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        removeNull();
        SoftValueRef<K, V> ref = mMap.get(key);
        return ref != null ? ref.get() : null;
    }

    @Override
    public V put(K key, V value) {
        removeNull();
        SoftValueRef<K, V> soft = new SoftValueRef<K, V>(key, value, mQueue);
        SoftValueRef<K, V> prev = mMap.put(key, soft);
        return prev != null ? prev.get() : null;
    }

    @SuppressWarnings("unchecked")
    private void removeNull() {
        SoftValueRef poll = (SoftValueRef) mQueue.poll();
        while (poll != null) {
            mMap.remove(poll.key);
            poll = (SoftValueRef) mQueue.poll();
        }
    }

    @Override
    public V remove(Object key) {
        removeNull();
        final SoftValueRef<K, V> value = mMap.remove(key);
        return value != null ? value.get() : null;
    }

    @Override
    public void putAll(@NonNull Map<? extends K, ? extends V> m) {
        if (m == null || m.isEmpty()) {
            return;
        }
        for (Entry<? extends K, ? extends V> en : m.entrySet()) {
            put(en.getKey(), en.getValue());
        }
    }

    @Override
    public void clear() {
        removeNull();
        mMap.clear();
    }

    @NonNull
    @Override
    public Set<K> keySet() {
        removeNull();
        return mMap.keySet();
    }

    @NonNull
    @Override
    public Collection<V> values() {
        removeNull();
        List<V> ret = new ArrayList<V>();
        for (SoftValueRef<K, V> val : mMap.values()){
            V v = val.get();
            if (v != null) {
                ret.add(v);
            }
        }
        return ret;
    }

    @NonNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        removeNull();
        HashMap<K, SoftValueRef<K, V>> m = mMap;
        Set<Entry<K, V>> entrySet = new HashSet<>(m.size());
        for (SoftValueRef<K, V> value : m.values()){
            V v = value.get();
            if (v != null) {
                entrySet.add(new AbstractMap.SimpleEntry<>(value.key, v));
            }
        }
        return entrySet;
    }

    private static class SoftValueRef<K, V> extends SoftReference<V> {

        private final K key;

        SoftValueRef(K k, V referent, ReferenceQueue<? super V> q) {
            super(referent, q);
            key = k;
        }
    }

}
