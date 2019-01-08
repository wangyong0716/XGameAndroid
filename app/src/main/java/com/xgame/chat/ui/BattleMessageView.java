package com.xgame.chat.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.xgame.R;
import com.xgame.base.GameProvider;
import com.xgame.chat.BattleManager;
import com.xgame.chat.db.GameBattle;

public class BattleMessageView extends FrameLayout {
    public interface OnBattleUpdateListener {
        void onInvitationCancel(GameBattle battle);
        void onInvitationAccept(GameBattle battle);
        void onRequestOneMoreGame(GameBattle battle);
    }

    private static final int MSG_UPDATE_COUNTDOWN = 1;

    private static final long COUNT_DOWN_INTEVAL = 60 * 1000L;

    private ImageView mUserImage;
    private FrameLayout mBattleInfoPanel;
    private TextView mCountdownText;
    private ImageView mGameIcon;
    private TextView mGameNameText;
    private TextView mGameResultText;
    private TextView mGameActionText;
    private View mMaskView;

    private Handler mHandler;
    private GameBattle mGameBattle;
    private OnBattleUpdateListener mOnBattleUpdateListener;
    private OnClickListener mClickListenerForOneMoreGame;
    private OnClickListener mClickListenerForAcceptInvitation;

    // 距左右边缘
    private int mGamePanelMarginToEdge;
    private int mGamePanelMarginBottom;
    private int mUserImageMarginToEdge;

    private int mAcceptInviteTextColor;
    private int mAcceptInviteBgColor;
    private int mInviteOneMoreTextColor;
    private int mGameActionBtnHeight;

