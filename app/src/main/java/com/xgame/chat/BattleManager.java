package com.xgame.chat;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.text.TextUtils;

import com.google.gson.JsonSyntaxException;
import com.xgame.account.event.AccountEventController;
import com.xgame.base.GameProvider;
import com.xgame.battle.BattleUtils;
import com.xgame.battle.api.BattleService;
import com.xgame.battle.event.GameOverMsg;
import com.xgame.battle.model.BattleRecords;
import com.xgame.battle.model.ServerBattleRecord;
import com.xgame.chat.db.BattleDao;
import com.xgame.chat.db.BattleDatabase;
import com.xgame.chat.db.GameBattle;
import com.xgame.common.api.ApiServiceManager;
import com.xgame.common.api.OnCallback;
import com.xgame.common.net.Result;
import com.xgame.common.util.GlobalGson;
import com.xgame.common.util.LogUtil;
import com.xgame.invite.api.InviteService;
import com.xgame.invite.model.FriendRelation;
import com.xgame.push.event.InvitationEvent;
import com.xgame.push.event.LeaveRoomEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BattleManager {

    private static final String TAG = "ChatBM";

    public static final long GAME_INVITATION_COUNTDOWN_TIME = 60 * 1000L;

    private static final long INVALID_USER_ID = -1;

    private static BattleManager sInstance;

    public static void init(Context context) {
        sInstance = new BattleManager(context);
    }

    public static BattleManager getInstance() {
        return sInstance;
    }

    public interface onBattleDataListener {
        void onNewData(List<GameBattle> battles);
        void onUpdateData(GameBattle battle);
        void onStartGame(GameBattle battle);
        void onRequestFail();
        void onTempSessionClosed();
        void onNewGameLists(ArrayList<GameProvider.GameProfile> games);
    }

    private Context mContext;
    private BattleDatabase mBattleDatabase;
    private BattleDao mBattleDao;
    private Executor mDiskExecutor;
    private Handler mMainThreadHandler;

    private long mCurrentOpponentId;
    private onBattleDataListener mOnBattleDataListener;

    private ArrayList<GameProvider.GameProfile> mAllGames;

    private BattleService mBattleService;
    private retrofit2.Callback mEmptyCallback = new Callback() {
        @Override
        public void onResponse(Call call, Response response) {
        }

        @Override
        public void onFailure(Call call, Throwable t) {
        }
    };

    private long mAddingFriendId;

    public BattleManager(Context context) {
        mContext = context.getApplicationContext();
        mDiskExecutor = Executors.newSingleThreadExecutor();
        mDiskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                initBattleDB();
            }
        });
        EventBus.getDefault().register(this);
        mMainThreadHandler = new Handler(Looper.getMainLooper());

        mBattleService = ApiServiceManager.obtain(BattleService.class);

        mAllGames = new ArrayList<>();
        updateGamesInfo();
    }

    private void initBattleDB() {
        mBattleDatabase = Room.databaseBuilder(mContext,
                BattleDatabase.class, "battle").build();
        mBattleDao = mBattleDatabase.battleDao();
    }

    public void startMonitoringBattleData(long opponentId, onBattleDataListener listener) {
        mCurrentOpponentId = opponentId;
        mOnBattleDataListener = listener;
    }

    public void stopMonitoringBattleData(long opponentId, onBattleDataListener listener) {
        if (mCurrentOpponentId == opponentId) {
            mCurrentOpponentId = INVALID_USER_ID;
        }
        if (mOnBattleDataListener == listener) {
            mOnBattleDataListener = null;
        }
    }

    public void cancelBattleInvitation(final GameBattle battle) {
        mDiskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mBattleDao.updateBattles(battle);
            }
        });
        mBattleService.cancelMatch(battle.gameId, battle.sessionId, battle.opponentId)
                .enqueue(mEmptyCallback);
    }

    public void cancelBattleInvitation(final ArrayList<GameBattle> battles) {
        mDiskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mBattleDao.updateBattles(battles);
            }
        });

        int size = battles.size();
        for (int i = 0; i < size; i++) {
            GameBattle battle = battles.get(i);
            mBattleService.cancelMatch(battle.gameId, battle.sessionId, battle.opponentId)
                    .enqueue(mEmptyCallback);
        }
    }

    // 非第一次展现时调用，先查询本地，本地有结果直接返回；无结果查询云端 。 逻辑有优化空间，避免无用的数据库查询
    public void queryBattles(final long opponentId, final long lastTime) {
        mDiskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final List<GameBattle> battles = mBattleDao.getBattles(opponentId, lastTime);
                reverseItemInList(battles);
                if (opponentId != mCurrentOpponentId) {
                    return;
                }

                if (battles.size() > 0) {
                    mMainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyNewData(battles);
                        }
                    });
                    return;
                }

                mBattleService.getBattleHistoryBeforeTime(opponentId, lastTime).enqueue(new Callback<Result<BattleRecords>>() {
                    @Override
                    public void onResponse(Call<Result<BattleRecords>> call, Response<Result<BattleRecords>> response) {
                        if (response.code() != 200) {
                            notifyNewData(null);
                            return;
                        }
                        BattleRecords records = response.body().getData();
                        long timestamp = response.body().getTimestamp();
                        if (records != null) {
                            final List<GameBattle> results = getBattlesFromServerResult(records, timestamp);
                            if (opponentId == mCurrentOpponentId){
                                notifyNewData(results);
                            }
                            if (results != null && results.size() > 0) {
                                mDiskExecutor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            mBattleDao.addBattles(results);
                                        } catch (SQLiteException e) {
                                            LogUtil.d("battle", e.toString());
                                        }
                                    }
                                });
                            }
                        } else {
                            notifyNewData(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<Result<BattleRecords>> call, Throwable t) {
                        notifyNewData(null);
                    }
                });
            }
        });
    }

    // 第一次展现时调用，先查询本地，本地有结果直接返回；然后从云端查询本地结果，时间点之后最新的数据
    public void queryBattles(final long opponentId) {
        mDiskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final List<GameBattle> battles = mBattleDao.getBattles(opponentId);
                reverseItemInList(battles);
                if (opponentId != mCurrentOpponentId) {
                    return;
                }

                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyNewData(battles);
                    }
                });
                long startTime;
                if (battles.size() > 0) {
                    startTime = battles.get(battles.size() - 1).createTime;
                } else {
                    startTime = 0;
                }
                mBattleService.getBattleHistoryAfterTime(opponentId, startTime).enqueue(new Callback<Result<BattleRecords>>() {
                    @Override
                    public void onResponse(Call<Result<BattleRecords>> call, Response<Result<BattleRecords>> response) {
                        if (response.code() != 200) {
                            return;
                        }
                        BattleRecords records = response.body().getData();
                        long timestamp = response.body().getTimestamp();
                        if (records != null) {
                            final List<GameBattle> results = getBattlesFromServerResult(records, timestamp);
                            if (results != null && results.size() > 0) {
                                if (opponentId == mCurrentOpponentId){
                                    notifyNewData(results);
                                }
                                mDiskExecutor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            mBattleDao.addBattles(results);
                                        } catch (SQLiteException e) {
                                            LogUtil.d("battle", e.toString());
                                        }
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Result<BattleRecords>> call, Throwable t) {
                    }
                });
            }
        });
    }

    private void reverseItemInList(List<GameBattle> battles) {
        int size = battles.size();
        if (size < 2) {
            return;
        }
        for (int i = 0; i <= size / 2 - 1 ; i++) {
            GameBattle temp = battles.get(i);
            battles.set(i, battles.get(size - 1 - i));
            battles.set(size - 1 - i, temp);
        }
    }

    private void notifyNewData(List<GameBattle> battles) {
        if (mOnBattleDataListener != null) {
            mOnBattleDataListener.onNewData(battles);
        }
    };

    private List<GameBattle> getBattlesFromServerResult(BattleRecords records, long serverTimestamp) {
        List<ServerBattleRecord> serverResults = records.getBattleList();
        if (serverResults == null || serverResults.size() == 0) {
            return null;
        }
        int size = serverResults.size();
        List<GameBattle> battles = new ArrayList<GameBattle>(size);
        for (int i = 0; i < size; i++) {
            ServerBattleRecord serverBattleRecord = serverResults.get(i);
            battles.add(createGameBattleFromServerRecord(serverBattleRecord));
        }
        long now = System.currentTimeMillis();
        for (int i = 0; i < size; i++) {
            GameBattle battle = battles.get(i);
            battle.localStartTime = now - (serverTimestamp - battle.createTime);
        }
        return battles;
    }

    private GameBattle createGameBattleFromServerRecord(ServerBattleRecord record) {
        GameBattle battle = new GameBattle();
        battle.createTime = record.createTime;
        battle.sessionId = record.sessionId;
        battle.opponentId = record.otherUserId;
        battle.gameId = record.gameId;
        battle.roomId = record.roomId;
        battle.direction = record.type == ServerBattleRecord.TYPE_SEND ?
                GameBattle.DIRECTION_SEND : GameBattle.DIRECTION_RECEIVE;
        int status;
        switch (record.messageDetailType) {
            case ServerBattleRecord.MESSAGE_TYPE_INVITE_WAITING:
                status = GameBattle.STATUS_INVITE_WAITING;
                break;
            case ServerBattleRecord.MESSAGE_TYPE_INVITE_CANCEL:
            case ServerBattleRecord.MESSAGE_TYPE_INVITE_DENY:
                status = GameBattle.STATUS_INVITE_CANCEL;
                break;
            case ServerBattleRecord.MESSAGE_TYPE_INVITE_SUCCESS:
                status = GameBattle.STATUS_GAMING;
                break;
            case ServerBattleRecord.MESSAGE_TYPE_RESULT_WIN:
                status = GameBattle.STATUS_WIN;
                break;
            case ServerBattleRecord.MESSAGE_TYPE_RESULT_DRAW:
                status = GameBattle.STATUS_DRAW;
                break;
            case ServerBattleRecord.MESSAGE_TYPE_RESULT_LOSE:
                status = GameBattle.STATUS_LOSE;
                break;
            case ServerBattleRecord.MESSAGE_TYPE_BECOME_FRIEND:
                status = GameBattle.STATUS_BECOME_FRIEND;
                break;
            default:
                status = GameBattle.STATUS_INVITE_CANCEL;
                break;
        }
        battle.status = status;
        return battle;
    }

    public void inviteGame(final long opponentId, int gameId, boolean isFriend) {
        mBattleService.inviteMatch(gameId, opponentId, isFriend).enqueue(new Callback<Result<ServerBattleRecord>>() {
            @Override
            public void onResponse(Call<Result<ServerBattleRecord>> call, Response<Result<ServerBattleRecord>> response) {
                if (response.code() != 200) {
                    notifyRequestError();
                    return;
                }
                ServerBattleRecord serverBattleRecord = response.body().getData();
                LogUtil.d(TAG, "invite res " + response.body().getCode() + " " + response.body().getMsg());
                if (serverBattleRecord == null) {
                    notifyRequestError();
                    return;
                }
                final GameBattle battle = new GameBattle();
                battle.createTime = serverBattleRecord.createTime;
                battle.sessionId = serverBattleRecord.sessionId;
                battle.gameId = serverBattleRecord.gameId;
                battle.localStartTime = System.currentTimeMillis();
                battle.opponentId = opponentId;
                battle.status = GameBattle.STATUS_INVITE_WAITING;
                battle.direction = GameBattle.DIRECTION_SEND;

                mDiskExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mBattleDao.addBattle(battle);
                        } catch (SQLiteException e) {
                            LogUtil.d("battle", e.toString());
                        }
                    }
                });

                if (opponentId == mCurrentOpponentId && mOnBattleDataListener != null) {
                    ArrayList<GameBattle> newData = new ArrayList<>(1);
                    newData.add(battle);
                    mOnBattleDataListener.onNewData(newData);
                }

            }

            @Override
            public void onFailure(Call<Result<ServerBattleRecord>> call, Throwable t) {
                notifyRequestError();
            }
        });
    }

    private void notifyRequestError() {
        if (mOnBattleDataListener != null) {
            mOnBattleDataListener.onRequestFail();
        }
    }

    public void acceptInvitation(int gameId, String sessionId, long opponentId) {
        mBattleService.acceptInvite(gameId, sessionId, opponentId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                notifyRequestError();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInvitationEvent(InvitationEvent event) {
        if (mCurrentOpponentId == INVALID_USER_ID) {
            // 不在聊天界面
            return;
        }

        String type = event.getType();
        String content = event.getContent();
        if (TextUtils.isEmpty(content)) {
            return;
        }
        ServerBattleRecord record;
        try {
            record = GlobalGson.get().fromJson(content, ServerBattleRecord.class);
        } catch (JsonSyntaxException e) {
            return;
        }

        if (mCurrentOpponentId != record.otherUserId) {
            return;
        }

        final GameBattle battle = createGameBattleFromServerRecord(record);

        if (InvitationEvent.PUSH_EVENT_INVITATION.equals(type)) {
            battle.localStartTime = System.currentTimeMillis();
            mDiskExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        mBattleDao.addBattle(battle);
                    } catch (SQLiteException e) {
                        LogUtil.d(TAG, e.toString());
                    }
                }
            });

            if (mOnBattleDataListener != null) {
                ArrayList<GameBattle> newData = new ArrayList<>(1);
                newData.add(battle);
                mOnBattleDataListener.onNewData(newData);
            }
        } else if (InvitationEvent.PUSH_EVENT_INVITATION_CANCEL.equals(type)
                || InvitationEvent.PUSH_EVENT_INVITATION_REJECT.equals(type)) {
            if (mOnBattleDataListener != null) {
                mOnBattleDataListener.onUpdateData(battle);
            }
        } else if (InvitationEvent.PUSH_EVENT_INVITATION_SUCCESS.equals(type)) {
            if (mOnBattleDataListener != null) {
                mOnBattleDataListener.onStartGame(battle);
            }
        }
    }

    @Subscribe
    public void onLeaveRoomEvent(LeaveRoomEvent event) {
        String content = event.getContent();
        ServerBattleRecord record;
        try {
            record = GlobalGson.get().fromJson(content, ServerBattleRecord.class);
        } catch (JsonSyntaxException e) {
            return;
        }

        if (record != null && record.otherUserId == mCurrentOpponentId) {
            if (mOnBattleDataListener != null) {
                mOnBattleDataListener.onTempSessionClosed();
            }
        }
    }

    public void leaveTempSession(long opponentId) {
        mBattleService.leaveMatch(opponentId).enqueue(mEmptyCallback);
        mAddingFriendId = INVALID_USER_ID;
    }

    public void requestAddFriend(long userId) {
        mAddingFriendId = userId;
        ApiServiceManager.obtain(InviteService.class).addFriend(String.valueOf(userId))
                .enqueue(new OnCallback<FriendRelation>() {
                    @Override
                    public void onResponse(FriendRelation result) {
                    }
                    @Override
                    public void onFailure(FriendRelation result) {
                    }
                });
    }

    public boolean isAddingFriend(long userId) {
        return userId == mAddingFriendId;
    }

    public ArrayList<GameProvider.GameProfile> getAllGames() {
        return mAllGames;
    }

    public GameProvider.GameProfile getGameProfile(int gameId) {
        int size = mAllGames.size();
        for (int i = 0; i < size; i++) {
            GameProvider.GameProfile profile = mAllGames.get(i);
            if (Integer.valueOf(profile.id) == gameId) {
                return profile;
            }
        }
        return null;
    }

    public void updateGamesInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Collection<GameProvider.GameProfile> games = GameProvider.get().list();
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onGetNewGamesData(games);
                    }
                });
            }
        }).start();
    }

    private void onGetNewGamesData(Collection<GameProvider.GameProfile> games) {
        if (games == null) {
            return;
        }
        mAllGames.clear();
        mAllGames.addAll(games);
        if (mOnBattleDataListener != null) {
            mOnBattleDataListener.onNewGameLists(mAllGames);
        }
    }

    public GameProvider.GameProfile getGameInfo(int gameId) {
        int size = mAllGames.size();
        for (int i = 0; i < size; i++) {
            GameProvider.GameProfile game = mAllGames.get(i);
            if (Integer.valueOf(game.id) == gameId) {
                return game;
            }
        }
        return null;
    }

    public void updateGameBattle(final GameBattle battle) {
        mDiskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mBattleDao.updateBattles(battle);
            }
        });
    }

    @Subscribe
    public void onLogoutEvent(AccountEventController.LogoutEvent event) {
        mDiskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mBattleDao.deleteAllDatas();
            }
        });
    }

    @Subscribe
    public void onGameOverMsg(GameOverMsg msg) {
        LogUtil.d(TAG, "game result " + msg.sessionId + " " + msg.gameResult);
        final long opponentId = msg.userId;
        final String sessionId = msg.sessionId;
        final int status;
        if (BattleUtils.GAME_OVER_RESULT_WIN.equals(msg.gameResult)) {
            status = GameBattle.STATUS_WIN;
        } else if (BattleUtils.GAME_OVER_RESULT_DOGFALL.equals(msg.gameResult)) {
            status = GameBattle.STATUS_DRAW;
        } else if (BattleUtils.GAME_OVER_RESULT_LOSE.equals(msg.gameResult)) {
            status = GameBattle.STATUS_LOSE;
        } else {
            status = GameBattle.STATUS_GAMING;
        }
        mDiskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                GameBattle battle = mBattleDao.getBattle(opponentId, sessionId);
                if (battle != null) {
                    battle.status = status;
                    mBattleDao.updateBattles(battle);
                }
            }
        });
    }

    public void sendPendingCancelEvent() {
        // 主动退出聊天界面时，给首页发个 event ，让其更新消息列表的数据
        final InvitationEvent cancel = new InvitationEvent(
                InvitationEvent.PUSH_EVENT_INVITATION_CANCEL, null);
        mMainThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                EventBus.getDefault().post(cancel);
            }
        }, 1200);
    }
 }
