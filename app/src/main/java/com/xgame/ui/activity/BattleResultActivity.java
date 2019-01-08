package com.xgame.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.xgame.R;
import com.xgame.account.UserManager;
import com.xgame.base.ServiceFactory;
import com.xgame.battle.BattleConstants;
import com.xgame.battle.BattleManager;
import com.xgame.battle.BattleUtils;
import com.xgame.battle.event.GameOverMsg;
import com.xgame.battle.model.Player;
import com.xgame.battle.model.ServerBattleRecord;
import com.xgame.common.net.Result;
import com.xgame.common.util.GlobalGson;
import com.xgame.common.util.LogUtil;
import com.xgame.common.util.StatusBarUtil;
import com.xgame.push.PushConstants;
import com.xgame.push.event.AnotherGameEvent;
import com.xgame.push.event.InvitationEvent;
import com.xgame.push.event.LeaveRoomEvent;
import com.xgame.ui.view.MatchView;
import com.xgame.util.Analytics;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by wangyong on 18-1-26.
 */

public class BattleResultActivity extends BaseActivity {
    private static final String TAG = BattleResultActivity.class.getSimpleName();
    private MatchView mMatchView;
    private PopupWindow mPlayerPop;
    private PopupWindow mBtnPop;

    private TextView mBattleResult;
    private TextView mTryAgainBtn;
    private TextView mChangePeer;
    private TextView mChangeGame;
    private String mResult;

    private long mPeerId;
    private String mToken;
    private boolean mIsFriend;
    private Player mPlayer;

    private int mGameId;
    private String mGameUrl;
    private String mGameName;
    private ServerBattleRecord mBattleRecord;

    private CountDownTimer mCountDownTimer;

    private INVITE_STATUS mInviteStatus = INVITE_STATUS.NONE;

    private boolean hasShowPlayerPop = false;

    private enum INVITE_STATUS {
        NONE, //默认状态
        INVITED, SUCCEED, LEFT, CHANGED, //被邀战,邀战成功,对方离开,对方换个游戏
        INVITING, ALLOWING, LEAVING, CHANGING //已邀战,接受邀战,离开,换个游戏
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LogUtil.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle_result);
        mBattleResult = findViewById(R.id.battle_result);
        mMatchView = findViewById(R.id.match_view);
        mTryAgainBtn = findViewById(R.id.try_again_btn);
        mChangePeer = findViewById(R.id.change_competitor_btn);
        mChangeGame = findViewById(R.id.change_game_btn);

        findViewById(R.id.back).setOnClickListener(mOnClickListener);
        mTryAgainBtn.setOnClickListener(mOnClickListener);
        mChangePeer.setOnClickListener(mOnClickListener);
        mChangeGame.setOnClickListener(mOnClickListener);
        mToken = UserManager.getInstance().getToken();

        mGameId = BattleManager.getInstance().getGameId();
        mGameUrl = BattleManager.getInstance().getGameUrl();
        mGameName = BattleManager.getInstance().getGameName();
        mPlayer = BattleManager.getInstance().getPeerPlayer();
        mResult = BattleManager.getInstance().getBattleResult();
        LogUtil.i(TAG, "onCreate -> gameId = " + mGameId + ", gameUrl = " + mGameUrl + ", gameName = " + mGameName);
        LogUtil.i(TAG, "player = " + mPlayer);
        LogUtil.i(TAG, "result = " + mResult);

        if (mPlayer != null) {
            mPeerId = mPlayer.getUserId();
        } else {
            //TODO load user info form server
            mPlayer = new Player();
        }

