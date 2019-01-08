package com.xgame.ui.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.xgame.R;

import java.lang.ref.WeakReference;

/**
 * Created by wangyong on 18-1-31.
 */

public class BattleProgressBar extends View {
    public static int ANIMATION_DURATION = 10000;
    private static int ANIMATION_PROGRESS_MAX = 1000;
    private Context mContext;
    MyAnimator mProgressAnimator;

    private RectF mBackRect;
    private RectF mForeRect;
    private float mCornerRadius;
    private Paint mPaint = new Paint();

    private int mBackgroundColor;
    private int mForegroundColor;
    private int mWidth = 30;
    private int mHeight = 620;
    private long mDuration;
    private int mProgress = 0; //表示当前进度。0～100,百分比

    public BattleProgressBar(Context context) {
        super(context);
        init(context, null);
    }

    public BattleProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BattleProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.battle_progress_bar_style);
            mBackgroundColor = ta.getColor(R.styleable.battle_progress_bar_style_background_color,
                    getResources().getColor(R.color.bw_progress_background_color));
            mForegroundColor = ta.getColor(R.styleable.battle_progress_bar_style_foreground_color,
                    getResources().getColor(R.color.bw_progress_foreground_color));
            mDuration = ta.getInteger(R.styleable.battle_progress_bar_style_duration, ANIMATION_DURATION);
            mWidth = ta.getDimensionPixelOffset(R.styleable.battle_progress_bar_style_android_layout_width, 0);
            mHeight = ta.getDimensionPixelOffset(R.styleable.battle_progress_bar_style_android_layout_height, 0);

            ta.recycle();
        } else {
            mBackgroundColor = getResources().getColor(R.color.bw_progress_background_color);
            mForegroundColor = getResources().getColor(R.color.bw_progress_foreground_color);
            mDuration = ANIMATION_DURATION;
        }
        mProgress = 0;

        mContext = context;
        mBackRect = new RectF(0, 0, mWidth, mHeight);
        mForeRect = new RectF(0, 0, getRightEdge(mProgress), mHeight);
        mCornerRadius = mHeight / 2;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(mBackgroundColor);
        canvas.drawRoundRect(mBackRect, mCornerRadius, mCornerRadius, mPaint);
        mPaint.setColor(mForegroundColor);
        canvas.drawRoundRect(mForeRect, mCornerRadius, mCornerRadius, mPaint);
    }

    private int getRightEdge(int progress) {
        return progress * mWidth / ANIMATION_PROGRESS_MAX;
    }

    private void updateProgress(int progress) {
        mProgress = progress * 100 / ANIMATION_PROGRESS_MAX;
        float right = getRightEdge(progress);
        mForeRect.set(mForeRect.left, mForeRect.top, right, mForeRect.bottom);
        invalidate();
    }

    /**
     * 在指定时间范围内从当前进度跳转到指定进度
     *
     * @param duration
     * @param percentage
     */
    public void startProgress(long duration, int percentage) {
        if (percentage < mProgress) {
            mProgress = 0;
        }
        int max = percentage * ANIMATION_PROGRESS_MAX / 100;
        int start = mProgress * ANIMATION_PROGRESS_MAX / 100;
        if (mProgressAnimator == null) {
            mProgressAnimator = new MyAnimator(this);
            mProgressAnimator.setDuration(duration);
            mProgressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            mProgressAnimator.setIntValues(start, max);
            mProgressAnimator.addAnimatorUpdateListener();
        } else {
            mProgressAnimator.cancel();
            mProgressAnimator.setDuration(duration);
            mProgressAnimator.setIntValues(start, max);
        }
        mProgressAnimator.start();
    }

    /**
     * 进行到指定进度
     *
     * @param percentage
     */
    public void startProgress(int percentage) {
        startProgress(mDuration, percentage);
    }

    /**
     * 从0开始跳转到借宿位置
     */
    public void startProgress() {
        mProgress = 0;
        startProgress(mDuration, 100);
    }

    /**
     * 从当前位置执行完
     *
     * @param duration
     */
    public void continueProgress(long duration) {
        startProgress(duration, 100);
    }

    public BattleProgressBar setDuration(long duration) {
        this.mDuration = duration;
        return this;
    }

    private static class MyAnimator extends ValueAnimator {
        private WeakReference<BattleProgressBar> mReference;

        private AnimatorUpdateListener mAnimatorUpdateListener;

        public MyAnimator(BattleProgressBar progressBar) {
            mReference = new WeakReference<BattleProgressBar>(progressBar);
        }

        public void addAnimatorUpdateListener() {
            mAnimatorUpdateListener = new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    BattleProgressBar progressBar = mReference.get();
                    if (progressBar == null) {
                        cancel();
                    } else {
                        int animatedValue = (Integer) valueAnimator.getAnimatedValue();
                        progressBar.updateProgress(animatedValue);
                    }
                }
            };
            addUpdateListener(mAnimatorUpdateListener);
        }

    }
}
