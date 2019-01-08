package com.xgame.ui.activity.invite.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xgame.R;

/**
 * Created by Albert
 * on 18-2-5.
 */

public class RelationButton extends FrameLayout {

    private TextView mRelationView;
    private ProgressBar mLoadingView;

    public RelationButton(Context context) {
        super(context);
        init(context, null);
    }

    public RelationButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RelationButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View internal = inflate(context, R.layout.layout_relation_btn, this);
        mRelationView = internal.findViewById(R.id.relation);
        mLoadingView = internal.findViewById(R.id.loading);
        parseAttrs(context, attrs);
    }

    private void parseAttrs(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RelationButton);
        int textColor = ta.getColor(R.styleable.RelationButton_textColor, getResources().getColor(R.color.default_hint_text_color));
        mRelationView.setTextColor(textColor);
        int textSize = ta.getDimensionPixelSize(R.styleable.RelationButton_textSize, getResources().getDimensionPixelSize(R.dimen.sp_12));
        mRelationView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        boolean textBold = ta.getBoolean(R.styleable.RelationButton_textBold, false);
        mRelationView.getPaint().setFakeBoldText(textBold);
        boolean textGravityCenter = ta.getBoolean(R.styleable.RelationButton_textGravityCenter, true);
        if (textGravityCenter) {
            FrameLayout.LayoutParams params = (LayoutParams) mRelationView.getLayoutParams();
            params.gravity = Gravity.CENTER;
            params = (LayoutParams) mLoadingView.getLayoutParams();
            params.gravity = Gravity.CENTER;
        }
        ta.recycle();
    }

    public void showLoading() {
        mLoadingView.setVisibility(VISIBLE);
        mRelationView.setVisibility(INVISIBLE);
    }

    public void showText() {
        mRelationView.setVisibility(VISIBLE);
        mLoadingView.setVisibility(GONE);
    }

    public void setText(String text) {
        mRelationView.setText(text);
        showText();
    }

    public void setText(int textResId) {
        mRelationView.setText(textResId);
        showText();
    }

    public void setText(String text, int textColorRes) {
        mRelationView.setText(text);
        mRelationView.setTextColor(getResources().getColor(textColorRes));
        showText();
    }

    public void setText(int textResId, int textColorRes) {
        mRelationView.setText(textResId);
        mRelationView.setTextColor(getResources().getColor(textColorRes));
        showText();
    }

    public void setTextColor(int textColor) {
        mRelationView.setTextColor(textColor);
    }
}
