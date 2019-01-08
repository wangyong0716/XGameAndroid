package com.xgame.invite.model;

import com.xgame.base.api.DataProtocol;

/**
 * Created by Albert
 * on 18-2-4.
 */

public class FriendRelation implements DataProtocol {

    private long userId;
    private long friendId;
    private int status;
    private long updateTime;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getFriendId() {
        return friendId;
    }

    public void setFriendId(long friendId) {
        this.friendId = friendId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "FriendRelation{" +
                "userId=" + userId +
                ", friendId=" + friendId +
                ", status=" + status +
                ", updateTime=" + updateTime +
                '}';
    }
}
