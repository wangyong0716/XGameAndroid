package com.xgame.ui.activity.personal.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xgame.R;
import com.xgame.common.util.LaunchUtils;
import com.xgame.personal.model.PersonalInfoModel;
import com.xgame.personal.model.PersonalMenuItem;
import com.xgame.ui.activity.CommonWebViewActivity;
import com.xgame.util.UrlUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by dingning1
 * on 18-1-30.
 */
public class ItemViewHolder {

    @BindView(R.id.name)
    public TextView name;

    @BindView(R.id.info)
    public TextView info;

    @BindView(R.id.icon_new)
    public ImageView iconNew;

    public View itemView;
    public PersonalMenuItem item;

    private String mAction;
    private View.OnClickListener mListener;

    private View.OnClickListener mListenerWrapper = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showPromptIcon(false);
            Context ctx = itemView.getContext();
            if (item != null) {
                PersonalInfoModel.setPointClickState(ctx, item.status, item.state);
            }
            if (mListener != null) {
                mListener.onClick(v);
            } else if (!TextUtils.isEmpty(mAction)) {
                if (mAction.startsWith("http")) {
                    String url = UrlUtils.getRequestUrl(mAction);
                    CommonWebViewActivity.startWeb(ctx, url, ctx.getString(R.string.personal_item_faq));
                } else {
                    LaunchUtils.startActivity(ctx, mAction);
                }
            }
        }
    };

    public ItemViewHolder(View view) {
        itemView = view;
        ButterKnife.bind(this, view);
    }

    public ItemViewHolder setAction(final String action) {
        if (!TextUtils.isEmpty(action)) {
            mAction = action;
            itemView.setOnClickListener(mListenerWrapper);
        }
        return this;
    }

    public ItemViewHolder setAction(final View.OnClickListener listener) {
        if (listener != null) {
            mListener = listener;
            itemView.setOnClickListener(mListenerWrapper);
        }
        return this;
    }

    public ItemViewHolder setName(int nameId) {
        name.setText(nameId);
        return this;
    }

    public ItemViewHolder setInfo(String infoStr) {
        info.setVisibility(TextUtils.isEmpty(infoStr) ? View.GONE : View.VISIBLE);
        info.setText(infoStr);
        return this;
    }

    public ItemViewHolder showPromptIcon(boolean visible) {
        if (iconNew != null) {
            iconNew.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
        return this;
    }
}
