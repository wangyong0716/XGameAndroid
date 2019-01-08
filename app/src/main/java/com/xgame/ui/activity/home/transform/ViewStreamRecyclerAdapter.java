package com.xgame.ui.activity.home.transform;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-30.
 */


public class ViewStreamRecyclerAdapter extends RecyclerView.Adapter<ViewHolderWrapper> {

    private final ViewStream mStream;

    public ViewStreamRecyclerAdapter(ViewStream stream) {
        this.mStream = stream;
    }

    @Override
    public ViewHolderWrapper onCreateViewHolder(ViewGroup parent, int viewType) {
        return HolderRegister.get().create(parent, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolderWrapper holder, int position) {
        holder.onBind(mStream.seek(position).map());
    }

    @Override
    public void onBindViewHolder(ViewHolderWrapper holder, int position, List payloads) {
        holder.onBind(mStream.seek(position).map(), payloads);
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mStream.size()) {
            return mStream.seek(position).map().viewType();
        }
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return mStream.size();
    }
}
