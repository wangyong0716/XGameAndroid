package com.xgame.battle.model;

import com.xgame.base.api.DataProtocol;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by wangyong on 18-1-30.
 */

public class ServerCoinGame implements DataProtocol {
    private String gameId;
    private String gameName;
    private int coin;
    private String imgUrl;
    private String ruleDesc;
    private int playType;
    private String gameUrl;
    private ServerCoinItem[] items;

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public int getCoin() {
        return coin;
    }

    public void setCoin(int coin) {
        this.coin = coin;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getRuleDesc() {
        return ruleDesc;
    }

    public void setRuleDesc(String ruleDesc) {
        this.ruleDesc = ruleDesc;
    }

    public int getPlayType() {
        return playType;
    }

    public void setPlayType(int playType) {
        this.playType = playType;
    }

    public ServerCoinItem[] getItems() {
        return items;
    }

    public void setItems(ServerCoinItem[] items) {
        this.items = items;
    }

    public String getGameUrl() {
        return gameUrl;
    }

    public void setGameUrl(String gameUrl) {
        this.gameUrl = gameUrl;
    }

    @Override
    public String toString() {
        return "ServerCoinGame{" +
                "gameId='" + gameId + '\'' +
                ", gameName='" + gameName + '\'' +
                ", coin=" + coin +
                ", imgUrl='" + imgUrl + '\'' +
                ", ruleDesc='" + ruleDesc + '\'' +
                ", playType=" + playType +
                ", gameUrl='" + gameUrl + '\'' +
                ", items=" + Arrays.toString(items) +
                '}';
    }
}
