package com.xgame.ui.activity.invite;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.miui.zeus.utils.CollectionUtils;
import com.xgame.R;
import com.xgame.app.GlideApp;
import com.xgame.base.GameProvider;
import com.xgame.base.ServiceFactory;
import com.xgame.common.api.OnCallback;
import com.xgame.common.util.ExecutorHelper;
import com.xgame.common.util.ScreenUtil;
import com.xgame.invite.InviteHelper;
import com.xgame.invite.api.FriendManager;
import com.xgame.invite.model.FriendRelation;
import com.xgame.invite.model.GameInfo;
import com.xgame.invite.model.InvitedUser;
import com.xgame.ui.activity.BaseActivity;
import com.xgame.ui.activity.home.view.RefreshLoadLayout;
import com.xgame.ui.activity.invite.view.FlowLayout;
import com.xgame.ui.activity.invite.view.RelationButton;
import com.xgame.ui.activity.personal.view.RefreshLayout;
import com.xgame.util.StringUtil;

import java.util.Map;

/**
 * Created by Albert
 * on 18-1-28.
 */

public class StrangerDetailActivity extends BaseActivity {

    private Toolbar mToolbar;


    private RefreshLayout mRefreshLayout;
    private ScrollView mScrollView;

    private ImageView mAvatar;
    private TextView mNameView;
    private TextView mMessageView;
    private FlowLayout mGameLayout;
    private RelationButton mBottomBtn;

    private String mAccountId;
    private InvitedUser mUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stranger_detail);

        parseIntent();
        initViews();
        onCreateToolbar();
        onCreateData();
    }

    protected void parseIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        // Parse intent user.
        mUser = (InvitedUser) intent.getSerializableExtra("user");
        if (mUser == null) {
            mAccountId = intent.getStringExtra("accountId");
        } else {
            mAccountId = mUser.getAccountId();
        }
        if (StringUtil.isEmpty(mAccountId)) {
            finish();
        }
        // Parse intent title.
        String title = intent.getStringExtra("title");
        if (!StringUtil.isEmpty(title)) {
            setTitle(title);
        }
    }

    private void initRefreshLayout() {
        mScrollView = findViewById(R.id.scroll_view);
        mRefreshLayout = findViewById(R.id.refresh_layout);
        mRefreshLayout.setRefreshLoadListener(new RefreshLoadLayout.RefreshCallbacks() {
            @Override
            public void onRefresh() {
                requestUser(mAccountId);
            }
        });
        mRefreshLayout.setChecker(new RefreshLayout.IRefreshChecker() {
            @Override
            public boolean canRefresh() {
                return mScrollView.getScrollY() == 0;
            }
        });
    }

    private void initViews() {
        // Refresh layout.
        initRefreshLayout();
        // Other views.
        final int sideMargin = getResources().getDimensionPixelSize(R.dimen.dp_12);
        mAvatar = findViewById(R.id.avatar);
        mAvatar.getLayoutParams().height = ScreenUtil.getScreenWidth(this);
        mAvatar.setImageResource(R.color.default_line_color);
        mNameView = findViewById(R.id.nick_name);
        mMessageView = findViewById(R.id.message);
        mGameLayout = findViewById(R.id.game_layout);
        mBottomBtn = findViewById(R.id.bottom_btn);
    }

    private void onCreateToolbar() {
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle(getTitle());
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void onCreateData() {
        if (mUser != null) {
            onLoadData(mUser);
        }
        requestUser(mAccountId);
    }

    private String getMessageStr(InvitedUser invitedUser) {
        String message = invitedUser.getGenderString(this) + " · "
                + invitedUser.getAgeString(this);
        String suffix = invitedUser.getConstellation();
        String location = invitedUser.getLocation();
        if (!StringUtil.isEmpty(location)) {
            suffix += (StringUtil.isEmpty(suffix) ? location : " | " + location);
        }
        if (!StringUtil.isEmpty(suffix)) {
            message += (" · " + suffix);
        }
        return message;
    }

    private void onLoadData(final InvitedUser invitedUser) {
        if (isDestroyed()) {
            return;
        }
        // Title.
        mToolbar.setTitle(getTitle());
        // Avatar view.
        if (StringUtil.isEmpty(invitedUser.getAvatar())) {
            mAvatar.setImageResource(R.drawable.default_avatar);
        } else {
            GlideApp.with(this).load(invitedUser.getAvatar()).into(mAvatar);
        }
        mNameView.setText(invitedUser.getNickname());
        mMessageView.setText(getMessageStr(invitedUser));
        //Game layout.
        loadGames(invitedUser.getFreqPlay());
        //Bottom btn.
        if (InviteHelper.isAccountUser(invitedUser.getAccountId())) {
            mBottomBtn.setText(R.string.myself);
            mBottomBtn.setOnClickListener(null);
        } else {
            mBottomBtn.setText(invitedUser.getRelationString(this));
            mBottomBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeRelation(invitedUser);
                }
            });
        }
    }

    private void loadGames(final GameInfo[] games) {
        mGameLayout.removeAllViews();
        if (CollectionUtils.isEmpty(games)) {
            return;
        }
        ExecutorHelper.runInBackground(new Runnable() {
            @Override
            public void run() {
                updateGameLayout(games, GameProvider.get().loadData());
            }
        });
    }

    private void updateGameLayout(final GameInfo[] games, final Map<String, GameProvider.GameProfile> gameMap) {
        ExecutorHelper.runInUIThread(new Runnable() {
            @Override
            public void run() {
                if (isDestroyed()) {
                    return;
                }
                for (GameInfo game : games) {
                    View gameView = View.inflate(StrangerDetailActivity.this, R.layout.layout_game_item, null);
                    ImageView iconView = gameView.findViewById(R.id.game_icon);
                    GameProvider.GameProfile gameProfile = gameMap.get(game.getId());
                    if (gameProfile == null) {
                        iconView.setImageResource(R.drawable.round_game_rectangle_background);
                    } else {
                        Drawable errorDrawable = getResources().getDrawable(R.drawable.round_game_rectangle_background);
                        GlideApp.with(StrangerDetailActivity.this).load(gameProfile.icon).error(errorDrawable).into(iconView);
                    }
                    TextView nameView = gameView.findViewById(R.id.game_name);
                    nameView.setText(game.getName());
                    mGameLayout.addView(gameView);
                }
            }
        });
    }

    private void changeRelation(final InvitedUser invitedUser) {
        if (invitedUser == null) {
            return;
        }
        FriendManager.FriendLoader loader = new FriendManager.FriendLoader() {
            @Override
            public void onLoading() {
                mBottomBtn.showLoading();
            }

            @Override
            public void onRelationChanged(FriendRelation relation) {
                invitedUser.setRelative(relation.getStatus());
                mBottomBtn.setText(invitedUser.getRelationString(StrangerDetailActivity.this));
            }

            @Override
            public void onRelationFailed(FriendRelation relation) {
                mBottomBtn.showText();
            }
        };
        if (invitedUser.isStranger()) {
            FriendManager.add(invitedUser.getAccountId(), loader);
        } else if (invitedUser.isWaitConfirm()) {
            FriendManager.accept(invitedUser.getAccountId(), loader);
        }
    }

    private void requestUser(String accountId) {
        ServiceFactory.inviteService().getUserDetail(accountId).enqueue(new OnCallback<InvitedUser>() {
            @Override
            public void onResponse(InvitedUser result) {
                onLoadData(result);
                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(InvitedUser result) {
                mRefreshLayout.setRefreshing(false);
            }
        });
    }
}
