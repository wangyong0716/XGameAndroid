package com.xgame.battle;

/**
 * Created by wangyong on 18-1-30.
 */

public class BattleConstants {
    public static final String TOKEN = "TOKEN";
    public static final String BATTLE_ROOM_ID = "BATTLE_ROOM_ID";
    public static final String BATTLE_GAME_URL = "BATTLE_GAME_URL";
    public static final String BATTLE_GAME_NAME = "BATTLE_GAME_NAME";

    public static final String RESULT_WIN_SELF = "RESULT_WIN_SELF";
    public static final String RESULT_WIN_PEER = "RESULT_WIN_PEER";
    public static final String BATTLE_RESULT = "BATTLE_RESULT";

    public static final String BATTLE_WIN_COIN = "BATTLE_WIN_COIN";
    public static final String BATTLE_LOSE_COIN = "BATTLE_LOSE_COIN";

    public static final String BATTLE_TYPE = "BATTLE_TYPE";
    public static final int BATTLE_TYPE_MATCH = 1;
    public static final int BATTLE_TYPE_COIN = 2;
    public static final int BATTLE_TYPE_BW = 3;

    public static final String BATTLE_GAME_ID = "BATTLE_GAME_ID";
    public static final String BATTLE_RULE_ID = "BATTLE_RULE_ID";
    public static final String BATTLE_RULE_TITLE = "BATTLE_RULE_TITLE";
    public static final String BATTLE_PLAYER_SELF = "BATTLE_PLAYER_SELF";
    public static final String BATTLE_PLAYER_PEER = "BATTLE_PLAYER_PEER";

    public static final String BATTLE_GAME_TITLE = "BATTLE_GAME_TITLE";
    public static final String BATTLE_RULE_DESC = "BATTLE_RULE_DESC";
    public static final String COIN = "coin";

    public static final String IS_FRIEND = "IS_FRIEND";
    public static final String SESSION_ID = "session_id";

    public static final int COIN_BATTLE_PLAYTYPE_MATCH = 1;
    public static final int COIN_BATTLE_PLAYTYPE_MULTI = 2;

    //服务端好友关系常量
    public static final int RELATIONSHIP_STRANGER = 0;
    public static final int RELATIONSHIP_SENDER_UNVERIFY = 1;
    public static final int RELATIONSHIP_RECEIVER_UNVERIFY = 2;
    public static final int RELATIONSHIP_FRIENDS = 3;
    public static final int RELATIONSHIP_BLACKLIST = 4;

    public static final int COIN_TYPE_YELLOW = 1;
    public static final int COIN_TYPE_BLUE = 2;

    public static final int BONUS_STATUS_IMMEDIATE = 1;
    public static final int BONUS_STATUS_DELAY = 2;

    public static final String MATCH_FROM = "MATCH_FROM";
    public static final String MATCH_FROM_CHANGE_PEER = "change_peer";

    // 百万场活动
    public static final String BW_ID = "BW_ID";
    public static final String BW_BATTLE_DETAIL = "BW_BATTLE_DETAIL";
    public static final String BW_BATTLE_MATCH_RESULT = "BW_BATTLE_MATCH_RESULT";
    public static final String BW_RESTART_TIME = "BW_RESTART_TIME";
    public static final String BW_BATTLE_QUIT = "BW_BATTLE_QUIT";
    public static final String BW_BONUS = "BW_BONUS";
    public static final String BW_BATTLE_ROUND = "BW_BONUS_ROUND";
    public static final String BW_REVIVE_COUNT = "BW_REVIVE_COUNT";
    //复活常量定义
    public static final int BW_REVIVE_SUCCESS = 1;
    public static final int BW_REVIVE_COIN_LACK = 2;
    public static final int BW_REVIVE_COUNT_LIMITATION = 3;
    public static final int BW_REVIVE_ROUND_LIMITATION = 4;
    public static final int BW_REVIVE_DEFAULT = 0;
}
