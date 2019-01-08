package com.xgame.ui.activity.invite.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by Albert
 * on 18-2-3.
 */

public class ObservedListView extends ListView {

    private View mEmptyView;
    private final DataSetObserver mDataObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            checkIfEmpty();
        }

        @Override
        public void onInvalidated() {
            checkIfEmpty();
        }
    };

    public ObservedListView(Context context) {
        super(context);
    }

    public ObservedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ObservedListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        final ListAdapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterDataSetObserver(mDataObserver);
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerDataSetObserver(mDataObserver);
        }
        checkIfEmpty();
    }

    public void setEmptyView(View emptyView) {
        this.mEmptyView = emptyView;
        checkIfEmpty();
    }

    private void checkIfEmpty() {
        if (mEmptyView == null || getAdapter() == null) {
            return;
        }
        final boolean isEmpty = getAdapter().isEmpty();
        mEmptyView.setVisibility(isEmpty ? VISIBLE : GONE);
    }
}
