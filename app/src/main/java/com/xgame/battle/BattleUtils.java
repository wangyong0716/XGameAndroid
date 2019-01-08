package com.xgame.battle;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.xgame.account.UserManager;
import com.xgame.battle.model.Player;
import com.xgame.common.util.LogUtil;
import com.xgame.home.model.XGameItem;
import com.xgame.ui.activity.BWBattleActivity;
import com.xgame.ui.activity.BWBattleResultActivity;
import com.xgame.ui.activity.BWBattleRuleActivity;
import com.xgame.ui.activity.BWMatchActivity;
import com.xgame.ui.activity.BattleActivity;
import com.xgame.ui.activity.BattleActivity2;
import com.xgame.ui.activity.BattleResultActivity;
import com.xgame.ui.activity.ChatActivity;
import com.xgame.ui.activity.CoinBattleDetailActivity;
import com.xgame.ui.activity.CoinBattleResultActivity;
import com.xgame.ui.activity.MatchActivity;

/**
 * Created by zhanglianyu on 18-1-24.
 */

public final class BattleUtils {

    public static final String EXTRA_SELF_TOKEN = "EXTRA_SELF_TOKEN";

    public static final String EXTRA_WIN_SELF = "EXTRA_WIN_SELF";
    public static final String EXTRA_WIN_PEER = "EXTRA_WIN_PEER";

    public static final String URL_PARAM_TOKEN = "token";
    public static final String URL_PARAM_ROOMID = "roomId";
    public static final String URL_PARAM_BWID = "bwId";
    public static final String URL_PARAM_CLIENTINFO = "clientInfo";
    public static final String URL_PARAM_GAMEID = "gameId";
    public static final String URL_PARAM_GAMETYPE = "gameType";
    public static final String URL_PARAM_RULEID = "ruleId";

    public static final String JS_CALLBACK_GAME_OVER = "callNativeGameOver";
    public static final String GAME_OVER_RESULT_NULL = "0"; // 无胜负结果
    public static final String GAME_OVER_RESULT_LOSE = "1"; // 负
    public static final String GAME_OVER_RESULT_DOGFALL = "2"; // 平
    public static final String GAME_OVER_RESULT_WIN = "3"; // 胜

    public static final String RT_CALLBACK_ERROR = "@onError";

    public static final long SECOND_1 = 1000L;
    public static final long SECOND_10 = 10 * SECOND_1;
    public static final long SECOND_30 = 30 * SECOND_1;
    public static final long MINUTE_1 = 60 * SECOND_1;
    public static final long MINUTE_10 = 10 * MINUTE_1;


    private static final String TAG = "BattleUtils";

    /**
     * 真人场匹配成功开始对战
     * @param context
     * @param token
     */
    public static void gotoMatchBattle(final Activity context, final String token) {
        if (context == null) {
            return;
        }
        final Intent intent = getBattleIntent(context);
        intent.putExtra(BattleConstants.BATTLE_TYPE, BattleConstants.BATTLE_TYPE_MATCH);
        intent.putExtra(EXTRA_SELF_TOKEN, token);
        context.startActivity(intent);
    }

    /**
     * 真人场再来一局
     * @param context
     * @param sessionId
     * @param token
     */
    public static void gotoMatchBattle(final Activity context, final String sessionId, final String roomId, final String token) {
        if (context == null) {
            return;
        }
        BattleManager.getInstance().setSessionId(sessionId);
        BattleManager.getInstance().setRoomId(roomId);
        final Intent intent = getBattleIntent(context);
        intent.putExtra(BattleConstants.BATTLE_TYPE, BattleConstants.BATTLE_TYPE_MATCH);
        intent.putExtra(EXTRA_SELF_TOKEN, token);
        context.startActivity(intent);
    }

    /**
     * 金币场匹配模式开始对战
     * @param context
     * @param token
     */
    public static void gotoCoinBattle(final Activity context, final String token) {
        if (context == null) {
            return;
        }
        final Intent intent = getBattleIntent(context);
        intent.putExtra(BattleConstants.BATTLE_TYPE, BattleConstants.BATTLE_TYPE_COIN);
        intent.putExtra(EXTRA_SELF_TOKEN, token);
        context.startActivity(intent);
    }

