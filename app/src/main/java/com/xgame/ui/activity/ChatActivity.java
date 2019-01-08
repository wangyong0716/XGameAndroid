package com.xgame.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.xgame.R;
import com.xgame.account.UserManager;
import com.xgame.account.model.User;
import com.xgame.base.GameProvider;
import com.xgame.battle.BattleUtils;
import com.xgame.battle.model.Player;
import com.xgame.chat.BattleManager;
import com.xgame.chat.ChatMessage;
import com.xgame.chat.db.GameBattle;
import com.xgame.chat.ui.BattleMessageView;
import com.xgame.chat.ui.GamePicker;
import com.xgame.common.util.IntentParser;
import com.xgame.common.util.LogUtil;
import com.xgame.common.util.NetworkUtil;
import com.xgame.util.Analytics;

import static com.xgame.ui.Router.BASE_URL;

public class ChatActivity extends BaseActivity {

    public static final String EXTRA_OTHER_ID = "otherId";
    public static final String EXTRA_OTHER_GANDER = "otherGander";
    public static final String EXTRA_OTHER_AGE = "otherAge";
    public static final String EXTRA_OTHER_NAME = "otherName";
    public static final String EXTRA_OTHER_AVATAR = "otherAvatar";
    public static final String EXTRA_OTHER_IS_FRIEND = "otherIsFriend";

    private static final String TAG = ChatActivity.class.getName();

    private static final int MSG_CHECK_BATTLE_COUNTDOWN = 1;

    private static final long TIME_HINT_INTEVAL = 3 * 60 * 1000;

    private static final long INVALID_USER_ID = -1;

    private RecyclerView mMessageRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private GamePicker mGamePicker;

    private long mOpponentId;
    private String mOpponentName;
    private String mOpponentImageUrl;
    private int mOpponentAge;
    private int mOpponentGender;

    private boolean mIsOpponentFriend;
    private boolean mIsTempSessionClosed;

    private String mSelfImageUrl;

    private boolean mIsStartingGame;

    private ArrayList<ChatMessage> mMessages;
    private ArrayList<GameBattle> mBattles;
    private RecyclerView.Adapter mMessagesAdapter;
    private BattleManager.onBattleDataListener mOnNewDataListener;
    private BattleMessageView.OnBattleUpdateListener mOnBattleUpdateListener;

