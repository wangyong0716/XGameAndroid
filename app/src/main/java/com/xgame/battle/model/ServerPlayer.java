package com.xgame.battle.model;

import com.xgame.base.api.DataProtocol;

/**
 * Created by wangyong on 18-1-30.
 */

public class ServerPlayer implements DataProtocol {
    private String nickName;
    private long userId;
    private String avatar;
    private int gender;
    private int age;
    private int friendStatus;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getFriendStatus() {
        return friendStatus;
    }

    public void setFriendStatus(int friendStatus) {
        this.friendStatus = friendStatus;
    }

    @Override
    public String toString() {
        return "ServerPlayer{" +
                "nickName='" + nickName + '\'' +
                ", userId='" + userId + '\'' +
                ", avatar='" + avatar + '\'' +
                ", gender=" + gender +
                ", age=" + age +
                ", friendStatus=" + friendStatus +
                '}';
    }
}
