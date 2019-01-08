package com.xgame.home.model;

import com.xgame.base.api.DataProtocol;
import com.xgame.common.api.IProtocol;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 * <p>
 * Created by jackwang
 * on 18-1-31.
 */


public class MessageSession implements DataProtocol {

    public static final int viewType = ItemType.TYPE_MSG_BAR;

    public static final int GENDER_MALE = 1;

    public static final int GENDER_FEMALE = 2;

    public static final int STATUS_STRANGER = 0;

    public static final int STATUS_PRE_FRIEND = 1;

    public static final int STATUS_FRIEND = 2;

    public static final int STATUS_BLACK_USER = 3;

    // 1:邀战中, 2:邀战取消, 3:邀战拒绝, 4:邀战成功, 5:对战胜利， 6:对战失败, 7:对战平局
    public static final int MSG_INVITATION_ING = 1;

    public static final int MSG_INVITATION_CANCEL = 2;

    public static final int MSG_INVITATION_REJECT = 3;

    public static final int MSG_INVITATION_SUCC = 4;

    public static final int MSG_WIN = 5;

    public static final int MSG_LOSE = 6;

    public static final int MSG_DRAW = 7;

    public static final int MSG_FRIEND_PASS = 10;

    public static final int SESSION_SEND = 1;

    public static final int SESSION_RECEIVE = 2;

    public long userId;

    public long otherUserId;

    public int type;

    public String sessionId;

    public int messageDetailType;

    public int gameId;

    public long createTime;

    public int friendStatus;

    public Other otherUserInfo;

    public boolean needRemind() {
        return MSG_INVITATION_ING == messageDetailType;
    }

    public String otherName() {
        return otherUserInfo != null ? otherUserInfo.nickname : "";
    }

    public String otherAvatar() {
        return otherUserInfo != null ? otherUserInfo.headimgurl : "";
    }

    public static class Other implements IProtocol {

        public String nickname;

        public String headimgurl;

        public int sex;

        public int age;
    }

    public boolean isStranger() {
        return friendStatus == STATUS_STRANGER || friendStatus == STATUS_PRE_FRIEND;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MessageSession)) {
            return false;
        }
        final MessageSession that = (MessageSession) obj;
        return this.userId == that.userId && this.otherUserId == that.otherUserId;
    }
}
