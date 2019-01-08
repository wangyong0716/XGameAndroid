package com.xgame.ui.activity.home;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.xgame.uisupport.OnSelectedListener;

import static com.xgame.common.util.UiUtil.checkInMainThread;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-28.
 */
public class IndicatorWrapper {

    private final ViewGroup mIndicator;

    private int mCurrentPos = -1;

    private OnSelectedListener mOnSelectedListener;

    private ViewPager mViewPager;

    private final ViewPager.OnPageChangeListener mPageChangeListener
            = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            setSelect(position);
            if (mOnSelectedListener != null) {
                mOnSelectedListener.onSelected(position, null);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    public IndicatorWrapper(final ViewGroup indicatorView) {
        this.mIndicator = indicatorView;
        int cc = indicatorView.getChildCount();
        for (int i = 0; i < cc; i++) {
            final int pos = i;
            View child = indicatorView.getChildAt(pos);
            child.setSelected(false);
            child.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    IndicatorWrapper.this.setSelect(pos);
                }
            });
        }
        if (cc > 0) {
            setSelect(0);
        }
    }

    public void setSelectedListener(OnSelectedListener listener) {
        this.mOnSelectedListener = listener;
    }

    public void setSelect(final int pos) {
        checkInMainThread();
        checkIsValidPos(pos);
        if (mCurrentPos != -1) {
            mIndicator.getChildAt(mCurrentPos).setSelected(false);
        }
        mIndicator.getChildAt(pos).setSelected(true);
        mCurrentPos = pos;
        PagerAdapter adapter;
        if (mViewPager != null && (adapter = mViewPager.getAdapter()) != null
                && pos < adapter.getCount()) {
            mViewPager.setCurrentItem(pos);
        }
    }

    public void attachToViewPager(ViewPager viewPager) {
        checkInMainThread();
        if (this.mViewPager != null) {
            this.mViewPager.removeOnPageChangeListener(mPageChangeListener);
        }
        this.mViewPager = viewPager;
        viewPager.addOnPageChangeListener(mPageChangeListener);
    }

    private void checkIsValidPos(int pos) {
        int count = mIndicator.getChildCount();
        if (pos > count || pos < 0) {
            throw new IllegalStateException(
                    String.format("out of bounds %s, pos %s", count, pos));
        }
    }
}
