package com.xgame.ui.activity.invite;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.miui.zeus.utils.CollectionUtils;
import com.xgame.R;
import com.xgame.account.UserManager;
import com.xgame.base.ServiceFactory;
import com.xgame.common.api.OnCallback;
import com.xgame.common.util.ExecutorHelper;
import com.xgame.common.util.LogUtil;
import com.xgame.common.util.ToastUtil;
import com.xgame.invite.InviteHelper;
import com.xgame.invite.model.InvitedUser;
import com.xgame.social.share.SharePlatform;
import com.xgame.ui.activity.BaseActivity;
import com.xgame.ui.activity.home.view.RefreshLoadLayout;
import com.xgame.ui.activity.invite.util.ShareInvoker;
import com.xgame.ui.activity.invite.view.ObservedListView;
import com.xgame.ui.activity.personal.view.RefreshLayout;
import com.xgame.ui.adapter.UserFriendAdapter;
import com.xgame.util.Analytics;
import com.xgame.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Albert
 * on 18-1-28.
 */

public class InviteListActivity extends BaseActivity implements InviteHelper.InviteListener, ShareInvoker.ShareListener {

    private static final String TAG = "InviteListActivity";

    private static final int REQUEST_CODE_READ_PERMISSION = 4001;

    private TextView mSearchEditor;
    private ViewGroup mMatchLayout;
    private TextView mMatchTips;

    private ViewGroup mEmptyLayout;

    private RefreshLayout mRefreshLayout;

    private ObservedListView mListView;
    private UserFriendAdapter mListAdapter;

    private List<InvitedUser> mContactsMatched;

