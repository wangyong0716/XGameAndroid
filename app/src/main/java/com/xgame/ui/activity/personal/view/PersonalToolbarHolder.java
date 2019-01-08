package com.xgame.ui.activity.personal.view;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xgame.R;
import com.xgame.common.util.UiUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by dingning1
 * on 18-1-30.
 */


public class PersonalToolbarHolder {

    @BindView(R.id.title_container) View mTitleContainer;
    @BindView(R.id.icon_back) ImageView mIconBack;
    @BindView(R.id.title) TextView mTitle;
    @BindView(R.id.tool_icon) ImageView mToolIcon;
    @BindView(R.id.divider) View mDivider;

    private View mToolbar;

    public PersonalToolbarHolder(View view) {
        mToolbar = view;
        ButterKnife.bind(this, view);
    }

    public PersonalToolbarHolder setBackIcon(int resId, View.OnClickListener clickListener) {
        mIconBack.setImageResource(resId);
        mTitleContainer.setOnClickListener(clickListener);
        return this;
    }

    public PersonalToolbarHolder setToolIcon(int resId, View.OnClickListener clickListener) {
        mToolIcon.setImageResource(resId);
        mToolIcon.setOnClickListener(clickListener);
        UiUtil.expandHitArea(mToolIcon);
        return this;
    }

    public PersonalToolbarHolder setTitle(int resId, int colorResId) {
        mTitle.setText(resId);
        if (colorResId > 0) {
            mTitle.setTextColor(mTitle.getResources().getColor(colorResId));
        }
        return this;
    }

    public PersonalToolbarHolder enableDivider(boolean enable) {
        mDivider.setVisibility(enable ? View.VISIBLE : View.GONE);
        return this;
    }

    public PersonalToolbarHolder setBackground(int colorResId) {
        if (colorResId > 0) {
            mToolbar.setBackgroundColor(mToolbar.getResources().getColor(colorResId));
        } else {
            mToolbar.setBackground(null);
        }
        return this;
    }
}
