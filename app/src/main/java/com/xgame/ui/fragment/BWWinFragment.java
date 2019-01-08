package com.xgame.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.xgame.R;
import com.xgame.account.UserManager;
import com.xgame.account.model.User;
import com.xgame.app.GlideApp;
import com.xgame.battle.BWBattleManager;
import com.xgame.battle.BattleConstants;
import com.xgame.battle.model.BWBattleBonusResult;
import com.xgame.battle.model.BWBattleDetail;
import com.xgame.common.util.LogUtil;
import com.xgame.social.share.SharePlatform;
import com.xgame.ui.Router;
import com.xgame.ui.activity.BWBattleResultActivity;
import com.xgame.ui.activity.invite.util.ShareInvoker;
import com.xgame.util.Analytics;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by wangyong on 18-2-7.
 */

public class BWWinFragment extends Fragment implements ShareInvoker.ShareListener, BWBattleResultActivity.FragmentBackHandler {
    private static final String TAG = "BWWinFragment";
    private View mRoot;

    private ShareInvoker mShareInvoker;

    private BWBattleBonusResult mBonusResult;

    @BindView(R.id.avatar)
    CircleImageView mAvatar;
    @BindView(R.id.result_title)
    TextView mTitle;
    @BindView(R.id.result_pre_sub)
    TextView mPreSubTitle;
    @BindView(R.id.result_subtitle)
    TextView mSubTitle;
    @BindView(R.id.result_post_sub)
    TextView mPostSubTitle;
    @BindView(R.id.result_des)
    TextView mDes;
    @BindView(R.id.share_btn)
    TextView mShareBtn;
    @BindView(R.id.my_account)
    TextView mAccount;

    private Unbinder mUnbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_bw_win_v2, container, false);
        mUnbinder = ButterKnife.bind(this, mRoot);

        return mRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mShareBtn.setOnClickListener(mLister);
        mAccount.setOnClickListener(mLister);

        mBonusResult = BWBattleManager.getInstance().getBonus();
        if (mBonusResult == null) {
            LogUtil.i(TAG, "wrong status -> bonus = " + mBonusResult);
            leave();
        }
        LogUtil.i(TAG, "win getBonus = " + mBonusResult);
        setWinLayout(mBonusResult);
        User user = UserManager.getInstance().getUser();
        GlideApp.with(this).load(user.getHeadimgurl()).placeholder(R.drawable.default_avatar)
                .into(mAvatar);
        mShareInvoker = new ShareInvoker();
        mShareInvoker.setShareListener(this);
        playSound(BWBattleResultActivity.AUDIO_FINAL_WIN);
    }

    private void setWinLayout(BWBattleBonusResult result) {
        if (result == null) {
            return;
        }
        if (result.getStatus() == BattleConstants.BONUS_STATUS_DELAY) {
            mTitle.setText(R.string.bw_bonus_compute);
            mPreSubTitle.setText(R.string.bw_bonus_delay_prompt);
            mSubTitle.setVisibility(View.GONE);
            mPostSubTitle.setVisibility(View.GONE);
            mDes.setVisibility(View.GONE);
        } else {
            mSubTitle.setText(getBonusString(result.getBonus()));
        }
        mShareBtn.setText(result.getShareBtnText());
    }

    private String getBonusString(long bonus) {
        double b = ((double) bonus) / 100;
        DecimalFormat format = new DecimalFormat(getString(R.string.money_format));
        return format.format(b);
    }

    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
        super.onDestroyView();
        stopSound();

        BWBattleManager.getInstance().clearAll();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onBackPressed() {
        leave();
        return true;
    }

    @Override
    public void exit() {

    }

    private void handleException(String message, int delay) {
        if (getActivity() != null && !getActivity().isFinishing() && isAdded()) {
            ((BWBattleResultActivity) getActivity()).handleException(message, delay);
        }
    }

    private View.OnClickListener mLister = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.share_btn:
                    mShareInvoker.shareInDialog();
                    break;
                case R.id.my_account:
                    Router.toPersonal();
                    if (isActivityActive()) {
                        getActivity().finish();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void playSound(int sound) {
        if (isActivityActive()) {
            ((BWBattleResultActivity) getActivity()).playBackgroundSound(sound, 0);
        }
    }

    private void stopSound() {
        if (isActivityActive()) {
            ((BWBattleResultActivity) getActivity()).stopBackgroundSound();
        }
    }

    @Override
    public void onShareProceed(int platform) {
        if (platform == SharePlatform.WX) {
            //百万场分享分渠道点击 微信好友
            Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_SHARE, Analytics.Constans.STOCK_ID_SHARE_WX,
                    Analytics.Constans.STOCK_NAME_SHARE_WX, Analytics.Constans.STOCK_TYPE_BTN,
                    Analytics.Constans.PAGE_BAIWAN_RESULT, Analytics.Constans.SECTION_SHARE, getExtInfo());
        } else if (platform == SharePlatform.WX_TIMELINE) {
            //百万场分享分渠道点击 微信朋友圈
            Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_SHARE, Analytics.Constans.STOCK_ID_SHARE_WX_TIMELINE,
                    Analytics.Constans.STOCK_NAME_SHARE_WX_TIMELINE, Analytics.Constans.STOCK_TYPE_BTN,
                    Analytics.Constans.PAGE_BAIWAN_RESULT, Analytics.Constans.SECTION_SHARE, getExtInfo());
        } else if (platform == SharePlatform.QQ) {
            //百万场分享分渠道点击 QQ好友
            Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_SHARE, Analytics.Constans.STOCK_ID_SHARE_QQ,
                    Analytics.Constans.STOCK_NAME_SHARE_QQ, Analytics.Constans.STOCK_TYPE_BTN,
                    Analytics.Constans.PAGE_BAIWAN_RESULT, Analytics.Constans.SECTION_SHARE, getExtInfo());
        }
    }

    @Override
    public void onShareFailed(int type, String error) {

    }

    private String getExtInfo() {
        JsonObject json = new JsonObject();
        BWBattleDetail bwBattleDetail = BWBattleManager.getInstance().getBWBattleDetail();
        if (bwBattleDetail != null) {
            json.addProperty("game_id", bwBattleDetail.getGameId());
            json.addProperty("bw_id", bwBattleDetail.getBwId());
            json.addProperty("game_name", bwBattleDetail.getTitle());
        }
        return json.toString();
    }

    private void leave() {
        if (isActivityActive()) {
            ((BWBattleResultActivity) getActivity()).leave();
        }
    }

    private boolean isActivityActive() {
        return getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed() && isAdded();
    }
}