        mIsFriend = BattleManager.getInstance().isFriend();
        showResult();
        postGameResult();
        EventBus.getDefault().register(this);

    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setTransparent(this);
        StatusBarUtil.setMiuiStatusBarDarkMode(this, false);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !hasShowPlayerPop && (BattleUtils.GAME_OVER_RESULT_LOSE.equals(mResult)
                || BattleUtils.GAME_OVER_RESULT_WIN.equals(mResult))) {
            hasShowPlayerPop = true;
            showPlayerPop(mMatchView.getAvatar2(), getPromptId(BattleUtils.GAME_OVER_RESULT_WIN.equals(mResult)), true);
        }
    }

    private void postGameResult() {
        String sessionId = BattleManager.getInstance().getSessionId();
        if (!TextUtils.isEmpty(sessionId) || mPlayer == null) {
            EventBus.getDefault().post(new GameOverMsg(mPlayer.getUserId(), sessionId, mResult));
        }
    }

    private PopupWindow initialPopupWindow(int backGround) {
        TextView popView = new TextView(this);
        popView.setHeight(getResources().getDimensionPixelOffset(R.dimen.pop_window_height));
        int paddingHorizontal = getResources().getDimensionPixelOffset(R.dimen.pop_padding_horizontal);
        popView.setPadding(paddingHorizontal, 0, paddingHorizontal, 0);
        popView.setGravity(Gravity.CENTER);
        popView.setTextAppearance(this, R.style.popWindowTextStyle);
        popView.setBackgroundResource(backGround);
        PopupWindow popupWindow = new PopupWindow(popView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(false);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.color.transparent));
        return popupWindow;
    }

    private int getPromptId(boolean isWinner) {
        int random = (int) (Math.random() * 300);
        if (!isWinner) {
            switch (random % 3) {
                case 0:
                    return R.string.result_win_prompt1;
                case 1:
                    return R.string.result_win_prompt2;
                case 2:
                    return R.string.result_win_prompt3;
                default:
                    break;

            }
        } else {
            switch (random % 2) {
                case 0:
                    return R.string.result_lose_prompt1;
                case 1:
                    return R.string.result_lose_prompt2;
                default:
                    break;
            }
        }
        return -1;
    }

    private void showPlayerPop(View view, int promptId, boolean right) {
        int backGroundId;
        if (right) {
            backGroundId = R.drawable.pop_background_green_right;
        } else {
            backGroundId = R.drawable.pop_background_green_left;
        }
        if (mPlayerPop == null) {
            mPlayerPop = initialPopupWindow(backGroundId);
        }

        String text = getResources().getString(promptId);
        TextView textView = (TextView) mPlayerPop.getContentView();
        TextPaint textPaint = textView.getPaint();
        int textPaintWidth = (int) textPaint.measureText(text);
        textView.setText(text);

        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int locationX = location[0] + view.getWidth() / 2;
        int locationY = location[1] - getResources().getDimensionPixelOffset(R.dimen.pop_window_height)
                - getResources().getDimensionPixelOffset(R.dimen.pop_margin_avatar_vertical);

        if (right) {
            locationX = locationX - textPaintWidth - getResources().getDimensionPixelOffset(R.dimen.pop_margin_avatar_vertical)
                    - textView.getPaddingStart() - textView.getPaddingEnd();
        } else {
            locationX = locationX + getResources().getDimensionPixelOffset(R.dimen.pop_margin_avatar_vertical);
        }
        mPlayerPop.showAtLocation(view, Gravity.NO_GRAVITY, locationX, locationY);
    }

    public void setButtonWaitingStatus(TextView textView, boolean isWaiting) {
        if (isWaiting) {
            textView.setBackgroundResource(R.drawable.round_green_rectangle_background);
            textView.setTextColor(getResources().getColor(R.color.color_white));
        } else {
            textView.setBackgroundResource(R.drawable.round_yellow_rectangle_background);
            textView.setTextColor(getResources().getColorStateList(R.color.text_yellow_black_selector));
        }
    }

    /**
     * 连胜的任务提示，暂时取消
     *
     * @param view
     */
    private void showBtnPop(View view) {
        if (mBtnPop == null) {
            mBtnPop = initialPopupWindow(R.drawable.pop_background_blue);
        }
        int popHeight = getResources().getDimensionPixelOffset(R.dimen.pop_window_height);
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        mBtnPop.showAtLocation(view, Gravity.NO_GRAVITY,
                location[0] + view.getWidth() / 2 + getResources().getDimensionPixelOffset(R.dimen.pop_margin_btn_horizontal),
                location[1] - popHeight + getResources().getDimensionPixelOffset(R.dimen.pop_margin_btn_vertical));
    }

    private void showResult() {
        mMatchView.setUserInfo(UserManager.getInstance().getUser());
        mMatchView.setPeerInfo(mPlayer);
        mMatchView.setScore(BattleManager.getInstance().getSelfWin(), BattleManager.getInstance().getPeerWin());
        setButtonWaitingStatus(mTryAgainBtn, false);
        setButtonWaitingStatus(mChangePeer, false);
        setButtonWaitingStatus(mChangeGame, false);
        if (BattleUtils.GAME_OVER_RESULT_WIN.equals(mResult)) {
            ((TextView) findViewById(R.id.battle_result)).setText(getResources().getString(R.string.result_title_win));
//            showBtnPop(mTryAgainBtn);
        } else if (BattleUtils.GAME_OVER_RESULT_LOSE.equals(mResult)) {
            ((TextView) findViewById(R.id.battle_result)).setText(getResources().getString(R.string.result_title_lose));
        } else {
            ((TextView) findViewById(R.id.battle_result)).setText(getResources().getString(R.string.result_title_dogfall));
        }
        ((TextView) findViewById(R.id.game_name)).setText(BattleManager.getInstance().getGameName());
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.try_again_btn:
                    tryAgain();
                    break;
                case R.id.change_competitor_btn:
                    changePeer();
                    break;
                case R.id.change_game_btn:
                    changeGame();
                    break;
                case R.id.back:
                    onBackPressed();
                default:
                    break;
            }
        }
    };

    public void updateLayout(INVITE_STATUS status) {
        switch (status) {
            case INVITING:
                mBattleResult.setText(R.string.result_title_inviting);
                startCountDown(60, status);
                break;
            case INVITED:
                mBattleResult.setText(R.string.result_title_invited);
                setButtonStatus(mTryAgainBtn, true, false);
                startCountDown(60, status);
                break;
            case CHANGED:
                mBattleResult.setText(R.string.result_title_changing);
                startCountDown(3, status);
            default:
                break;
        }
    }

    private void startCountDown(int seconds, final INVITE_STATUS status) {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        updateViewOnTimer(status, seconds);
        mCountDownTimer = new CountDownTimer(seconds * 1000, 1000) {
            @Override
            public void onTick(long l) {
                LogUtil.i(TAG, "countDown status = " + status + ", second = " + (l/1000));
                updateViewOnTimer(status, (int) (l / 1000));
            }

            @Override
            public void onFinish() {
                LogUtil.i(TAG, "countDown finish status = " + status);
                if (status == INVITE_STATUS.INVITED || status == INVITE_STATUS.INVITING) {
                    leave();
                    BattleManager.getInstance().clearAll();
                } else if (status == INVITE_STATUS.CHANGED) {
                    LogUtil.i(TAG, "goto ChatListActivity");
                    Intent intent = ChatActivity.getChatIntent(mPlayer.getUserId(), mPlayer.getGender(),
                            mPlayer.getAge(), mPlayer.getName(), mPlayer.getAvatar(), mIsFriend);
                    startActivity(intent);
                    BattleResultActivity.this.finish();
                }
            }
        };
        mCountDownTimer.start();
    }

    private void updateViewOnTimer(final INVITE_STATUS status, int seconds) {
        switch (status) {
            case INVITED:
                mTryAgainBtn.setText(getString(R.string.result_btn_accepting, seconds));
                setButtonWaitingStatus(mTryAgainBtn, true);
                break;
            case INVITING:
                mTryAgainBtn.setText(getString(R.string.result_btn_waiting, seconds));
                setButtonWaitingStatus(mTryAgainBtn, true);
                break;
            case CHANGED:
                mChangeGame.setText(getString(R.string.result_btn_changing, seconds));
                setButtonWaitingStatus(mChangeGame, true);
                break;
            default:
                break;
        }
    }

    private void setButtonStatus(TextView view, boolean enableButton, boolean disableBackGroud) {
        view.setEnabled(enableButton);
        if (!enableButton && disableBackGroud) {
            view.setBackgroundResource(R.drawable.round_grey_rectangle_background);
        }
    }

    /**
     * 再来一局
     * 被邀请状态，则为同意邀请，同意之后等待服务器响应，等待约战成功push后方可进入游戏
     * 初始状态，发送邀请，开始倒计时，倒计时结束离开页面，回到首页
     * 其他状态不处理。
     */
    private void tryAgain() {
        LogUtil.i(TAG, "tryAgain -> status = " + mInviteStatus);
        if (mInviteStatus == INVITE_STATUS.INVITED && isCurrentRecordValid()) {
            mInviteStatus = INVITE_STATUS.ALLOWING;
            LogUtil.i(TAG, "request acceptInvite -> gameId = " + mGameId
                    + ", sessionId = " + mBattleRecord.sessionId + ", mpeerId = " + mPeerId);
            ServiceFactory.battleService().acceptInvite(mGameId, mBattleRecord.sessionId, mPeerId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    LogUtil.i(TAG, "response acceptInvite -> onResponse");
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    LogUtil.i(TAG, "response acceptInvite -> onFailure");
                }
            });
            setButtonStatus(mTryAgainBtn, false, false);
            //真人对战结果页按钮点击 接受邀请
            Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_CLICK, Analytics.Constans.STOCK_ID_BATTLE_ACCEPT,
                    Analytics.Constans.STOCK_NAME_BATTLE_ACCEPT, Analytics.Constans.STOCK_TYPE_BTN,
                    Analytics.Constans.PAGE_BATTLE_RESULT, Analytics.Constans.SECTION_BATTLE_RESULT, getExtInfo());
        } else if (mInviteStatus == INVITE_STATUS.NONE || !isCurrentRecordValid()) {
            mInviteStatus = INVITE_STATUS.INVITING;
            updateLayout(mInviteStatus);
            LogUtil.i(TAG, "request inviteMatch -> gameId = " + mGameId + ", mpeerId = " + mPeerId +
                    ", isFriend = " + mIsFriend);
            ServiceFactory.battleService().inviteMatch(mGameId, mPeerId, mIsFriend).enqueue(new Callback<Result<ServerBattleRecord>>() {
                @Override
                public void onResponse(Call<Result<ServerBattleRecord>> call, Response<Result<ServerBattleRecord>> response) {
                    LogUtil.i(TAG, "response inviteMatch -> onResponse = " + response);
                    if (response.code() == 200 && response.body() != null) {
                        ServerBattleRecord record = response.body().getData();
                        if (record != null && !TextUtils.isEmpty(record.sessionId)) {
                            mBattleRecord = record;
                        }
                        return;
                    }
                }

                @Override
                public void onFailure(Call<Result<ServerBattleRecord>> call, Throwable t) {
                    LogUtil.i(TAG, "response inviteMatch -> onFailure");
                }
            });
            setButtonStatus(mTryAgainBtn, false, false);
            //真人对战结果页按钮点击 再来一局
            Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_CLICK, Analytics.Constans.STOCK_ID_BATTLE_AGAIN,
                    Analytics.Constans.STOCK_NAME_BATTLE_AGAIN, Analytics.Constans.STOCK_TYPE_BTN,
                    Analytics.Constans.PAGE_BATTLE_RESULT, Analytics.Constans.SECTION_BATTLE_RESULT, getExtInfo());
        }
    }

    /**
     * 换个对手
     * 被邀请状态，则拒绝邀请，不等返回结果，直接离开
     * 已离开状态，不处理，重复点击事件忽略
     * 其他状态，不等返回结果，直接离开
     * 跳转到匹配页面
     */
    private void changePeer() {
        LogUtil.i(TAG, "changePeer -> status = " + mInviteStatus);
        LogUtil.i(TAG, "goto Match");
        setButtonStatus(mChangePeer, false, false);
        BattleUtils.startMatch(BattleResultActivity.this, mGameId, mGameName, mGameUrl, BattleConstants.MATCH_FROM_CHANGE_PEER);
        leave();
        //真人对战结果页按钮点击 换个对手
        Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_CLICK, Analytics.Constans.STOCK_ID_BATTLE_CHANGE_PEER,
                Analytics.Constans.STOCK_NAME_BATTLE_CHANGE_PEER, Analytics.Constans.STOCK_TYPE_BTN,
                Analytics.Constans.PAGE_BATTLE_RESULT, Analytics.Constans.SECTION_BATTLE_RESULT, getExtInfo());

        BattleManager.getInstance().clearAll();
    }

    /**
     * 换个游戏，直接进入会话列表
     */
    private void changeGame() {
        LogUtil.i(TAG, "changeGame -> status = " + mInviteStatus);
        setButtonStatus(mChangeGame, false, false);
        if (mInviteStatus != INVITE_STATUS.CHANGING && mInviteStatus != INVITE_STATUS.CHANGED) {
            mInviteStatus = INVITE_STATUS.CHANGING;
            LogUtil.i(TAG, "request changeGame -> gameId = " + mGameId + ", peerId = " + mPeerId);
            ServiceFactory.battleService().changeGame(mGameId, mPeerId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    LogUtil.i(TAG, "response changeGame -> onResponse");
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    LogUtil.i(TAG, "response changeGame -> onFailure");
                }
            });
            //真人对战结果页按钮点击 换个游戏
            Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_CLICK, Analytics.Constans.STOCK_ID_BATTLE_CHANGE_GAME,
                    Analytics.Constans.STOCK_NAME_BATTLE_CHANGE_GAME, Analytics.Constans.STOCK_TYPE_BTN,
                    Analytics.Constans.PAGE_BATTLE_RESULT, Analytics.Constans.SECTION_BATTLE_RESULT, getExtInfo());
        } else {
            //真人对战结果页按钮点击 对方想换个游戏
            Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_CLICK, Analytics.Constans.STOCK_ID_BATTLE_PEER_CHANGE_GAME,
                    Analytics.Constans.STOCK_NAME_BATTLE_PEER_CHANGE_GAME, Analytics.Constans.STOCK_TYPE_BTN,
                    Analytics.Constans.PAGE_BATTLE_RESULT, Analytics.Constans.SECTION_BATTLE_RESULT, getExtInfo());
        }

        LogUtil.i(TAG, "player = " + mPlayer.toString());
        if (mPlayer != null) {
            Intent intent = ChatActivity.getChatIntent(mPlayer.getUserId(), mPlayer.getGender(),
                    mPlayer.getAge(), mPlayer.getName(), mPlayer.getAvatar(), mIsFriend);
            startActivity(intent);
        }
        BattleResultActivity.this.finish();
    }

    private void acceptInvitation() {
        LogUtil.i(TAG, "request acceptInvite -> gameId = " + mGameId
                + ", sessionId = " + mBattleRecord.sessionId + ", mpeerId = " + mPeerId);
        ServiceFactory.battleService().acceptInvite(mGameId, mBattleRecord.sessionId, mPeerId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                LogUtil.i(TAG, "response acceptInvite -> onResponse");
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                LogUtil.i(TAG, "response acceptInvite -> onFailure");
            }
        });
        setButtonStatus(mTryAgainBtn, false, false);
    }

    /**
     * 离开，点击回退按钮，返回键，或者超时
     * 被邀请状态，则拒绝邀请
     * 已邀请状态，则取消邀请
     * 初始状态，发送离开
     * 回到首页
     */
    private void leave() {
        LogUtil.i(TAG, "leave");
        if (mInviteStatus == INVITE_STATUS.INVITING || mInviteStatus == INVITE_STATUS.INVITED) {
            if (!isCurrentRecordValid()) {
                return;
            }
            LogUtil.i(TAG, "request cancelMatch -> gameId = " + mGameId + ", sessionId = " + mBattleRecord.sessionId + ", peerId = " + mPeerId);
            ServiceFactory.battleService().cancelMatch(mGameId, mBattleRecord.sessionId, mPeerId).enqueue(new Callback<Result<ServerBattleRecord>>() {
                @Override
                public void onResponse(Call<Result<ServerBattleRecord>> call, Response<Result<ServerBattleRecord>> response) {
                    LogUtil.i(TAG, "response cancelMatch -> onResponse");
                }

                @Override
                public void onFailure(Call<Result<ServerBattleRecord>> call, Throwable t) {
                    LogUtil.i(TAG, "response cancelMatch -> onResponse");
                }
            });
            BattleManager.getInstance().sendPendingCancelEvent();
        }
        mInviteStatus = INVITE_STATUS.LEAVING;
        LogUtil.i(TAG, "request leaveMatch -> peerId = " + mPeerId);
        ServiceFactory.battleService().leaveMatch(mPeerId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                LogUtil.i(TAG, "response leaveMatch -> onResponse");
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                LogUtil.i(TAG, "response leaveMatch -> onFailure");
            }
        });
        BattleResultActivity.this.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        leave();
        BattleManager.getInstance().clearAll();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInvitationEvent(InvitationEvent event) {
        if (event == null) {
            return;
        }
        LogUtil.i(TAG, "receive push -> type = " + event.getType() + ", content = " + event.getContent());
        String type = event.getType();
        String content = event.getContent();
        ServerBattleRecord record = GlobalGson.get().fromJson(content, ServerBattleRecord.class);
        handlePushResult(type, record);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAnotherGameEvent(AnotherGameEvent event) {
        if (event == null) {
            return;
        }
        LogUtil.i(TAG, "receive push -> type = " + event.getType() + ", content = " + event.getContent());
        String type = event.getType();
        String content = event.getContent();
        ServerBattleRecord record = GlobalGson.get().fromJson(content, ServerBattleRecord.class);
        handlePushResult(type, record);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLeaveRoomEvent(LeaveRoomEvent event) {
        if (event == null) {
            return;
        }
        LogUtil.i(TAG, "receive push -> type = " + event.getType() + ", content = " + event.getContent());
        String type = event.getType();
        String content = event.getContent();
        ServerBattleRecord record = GlobalGson.get().fromJson(content, ServerBattleRecord.class);
        handlePushResult(type, record);
    }

    /**
     * 仅处理push结果，只保存一个sessionId,其他sesssionId的消息丢弃
     *
     * @param type
     * @param record
     */
    private void handlePushResult(String type, ServerBattleRecord record) {
        if (record == null || TextUtils.isEmpty(type)) {
            return;
        }

        if (!isExpected(type, record, record.type)) {
            return;
        }

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        switch (record.messageDetailType) {
            case ServerBattleRecord.MESSAGE_TYPE_INVITE_WAITING:
                //接收到邀战,开始倒计时,倒计时结束则回到首页
                if (mInviteStatus == INVITE_STATUS.INVITING) {
                    mInviteStatus = INVITE_STATUS.INVITED;
                    acceptInvitation();
                    break;
                }
                mInviteStatus = INVITE_STATUS.INVITED;
                updateLayout(mInviteStatus);
                break;
            case ServerBattleRecord.MESSAGE_TYPE_INVITE_CANCEL:
                //对手取消约战，此时对手已离开，按离开逻辑处理
            case ServerBattleRecord.MESSAGE_TYPE_INVITE_DENY:
                //对手拒绝约战
            case ServerBattleRecord.MESSAGE_TYPE_SESSION_LEAVE:
                //对手离开
                mInviteStatus = INVITE_STATUS.LEFT;
                setButtonStatus(mTryAgainBtn, false, true);
                mTryAgainBtn.setText(R.string.change_peer_left);
                mChangeGame.setVisibility(View.GONE);
                break;
            case ServerBattleRecord.MESSAGE_TYPE_INVITE_SUCCESS:
                //约战成功
                mInviteStatus = INVITE_STATUS.SUCCEED;
                BattleUtils.gotoMatchBattle(BattleResultActivity.this, mBattleRecord.sessionId, record.roomId, mToken);
                BattleResultActivity.this.finish();
                break;
            case ServerBattleRecord.MESSAGE_TYPE_SESSION_ANOTHER_GAME:
                //对手想换个游戏
                mInviteStatus = INVITE_STATUS.CHANGED;
                setButtonStatus(mTryAgainBtn, false, true);
//                mTryAgainBtn.setText(R.string.play_again);
                updateLayout(mInviteStatus);
                break;
            default:
                break;
        }
    }

    private boolean isExpected(String type, ServerBattleRecord record, int recordType) {
        boolean emptySessionId = PushConstants.TYPE_LEAVE_ROOM.equals(type) || PushConstants.TYPE_ANOTHER_GAME.equals(type);
        if (record == null || TextUtils.isEmpty(record.sessionId) && !emptySessionId) {
            return false;
        }

        if (recordType == ServerBattleRecord.TYPE_SEND && !PushConstants.TYPE_GAME_INVITATION_SUCCESS.equals(type)) {
            return false;
        }

        if (mInviteStatus == INVITE_STATUS.SUCCEED || mInviteStatus == INVITE_STATUS.CHANGED
                || mInviteStatus == INVITE_STATUS.CHANGING || mInviteStatus == INVITE_STATUS.CHANGING.LEAVING
                || mInviteStatus == INVITE_STATUS.LEFT) {
            return false;
        }

        if (PushConstants.TYPE_GAME_INVITATION.equals(type)
                || PushConstants.TYPE_GAME_INVITATION_SUCCESS.equals(type)) {
            if (changeSessionId(mBattleRecord, record)) {
                mBattleRecord = record;
                return true;
            }
        }

        if (!isCurrentRecordValid()) {
            mBattleRecord = record;
            return true;
        } else {
            if (!mBattleRecord.sessionId.equals(record.sessionId)) {
                return false;
            }
        }

        return true;
    }

    private boolean changeSessionId(ServerBattleRecord newRecord, ServerBattleRecord oldRecord) {
        if (newRecord == null || TextUtils.isEmpty(newRecord.sessionId)) {
            return false;
        }
        if (oldRecord == null || TextUtils.isEmpty(oldRecord.sessionId)) {
            return true;
        }
        LogUtil.i(TAG, "changeSessionId = " +
                (newRecord.createTime < oldRecord.createTime ? newRecord.sessionId : oldRecord.sessionId));
        return newRecord.createTime < oldRecord.createTime;
    }

    private boolean isCurrentRecordValid() {
        if (mBattleRecord == null || TextUtils.isEmpty(mBattleRecord.sessionId)) {
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayerPop != null) {
            mPlayerPop.dismiss();
            mPlayerPop = null;
        }
        if (mBtnPop != null) {
            mBtnPop.dismiss();
            mBtnPop = null;
        }

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        EventBus.getDefault().unregister(this);
    }

    private String getExtInfo() {
        JsonObject json = new JsonObject();
        json.addProperty("game_id", mGameId);
        json.addProperty("battle_type", Analytics.Constans.CUSTOM_GAME_TYPE_BATTLE);
        json.addProperty("game_name", mGameName);
        return json.toString();
    }
}
