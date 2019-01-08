package com.xgame.battle.model;

// http://wiki.n.miui.com/pages/viewpage.action?pageId=68695070

import android.support.annotation.Keep;

@Keep
public class ServerBattleRecord {
    public static final int TYPE_SEND = 1;
    public static final int TYPE_RECEIVE = 2;

    public static final int MESSAGE_TYPE_INVITE_WAITING = 1;
    public static final int MESSAGE_TYPE_INVITE_CANCEL = 2;
    public static final int MESSAGE_TYPE_INVITE_DENY = 3;
    public static final int MESSAGE_TYPE_INVITE_SUCCESS = 4;
    public static final int MESSAGE_TYPE_RESULT_WIN = 5;
    public static final int MESSAGE_TYPE_RESULT_LOSE = 6;
    public static final int MESSAGE_TYPE_RESULT_DRAW = 7;
    public static final int MESSAGE_TYPE_SESSION_LEAVE = 8;
    public static final int MESSAGE_TYPE_SESSION_ANOTHER_GAME = 9;
    public static final int MESSAGE_TYPE_BECOME_FRIEND = 10;

    public long createTime;
    public int gameId;
    public int type;
    public int messageDetailType;
    public long otherUserId;
    public long userId;
    public String sessionId;
    public String roomId;
}
