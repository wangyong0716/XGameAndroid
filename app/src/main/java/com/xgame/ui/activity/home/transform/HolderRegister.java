package com.xgame.ui.activity.home.transform;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xgame.common.util.LogUtil;
import com.xgame.home.model.ItemType;

import static java.lang.String.format;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-30.
 */


public final class HolderRegister {

    private static final HolderRegister GLOBAL_REGISTER = new HolderRegister();

    private static final String TAG = HolderRegister.class.getSimpleName();

    private final SparseArray<Holder> mRegisteredSet;

    private HolderRegister() {
        mRegisteredSet = new SparseArray<>();
    }

    public static HolderRegister get() {
        return GLOBAL_REGISTER;
    }

    public static HolderRegister create() {
        return new HolderRegister();
    }

    public void register(final @ItemType int viewType, final Holder holder) {
        if (holder == null) {
            throw new NullPointerException("holder is null");
        }
        Holder h;
        if ((h = mRegisteredSet.get(viewType)) != null) {
            LogUtil.e(TAG, "type %s is already exist(%s), maybe you need unregister first.",
                    viewType, h);
        }
        mRegisteredSet.put(viewType, holder);
    }

    public Holder unregister(int viewType) {
        Holder holder = mRegisteredSet.get(viewType);
        if (holder != null) {
            mRegisteredSet.delete(viewType);
            return holder;
        }
        return null;
    }

    private Holder obtain(int viewType) {
        return mRegisteredSet.get(viewType);
    }

    ViewHolderWrapper create(ViewGroup parent, int viewType) {
        Holder h = obtain(viewType);
        h = h != null ? h : (this != GLOBAL_REGISTER ? GLOBAL_REGISTER.obtain(viewType) : null);
        if (h == null) {
            throw new IllegalStateException(format("viewType %s is not registered", viewType));
        }
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = h.onInflate(inflater, parent);
        if (view == null) {
            view = inflater.inflate(h.layout(), parent, false);
        }
        return new ViewHolderWrapper(view);
    }
}
