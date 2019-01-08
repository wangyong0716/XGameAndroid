package com.xgame.common.var;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-28.
 */


public class ListVarHandle<E> extends LazyVarHandle<List<E>> {

    @Override
    protected List<E> constructor() {
        return new ArrayList<>();
    }

    public boolean isEmpty() {
        List<E> l = peek();
        return l == null || l.isEmpty();
    }

    public int size() {
        return isEmpty() ? 0 : get().size();
    }
}