    /**
     * 金币场多人模式开始对战
     * @param context
     * @param token
     */
    public static void gotoCoinBattle2(final Activity context, final String token) {
        if (context == null) {
            return;
        }
        final Intent intent = getBattleIntent2(context);
        intent.putExtra(BattleConstants.BATTLE_TYPE, BattleConstants.BATTLE_TYPE_COIN);
        intent.putExtra(EXTRA_SELF_TOKEN, token);
        context.startActivity(intent);
    }

    public static void gotoBWBattle(final Activity context, final String token) {
        if (context == null) {
            return;
        }
        final Intent intent = getBattleIntent(context);
        intent.putExtra(BattleConstants.BATTLE_TYPE, BattleConstants.BATTLE_TYPE_BW);
        intent.putExtra(EXTRA_SELF_TOKEN, token);
        context.startActivity(intent);
    }

    /**
     * 好友列表进入游戏
     * @param context
     * @param gameId
     * @param gameUrl
     * @param gameName
     * @param isFriend
     * @param player
     */

    public static void gotoBattleFromFriends(final Activity context, int gameId, String gameUrl, String gameName,
                                             String roomId, String sessionId, boolean isFriend, Player player) {
        if (context == null) {
            return;
        }

        BattleManager.getInstance().clearAll();
        BattleManager.getInstance().setGameId(gameId);
        BattleManager.getInstance().setGameType(BattleConstants.BATTLE_TYPE_MATCH);
        BattleManager.getInstance().setGameUrl(gameUrl);
        BattleManager.getInstance().setPeerPlayer(player);
        BattleManager.getInstance().setIsFriend(isFriend);
        BattleManager.getInstance().setGameName(gameName);
        BattleManager.getInstance().setSessionId(sessionId);
        BattleManager.getInstance().setRoomId(roomId);

        final Intent intent = getBattleIntent(context);
        intent.putExtra(BattleConstants.BATTLE_TYPE, BattleConstants.BATTLE_TYPE_MATCH);
        intent.putExtra(EXTRA_SELF_TOKEN, UserManager.getInstance().getToken());
        context.startActivity(intent);
    }

    /**
     * 好友列表进入游戏
     */
    public static void gotoBattleFromFriends(final Activity context,
                                             int gameId, String gameUrl, String gameName, String roomId,
                                             String sessionId, boolean isFriend,long userId, String userName,
                                             String avatar, int age, int gender) {
        Player player = new Player();
        player.setUserId(userId);
        player.setName(userName);
        player.setAge(age);
        player.setGender(gender);
        player.setAvatar(avatar);
        gotoBattleFromFriends(context, gameId, gameUrl,gameName, roomId, sessionId, isFriend, player);
    }

    public static void gotoBWBattleCover(final Activity context, final long bwId) {
        if (context == null) {
            LogUtil.i(TAG, "gotoBattle() : context null, return.");
            return;
        }
        BWBattleManager.getInstance().setBWId(bwId);
        final Intent intent = new Intent().setClass(context.getApplicationContext(),
                BWBattleActivity.class);
        context.startActivity(intent);
    }

    private static Intent getBattleIntent(final Activity context) {
        return new Intent().setClass(context.getApplicationContext(),
                BattleActivity.class);
    }

    private static Intent getBattleIntent2(final Activity context) {
        return new Intent().setClass(context.getApplicationContext(),
                BattleActivity2.class);
    }

    public static void gotoBWBattleRule(final Activity context, final String gameUrl) {
        if (context == null) {
            LogUtil.i(TAG, "gotoBWBattleRule() : context null, return.");
            return;
        }
        final Intent intent = new Intent().setClass(context.getApplicationContext(),
                BWBattleRuleActivity.class);
        // TODO: 18-1-24 put params into intent
        context.startActivity(intent);
    }

    /**
     * 真人场首页开始匹配
     * @param context
     * @param gameId
     */
    public static void startMatch(final Activity context, int gameId, String gameName, String gameUrl) {
        startMatch(context, gameId, gameName, gameUrl, null);
    }

    /**
     * * 真人场换个对手开始匹配
     */
    public static void startMatch(final Activity context, int gameId, String gameName, String gameUrl, String matchFrom) {
        if (context == null) {
            LogUtil.i(TAG, "startMatch() : context null, return.");
            return;
        }

        BattleManager.getInstance().clearAll();
        final Intent intent = new Intent().setClass(context.getApplicationContext(),
                MatchActivity.class);
        intent.putExtra(XGameItem.EXTRA_GAME_ID, String.valueOf(gameId));
        intent.putExtra(XGameItem.EXTRA_GAME_URL, gameUrl);
        intent.putExtra(XGameItem.EXTRA_GAME_NAME, gameName);
        intent.putExtra(XGameItem.EXTRA_GAME_TYPE, BattleConstants.BATTLE_TYPE_MATCH);
        intent.putExtra(BattleConstants.MATCH_FROM, matchFrom);
        context.startActivity(intent);
    }