    private boolean mIsRefreshingData;
    private Handler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);

        initOpponentInfo();
        mSelfImageUrl = UserManager.getInstance().getUser().getHeadimgurl();

        if (mOpponentId == INVALID_USER_ID) {
            finish();
            LogUtil.d(TAG, "missing opponent id");
            return;
        }

        mMessages = new ArrayList<>();
        mBattles = new ArrayList<>();

        initRecyclerView();

        initGamePicker();

        initToolbar();

        initStrangerPanelIfNeeded();

        mOnNewDataListener = new BattleManager.onBattleDataListener() {
            @Override
            public void onNewData(List<GameBattle> battles) {
                onGetNewBattles(battles);
            }

            @Override
            public void onUpdateData(GameBattle battle) {
                onBattleUpdate(battle);
            }

            @Override
            public void onStartGame(GameBattle battle) {
                boolean isValidData = onBattleUpdate(battle);
                if (isValidData) {
                    startGame(battle);
                }
            }

            @Override
            public void onRequestFail() {
                Toast.makeText(ChatActivity.this, R.string.chat_hint_error_common,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTempSessionClosed() {
                if (!mIsOpponentFriend) {
                    mIsTempSessionClosed = true;
                }
            }

            @Override
            public void onNewGameLists(ArrayList<GameProvider.GameProfile> games) {
                mGamePicker.setGames(games);
            }
        };
        BattleManager.getInstance().startMonitoringBattleData(mOpponentId, mOnNewDataListener);

        mHandler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case MSG_CHECK_BATTLE_COUNTDOWN:
                        cancelWaitingStatusForBattle((GameBattle)msg.obj);
                        break;
                    default:
                        break;
                }
            }
        };

        refreshForNewData();
    }

    private void initOpponentInfo() {
        Intent intent = getIntent();
//        String opponentId = IntentParser.getString(intent, EXTRA_OTHER_ID);
        mOpponentId = intent.getLongExtra(EXTRA_OTHER_ID, INVALID_USER_ID);
        if (mOpponentId == INVALID_USER_ID) {
            return;
        }

        mOpponentName = IntentParser.getString(intent, EXTRA_OTHER_NAME);
        mOpponentImageUrl = IntentParser.getString(intent, EXTRA_OTHER_AVATAR);
        mOpponentAge = IntentParser.getInt(intent, EXTRA_OTHER_AGE, -1);
        mOpponentGender = IntentParser.getInt(intent, EXTRA_OTHER_GANDER, 0);

        mIsOpponentFriend = intent.getBooleanExtra(EXTRA_OTHER_IS_FRIEND, true);
    }

    private void initRecyclerView() {
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshForNewData();
            }
        });

        mMessageRecyclerView = findViewById(R.id.message_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mMessageRecyclerView.setLayoutManager(layoutManager);

        mOnBattleUpdateListener = new BattleMessageView.OnBattleUpdateListener() {
            @Override
            public void onInvitationCancel(final GameBattle battle) {
                // layout过程中会调用，所以post执行
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelWaitingStatusForBattle(battle);
                    }
                });
            }

            @Override
            public void onInvitationAccept(GameBattle battle) {
                acceptBattleInvitation(battle);
            }

            @Override
            public void onRequestOneMoreGame(GameBattle battle) {
                inviteForGame(battle.gameId);
            }
        };

        mMessagesAdapter = new BattleMessageAdapter();
        mMessageRecyclerView.setAdapter(mMessagesAdapter);
    }

    private void initGamePicker() {
        mGamePicker = findViewById(R.id.game_picker);
        ArrayList<GameProvider.GameProfile> allGames = BattleManager.getInstance().getAllGames();
        if (allGames.size() == 0) {
            BattleManager.getInstance().updateGamesInfo();
        }
        mGamePicker.setGames(allGames);
        mGamePicker.setOnGameSelectedListener(new GamePicker.OnGameSelectedListener() {
            @Override
            public void onSelected(int gameId) {
                inviteForGame(gameId);
            }
        });
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(mOpponentName);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initStrangerPanelIfNeeded() {
        if (!mIsOpponentFriend) {
            View strangerPanel = findViewById(R.id.stranger_panel);
            strangerPanel.setVisibility(View.VISIBLE);

            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)
                    mSwipeRefreshLayout.getLayoutParams();
            int strangePanelHeight = getResources().getDimensionPixelOffset(
                    R.dimen.chat_stranger_panel_height);
            layoutParams.setMargins(0,layoutParams.topMargin + strangePanelHeight,
                    0, layoutParams.bottomMargin);
            mSwipeRefreshLayout.requestLayout();

            ImageView imageView = strangerPanel.findViewById(R.id.stranger_image);
            Glide.with(this).load(mOpponentImageUrl).into(imageView);

            TextView name = strangerPanel.findViewById(R.id.stranger_name);
            name.setText(mOpponentName);
            TextView info = strangerPanel.findViewById(R.id.stranger_info);
            info.setText(getStrangerInfoStr());

            final Button addFriendBtn = strangerPanel.findViewById(R.id.stranger_add_friend);
            if (BattleManager.getInstance().isAddingFriend(mOpponentId)) {
                addFriendBtn.setText(R.string.chat_hint_add_friend_wait);
                addFriendBtn.setClickable(false);
            } else {
                addFriendBtn.setText(R.string.chat_hint_add_friend);
                addFriendBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((Button)v).setText(R.string.chat_hint_add_friend_wait);
                        v.setClickable(false);
                        addFriend();
                    }
                });
            }

        }
    }

    private String getStrangerInfoStr() {
        String gender;
        if (mOpponentGender == User.GENDER_FEMALE) {
            gender = getString(R.string.female_text);
        } else if (mOpponentGender == User.GENDER_MALE) {
            gender = getString(R.string.male_text);
        } else {
            gender = "";
        }
        if (gender.length() == 0 || mOpponentAge <= 0) {
            return "";
        }
        return String.format(getString(R.string.chat_stranger_info), gender, mOpponentAge);
    }

    @Override
    protected void onPause() {
        super.onPause();

        ArrayList<GameBattle> battles = new ArrayList<>();
        int size = mMessages.size();
        for (int i = 0; i < size; i++) {
            ChatMessage message = mMessages.get(i);
            GameBattle battle = message.gameBattle;
            if (battle != null && battle.status == GameBattle.STATUS_INVITE_WAITING) {
                battle.status = GameBattle.STATUS_INVITE_CANCEL;
                mMessagesAdapter.notifyItemChanged(i);
                battles.add(message.gameBattle);
            }
        }

        BattleManager.getInstance().cancelBattleInvitation(battles);

        if (battles.size() > 0) {
            BattleManager.getInstance().sendPendingCancelEvent();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOpponentId == INVALID_USER_ID) {
            return;
        }

        if (!mIsStartingGame && !mIsOpponentFriend && !mIsTempSessionClosed) {
            BattleManager.getInstance().leaveTempSession(mOpponentId);
        }
        mHandler.removeCallbacksAndMessages(null);
        BattleManager.getInstance().stopMonitoringBattleData(mOpponentId, mOnNewDataListener);
    }

    private void onGetNewBattles(List<GameBattle> battles) {
        if (mIsRefreshingData) {
            mIsRefreshingData = false;
            mSwipeRefreshLayout.setRefreshing(false);
        }

        if (battles == null || battles.size() == 0) {
            return;
        }

        if (mBattles.size() == 0) {
            mBattles.addAll(battles);
            mMessages.addAll(generateMessages(battles, 0));
            mMessagesAdapter.notifyDataSetChanged();
            mMessageRecyclerView.getLayoutManager().scrollToPosition(mMessages.size() - 1);
            return;
        }

        int currentSize = mBattles.size();
        long endTime = mBattles.get(currentSize - 1).createTime;
        long startTime = mBattles.get(0).createTime;

        long newDataEndTime = battles.get(battles.size() - 1).createTime;
        if (newDataEndTime < startTime) {
            ArrayList<ChatMessage> newMessages = generateMessages(battles, 0);
            mBattles.addAll(0, battles);
            int newMessagesCount = newMessages.size();
            mMessages.addAll(0, newMessages);
            mMessagesAdapter.notifyItemRangeInserted(0, newMessagesCount);
            return;
        }

        long newDataStartTime = battles.get(0).createTime;
        if (newDataStartTime > endTime) {
            ArrayList<ChatMessage> newMessages = generateMessages(battles,
                    mBattles.get(mBattles.size() - 1).createTime);
            mBattles.addAll(battles);
            int oldSize = mMessages.size();
            mMessages.addAll(newMessages);
            mMessagesAdapter.notifyItemInserted(oldSize);
            mMessageRecyclerView.getLayoutManager().scrollToPosition(mMessages.size() - 1);
            return;
        }
    }

    // battles 按 server time 时间序升序排列
    private ArrayList<ChatMessage> generateMessages(List<GameBattle> battles, long lastBattleTime) {
        ArrayList<ChatMessage> messages = new ArrayList<>(battles.size());

        long lastTime;
        GameBattle first = battles.get(0);
        if (first.createTime - lastBattleTime > TIME_HINT_INTEVAL) {
            lastTime = first.createTime;
            messages.add(new ChatMessage(lastTime));
        } else {
            lastTime = lastBattleTime;
        }

        int size = battles.size();
        for (int i = 0; i < size; i++) {
            GameBattle battle = battles.get(i);
            if (battle.createTime - lastTime > TIME_HINT_INTEVAL) {
                messages.add(new ChatMessage(battle.createTime));
            }
            lastTime = battle.createTime;

            if (battle.status == GameBattle.STATUS_BECOME_FRIEND) {
                messages.add(new ChatMessage(getString(R.string.chat_hint_become_friend)));
            } else {
                messages.add(new ChatMessage(battle));
            }
        }

        return messages;
    }

    private boolean onBattleUpdate(GameBattle battle) {
        int size = mMessages.size();
        for (int i = 0; i < size; i++) {
            ChatMessage message = mMessages.get(i);
            if (isMessageForGame(message, battle)) {
                message.gameBattle.status = battle.status;
                BattleManager.getInstance().updateGameBattle(message.gameBattle);
                mMessagesAdapter.notifyItemChanged(i);
                return true;
            }
        }
        return false;
    }

    private boolean isMessageForGame(ChatMessage message, GameBattle battle) {
        return message.type == ChatMessage.TYPE_GAME
                && message.gameBattle.sessionId.equals(battle.sessionId);
    }

    private void cancelWaitingStatusForBattle(GameBattle battle) {
        if (battle.status != GameBattle.STATUS_INVITE_WAITING) {
            return;
        }

        battle.status = GameBattle.STATUS_INVITE_CANCEL;
        BattleManager.getInstance().cancelBattleInvitation(battle);

        int size = mMessages.size();
        for (int i = 0; i < size; i++) {
            ChatMessage message = mMessages.get(i);
            if (message.gameBattle != null &&
                    message.gameBattle.sessionId.equals(battle.sessionId)) {
                mMessagesAdapter.notifyItemChanged(i);
                break;
            }
        }
    }

    private void inviteForGame(int gameId) {
        if (mIsTempSessionClosed) {
            int oldSize = mMessages.size();
            mMessages.add(new ChatMessage(getString(R.string.chat_hint_temp_session_closed)));
            mMessagesAdapter.notifyItemInserted(oldSize);
            mMessageRecyclerView.getLayoutManager().scrollToPosition(oldSize);
            return;
        }
        if (!NetworkUtil.hasNetwork(this)) {
            Toast.makeText(this, R.string.chat_hint_no_network, Toast.LENGTH_SHORT).show();
            return;
        }
        BattleManager.getInstance().inviteGame(mOpponentId, gameId, mIsOpponentFriend);
        trackEvent(gameId, "");
    }

    private void trackEvent(int gameId, String gameName) {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("game_id", gameId);
            json.addProperty("game_name", gameName);
            Analytics.trackCustomEvent(Analytics.Constans.ACTION_PATH_INVITE_GAME, Analytics.Constans.ACTION_TYPE_INVITE_GAME,
                    "", json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void acceptBattleInvitation(GameBattle battle) {
        BattleManager.getInstance().acceptInvitation(battle.gameId,
                battle.sessionId, battle.opponentId);
    }

    private void refreshForNewData() {
        mIsRefreshingData = true;

        if (mBattles.size() == 0) {
            BattleManager.getInstance().queryBattles(mOpponentId);
        } else {
            BattleManager.getInstance().queryBattles(mOpponentId, mBattles.get(0).createTime);
        }
    }

    private void startGame(GameBattle battle) {
        mIsStartingGame = true;

        GameProvider.GameProfile gameProfile = BattleManager.getInstance().getGameProfile(battle.gameId);
        if (gameProfile == null) {
            Toast.makeText(this, R.string.chat_hint_error_common, Toast.LENGTH_SHORT).show();
            BattleManager.getInstance().updateGamesInfo();
            return;
        }

        Player player = new Player().setAge(mOpponentAge).setGender(mOpponentGender)
                .setName(mOpponentName).setUserId(mOpponentId).setAvatar(mOpponentImageUrl);
        BattleUtils.gotoBattleFromFriends(this, battle.gameId, gameProfile.url,
                gameProfile.name, battle.roomId, battle.sessionId, mIsOpponentFriend, player);

        finish();
    }

    private void addFriend() {
        BattleManager.getInstance().requestAddFriend(mOpponentId);
        Toast.makeText(this, R.string.chat_hint_add_friend_send, Toast.LENGTH_SHORT).show();
    }

    private class BattleMessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {

        private static final int VIEW_TYPE_GAME = 1;
        private static final int VIEW_TYPE_TIME_HINT = 2;
        private static final int VIEW_TYPE_EVENT_HINT = 3;

        @Override
        public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_GAME) {
                BattleMessageView battleMessageView = new BattleMessageView(ChatActivity.this);
                battleMessageView.setOnBattleUpdateListener(mOnBattleUpdateListener);
                return new MessageViewHolder(battleMessageView);
            } else if (viewType == VIEW_TYPE_TIME_HINT) {
                LayoutInflater inflater = LayoutInflater.from(ChatActivity.this);
                TextView textView = (TextView) inflater.inflate(R.layout.chat_time_hint_text,
                        parent, false);
                return new MessageViewHolder(textView);
            } else if (viewType == VIEW_TYPE_EVENT_HINT) {
                LayoutInflater inflater = LayoutInflater.from(ChatActivity.this);
                FrameLayout hintView = (FrameLayout) inflater.inflate(R.layout.chat_event_hint_text,
                        parent, false);
                return new MessageViewHolder(hintView);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(MessageViewHolder holder, int position) {
            ChatMessage message = mMessages.get(position);
            if (holder.getItemViewType() == VIEW_TYPE_GAME) {
                holder.battleMessageView.bindData(message.gameBattle, mSelfImageUrl, mOpponentImageUrl);
            } else if (holder.getItemViewType() == VIEW_TYPE_TIME_HINT) {
                holder.hintTextView.setText(message.text);
            } else if (holder.getItemViewType() == VIEW_TYPE_EVENT_HINT) {
                holder.hintTextView.setText(message.text);
            }
        }

        @Override
        public int getItemCount() {
            return mMessages.size();
        }

        @Override
        public int getItemViewType(int position) {
            ChatMessage message = mMessages.get(position);
            if (message.type == ChatMessage.TYPE_GAME) {
                return VIEW_TYPE_GAME;
            } else if (message.type == ChatMessage.TYPE_TIME) {
                return VIEW_TYPE_TIME_HINT;
            } else if (message.type == ChatMessage.TYPE_EVENT) {
                return VIEW_TYPE_EVENT_HINT;
            }
            return -1;
        }

        @Override
        public void onViewAttachedToWindow(MessageViewHolder holder) {
            if (holder.battleMessageView != null) {
                GameBattle battle = holder.battleMessageView.getShowingBattle();
                if (battle.status == GameBattle.STATUS_INVITE_WAITING) {
                    mHandler.removeMessages(MSG_CHECK_BATTLE_COUNTDOWN, battle);
                }
            }
        }

        @Override
        public void onViewDetachedFromWindow(MessageViewHolder holder) {
            if (holder.battleMessageView != null) {
                GameBattle battle = holder.battleMessageView.getShowingBattle();
                if (battle.status == GameBattle.STATUS_INVITE_WAITING) {
                    android.os.Message msg = android.os.Message.obtain();
                    msg.what = MSG_CHECK_BATTLE_COUNTDOWN;
                    msg.obj = battle;
                    long remainTime = battle.localStartTime + BattleManager.GAME_INVITATION_COUNTDOWN_TIME
                            - System.currentTimeMillis();
                    mHandler.sendMessageDelayed(msg, remainTime);
                }
            }
        }
    }

    private class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView hintTextView;
        public BattleMessageView battleMessageView;

        public MessageViewHolder(FrameLayout itemView) {
            super(itemView);
            hintTextView = itemView.findViewById(R.id.hint_text);
        }

        public MessageViewHolder(TextView textView) {
            super(textView);
            hintTextView = textView;
        }

        public MessageViewHolder(BattleMessageView battleMessageView) {
            super(battleMessageView);
            this.battleMessageView = battleMessageView;
        }
    }

    public static Intent createChatIntent(long otherId, int gander, int age, String otherName,
            String otherAvatar) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(BASE_URL + "chat"));
        intent.putExtra(ChatActivity.EXTRA_OTHER_ID, otherId);
        intent.putExtra(ChatActivity.EXTRA_OTHER_GANDER, gander);
        intent.putExtra(ChatActivity.EXTRA_OTHER_AGE, age);
        intent.putExtra(ChatActivity.EXTRA_OTHER_NAME, otherName);
        intent.putExtra(ChatActivity.EXTRA_OTHER_AVATAR, otherAvatar);
        return intent;
    }

    public static Intent getChatIntent(long otherId, int gander, int age, String otherName,
                                       String otherAvatar, boolean isFriend) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(BASE_URL + "chat"));
        intent.putExtra(ChatActivity.EXTRA_OTHER_ID, otherId);
        intent.putExtra(ChatActivity.EXTRA_OTHER_GANDER, gander);
        intent.putExtra(ChatActivity.EXTRA_OTHER_AGE, age);
        intent.putExtra(ChatActivity.EXTRA_OTHER_NAME, otherName);
        intent.putExtra(ChatActivity.EXTRA_OTHER_AVATAR, otherAvatar);
        intent.putExtra(ChatActivity.EXTRA_OTHER_IS_FRIEND, isFriend);
        return intent;
    }
}
