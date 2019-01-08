package com.xgame.chat.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.xgame.R;

public class RoundCornerFrameLayout extends FrameLayout {
    private Path mOutlinePath = new Path();
    private RectF mRectF = new RectF();
    private float mRadius;

    public RoundCornerFrameLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public RoundCornerFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mRadius = getResources().getDimension(R.dimen.chat_msg_roundcorner_radius);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mRectF.set(0, 0, right - left, bottom - top);
        mOutlinePath.reset();
        mOutlinePath.addRoundRect(mRectF, mRadius, mRadius, Path.Direction.CW);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.clipPath(mOutlinePath);
        super.dispatchDraw(canvas);
        canvas.restore();
    }
}
