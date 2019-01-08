package com.xgame.chat.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "battle")
public class GameBattle {

    public static final int STATUS_WIN    = 1;
    public static final int STATUS_DRAW   = 2;
    public static final int STATUS_LOSE   = 3;
    public static final int STATUS_INVITE_WAITING = 4;
    public static final int STATUS_INVITE_CANCEL = 5;
    public static final int STATUS_GAMING = 6;
    public static final int STATUS_BECOME_FRIEND = 7;

    public static final int DIRECTION_SEND = 1;
    public static final int DIRECTION_RECEIVE = 2;

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "session_id")
    public String sessionId;

    @ColumnInfo(name = "opponent_id")
    public long opponentId;

    @ColumnInfo(name = "game_id")
    public int gameId;

    @Ignore
    public String roomId;

    public int status;
    public int direction;

    // local time，倒计时用
    @Ignore
    public long localStartTime;

    // 服务端时间，真正的排序依据
    @ColumnInfo(name = "create_time")
    public long createTime;

    public GameBattle() {
    }

}
