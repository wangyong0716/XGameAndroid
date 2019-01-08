package com.xgame.invite.model;

import android.content.Context;

import com.xgame.R;
import com.xgame.account.model.User;
import com.xgame.base.api.DataProtocol;
import com.xgame.home.model.MessageSession;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by Albert
 * on 18-1-28.
 */

public class InvitedUser implements DataProtocol, Serializable {

    public static final int STRANGER = 0;
    public static final int WAIT_FRIEND = 1;
    public static final int WAIT_CONFIRM = 2;
    public static final int FRIEND = 3;
    public static final int BLACKLIST = 4;

    private String avatar;
    private String accountId;
    private String phone;
    private String nickname;
    private int gender = -1;
    private int age;
    private String message;
    private int relative;
    private String location;
    private String constellation;
    private GameInfo[] freqPlay;

    private char factor;
    private boolean head;
    private String contactName;

    public InvitedUser() {

    }

    public InvitedUser(MessageSession record) {
        this.relative = record.friendStatus;
        this.accountId = String.valueOf(record.otherUserId);
        MessageSession.Other other = record.otherUserInfo;
        if (other != null) {
            this.nickname = other.nickname;
            this.gender = other.sex;
            this.age = other.age;
            this.avatar = other.headimgurl;
        }
    }

    public InvitedUser(String accountId, String nickname, int gender, int age, int relative) {
        this.accountId = accountId;
        this.nickname = nickname;
        this.gender = gender;
        this.age = age;
        this.relative = relative;
    }

    public InvitedUser(String accountId, String nickname, String message, int gender, int relative) {
        this.accountId = accountId;
        this.nickname = nickname;
        this.message = message;
        this.gender = gender;
        this.relative = relative;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setRelative(int relative) {
        this.relative = relative;
    }

    public boolean isHead() {
        return head;
    }

    public void setHead(boolean head) {
        this.head = head;
    }

    public char getFactor() {
        return factor;
    }

    public void setFactor(char factor) {
        this.factor = factor;
    }

    public String getAccountId() {
        return accountId;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean hasGames() {
        return freqPlay != null && freqPlay.length > 0;
    }

    public GameInfo[] getFreqPlay() {
        return freqPlay;
    }

    public void setFreqPlay(GameInfo[] freqPlay) {
        this.freqPlay = freqPlay;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getConstellation() {
        return constellation;
    }

    public void setConstellation(String constellation) {
        this.constellation = constellation;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isNeedActive() {
        return isStranger() || isWaitConfirm();
    }

    public boolean isCouldDelete() {
        return !isStranger() && !isInBlacklist();
    }

    public boolean isStranger() {
        return relative == STRANGER;
    }

    public boolean isWaitConfirm() {
        return relative == WAIT_CONFIRM;
    }

    public boolean isInBlacklist() {
        return relative == BLACKLIST;
    }

    public String getRelationString(Context context) {
        if (context == null) {
            return "";
        }
        String str;
        switch (relative) {
            default:
            case STRANGER:
                str = context.getString(R.string.add_friend);
                break;
            case WAIT_FRIEND:
                str = context.getString(R.string.wait_friend);
                break;
            case FRIEND:
                str = context.getString(R.string.active_friend);
                break;
            case BLACKLIST:
                str = context.getString(R.string.blacklist);
                break;
            case WAIT_CONFIRM:
                str = context.getString(R.string.wait_confirm);
                break;
        }
        return str;
    }

    public String getAgeString(Context context) {
        if (context == null) {
            return String.valueOf(age);
        } else {
            return context.getResources().getString(R.string.age_format, age);
        }
    }

    public String getGenderString(Context context) {
        if (context == null) {
            return "";
        }
        String str;
        switch (gender) {
            case User.GENDER_MALE:
                str = context.getResources().getString(R.string.male_text);
                break;
            case User.GENDER_FEMALE:
                str = context.getResources().getString(R.string.female_text);
                break;
            default:
                str = "";
                break;
        }
        return str;
    }

    @Override
    public String toString() {
        return "InvitedUser{" +
                "avatar='" + avatar + '\'' +
                ", accountId='" + accountId + '\'' +
                ", phone='" + phone + '\'' +
                ", nickname='" + nickname + '\'' +
                ", gender='" + gender + '\'' +
                ", age='" + age + '\'' +
                ", message='" + message + '\'' +
                ", relative=" + relative +
                ", location='" + location + '\'' +
                ", constellation='" + constellation + '\'' +
                ", freqPlay=" + Arrays.toString(freqPlay) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InvitedUser)) {
            return false;
        }
        final InvitedUser that = (InvitedUser) o;
        return accountId != null ? accountId.equals(that.accountId) : that.accountId == null;
    }

    @Override
    public int hashCode() {
        return accountId != null ? accountId.hashCode() : 0;
    }
}
