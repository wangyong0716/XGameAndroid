package com.xgame.ui.activity.personal.view;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import com.xgame.R;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by dingning1
 * on 18-1-31.
 */


public class ItemAvatarViewHolder extends ItemViewHolder {

    @BindView(R.id.info_container)
    ViewGroup mContainer;

    public CircleImageView avatar;

    public ItemAvatarViewHolder(View view) {
        super(view);
        info.setVisibility(View.GONE);
        iconNew.setVisibility(View.GONE);
        avatar = new CircleImageView(view.getContext());
        mContainer.addView(avatar);
    }

    public ItemAvatarViewHolder setIcon(Drawable img) {
        avatar.setImageDrawable(img);
        return this;
    }

    public ItemAvatarViewHolder setIcon(int imgResId) {
        avatar.setImageResource(imgResId);
        return this;
    }
}
