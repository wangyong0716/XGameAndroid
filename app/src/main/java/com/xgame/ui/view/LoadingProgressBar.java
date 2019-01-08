package com.xgame.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.xgame.R;

/**
 * Created by jiangjianhe on 1/30/18.
 */

public class LoadingProgressBar extends ProgressBar {

    private Rect mRect;
    private Paint mPaint = new Paint();
    private String mProgressText;

    public LoadingProgressBar(Context context) {
        this(context, null);
    }

    public LoadingProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressWarnings("ResourceType")
    public LoadingProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mRect = new Rect();
        mPaint = new Paint();
        int textColor = Color.WHITE;
        float textSize = 30.0f;
        Drawable indeterminateDrawable = null;
        if (attrs != null) {
            int[] set = {
                    android.R.attr.textSize,
                    android.R.attr.textColor,
                    android.R.attr.indeterminateDrawable
            };
            TypedArray a = context.obtainStyledAttributes(attrs, set);
            try {
                textSize = a.getDimensionPixelSize(0, 0);
                textColor = a.getColor(1, Color.WHITE);
                indeterminateDrawable = a.getDrawable(2);
            } finally {
                a.recycle();
            }
        }
        mPaint.setColor(textColor);
        mPaint.setTextSize(textSize);
        if (indeterminateDrawable == null) {
            indeterminateDrawable = getResources().getDrawable(R.drawable.exo_player_loading);
        }
        setIndeterminateDrawable(indeterminateDrawable);
        setIndeterminate(true);
    }

    @Override
    public synchronized void setProgress(int progress) {
        mProgressText = progress + "%";
        super.setProgress(progress);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mProgressText != null) {
            this.mPaint.getTextBounds(mProgressText, 0, mProgressText.length(), mRect);
            int x = (getWidth() / 2) - mRect.centerX();
            int y = (getHeight() / 2) - mRect.centerY();
            canvas.drawText(mProgressText, x, y, this.mPaint);
        }
    }
}
