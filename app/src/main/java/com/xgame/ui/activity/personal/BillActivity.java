package com.xgame.ui.activity.personal;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.TextView;

import com.xgame.R;
import com.xgame.common.util.StatusBarUtil;
import com.xgame.common.util.UiUtil;
import com.xgame.ui.Router;
import com.xgame.ui.activity.BaseActivity;
import com.xgame.ui.activity.personal.view.PersonalToolbarHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by dingning1
 * on 18-1-30.
 */


public class BillActivity extends BaseActivity {

    private static final int ANIM_DURATION = 300;

    @BindView(R.id.tab_container) View mTabContainer;
    @BindView(R.id.coin_bill) TextView mCoinTab;
    @BindView(R.id.cash_bill) TextView mCashTab;
    @BindView(R.id.indicator) View mIndicator;
    @BindView(R.id.pager) ViewPager mPager;
    @BindView(R.id.bill_disclaimer) View mDisclaimer;
    @BindView(R.id.coin_link) TextView mGetCoin;
    @BindView(R.id.btn) TextView mButton;

    private int mSelectTab = -1;
    private Integer mIndicatorOffset;

    private BillListFragment mCoinBillFragment = new BillListFragment();
    private BillListFragment mCashBillFragment = new BillListFragment();

    private TextView[] mTabViews;

    private BillListFragment[] mFragments;

    private PagerAdapter mTabAdapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {

        @Override
        public Fragment getItem(int position) {
            return mFragments[position];
        }

        @Override
        public int getCount() {
            return mFragments.length;
        }
    };

    private BillListFragment.IDataLoadListener mLoadListener = new BillListFragment.IDataLoadListener() {
        @Override
        public void onLoad(int type, boolean hasData) {
            setBottomUI(type, hasData);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);
        ButterKnife.bind(this);

        initToolbar();
        initView();
    }

    private void initToolbar() {
        StatusBarUtil.setTransparent(this);
        StatusBarUtil.setMiuiStatusBarDarkMode(this, false);

        new PersonalToolbarHolder(findViewById(R.id.toolbar))
                .setTitle(R.string.personal_item_bill, R.color.color_white)
                .setBackIcon(R.drawable.back, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onBackPressed();
                    }
                });
    }

    private void initView() {
        mTabViews = new TextView[] { mCoinTab, mCashTab };

        mCoinBillFragment.setType(BillListFragment.BILL_COIN, mLoadListener);
        mCashBillFragment.setType(BillListFragment.BILL_CASH, mLoadListener);
        mFragments = new BillListFragment[] { mCoinBillFragment, mCashBillFragment };

        mCoinTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTab(getSelectTabIndex(mCoinTab));
            }
        });
        mCashTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTab(getSelectTabIndex(mCashTab));
            }
        });
        mCoinTab.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        initTab();
                        mCoinTab.getViewTreeObserver().removeOnPreDrawListener(this);
                        return true;
                    }
                });

        mPager.setAdapter(mTabAdapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                    int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                selectTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        UiUtil.expandHitArea(mGetCoin);
        mGetCoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Router.toTaskCenter();
            }
        });
    }

    private void initTab() {
        mIndicatorOffset = (mCoinTab.getWidth() - mIndicator.getWidth()) / 2;
        UiUtil.expandHitArea(mCoinTab);
        UiUtil.expandHitArea(mCashTab);
        selectTab(0);
    }

    private void setBottomUI(final int type, boolean hasData) {
        if (mSelectTab < 0) {
            return;
        }
        BillListFragment fragment = mFragments[mSelectTab];
        if (fragment.getType() != type) {
            return;
        }
        mDisclaimer.setVisibility(hasData ? View.VISIBLE : View.GONE);
        if (!hasData) {
            mButton.setVisibility(View.VISIBLE);
            getString(R.string.gold_coin);
            mButton.setText(R.string.btn_get_coin);
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Router.toTaskCenter();
                }
            });
        } else {
            final boolean isCash = type == BillListFragment.BILL_CASH;
            // mGetCoin.setVisibility(isCash ? View.GONE : View.VISIBLE);

            mButton.setVisibility(isCash ? View.VISIBLE : View.GONE);
            mButton.setText(R.string.will_withdraw);
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Router.toWithdrawCash();
                }
            });
            mGetCoin.setText(isCash ? R.string.get_cash_detail : R.string.get_coin);
            mGetCoin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isCash) {
                        Router.toWithdrawCashRecord();
                    } else {
                        Router.toTaskCenter();
                    }
                }
            });
        }
    }

    private void selectTab(int i) {
        if (mSelectTab != i) {
            mSelectTab = i;
            moveIndicator(i, true);
            mPager.setCurrentItem(i, true);
            BillListFragment fragment = mFragments[i];
            setBottomUI(fragment.getType(), fragment.hasData());
        }
    }

    private void moveIndicator(final int tab, boolean anim) {
        final float startPos = mIndicator.getX();
        final float endPos = getXIn(mTabViews[tab], mIndicator.getParent()) + mIndicatorOffset;
        int duration = anim ? ANIM_DURATION : 0;
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (Float.compare(interpolatedTime, 1) == 0) {
                    for (int i = 0; i < mTabViews.length; i++) {
                        int colorId = i == tab ? R.color.yellow : R.color.color_white;
                        mTabViews[i].setTextColor(getResources().getColor(colorId));
                    }
                    mIndicator.setX(endPos);
                } else {
                    mIndicator.setX(startPos + (endPos - startPos) * interpolatedTime);
                }
            }
        };
        animation.setDuration(duration);
        mIndicator.startAnimation(animation);
    }

    private int getSelectTabIndex(View tab) {
        for (int i = 0; i < mTabViews.length; i++) {
            if (mTabViews[i] == tab) {
                return i;
            }
        }
        return 0;
    }

    private float getXIn(View view, ViewParent container) {
        float x = view.getX();
        View curView = view;
        while (true) {
            ViewParent parent = curView.getParent();
            if (parent != null && parent != container) {
                curView = (View) parent;
                x += curView.getX();
            } else {
                break;
            }
        }
        return x;
    }
}
