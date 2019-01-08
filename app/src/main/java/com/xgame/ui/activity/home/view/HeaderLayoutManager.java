package com.xgame.ui.activity.home.view;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-30.
 */


public class HeaderLayoutManager extends GridLayoutManager {

    private Profile mHeaderProfile;

    private Profile mFooterProfile;

    public HeaderLayoutManager(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public HeaderLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public HeaderLayoutManager(Context context, int spanCount, int orientation,
            boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    private static int calcSpanSize(Profile profile, int position) {
        return calcSpanSize(profile, position, 0);
    }

    private static int calcSpanSize(Profile profile, final int position, int offset) {
        if (profile == null) {
            return -1;
        }
        for (int i = position; i < profile.headerCount() + offset; i++) {
            int span = profile.headerSpanSize(i);
            if (span > 0) {
                return span;
            }
        }
        return -1;
    }

    @Override
    public void setSpanSizeLookup(SpanSizeLookup lookup) {
        if (lookup != null) {
            SpanSizeLookup old = super.getSpanSizeLookup();
            if (old != null && lookup instanceof SpanSizeLookupProxy) {
                ((SpanSizeLookupProxy) super.getSpanSizeLookup()).nLookup = lookup;
            } else {
                super.setSpanSizeLookup(new SpanSizeLookupProxy(lookup));
            }
        } else {
            super.setSpanSizeLookup(new SpanSizeLookupProxy(null));
        }
    }

    public void setHeaderProfile(Profile profile) {
        this.mHeaderProfile = profile;
        setSpanSizeLookup(getSpanSizeLookup());
    }

    @Deprecated
    public void setFooterProfile(Profile profile) {
        this.mFooterProfile = profile;
    }

    public interface Profile {

        int headerSpanSize(int position);

        int headerCount();

        int contentSpanSize();

    }

    private class SpanSizeLookupProxy extends SpanSizeLookup {

        private volatile SpanSizeLookup nLookup;

        SpanSizeLookupProxy(SpanSizeLookup lookup) {
            this.nLookup = lookup;
        }

        @Override
        public int getSpanSize(int position) {
            int headerSpan;
            if ((headerSpan = calcSpanSize(mHeaderProfile, position)) > 0) {
                return headerSpan;
            }
            if (mHeaderProfile != null) {
                return mHeaderProfile.contentSpanSize();
            }
            // TODO: 18-1-29 cale footer offset
            if (nLookup != null) {
                return nLookup instanceof DefaultSpanSizeLookup ?
                        getSpanCount() : nLookup.getSpanSize(position);
            } else {
                return getSpanCount();
            }
        }
    }
}