    /**
     * 金币场二级页面开始匹配
     * 金币场再来一局开始匹配
     * @param context
     * @param gameId
     * @param gameUrl
     * @param ruleId
     */
    public static void startMatch(final Activity context, int gameId, String gameName, String gameUrl,
                                  int ruleId, String ruleTitle, int winCoin, int loseCoin) {
        if (context == null) {
            LogUtil.i(TAG, "startMatch() : context null, return.");
            return;
        }

        BattleManager.getInstance().clearAll();
        final Intent intent = new Intent().setClass(context.getApplicationContext(),
                MatchActivity.class);
        intent.putExtra(XGameItem.EXTRA_GAME_ID, String.valueOf(gameId));
        intent.putExtra(XGameItem.EXTRA_GAME_URL, gameUrl);
        intent.putExtra(XGameItem.EXTRA_GAME_NAME, gameName);
        intent.putExtra(XGameItem.EXTRA_GAME_TYPE, BattleConstants.BATTLE_TYPE_COIN);
        intent.putExtra(BattleConstants.BATTLE_RULE_ID, ruleId);
        intent.putExtra(BattleConstants.BATTLE_RULE_TITLE, ruleTitle);
        intent.putExtra(BattleConstants.BATTLE_WIN_COIN, winCoin);
        intent.putExtra(BattleConstants.BATTLE_LOSE_COIN, loseCoin);
        context.startActivity(intent);
    }

    //百万场开始匹配
    public static void startMatch(final Activity context) {
        if (context == null) {
            LogUtil.i(TAG, "startMatch() : context null, return.");
            return;
        }

        final Intent intent = new Intent().setClass(context.getApplicationContext(),
                BWMatchActivity.class);

        context.startActivity(intent);
    }

    /**
     * 首页打开金币场二级页面
     * @param context
     * @param gameId
     * @param gameTitle
     * @param coin
     */
    public static void gotoCoinBattleDetail(final Activity context, final String gameId, final String gameUrl,
                                     final String gameTitle, final int coin) {
        if (context == null) {
            LogUtil.i(TAG, "startMatch() : context null, return.");
            return;
        }

        final Intent intent = new Intent().setClass(context.getApplicationContext(),
                CoinBattleDetailActivity.class);
        intent.putExtra(XGameItem.EXTRA_GAME_ID, gameId);
        intent.putExtra(XGameItem.EXTRA_GAME_URL, gameUrl);
        intent.putExtra(XGameItem.EXTRA_GAME_NAME, gameTitle);
        intent.putExtra(BattleConstants.COIN, coin);
        context.startActivity(intent);
    }

    /**
     * 真人场游戏结束
     * @param context
     */
    public static void showResult(final Activity context, final int battleType) {
        if (context == null) {
            LogUtil.i(TAG, "startMatch() : context null, return.");
            return;
        }
        Intent intent;
        if (battleType == BattleConstants.BATTLE_TYPE_MATCH) {
            intent = new Intent().setClass(context.getApplicationContext(),
                    BattleResultActivity.class);
        } else if (battleType == BattleConstants.BATTLE_TYPE_COIN) {
            intent = new Intent().setClass(context.getApplicationContext(),
                    CoinBattleResultActivity.class);
        } else {
            intent = new Intent().setClass(context.getApplicationContext(),
                    BWBattleResultActivity.class);
        }

        context.startActivity(intent);
    }

    public static String getRmbTxt(final long value) {
        float f = (float) value;
        return String.valueOf(f / 100);
    }

    public static long getLongBWId(final String strId) {
        if (TextUtils.isEmpty(strId)) {
            return -1;
        }
        try {
            return Long.parseLong(strId);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private BattleUtils() {}

    private void gotoChatList(Player player) {
        Intent intent = ChatActivity.getChatIntent(player.getUserId(), player.getGender(), player.getAge(), player.getName(), player.getAvatar(), player.isFriend());
        Uri uri = new Uri.Builder().scheme("app").authority("xgame.com").path("/chat").build();

    }
}
