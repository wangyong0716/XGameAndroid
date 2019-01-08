package com.xgame.common.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import java.io.InputStream;

import com.xgame.common.R;
import com.xgame.common.util.StreamHelper;

public class GifView extends ImageView {

    private Movie mMovie;
    private long mMovieStart;
    private boolean mActive = false;

    public GifView(Context context) {
        super(context);
    }

    public GifView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GifView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GifView);

        int resourceId = a.getResourceId(R.styleable.GifView_android_src, 0);
        if (resourceId != 0) {
            InputStream is = getResources().openRawResource(resourceId);
            mMovie = Movie.decodeStream(is);
            StreamHelper.closeSafe(is);
            if (mMovie != null) {
                // 需要关闭硬件加速，否则movie不能正常播放gif
                setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                startAnimation();
            }
        }

        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mMovie == null) {
            super.onDraw(canvas);
        } else {
            long now = SystemClock.uptimeMillis();
            if (mMovieStart == 0) {
                mMovieStart = now;
            }
            int duration = mMovie.duration();
            if (duration == 0) {
                duration = 1000;
            }
            int relTime = (int) ((now - mMovieStart) % duration);
            mMovie.setTime(relTime);
            mMovie.draw(canvas, 0, 0);

            if (mActive) {
                invalidate();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mMovie != null) {
            setMeasuredDimension(mMovie.width(), mMovie.height());
        }
    }

    public void startAnimation() {
        mActive = true;
        invalidate();
    }

    public void endAnimation() {
        mActive = false;
    }
}