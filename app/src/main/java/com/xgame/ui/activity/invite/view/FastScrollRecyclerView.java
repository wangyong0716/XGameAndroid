package com.xgame.ui.activity.invite.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.SectionIndexer;

/**
 * Created by Albert
 * on 18-1-31.
 */

public class FastScrollRecyclerView extends ObservedRecyclerView implements AlphabetFastScroller.FastScrollListener {

    public FastScrollRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public FastScrollRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FastScrollRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    protected void init(Context context) {

    }

    @Override
    public void onSectionChanged(int section) {
        Adapter adapter = getAdapter();
        if (adapter != null && adapter instanceof SectionIndexer) {
            SectionIndexer indexer = (SectionIndexer) adapter;
            int pos = indexer.getPositionForSection(section);
            if (pos >= 0 && pos < adapter.getItemCount()) {
                scrollToPosition(pos);
            }
        }
    }
}
