package com.xgame.battle.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.xgame.base.api.DataProtocol;

import java.io.Serializable;

/**
 * Created by wangyong on 18-1-30.
 */

public class ServerCoinItem implements DataProtocol, Parcelable, Serializable {
    private static final long serialVersionUID = -3796732837053271079L;
    private int id;
    private String title;
    private int ticketGold;
    private int winnerGold;
    private String gameUrl;
    private int styleType;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTicketGold() {
        return ticketGold;
    }

    public void setTicketGold(int ticketGold) {
        this.ticketGold = ticketGold;
    }

    public int getWinnerGold() {
        return winnerGold;
    }

    public void setWinnerGold(int winnerGold) {
        this.winnerGold = winnerGold;
    }

    public String getGameUrl() {
        return gameUrl;
    }

    public void setGameUrl(String gameUrl) {
        this.gameUrl = gameUrl;
    }

    public int getStyleType() {
        return styleType;
    }

    public void setStyleType(int styleType) {
        this.styleType = styleType;
    }

    public ServerCoinItem() {
    }

    private ServerCoinItem(Parcel parcel) {
        id = parcel.readInt();
        title = parcel.readString();
        ticketGold = parcel.readInt();
        winnerGold = parcel.readInt();
        gameUrl = parcel.readString();
        styleType = parcel.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(title);
        parcel.writeInt(ticketGold);
        parcel.writeInt(winnerGold);
        parcel.writeString(gameUrl);
        parcel.writeInt(styleType);
    }

    public static final Parcelable.Creator<ServerCoinItem> CREATOR = new Parcelable.Creator<ServerCoinItem>() {
        @Override
        public ServerCoinItem createFromParcel(Parcel parcel) {
            return new ServerCoinItem(parcel);
        }

        @Override
        public ServerCoinItem[] newArray(int i) {
            return new ServerCoinItem[i];
        }
    };

    @Override
    public String toString() {
        return "ServerCoinItem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", ticketGold=" + ticketGold +
                ", winnerGold=" + winnerGold +
                ", gameUrl='" + gameUrl + '\'' +
                ", styleType=" + styleType +
                '}';
    }
}
