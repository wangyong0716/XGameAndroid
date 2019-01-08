package com.xgame.ui.adapter;

import android.content.Context;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Albert
 * on 18-1-28.
 */

public abstract class AbstractListAdapter<T> extends BaseAdapter {

    protected Context mContext;

    protected List<T> mList;

    public AbstractListAdapter(Context context) {
        mContext = context;
    }

    @Override
    public T getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setItem(List<T> list) {
        if (mList == null && list == null) {
            mList = new ArrayList<T>();
        } else if (list == null) {
            clearItem();
        } else {
            mList = list;
        }
        notifyDataSetChanged();
    }

    public void setItem(T[] array) {
        setItem(Arrays.asList(array));
    }

    public void clearItem() {
        if (mList != null) {
            mList.clear();
        }
    }

    public void addItem(T t) {
        if (mList == null) {
            mList = Collections.emptyList();
        }
        mList.add(t);
    }

    public void addItem(int position, T t) {
        if (mList == null) {
            mList = Collections.emptyList();
        }
        mList.add(position, t);
    }

    public void addItem(List<T> list) {
        if (mList == null) {
            mList = Collections.emptyList();
        }
        mList.addAll(list);
    }

    public void addItem(T[] array) {
        addItem(Arrays.asList(array));
    }

    public List<T> getList() {
        return mList;
    }

    public T removeItem(int position) {
        if (mList == null) {
            mList = Collections.emptyList();
        }
        return mList.remove(position);
    }

    public void removeAll(List<T> list) {
        if (list != null && list.size() > 0) {
            mList.removeAll(list);
        }
    }

    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    public boolean isEmpty() {
        return getCount() == 0;
    }
}

