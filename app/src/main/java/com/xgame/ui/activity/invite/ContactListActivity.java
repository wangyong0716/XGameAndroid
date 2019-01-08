package com.xgame.ui.activity.invite;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.xgame.R;
import com.xgame.invite.model.InvitedUser;
import com.xgame.ui.activity.BaseActivity;
import com.xgame.ui.activity.invite.view.AlphabetFastScroller;
import com.xgame.ui.activity.invite.view.FastScrollRecyclerView;
import com.xgame.ui.adapter.ContactAdapter;

import java.util.ArrayList;

/**
 * Created by Albert
 * on 18-1-30.
 */

public class ContactListActivity extends BaseActivity {

    private ViewGroup mEmptyLayout;
    private FastScrollRecyclerView mRecyclerView;
    private AlphabetFastScroller mFastScroller;

    private ContactAdapter mAdapter;

    private View.OnClickListener mClickReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        initViews();
        onCreateToolbar();
        onCreateData();
    }

    protected void initViews() {
        initClicker();
        mEmptyLayout = findViewById(R.id.empty_layout);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setEmptyView(mEmptyLayout);
        mFastScroller = findViewById(R.id.fast_scroller);
        mFastScroller.setOnClickListener(mClickReceiver);
        mFastScroller.setFastScrollListener(mRecyclerView);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (mAdapter == null || mAdapter.getItemCount() <= 0) {
                    return;
                }
                View childView = recyclerView.getChildAt(0);
                if (childView == null) {
                    return;
                }
                int position = recyclerView.getChildAdapterPosition(childView);
                int section = mAdapter.getSectionForPosition(position);
                mFastScroller.changeSection(section, false);
            }
        });
    }

    protected void initClicker() {
        mClickReceiver = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };
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

    protected void onCreateData() {
        mAdapter = new ContactAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAdapter.setItemAsync(parseDataFromIntent(), new Runnable() {
            @Override
            public void run() {
                mFastScroller.setSections(mAdapter.getSections());
            }
        });
    }

    protected ArrayList<InvitedUser> parseDataFromIntent() {
        return (ArrayList<InvitedUser>) getIntent().getSerializableExtra("contacts");
    }
}
