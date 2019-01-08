package com.xgame.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.miui.zeus.mario.sdk.MarioSdk;
import com.xgame.R;
import com.xgame.account.UserManager;
import com.xgame.account.model.User;
import com.xgame.base.ServiceFactory;
import com.xgame.battle.BattleConstants;
import com.xgame.battle.BattleManager;
import com.xgame.battle.BattleUtils;
import com.xgame.battle.model.MatchResult;
import com.xgame.battle.model.Player;
import com.xgame.common.api.OnCallback;
import com.xgame.common.os.WeakHandler;
import com.xgame.common.util.GlobalGson;
import com.xgame.common.util.IntentParser;
import com.xgame.common.util.LogUtil;
import com.xgame.common.util.StatusBarUtil;
import com.xgame.common.util.ToastUtil;
import com.xgame.home.model.XGameItem;
import com.xgame.push.PushConstants;
import com.xgame.push.event.MatchResultEvent;
import com.xgame.ui.view.MatchView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by wangyong on 18-1-26.
 */

public class MatchActivity extends BaseActivity {

    public static final int MATCH_ONGOING = 1;
    public static final int MATCH_SUCCEED = 2;
    public static final int MATCH_INVALID_RULE_ID = 3;
    public static final int MATCH_LESS_COIN = 4;

    private static final long MAX_WAITING_TIME = 20000;
    private static final long REQUEST_DURATION = 5000;
    private static final long DISPLAY_DURATION = 2000;
    private static final int REQUEST_MATCH = 1;
    private static final int REQUEST_LAST = 2;
    private static final int GOTO_BATTLE = 3;
    private static final int FINISH = 4;

    private static final String TAG = MatchActivity.class.getSimpleName();
    private int mGameId;
    private String mGameUrl;
    private int mGameType;
    private int mRuleId;
    private String mRuleTitle;
    private String mFrom;
    private String mGameName;

    private int mRequestNum = 0;
    private boolean mGotResult = false;

