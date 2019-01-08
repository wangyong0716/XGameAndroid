package com.xgame.battle.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.xgame.battle.BattleConstants;

/**
 * Created by zhanglianyu on 18-1-30.
 */

public class Player implements Parcelable {
    private long mUserId;
    private String mToken;
    private String mName;
    private String mAvatar;
    private int mAge;
    private int mGender;
    private int mScore = 0;
    private boolean mIsFriend;

    public Player() {
    }

    private Player(Parcel parcel) {
        mUserId = parcel.readLong();
        mToken = parcel.readString();
        mName = parcel.readString();
        mAvatar = parcel.readString();
        mAge = parcel.readInt();
        mGender = parcel.readInt();
        mScore = parcel.readInt();
        mIsFriend = parcel.readByte() != 1;
    }

    public long getUserId() {
        return mUserId;
    }

    public Player setUserId(long userId) {
        mUserId = userId;
        return this;
    }

    public String getToken() {
        return mToken;
    }

    public Player setToken(String token) {
        mToken = token;
        return this;
    }

    public String getName() {
        return mName;
    }

    public Player setName(String name) {
        mName = name;
        return this;
    }

    public String getAvatar() {
        return mAvatar;
    }

    public Player setAvatar(String avatar) {
        mAvatar = avatar;
        return this;
    }

    public int getAge() {
        return mAge;
    }

    public Player setAge(int age) {
        mAge = age;
        return this;
    }

    public int getGender() {
        return mGender;
    }

    public Player setGender(int gender) {
        mGender = gender;
        return this;
    }

    public boolean isFriend() {
        return mIsFriend;
    }

    public void setFriend(boolean friend) {
        mIsFriend = friend;
    }

    public static Player getInstance(ServerPlayer serverPlayer) {
        if (serverPlayer == null) {
            return null;
        }
        Player player = new Player();
        player.setUserId(serverPlayer.getUserId());
        player.setName(serverPlayer.getNickName());
        player.setAvatar(serverPlayer.getAvatar());
        player.setAge(serverPlayer.getAge());
        player.setGender(serverPlayer.getGender());
        player.setFriend(serverPlayer.getFriendStatus() == BattleConstants.RELATIONSHIP_FRIENDS);
        return player;
    }

    public int getScore() {
        return mScore;
    }

    public Player setScore(int score) {
        mScore = score;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(mUserId);
        parcel.writeString(mToken);
        parcel.writeString(mName);
        parcel.writeString(mAvatar);
        parcel.writeInt(mAge);
        parcel.writeInt(mGender);
        parcel.writeInt(mScore);
        parcel.writeByte((byte) (mIsFriend ? 1 : 0));
    }

    public static final Parcelable.Creator<Player> CREATOR = new Parcelable.Creator<Player>() {
        @Override
        public Player createFromParcel(Parcel parcel) {
            return new Player(parcel);
        }

        @Override
        public Player[] newArray(int i) {
            return new Player[i];
        }
    };

    @Override
    public String toString() {
        return "Player{" +
                "mUserId=" + mUserId +
                ", mToken='" + mToken + '\'' +
                ", mName='" + mName + '\'' +
                ", mAvatar='" + mAvatar + '\'' +
                ", mAge=" + mAge +
                ", mGender=" + mGender +
                ", mScore=" + mScore +
                ", mIsFriend=" + mIsFriend +
                '}';
    }
}
