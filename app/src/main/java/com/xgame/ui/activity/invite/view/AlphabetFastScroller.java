package com.xgame.ui.activity.invite.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.miui.zeus.utils.CollectionUtils;
import com.xgame.R;

/**
 * Created by Albert
 * on 18-1-31.
 */

public class AlphabetFastScroller extends View {

    private Object[] mSections;
    private Paint mPaint;
    private int color;
    private int selectedColor;
    private int x;
    private int yOffset;
    private int selectedSection = -1;

    private FastScrollListener mListener;

    public AlphabetFastScroller(Context context) {
        super(context);
        init(context);
    }

    public AlphabetFastScroller(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AlphabetFastScroller(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setFastScrollListener(FastScrollListener listener) {
        this.mListener = listener;
    }

    protected void init(Context context) {
        Resources res = context.getResources();
        color = res.getColor(R.color.default_hint_text_color);
        selectedColor = res.getColor(R.color.fast_scroll_selected_color);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(res.getDimensionPixelSize(R.dimen.sp_11));
        mPaint.setColor(color);
        mPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                int section = (int) (event.getY() / yOffset) - 1;
                changeSection(section, true);
                break;
        }
        return super.onTouchEvent(event);
    }

    public void changeSection(int section, boolean anim) {
        if (CollectionUtils.isEmpty(mSections) || section == selectedSection) {
            return;
        }
        selectedSection = Math.max(Math.min(section, mSections.length - 1), 0);
        invalidate();
        if (mListener != null && anim) {
            mListener.onSectionChanged(section);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawAlphabet(canvas);
    }

    protected void drawAlphabet(Canvas canvas) {
        if (mSections == null || mSections.length <= 0) {
            return;
        }
        x = getWidth() / 2;
        yOffset = getHeight() / (mSections.length + 1);
        for (int i = 0; i < mSections.length; i++) {
            mPaint.setColor(selectedSection == i ? selectedColor : color);
            int section = (int) mSections[i];
            char sectionChar = (char) section;
            canvas.drawText(("" + sectionChar).toUpperCase(), x, (i + 1) * yOffset, mPaint);
        }
    }

    public void setSections(Object[] sections) {
        mSections = sections;
    }

    public interface FastScrollListener {
        void onSectionChanged(int section);
    }
}
