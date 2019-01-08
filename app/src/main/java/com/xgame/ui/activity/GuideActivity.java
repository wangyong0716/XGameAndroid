
package com.xgame.ui.activity;

import com.xgame.R;
import com.xgame.common.os.WeakHandler;
import com.xgame.common.util.ScreenUtil;
import com.xgame.common.util.SharePrefUtils;
import com.xgame.common.util.StatusBarUtil;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bingoogolapple.bgabanner.BGABanner;
import cn.bingoogolapple.bgabanner.BGALocalImageSize;

public class GuideActivity extends BaseActivity {
    public static final String KEY_SHOW_GUIDE = "show_guide";
    @BindView(R.id.banner)
    BGABanner mBanner;
    @BindView(R.id.guide_skip)
    TextView mGuideSkip;
    @BindView(R.id.guide_enter)
    ConstraintLayout mGuideEnterBtn;
    WeakHandler mTimer = new WeakHandler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        ButterKnife.bind(this);
        init();
        SharePrefUtils.putBoolean(GuideActivity.this, KEY_SHOW_GUIDE, true);
    }

    private void init() {
        initData();
        mTimer.postDelayed(new Runnable() {
            @Override
            public void run() {
                mGuideSkip.setVisibility(View.VISIBLE);
            }
        }, 2000);
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setTranslucentForCoordinatorLayout(this, 0);
    }

    private void initData() {
        BGALocalImageSize localImageSize = new BGALocalImageSize(ScreenUtil.getScreenWidth(this),
                ScreenUtil.getScreenHeight(this), 320, 640);
        // 设置数据源
        mBanner.setData(localImageSize, ImageView.ScaleType.CENTER_CROP,
                R.drawable.guide_background_1, R.drawable.guide_background_2,
                R.drawable.guide_background_3);
        mBanner.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                    int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == mBanner.getItemCount() - 1) {
                    mGuideEnterBtn.setVisibility(View.VISIBLE);
                } else {
                    mGuideEnterBtn.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @OnClick({
            R.id.guide_skip, R.id.guide_enter
    })
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.guide_enter:
                if (mBanner.getCurrentItem() == mBanner.getItemCount() - 1) {
                    goNext();
                } else {
                    mBanner.setCurrentItem(mBanner.getCurrentItem() + 1);
                }
                break;
            case R.id.guide_skip:
                goNext();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTimer.removeCallbacksAndMessages(null);
    }

    private void goNext() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

}
