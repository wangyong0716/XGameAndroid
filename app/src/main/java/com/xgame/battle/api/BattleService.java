package com.xgame.battle.api;

import com.xgame.battle.model.BWBattleBonusResult;
import com.xgame.battle.model.BWBattleDetail;
import com.xgame.battle.model.BWBattleMatchResult;
import com.xgame.battle.model.BWBattlePlayer;
import com.xgame.battle.model.BWBattleReviveResult;
import com.xgame.battle.model.BWBattleUserInfo;
import com.xgame.battle.model.BWBattleWallet;
import com.xgame.battle.model.BWOnlineNumbers;
import com.xgame.battle.model.BattleRecords;
import com.xgame.battle.model.MatchResult;
import com.xgame.battle.model.ServerBattleRecord;
import com.xgame.battle.model.ServerCoinGame;
import com.xgame.common.api.FutureCall;
import com.xgame.common.net.Result;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by wangyong on 18-1-29.
 */

public interface BattleService {
    @GET("match/match")
    FutureCall<MatchResult> startMatch(@Query("gameId") int gameId, @Query("gameType") int gameType, @Query("from") String from);

    @GET("match/match")
    FutureCall<MatchResult> startMatch(@Query("gameId") int gameId, @Query("gameType") int gameType, @Query("ruleId") int ruleId,
                                       @Query("clientInfo") String clientInfo);

    @GET("match/cancel")
    Call<Void> cancelMatch(@Query("gameId") int gameId, @Query("ruleId") int ruleId, @Query("gameType") int gameType);

    @GET("match/battle")
    Call<Void> consumeMatch(@Query("gameId") int gameId, @Query("ruleId") int ruleId,
                            @Query("clientInfo") String clientInfo, @Query("gameType") int gameType);

    // qinzhao
    @GET("record/duet")
    Call<Result<BattleRecords>> getBattleHistoryAfterTime(@Query("otherUserId") long opponentId, @Query("startTime") long startTime);

    @GET("record/duet")
    Call<Result<BattleRecords>> getBattleHistoryBeforeTime(@Query("otherUserId") long opponentId, @Query("endTime") long startTime);

    @FormUrlEncoded
    @POST("message/invitation")
    Call<Result<ServerBattleRecord>> inviteMatch(@Field("gameId") int gameId,
                                                 @Field("otherUserId") long otherUserId,
                                                 @Field("isFriend") boolean isFriend);

    @FormUrlEncoded
    @POST("message/cancel")
    Call<Result<ServerBattleRecord>> cancelMatch(@Field("gameId") int gameId,
                                                 @Field("sessionId") String sessionId, @Field("otherUserId") long otherUserId);

    @FormUrlEncoded
    @POST("message/reject")
    Call<Result<ServerBattleRecord>> rejectMatch(@Field("gameId") int gameId,
                                                 @Field("sessionId") String sessionId, @Field("otherUserId") long otherUserId);

    @FormUrlEncoded
    @POST("message/leave")
    Call<Void> leaveMatch(@Field("otherUserId") long otherUserId);

    @FormUrlEncoded
    @POST("message/success")
    Call<Void> acceptInvite(@Field("gameId") int gameId, @Field("sessionId") String sessionId,
                            @Field("otherUserId") long otherUserId);

    // qinzhao

    @FormUrlEncoded
    @POST("message/anothergame")
    Call<Void> changeGame(@Field("gameId") int gameId, @Field("otherUserId") long otherUserId);

    @GET("coingame/rule")
    FutureCall<ServerCoinGame> getCoinGameDetail(@Query("gameId") int gameId, @Query("clientInfo") String clientInfo);

    @GET("coingame/multi")
    FutureCall<Result<MatchResult>> joinCoinMulti(@Query("gameId") int gameId);

    @GET("coingame/match")
    FutureCall<Result<MatchResult>> startCoinMatch(@Query("gameId") String gameId,
                                                   @Query("ruleId") int ruleId, @Query("clientInfo") String clientInfo);

    @GET("bwbattle/detail")
    FutureCall<BWBattleDetail> getBWBattleDetail(@Query("token") String token, @Query("bwId") long bwId);

    @GET("bwbattle/detail/onlines")
    FutureCall<BWOnlineNumbers> getOnlineNum(@Query("token") String token, @Query("bwId") long bwId, @Query("gameId") long gameId);

    @GET("bwbattle/match/result")
    FutureCall<BWBattleMatchResult> getBWBattleMatchResult(@Query("token") String token, @Query("bwId") long bwId,
                                                           @Query("gameId") long gameId, @Query("roundId") long roundId);

    @FormUrlEncoded
    @POST("bwbattle/restart")
    FutureCall<BWBattleReviveResult> requestRevive(@Field("gameId") long gameId, @Field("bwId") long bwId,
                                                   @Field("roundId") long roundId);

    @GET("bwbattle/join")
    FutureCall<BWBattleMatchResult> joinBWBattle(@Query("token") String token, @Query("bwId") long bwId,
                                                 @Query("gameId") long gameId, @Query("roundId") long roundId);


    @GET("bwbattle/bonus")
    FutureCall<BWBattleBonusResult> getBWBattleBonus(@Query("bwId") long bwId,
                                                     @Query("gameId") long gameId);

    @GET("bwbattle/wallet")
    FutureCall<BWBattleWallet> getBWBattleWallet(@Query("token") String token, @Query("bwId") long bwId);

    @GET("bwbattle/cancel")
    FutureCall<Void> cancelBWBattle(@Query("bwId") long bwId, @Query("gameId") long gameId,
                                    @Query("roundId") long roundId);

    @GET("bwbattle/battle")
    FutureCall<Void> startBWBattle(@Query("bwId") long bwId, @Query("gameId") long gameId,
                                   @Query("roundId") long roundId);

    @GET("partner/battle/userinfo")
    FutureCall<BWBattleUserInfo> getPartnerInfo(@Query("roomId") String roomId, @Query("sign") String sign,
                                                @Query("token") String token);
}
