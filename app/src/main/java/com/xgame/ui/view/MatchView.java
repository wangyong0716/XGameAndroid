package com.xgame.ui.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xgame.R;
import com.xgame.account.model.User;
import com.xgame.app.GlideApp;
import com.xgame.battle.model.Player;

/**
 * Created by wangyong on 18-1-25.
 */

public class MatchView extends RelativeLayout {
    private ImageView mAvatar1;
    private TextView mName1;
    private TextView mGenderAge1;

    private ImageView mAvatar2;
    private TextView mName2;
    private TextView mGenderAge2;

    private ImageView mVs;

    private TextView mScore1;
    private TextView mColon;
    private TextView mScore2;

    private MatchLoadingView mLoadingView;

    public MatchView(Context context) {
        super(context);
    }

    public MatchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MatchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mAvatar1 = findViewById(R.id.avatar_1);
        mName1 = findViewById(R.id.name_1);
        mGenderAge1 = findViewById(R.id.gender_age_1);

        mAvatar2 = findViewById(R.id.avatar_2);
        mName2 = findViewById(R.id.name_2);
        mGenderAge2 = findViewById(R.id.gender_age_2);

        mVs = findViewById(R.id.vs);
        mScore1 = findViewById(R.id.score_1);
        mColon = findViewById(R.id.colon);
        mScore2 = findViewById(R.id.score_2);

        mLoadingView = findViewById(R.id.loading_view);
    }

    public void setUserInfo(User user) {
        if (user == null) {
            return;
        }
        Resources res = getResources();
        mName1.setText(user.getNickname());
        StringBuilder stringBuilder = new StringBuilder();
        if (user.getSex() == User.GENDER_MALE) {
            stringBuilder.append(res.getString(R.string.male_text) + " ");
        } else if (user.getSex() == User.GENDER_FEMALE) {
            stringBuilder.append(res.getString(R.string.female_text) + " ");
        }
        if (user.getAge() >= 0) {
            stringBuilder.append(res.getString(R.string.match_age, user.getAge()));
        }
        if (stringBuilder.length() > 0) {
            mGenderAge1.setVisibility(VISIBLE);
            mGenderAge1.setText(stringBuilder);
        } else {
            mGenderAge1.setVisibility(GONE);
        }
        if (!isFinish()) {
            GlideApp.with(this).load(user.getHeadimgurl()).placeholder(R.drawable.default_avatar)
                    .into(mAvatar1);
        }
    }

    public void setUserInfo(Player player) {
        if (player == null) {
            return;
        }
        Resources res = getResources();
        mName1.setText(player.getName());
        StringBuilder stringBuilder = new StringBuilder();
        if (player.getGender() == User.GENDER_MALE) {
            stringBuilder.append(res.getString(R.string.male_text) + " ");
        } else if (player.getGender() == User.GENDER_FEMALE) {
            stringBuilder.append(res.getString(R.string.female_text) + " ");
        }
        if (player.getAge() >= 0) {
            stringBuilder.append(res.getString(R.string.match_age, player.getAge()));
        }
        if (stringBuilder.length() > 0) {
            mGenderAge1.setVisibility(VISIBLE);
            mGenderAge1.setText(stringBuilder);
        } else {
            mGenderAge1.setVisibility(GONE);
        }

        if (!isFinish()) {
            GlideApp.with(this).load(player.getAvatar()).placeholder(R.drawable.default_avatar)
                    .into(mAvatar1);
        }
    }

    public void setScore(int myScore, int peerScore) {
        mVs.setVisibility(GONE);
        mScore1.setVisibility(VISIBLE);
        mScore1.setText(String.valueOf(myScore));
        mColon.setVisibility(VISIBLE);
        mScore2.setVisibility(VISIBLE);
        mScore2.setText(String.valueOf(peerScore));
    }

    public void setMatchingStatus() {
        mName2.setText(R.string.match_on_matching);
        mGenderAge2.setVisibility(GONE);
    }

    public void setPeerInfo(Player player) {
        if (player == null) {
            return;
        }
        Resources res = getResources();
        mName2.setText(player.getName());
        StringBuilder stringBuilder = new StringBuilder();
        if (player.getGender() == User.GENDER_MALE) {
            stringBuilder.append(res.getString(R.string.male_text) + " ");
        } else if (player.getGender() == User.GENDER_FEMALE) {
            stringBuilder.append(res.getString(R.string.female_text) + " ");
        }
        if (player.getAge() >= 0) {
            stringBuilder.append(res.getString(R.string.match_age, player.getAge()));
        }
        if (stringBuilder.length() > 0) {
            mGenderAge2.setVisibility(VISIBLE);
            mGenderAge2.setText(stringBuilder);
        } else {
            mGenderAge2.setVisibility(GONE);
        }

        mLoadingView.setVisibility(GONE);
        mAvatar2.setVisibility(VISIBLE);
        if (!isFinish()) {
            GlideApp.with(this).load(player.getAvatar()).placeholder(R.drawable.default_avatar)
                    .into(mAvatar2);
        }
    }

    public void setLoadingMode() {
        mLoadingView.setVisibility(VISIBLE);
        mAvatar2.setVisibility(GONE);
    }

    private boolean isFinish() {
        if (getContext() != null && getContext() instanceof Activity) {
            return ((Activity) getContext()).isFinishing();
        }
        return true;
    }

    public View getAvatar1() {
        return mAvatar1;
    }

    public View getAvatar2() {
        return mAvatar2;
    }
}
