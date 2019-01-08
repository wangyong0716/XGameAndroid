package com.xgame.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.xgame.R;

import java.lang.ref.WeakReference;

/**
 * Created by wangyong on 18-2-9.
 */

public class MatchLoadingView extends View {
    private static int DURATION = 60000;

    private static final int DOT_NUM = 3;
    private static final int LEFT = 0x01;
    private static final int CENTER_HORIZONTAL = 0x02;
    private static final int RIGHT = 0x04;
    private static final int TOP = 0x10;
    private static final int CENTER_VERTICAL = 0x20;
    private static final int BOTTOM = 0x40;
    private static final int CENTER = 0x22;
    private static final int HORIZONTAL_MASK = 0x0f;
    private static final int VERTICAL_MASK = 0xf0;

    private static final int MODE_ITERATION = 0;
    private static final int MODE_CIRCULATION = 1;

    private Paint mPaint = new Paint();
    private int mGravity = CENTER;
    private int mDotRadius;
    private int mBackGroundColor;
    private int mDotColor;
    private int mSelectColor;

    private int mSelectIndex;

    private int mHorizontalPadding;
    private int mVerticalPadding;

    private int mHeight;
    private int mWidth;

    private int mDotDistance;

    private int mMode;

    private CountTimer mCountTimer;

    public MatchLoadingView(Context context) {
        super(context);
        init(null);
    }

    public MatchLoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public MatchLoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.bw_match_loading_style);
            mGravity = ta.getInteger(R.styleable.bw_match_loading_style_gravity, CENTER);
            mBackGroundColor = ta.getColor(R.styleable.bw_match_loading_style_back_color, getColor(R.color.white_alpha_60));
            mDotColor = ta.getColor(R.styleable.bw_match_loading_style_dot_color,
                    getResources().getColor(R.color.bw_progress_background_color));
            mSelectColor = ta.getColor(R.styleable.bw_match_loading_style_dot_selected_color,
                    getResources().getColor(R.color.bw_progress_foreground_color));

            mMode = ta.getInteger(R.styleable.bw_match_loading_style_mode, MODE_ITERATION);
            mWidth = ta.getDimensionPixelOffset(R.styleable.bw_match_loading_style_android_layout_width, 0);
            mHeight = ta.getDimensionPixelOffset(R.styleable.bw_match_loading_style_android_layout_height, 0);
            mDotRadius = ta.getDimensionPixelOffset(R.styleable.bw_match_loading_style_dot_radius, 0);
            mDotDistance = ta.getDimensionPixelOffset(R.styleable.bw_match_loading_style_dot_distance, 0);
            mHorizontalPadding = ta.getDimensionPixelOffset(R.styleable.bw_match_loading_style_horizontal_padding, 0);
            mVerticalPadding = ta.getDimensionPixelOffset(R.styleable.bw_match_loading_style_vertical_padding, 0);
            ta.recycle();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        startTick();
    }

    private float getCenterX(int index) {
        switch (HORIZONTAL_MASK & mGravity) {
            case LEFT:
                return mHorizontalPadding + (mDotRadius * 2 + mDotDistance) * index + mDotRadius;
            case CENTER_HORIZONTAL:
                if (DOT_NUM % 2 == 0) {
                    return mWidth / 2 + (index - DOT_NUM / 2) * (mDotRadius * 2 + mDotDistance) + mDotDistance / 2 + mDotRadius;
                } else {
                    return mWidth / 2 + (index - DOT_NUM / 2) * (mDotRadius * 2 + mDotDistance);
                }
            case RIGHT:
            default:
                return mWidth - mHorizontalPadding - (DOT_NUM - index) * (mDotRadius * 2 + mDotDistance) - mDotRadius;
        }
    }

    private float getCenterY() {
        switch (VERTICAL_MASK & mGravity) {
            case TOP:
                return mVerticalPadding + mDotRadius;
            case CENTER_VERTICAL:
                return mHeight / 2;
            case BOTTOM:
            default:
                return mHeight - mVerticalPadding - mDotRadius;
        }
    }

    private int getColor(int index) {
        if (mMode == MODE_ITERATION) {
            if (index <= mSelectIndex) {
                return mSelectColor;
            }
            return Color.TRANSPARENT;
        } else {
            if (index == mSelectIndex) {
                return mSelectColor;
            }
            return mDotColor;
        }
    }

    private int getBackGroundRadius() {
        return (mWidth > mHeight ? mHeight : mWidth) / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < DOT_NUM; i++) {
            mPaint.setColor(getColor(i));
            canvas.drawCircle(getCenterX(i), getCenterY(), mDotRadius, mPaint);
        }
        mPaint.setColor(mBackGroundColor);
        canvas.drawCircle(mWidth / 2, mHeight / 2, getBackGroundRadius(), mPaint);
    }

//    @Override
//    public void setVisibility(int visibility) {
//        if (getVisibility() == visibility) {
//            return;
//        }
//        if (visibility == VISIBLE) {
//            startTick();
//        } else {
//            cancelTick();
//        }
//        super.setVisibility(visibility);
//    }

    private void handleTick() {
        mSelectIndex++;
        if (mSelectIndex >= DOT_NUM) {
            mSelectIndex = mMode == MODE_ITERATION ? -1 : 0;
        }
        invalidate();
    }

    private void startTick() {
        if (mCountTimer == null) {
            mCountTimer = new CountTimer(this, DURATION, 500);
        }
        mCountTimer.start();
    }

    private void cancelTick() {
        mCountTimer.cancel();
    }

    public static class CountTimer extends CountDownTimer {
        private WeakReference<MatchLoadingView> mWeakReference;

        public CountTimer(MatchLoadingView view, long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            mWeakReference = new WeakReference<MatchLoadingView>(view);
        }

        @Override
        public void onTick(long l) {
            MatchLoadingView view = mWeakReference.get();
            if (view != null) {
                view.handleTick();
            } else {
                cancel();
            }
        }

        @Override
        public void onFinish() {
            MatchLoadingView view = mWeakReference.get();
            if (view != null) {
                view.handleTick();
                start();
            } else {
                cancel();
            }
        }
    }
}