    public BattleMessageView(@NonNull Context context) {
        super(context);
        initLayout();

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_UPDATE_COUNTDOWN:
                        updateCountdown();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void initLayout() {
        Resources res = getResources();
        mGamePanelMarginToEdge = res.getDimensionPixelOffset(R.dimen.chat_game_panel_margin_to_edge);
        mGamePanelMarginBottom = res.getDimensionPixelOffset(R.dimen.chat_game_panel_margin_bottom);
        mUserImageMarginToEdge = res.getDimensionPixelOffset(R.dimen.chat_user_image_margin_to_edge);
        mAcceptInviteBgColor = res.getColor(R.color.chat_accept_invite_bg_color);
        mAcceptInviteTextColor = res.getColor(R.color.chat_accept_invite_text_color);
        mInviteOneMoreTextColor = res.getColor(R.color.chat_invite_one_more_text_color);
        mGameActionBtnHeight = res.getDimensionPixelSize(R.dimen.chat_game_action_textview_height);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(layoutParams);
        inflate(getContext(), R.layout.chat_battle_message_view, this);

        mUserImage = findViewById(R.id.image);
        mBattleInfoPanel = findViewById(R.id.game_info);
        mGameIcon = mBattleInfoPanel.findViewById(R.id.game_icon);
        mCountdownText = mBattleInfoPanel.findViewById(R.id.count_down);
        mGameNameText = mBattleInfoPanel.findViewById(R.id.game_name);
        mGameResultText = mBattleInfoPanel.findViewById(R.id.game_result);
        mGameActionText = mBattleInfoPanel.findViewById(R.id.game_action);
        mMaskView = mBattleInfoPanel.findViewById(R.id.mask);

        mClickListenerForOneMoreGame = new OnClickListener() {
            @Override
            public void onClick(View v) {
                inviteOneMoreGame();
            }
        };
        mClickListenerForAcceptInvitation = new OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptInvitation();
            }
        };
    }

    public void bindData(GameBattle battle, String selfImageUrl, String opponentImageUrl) {
        mHandler.removeMessages(MSG_UPDATE_COUNTDOWN);
        mGameBattle = battle;

        int direction = battle.direction;
        if (direction == GameBattle.DIRECTION_RECEIVE) {
            LayoutParams layoutParams = (LayoutParams) mUserImage.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            layoutParams.setMargins(mUserImageMarginToEdge, 0, 0, 0);
            layoutParams = (LayoutParams) mBattleInfoPanel.getLayoutParams();
            layoutParams.gravity = Gravity.LEFT;
            layoutParams.setMargins(mGamePanelMarginToEdge, 0, 0, mGamePanelMarginBottom);

            Glide.with(getContext()).load(opponentImageUrl).into(mUserImage);
        } else {
            LayoutParams layoutParams = (LayoutParams) mUserImage.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            layoutParams.setMargins(0, 0, mUserImageMarginToEdge, 0);
            layoutParams = (LayoutParams) mBattleInfoPanel.getLayoutParams();
            layoutParams.gravity = Gravity.RIGHT;
            layoutParams.setMargins(0, 0, mGamePanelMarginToEdge, mGamePanelMarginBottom);

            Glide.with(getContext()).load(selfImageUrl).into(mUserImage);
        }

        GameProvider.GameProfile gameInfo = BattleManager.getInstance().getGameInfo(battle.gameId);
        if (gameInfo != null) {
            mGameNameText.setText(gameInfo.name);
            Glide.with(getContext()).load(gameInfo.icon).into(mGameIcon);
        } else {
            mGameNameText.setText(R.string.chat_game_default_name);
            mGameIcon.setImageResource(R.drawable.game_picker_default_game_icon);
        }


        int actionButtonBgColor;
        int actionButtonTextRes;
        int actionButtonTextColor;
        OnClickListener actionButtonClickListener = null;

        int status = battle.status;
        if (status == GameBattle.STATUS_INVITE_CANCEL) {
            mCountdownText.setVisibility(View.GONE);
            mGameResultText.setVisibility(View.GONE);
            mMaskView.setVisibility(View.VISIBLE);
            mMaskView.setTranslationY(0);
            actionButtonBgColor = Color.WHITE;
            actionButtonTextRes = R.string.chat_invite_cancel;
            actionButtonTextColor = Color.BLACK;
        } else if (status == GameBattle.STATUS_INVITE_WAITING) {
            mCountdownText.setVisibility(View.VISIBLE);
            mGameResultText.setVisibility(View.GONE);
            mMaskView.setVisibility(View.GONE);
            if (battle.direction == GameBattle.DIRECTION_SEND) {
                actionButtonBgColor = Color.WHITE;
                actionButtonTextRes = R.string.chat_invite_waiting_for_accept;
                actionButtonTextColor = Color.BLACK;
            } else {
                actionButtonBgColor = mAcceptInviteBgColor;
                actionButtonTextRes = R.string.chat_invite_accept;
                actionButtonTextColor = mAcceptInviteTextColor;
                actionButtonClickListener = mClickListenerForAcceptInvitation;
            }
            updateCountdown();
        } else if (status == GameBattle.STATUS_GAMING) {
            mCountdownText.setVisibility(View.GONE);
            mGameResultText.setVisibility(View.GONE);
            mMaskView.setVisibility(View.GONE);
            actionButtonBgColor = Color.WHITE;
            actionButtonTextRes = R.string.chat_invite_one_more;
            actionButtonTextColor = Color.BLACK;
            actionButtonClickListener = mClickListenerForOneMoreGame;
        } else {
            mCountdownText.setVisibility(View.GONE);
            mGameResultText.setVisibility(View.VISIBLE);
            mMaskView.setVisibility(View.VISIBLE);
            mMaskView.setTranslationY(-mGameActionBtnHeight);
            actionButtonBgColor = Color.WHITE;
            actionButtonTextRes = R.string.chat_invite_one_more;
            actionButtonTextColor = mInviteOneMoreTextColor;
            actionButtonClickListener = mClickListenerForOneMoreGame;

            int resultString;
            int resultBgRes;
            if (status == GameBattle.STATUS_WIN) {
                resultString = R.string.chat_game_result_win;
                resultBgRes = R.drawable.chat_game_win;
            } else if (status == GameBattle.STATUS_DRAW) {
                resultString = R.string.chat_game_result_draw;
                resultBgRes = R.drawable.chat_game_draw;
            } else {
                resultString = R.string.chat_game_result_lose;
                resultBgRes = R.drawable.chat_game_lose;
            }
            mGameResultText.setText(resultString);
            mGameResultText.setBackgroundResource(resultBgRes);
        }
        mGameActionText.setText(actionButtonTextRes);
        mGameActionText.setBackgroundColor(actionButtonBgColor);
        mGameActionText.setTextColor(actionButtonTextColor);
        mGameActionText.setOnClickListener(actionButtonClickListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeMessages(MSG_UPDATE_COUNTDOWN);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mGameBattle.status == GameBattle.STATUS_INVITE_WAITING) {
            updateCountdown();
        }
    }

    private void updateCountdown() {
        if (mGameBattle.status !=  GameBattle.STATUS_INVITE_WAITING) {
            return;
        }

        long startTime = mGameBattle.localStartTime;
        long current = System.currentTimeMillis();
        if (current < startTime || current >= startTime + COUNT_DOWN_INTEVAL) {
            onTimeOut();
            return;
        }
        long remindTime = startTime + COUNT_DOWN_INTEVAL - current;
        mCountdownText.setText(String.valueOf(remindTime / 1000));
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_COUNTDOWN, 1000);
    }

    private void onTimeOut() {
        if (mOnBattleUpdateListener != null) {
            mOnBattleUpdateListener.onInvitationCancel(mGameBattle);
        }
    }

    private void inviteOneMoreGame() {
        if (mOnBattleUpdateListener != null) {
            mOnBattleUpdateListener.onRequestOneMoreGame(mGameBattle);
        }
    }

    private void acceptInvitation() {
        if (mOnBattleUpdateListener != null) {
            mOnBattleUpdateListener.onInvitationAccept(mGameBattle);
        }
    }

    public GameBattle getShowingBattle() {
        return mGameBattle;
    }

    public void setOnBattleUpdateListener(OnBattleUpdateListener listener) {
        mOnBattleUpdateListener = listener;
    }

}
