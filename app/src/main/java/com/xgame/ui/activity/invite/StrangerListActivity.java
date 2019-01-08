package com.xgame.ui.activity.invite;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.miui.zeus.utils.CollectionUtils;
import com.xgame.R;
import com.xgame.base.ServiceFactory;
import com.xgame.common.api.OnCallback;
import com.xgame.home.model.MessageSession;
import com.xgame.invite.model.InvitedUser;
import com.xgame.ui.activity.BaseActivity;
import com.xgame.ui.activity.home.view.AbsRefreshLoadLayout;
import com.xgame.ui.activity.home.view.RefreshLoadLayout;
import com.xgame.ui.activity.home.view.RefreshLoadWrapper;
import com.xgame.ui.activity.invite.view.ObservedRecyclerView;
import com.xgame.ui.adapter.StrangerAdapter;

import java.util.List;

/**
 * Created by Albert
 * on 18-1-28.
 */

public class StrangerListActivity extends BaseActivity {

    private RefreshLoadWrapper mRefreshLoadWrapper;
    private ViewGroup mEmptyLayout;
    private ObservedRecyclerView mRecyclerView;
    private StrangerAdapter mListAdapter;

    private long mTimePair[];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stranger_list);

        initViews();
        onCreateToolbar();
        onCreateData();
    }

    private void initViews() {
        mEmptyLayout = findViewById(R.id.empty_layout);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setEmptyView(mEmptyLayout);
        RefreshLoadLayout loadLayout = findViewById(R.id.refresh_load_layout);
        mRefreshLoadWrapper = new RefreshLoadWrapper(loadLayout, mRecyclerView);
        mRefreshLoadWrapper.setRefreshLoadListener(new AbsRefreshLoadLayout.RefreshLoadListener() {
            @Override
            public void onRefresh() {
                requestStrangers(true, false);
            }

            @Override
            public void onLoad() {
                requestStrangers(false, false);
            }
        });
    }

    protected void onCreateToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getTitle());
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void onCreateData() {
        mRecyclerView.setAdapter(getAdapter());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        requestStrangers(true, true);
    }

    private void onLoadUsers(List<InvitedUser> users, boolean refresh) {
        mListAdapter.addItems(users, refresh);
        if (refresh) {
            mRefreshLoadWrapper.setRefreshing(false);
        } else {
            mRefreshLoadWrapper.setLoading(false);
        }
    }

    private void onLoadRecords(List<MessageSession> records, boolean refresh) {
        mListAdapter.addItemAsync(records, refresh);
        if (refresh) {
            mRefreshLoadWrapper.setRefreshing(false);
        } else {
            mRefreshLoadWrapper.setLoading(false);
        }
    }

    private StrangerAdapter getAdapter() {
        if (mListAdapter == null) {
            mListAdapter = new StrangerAdapter(this);
        }
        return mListAdapter;
    }

    private void handleTimePair(List<MessageSession> result, boolean refresh) {
        if (CollectionUtils.isEmpty(result)) {
            return;
        }
        if (refresh) {
            mTimePair[1] = result.get(0).createTime;
        } else {
            mTimePair[0] = result.get(result.size() - 1).createTime;
        }
    }

    private void requestStrangers(final boolean refresh, boolean showLoading) {
        if (showLoading) {
            mRefreshLoadWrapper.setRefreshing(true);
        }
        if (mTimePair == null) {
            mTimePair = new long[2];
        }
        long endTime = refresh ? 0 : mTimePair[0];
        long startTime = refresh ? mTimePair[1] : 0;
        ServiceFactory.homeService().loadRecordHistory(startTime, endTime, false)
                .enqueue(new OnCallback<List<MessageSession>>() {
                    @Override
                    public void onResponse(List<MessageSession> result) {
                        handleTimePair(result, refresh);
                        onLoadRecords(result, refresh);
                    }

                    @Override
                    public void onFailure(List<MessageSession> result) {
                        mRefreshLoadWrapper.setRefreshing(false);
                    }
                });
    }
}