    private InviteHelper mInviteHelper = new InviteHelper();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_invite_list);

        initViews();
        onCreateToolbar();
        onCreateData();

        systemConfig();
    }

    private void initRefreshLayout() {
        mRefreshLayout = findViewById(R.id.refresh_layout);
        mRefreshLayout.setRefreshLoadListener(new RefreshLoadLayout.RefreshCallbacks() {
            @Override
            public void onRefresh() {
                requestFriends(false);
            }
        });
        mRefreshLayout.setChecker(new RefreshLayout.IRefreshChecker() {
            @Override
            public boolean canRefresh() {
                return mListView.getScrollY() == 0;
            }
        });
    }

    protected void systemConfig() {
        setupHelper();
        requestContactPermission();
    }

    private void setupHelper() {
        mInviteHelper.setup(this, this, this);
        mInviteHelper.setShareView(findViewById(R.id.share_layout));
    }

    private void requestContactPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            matchContacts();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE_READ_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length <= 0) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_READ_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    matchContacts();
                }
                break;
        }
    }

    private void initViews() {
        // Refresh layout.
        initRefreshLayout();
        // List view.
        mListView = findViewById(R.id.list_view);
        View listHeader = View.inflate(this, R.layout.header_invite, null);

        mEmptyLayout = listHeader.findViewById(R.id.empty_layout);
        mListView.setEmptyView(mEmptyLayout);

        initSearchEditor(listHeader);

        TextView tips = listHeader.findViewById(R.id.tips);
        tips.setText(getResources().getString(R.string.invite_tips, UserManager.getInstance().getUserId()));
        mMatchLayout = listHeader.findViewById(R.id.match_layout);
        mMatchTips = mMatchLayout.findViewById(R.id.match_result);
        mMatchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InviteListActivity.this, ContactListActivity.class);
                intent.putExtra("contacts", (ArrayList) mContactsMatched);
                startActivity(intent);
                onMatchFinished(false);
            }
        });

        mListView.addHeaderView(listHeader, null, false);
    }

    private void initSearchEditor(View listHeader) {
        mSearchEditor = listHeader.findViewById(R.id.search_editor);
        mSearchEditor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(InviteListActivity.this, SearchActivity.class));
            }
        });
    }

    public void onInviteThirdUser(View v) {
        mInviteHelper.onInviteThirdUser(v);
    }

    private void onCreateToolbar() {
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
        mListView.setAdapter(getAdapter());
        requestFriends(true);
    }

    private void onLoadData(List<InvitedUser> users) {
        mListAdapter.setItem(users);
    }

    private void matchContacts() {
        mMatchTips.setText(R.string.contacts_matching);
        mMatchLayout.setClickable(false);
        mInviteHelper.startMatch(this);
        LogUtil.d(TAG, "startMatch");
    }

    @Override
    public void onMatched(List<InvitedUser> matched, Map<String, InvitedUser> all) {
        LogUtil.d(TAG, "onMatched");
        mContactsMatched = matched;
        matchUpdated(CollectionUtils.getSize(matched));
    }

    @Override
    public void onMatchFailed(Map<String, InvitedUser> all) {
        LogUtil.d(TAG, "onMatchFailed");
        matchUpdated(0);
        onMatchFinished(false);
    }

    @Override
    public void onMatchFinished(final boolean updated) {
        LogUtil.d(TAG, "onMatchFinished");
        ExecutorHelper.runInUIThread(new Runnable() {
            @Override
            public void run() {
                mMatchLayout.setClickable(true);
                mMatchTips.setCompoundDrawablesWithIntrinsicBounds(0, 0, updated ? R.drawable.red_point : 0, 0);
            }
        });
    }

    private void matchUpdated(int matchCount) {
        mMatchTips.setText(getResources().getString(R.string.match_result, matchCount));
    }

    private UserFriendAdapter getAdapter() {
        if (mListAdapter == null) {
            mListAdapter = new UserFriendAdapter(this);
        }
        return mListAdapter;
    }

    private boolean searchUser(final String keyword) {
        if (StringUtil.isEmpty(keyword)) {
            return false;
        }
        ServiceFactory.inviteService().searchUser(keyword).enqueue(new OnCallback<InvitedUser>() {
            @Override
            public void onResponse(InvitedUser result) {
                mSearchEditor.setText("");
                if (result == null) {
                    return;
                }
                Intent intent = new Intent(InviteListActivity.this, StrangerDetailActivity.class);
                intent.putExtra("title", getString(R.string.search_title));
                intent.putExtra("user", result);
                startActivity(intent);
            }

            @Override
            public void onFailure(InvitedUser result) {
                ToastUtil.showToast(InviteListActivity.this, getString(R.string.no_search_result, keyword), false);
            }
        });
        return true;
    }

    private void requestFriends(boolean showLoading) {
        if (showLoading) {
            mRefreshLayout.setRefreshing(true);
        }
        ServiceFactory.inviteService().getUseFriends().enqueue(new OnCallback<List<InvitedUser>>() {
            @Override
            public void onResponse(List<InvitedUser> user) {
                onLoadData(user);
                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(List<InvitedUser> user) {
                mRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mInviteHelper.release();
    }

    @Override
    public void onShareProceed(int platform) {
        if (platform == SharePlatform.WX) {
            //添加好友页渠道分享点击 微信好友
            Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_SHARE, Analytics.Constans.STOCK_ID_SHARE_WX,
                    Analytics.Constans.STOCK_NAME_SHARE_WX, Analytics.Constans.STOCK_TYPE_BTN,
                    Analytics.Constans.PAGE_ADD_FRIENDS, Analytics.Constans.SECTION_INVITE_FRIENDS, null);
        } else if (platform == SharePlatform.WX_TIMELINE) {
            //添加好友页渠道分享点击 微信朋友圈
            Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_SHARE, Analytics.Constans.STOCK_ID_SHARE_WX_TIMELINE,
                    Analytics.Constans.STOCK_NAME_SHARE_WX_TIMELINE, Analytics.Constans.STOCK_TYPE_BTN,
                    Analytics.Constans.PAGE_ADD_FRIENDS, Analytics.Constans.SECTION_INVITE_FRIENDS, null);
        } else if (platform == SharePlatform.QQ) {
            //添加好友页渠道分享点击 QQ好友
            Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_SHARE, Analytics.Constans.STOCK_ID_SHARE_QQ,
                    Analytics.Constans.STOCK_NAME_SHARE_QQ, Analytics.Constans.STOCK_TYPE_BTN,
                    Analytics.Constans.PAGE_ADD_FRIENDS, Analytics.Constans.SECTION_INVITE_FRIENDS, null);
        }
    }

    @Override
    public void onShareFailed(int type, String error) {

    }
}
