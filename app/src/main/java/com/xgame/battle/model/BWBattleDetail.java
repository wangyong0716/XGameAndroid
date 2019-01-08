package com.xgame.battle.model;

import com.xgame.base.api.DataProtocol;

/**
 * Created by zhanglianyu on 18-2-1.
 */

public class BWBattleDetail implements DataProtocol {

    private static final long  INTERLUDE_VIDEO_LENGTH = (5 * 60 + 10) * 1000L; // 5分10秒

    private long bwId; // long
    private long userId; // long，用户id
    private String title; // string
    private String subTitle; // string，游戏的副标题
    private String shownBonus; // string

    private String imageBanner; // string, 对战详情页上方显示的图片url
    private String imageRule; // string, 对战规则界面视频的封面url

    private String videoRule; // string, 对战规则视频地址url
    private String videoTransition; // string, 串场视频地址url

    private String ruleVideoSize; // string, 播放对战规则视频所需要的流量提示
    private String ruleVideoTitle; // string, 播放对战规则界面的文字

    private int restartCoins; // int, 复活需要的金币数目

    private long gameId; // long, gameId
    private String gameIcon; // string, 游戏图片url
    private String gameUrl; // string, 游戏地址url

    private int gameTotalRoundNum; // int,一场总共多少轮
    private long gameLength; // long， 游戏时长，单位：毫秒

    private long showStartTime; // long, 对战展示开始的时间戳，是百万场整个开始的时间(而不是某一个轮次开始的时间)，这个时间只是用来播放视频的（举例3：00）
    private String showStartTimeStr; // string, 对战展示开始的时间，展示在百万场detail界面，如上图中的“21:00”，是一个24时制的字符串（小时：分）

    private long openDoorTime; // long, 真正开始调用进入接口的时间（举例3：03）
    private long closeDoorTime; // long, 限制用户进入的时间戳，之后不允许用户进入（举例3：04）
    private long realStartTime; // long, 真正开始的时间。（举例3：05）
    private long serverTime; // long, 服务当前时间
    private long clientTime; // long

    private long totalBonus; // long, 累积金额，单位人民币分
    private long cashBonus; // long, 可提现金额，单位人民币分

    public long getBwId() {
        return bwId;
    }

    public void setBwId(long bwId) {
        this.bwId = bwId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getImageBanner() {
        return imageBanner;
    }

    public void setImageBanner(String imageBanner) {
        this.imageBanner = imageBanner;
    }

    public String getImageRule() {
        return imageRule;
    }

    public void setImageRule(String imageRule) {
        this.imageRule = imageRule;
    }

    public String getVideoRule() {
        return videoRule;
    }

    public void setVideoRule(String videoRule) {
        this.videoRule = videoRule;
    }

    public String getVideoTransition() {
        return videoTransition;
    }

    public void setVideoTransition(String videoTransition) {
        this.videoTransition = videoTransition;
    }

    public String getRuleVideoSize() {
        return ruleVideoSize;
    }

    public void setRuleVideoSize(String ruleVideoSize) {
        this.ruleVideoSize = ruleVideoSize;
    }

    public String getRuleVideoTitle() {
        return ruleVideoTitle;
    }

    public void setRuleVideoTitle(String ruleVideoTitle) {
        this.ruleVideoTitle = ruleVideoTitle;
    }

    public int getRestartCoins() {
        return restartCoins;
    }

    public void setRestartCoins(int restartCoins) {
        this.restartCoins = restartCoins;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public String getGameIcon() {
        return gameIcon;
    }

    public void setGameIcon(String gameIcon) {
        this.gameIcon = gameIcon;
    }

    public String getGameUrl() {
        return gameUrl;
    }

    public void setGameUrl(String gameUrl) {
        this.gameUrl = gameUrl;
    }

    public int getGameTotalRoundNum() {
        return gameTotalRoundNum;
    }

    public void setGameTotalRoundNum(int gameTotalRoundNum) {
        this.gameTotalRoundNum = gameTotalRoundNum;
    }

    public long getGameLength() {
        return gameLength;
    }

    public void setGameLength(long gameLength) {
        this.gameLength = gameLength;
    }

    public long getShowStartTime() {
        return showStartTime;
    }

    public void setShowStartTime(long showStartTime) {
        this.showStartTime = showStartTime;
    }

    public String getShowStartTimeStr() {
        return showStartTimeStr;
    }

    public void setShowStartTimeStr(String showStartTimeStr) {
        this.showStartTimeStr = showStartTimeStr;
    }

    public long getOpenDoorTime() {
        return openDoorTime;
    }

    public void setOpenDoorTime(long openDoorTime) {
        this.openDoorTime = openDoorTime;
    }

    public long getCloseDoorTime() {
        return closeDoorTime;
    }

    public void setCloseDoorTime(long closeDoorTime) {
        this.closeDoorTime = closeDoorTime;
    }

    public long getRealStartTime() {
        return realStartTime;
    }

    public void setRealStartTime(long realStartTime) {
        this.realStartTime = realStartTime;
    }

    public long getServerTime() {
        return serverTime;
    }

    public void setServerTime(long serverTime) {
        this.serverTime = serverTime;
    }

    public long getClientTime() {
        return clientTime;
    }

    public void setClientTime(long clientTime) {
        this.clientTime = clientTime;
    }

    public long getTotalBonus() {
        return totalBonus;
    }

    public void setTotalBonus(long totalBonus) {
        this.totalBonus = totalBonus;
    }

    public long getCashBonus() {
        return cashBonus;
    }

    public void setCashBonus(long cashBonus) {
        this.cashBonus = cashBonus;
    }

    public String getShownBonus() {
        return shownBonus;
    }

    public void setShownBonus(String shownBonus) {
        this.shownBonus = shownBonus;
    }

    public long getStartInterludeVideoPos() {
        if (getClientTime() <= 0) {
            return 0;
        }
        final long current = System.currentTimeMillis();
        final long startInterludeClientTime = getClientTime() +
                (getShowStartTime() - getServerTime());
        if (current <= startInterludeClientTime) {
            return 0;
        }
        final long over = current - startInterludeClientTime;
        if (over > INTERLUDE_VIDEO_LENGTH) {
            return INTERLUDE_VIDEO_LENGTH;
        }
        return over;
    }

    @Override
    public String toString() {
        return "BWBattleDetail{" +
                "bwId=" + bwId +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", subTitle='" + subTitle + '\'' +
                ", shownBonus='" + shownBonus + '\'' +
                ", imageBanner='" + imageBanner + '\'' +
                ", imageRule='" + imageRule + '\'' +
                ", videoRule='" + videoRule + '\'' +
                ", videoTransition='" + videoTransition + '\'' +
                ", ruleVideoSize='" + ruleVideoSize + '\'' +
                ", ruleVideoTitle='" + ruleVideoTitle + '\'' +
                ", restartCoins=" + restartCoins +
                ", gameId=" + gameId +
                ", gameIcon='" + gameIcon + '\'' +
                ", gameUrl='" + gameUrl + '\'' +
                ", gameTotalRoundNum=" + gameTotalRoundNum +
                ", gameLength=" + gameLength +
                ", showStartTime=" + showStartTime +
                ", showStartTimeStr='" + showStartTimeStr + '\'' +
                ", openDoorTime=" + openDoorTime +
                ", closeDoorTime=" + closeDoorTime +
                ", realStartTime=" + realStartTime +
                ", serverTime=" + serverTime +
                ", clientTime=" + clientTime +
                ", totalBonus=" + totalBonus +
                ", cashBonus=" + cashBonus +
                '}';
    }
}
