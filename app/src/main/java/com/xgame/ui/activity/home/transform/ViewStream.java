package com.xgame.ui.activity.home.transform;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-30.
 */


public final class ViewStream<T> {

    private final List<T> mImpl;

    private final ViewAdapter<T> mAdapter;

    private T mCurrent;

    private int mPosition = -1;

    private final ViewData mViewData = new ViewData() {
        @Override
        public int viewType() {
            return mAdapter.viewType(getCurrent());
        }

        @Override
        public String title(TextView tv) {
            return mAdapter.title(getCurrent(), tv);
        }

        @Override
        public String subTitle(TextView tv) {
            return mAdapter.subTitle(getCurrent(), tv);
        }

        @Override
        public String image(View iv) {
            return mAdapter.image(getCurrent(), iv);
        }

        @Override
        public String stamp(TextView tv) {
            return mAdapter.stamp(getCurrent(), tv);
        }

        @Override
        public boolean remind(TextView tv) {
            return mAdapter.hasRemind(getCurrent(), tv);
        }

        @Override
        public Intent extension() {
            return mAdapter.extension(getCurrent());
        }
    };

    private State mState;

    private ViewStream(List<T> list, ViewAdapter<T> adapter) {
        this.mImpl = list == null ? new ArrayList<T>() : list;
        this.mAdapter = adapter;
        this.mState = new State();
    }

    public ViewData map() {
        return mViewData;
    }

    public synchronized ViewStream<T> insertAll(List<T> list) {
        mImpl.addAll(0, list);
        return this;
    }

    public synchronized ViewStream<T> appendAll(List<T> list) {
        mImpl.addAll(list);
        return this;
    }

    public synchronized ViewStream<T> insert(T t) {
        mImpl.add(0, t);
        return this;
    }

    public synchronized ViewStream<T> append(T t) {
        mImpl.add(t);
        return this;
    }

    public ViewStream<T> then(Action r) {
        if (r != null) {
            r.run(mState.clone());
            mState.reset();
        }
        return this;
    }

    public synchronized ViewStream<T> diffUpdate(List<T> list) {
        List<T> tmp = new ArrayList<>(list);
        if (tmp.removeAll(mImpl) && tmp.size() == 0) {
            mState.hasUpdate = false;
            return this;
        }
        mImpl.clear();
        mImpl.addAll(list);
        resetSeek();
        mState.hasUpdate = true;
        return this;
    }

    private void resetSeek() {
        mPosition = -1;
        mCurrent = null;
    }

    public synchronized int size() {
        return mImpl.size();
    }

    final synchronized ViewStream<T> seek(int position) {
        if (position < mImpl.size() && position > -1) {
            mPosition = position;
            mCurrent = mImpl.get(position);
        } else {
            throw new IllegalStateException(
                    format("out of bound stream size 0, position %s", position));
        }
        return this;
    }

    private T getCurrent() {
        if (mPosition == -1) {
            throw new IllegalStateException("call seek first");
        }
        return mCurrent;
    }

    public synchronized void clear() {
        mImpl.clear();
        resetSeek();
    }

    public synchronized void refresh(List<T> result) {
        if (result == null || result.isEmpty()) {
            return;
        }
        mImpl.removeAll(result);
        insertAll(result);
    }

    public interface Action {

        void run(State s);

    }

    public static class State implements Cloneable {

        private boolean hasUpdate;

        private State() {
            reset();
        }

        void reset() {
            this.hasUpdate = false;
        }

        public boolean hasUpdate() {
            return this.hasUpdate;
        }

        @Override
        protected State clone() {
            try {
                return (State) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public static class Builder<T> {

        List<T> list;

        ViewAdapter<T> adapter;

        public static <T> Builder<T> of(List<T> list) {
            if (list == null) {
                throw new NullPointerException("list is null");
            }
            final Builder<T> b = new Builder<T>();
            b.list = list;
            return b;
        }

        public Builder<T> adapt(ViewAdapter<T> adapter) {
            if (adapter == null) {
                throw new NullPointerException("ViewAdapter is null");
            }
            this.adapter = adapter;
            return this;
        }

        public ViewStream<T> build() {
            if (adapter == null) {
                throw new NullPointerException("ViewAdapter is null");
            }
            return new ViewStream<>(list, adapter);
        }
    }

}