    private User mUser;
    private MatchView mMatchView;
    private TextView mMatchStatusView;
    private WeakHandler mHandler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == REQUEST_MATCH) {
                startMatch(false);
                if (mRequestNum + 1 < MAX_WAITING_TIME / REQUEST_DURATION) {
                    mHandler.sendEmptyMessageDelayed(REQUEST_MATCH, REQUEST_DURATION);
                }
            } else if (msg.what == REQUEST_LAST) {
                startMatch(true);
            } else if (msg.what == GOTO_BATTLE) {
                gotoBattle();
            } else if (msg.what == FINISH) {
                leave();
            }
            return false;
        }
    });


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);
        mMatchView = findViewById(R.id.match_view);
        mMatchView.setLoadingMode();
        mMatchStatusView = findViewById(R.id.match_status);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        keepValues(getIntent());

        if (mGameType == BattleConstants.BATTLE_TYPE_COIN) {
            mRuleId = getIntent().getIntExtra(BattleConstants.BATTLE_RULE_ID, 0);
        }

        setUserInfo();
        setMatchingPage();
        startMatch(false);
        mHandler.sendEmptyMessageDelayed(REQUEST_MATCH, REQUEST_DURATION);
        mHandler.sendEmptyMessageDelayed(REQUEST_LAST, MAX_WAITING_TIME);

        EventBus.getDefault().register(this);
    }

    private void keepValues(Intent intent) {
        String gameId = IntentParser.getString(intent, XGameItem.EXTRA_GAME_ID);
        if (!TextUtils.isEmpty(gameId)) {
            mGameId = Integer.parseInt(gameId);
        }
        mGameUrl = IntentParser.getString(intent, XGameItem.EXTRA_GAME_URL);
        mGameName = IntentParser.getString(intent, XGameItem.EXTRA_GAME_NAME);
        mGameType = IntentParser.getInt(intent, XGameItem.EXTRA_GAME_TYPE, BattleConstants.BATTLE_TYPE_MATCH);
        mFrom = IntentParser.getString(intent, BattleConstants.MATCH_FROM);
        LogUtil.i(TAG, "getValue -> gameId= " + mGameId + ", gameUrl = " + mGameUrl + ", gameType = " + mGameType);
        if (mGameId < 0 || TextUtils.isEmpty(mGameUrl)) {
            handleException(getString(R.string.match_wrong_game), 1000);
        }
        BattleManager.getInstance().clearAll();
        BattleManager.getInstance().setGameId(mGameId);
        BattleManager.getInstance().setGameUrl(mGameUrl);
        BattleManager.getInstance().setGameType(mGameType);
        BattleManager.getInstance().setGameName(mGameName);
        if (mGameType == BattleConstants.BATTLE_TYPE_COIN) {
            mRuleId = IntentParser.getInt(intent, BattleConstants.BATTLE_RULE_ID, -1);
            mRuleTitle = IntentParser.getString(intent, BattleConstants.BATTLE_RULE_TITLE);
            BattleManager.getInstance().setRuleId(mRuleId);
            BattleManager.getInstance().setRuleTitle(mRuleTitle);
            BattleManager.getInstance().setWinCoin(IntentParser.getInt(intent, BattleConstants.BATTLE_WIN_COIN, 0));
            BattleManager.getInstance().setLoseCoin(IntentParser.getInt(intent, BattleConstants.BATTLE_LOSE_COIN, 0));
        }
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setTransparent(this);
        StatusBarUtil.setMiuiStatusBarDarkMode(this, false);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void setUserInfo() {
        if (!UserManager.getInstance().isLogin() || UserManager.getInstance().getUser() == null) {
            finish();
        } else {
            mUser = UserManager.getInstance().getUser();
        }

        mMatchView.setUserInfo(mUser);
        //store self info in BattleManager
        Player self = getSelfInfo();
        self.setScore(getIntent().getIntExtra(BattleUtils.EXTRA_WIN_SELF, 0));
        BattleManager.getInstance().setSelfPlayer(self);
    }

    /**
     * 初始展现匹配中状态
     */
    public void setMatchingPage() {
        ((TextView) findViewById(R.id.match_status)).setText(R.string.match_status_matching);
        ((TextView) findViewById(R.id.match_tips)).setText(R.string.match_waiting);
        mMatchView.setMatchingStatus();
    }

    /**
     * 设备网络连接改变时调用
     *
     * @param isWifi 是否为wifi连接
     */
    public void promptNetConnect(boolean isWifi) {
        findViewById(R.id.tips).setVisibility(isWifi ? View.GONE : View.VISIBLE);
    }

    /**
     * request to match
     */
    private void startMatch(final boolean lastTry) {
        LogUtil.i(TAG, "startMatch -> token = " + UserManager.getInstance().getToken() + ", userId = " + mUser.getUserid());
        LogUtil.i(TAG, " gameId = " + mGameId + ", gameType = " + mGameType);
        mRequestNum++;
        if (mGameType == BattleConstants.BATTLE_TYPE_MATCH) {
            ServiceFactory.battleService().startMatch(mGameId, mGameType, mFrom).enqueue(new OnCallback<MatchResult>() {
                @Override
                public void onResponse(MatchResult result) {
                    LogUtil.i(TAG, "startMatch onResponse -> result = " + result);
                    handleMatchResult(result, lastTry);
                }

                @Override
                public void onFailure(MatchResult result) {
                    LogUtil.i(TAG, "startMatch onFailure -> result = " + result);
                    if (lastTry) {
                        handleException(getString(R.string.match_fail), 0);
                    }
                }
            });
        } else {
            ServiceFactory.battleService().startMatch(mGameId, mGameType, mRuleId, MarioSdk.getClientInfo()).enqueue(new OnCallback<MatchResult>() {
                @Override
                public void onResponse(MatchResult result) {
                    LogUtil.i(TAG, "startMatch onResponse -> result = " + result);
                    handleMatchResult(result, lastTry);
                }

                @Override
                public void onFailure(MatchResult result) {
                    LogUtil.i(TAG, "startMatch onFailure -> result = " + result);
                    if (lastTry) {
                        handleException(getString(R.string.match_fail), 0);
                    }

                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMatchSuccessPush(MatchResultEvent event) {
        if (event == null) {
            return;
        }
        LogUtil.i(TAG, "receive push -> type = " + event.getType() + ", content = " + event.getContent());
        if (PushConstants.TYPE_MATCH_SUCCESS.equals(event.getType())) {
            if (!TextUtils.isEmpty(event.getContent())) {
                MatchResult result = GlobalGson.get().fromJson(event.getContent(), MatchResult.class);
                handleMatchResult(result, false);
                LogUtil.i(TAG, "receive result = " + result);
            }
        }
    }

    /**
     * 处理push结果
     */
    private void handleMatchResult(MatchResult result, boolean lastTry) {
        if (mGotResult) {
            return;
        }
        if (result == null) {
            if (lastTry) {
                handleException(getString(R.string.match_overtime), 0);
            }
            return;
        }
        if (result.getMatchStatus() == MATCH_LESS_COIN) {
            handleException(getString(R.string.match_coin_lack), 1000);
        }
        if (result.getMatchStatus() != MATCH_SUCCEED) {
            if (lastTry) {
                handleException(getString(R.string.match_overtime), 0);
            }
            return;
        }

        mGotResult = true;
        mMatchStatusView.setText(R.string.match_status_matched);
        mHandler.removeMessages(REQUEST_MATCH);
        mHandler.removeMessages(REQUEST_LAST);

        final Player player = Player.getInstance(result.getPeer());
        if (player != null) {
            storeMatchResult(result.getRoomId(), player);
            mMatchView.setPeerInfo(player);
        }
    }

    /**
     * match succeed
     */
    private void gotoBattle() {
        switch (mGameType) {
            case BattleConstants.BATTLE_TYPE_COIN:
                BattleUtils.gotoCoinBattle(this, UserManager.getInstance().getToken());
                break;
            case BattleConstants.BATTLE_TYPE_BW:
                BattleUtils.gotoBWBattle(this, UserManager.getInstance().getToken());
                break;
            case BattleConstants.BATTLE_TYPE_MATCH:
            default:
//                BattleUtils.gotoMatchBattle(this, UserManager.getInstance().getToken());
                BattleUtils.gotoMatchBattle(this, UserManager.getInstance().getToken());
                break;
        }
        consumeMatch();
        MatchActivity.this.finish();
    }

    private Player getSelfInfo() {
        Player self = new Player();
        self.setName(mUser.getNickname());
        self.setAvatar(mUser.getHeadimgurl());
        self.setAge(mUser.getAge());
        self.setGender(mUser.getSex());
        return self;
    }

    /**
     * 真人场需要roomId
     *
     * @param peer
     */
    private void storeMatchResult(String roomId, Player peer) {
        BattleManager.getInstance().setRoomId(roomId);
        BattleManager.getInstance().setPeerPlayer(peer);
        BattleManager.getInstance().setIsFriend(peer.isFriend());

        mHandler.sendEmptyMessageDelayed(GOTO_BATTLE, DISPLAY_DURATION);
    }

    public void handleException(String exception, long delay) {
        ToastUtil.showToast(getApplication(), exception, true);
        mHandler.sendEmptyMessageDelayed(FINISH, delay);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        leave();
    }

    private void leave() {
        mHandler.removeMessages(REQUEST_MATCH);
        mHandler.removeMessages(REQUEST_LAST);
        mHandler.removeMessages(GOTO_BATTLE);
        mHandler.removeMessages(FINISH);
        if (!mGotResult) {
            mGotResult = true;
            ServiceFactory.battleService().cancelMatch(mGameId, mRuleId, mGameType).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    LogUtil.i(TAG, "cancelMatch onResponse");
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    LogUtil.i(TAG, "cancelMatch onFailure");
                }
            });
        }
        finish();
    }

    private void consumeMatch() {
        ServiceFactory.battleService().consumeMatch(mGameId, mRuleId, MarioSdk.getClientInfo(),
                mGameType).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                LogUtil.i(TAG, "consumeMatch onResponse");
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                LogUtil.i(TAG, "consumeMatch onFailure");
            }
        });
    }
}
