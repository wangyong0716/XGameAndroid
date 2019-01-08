package com.xgame.invite.api;

import com.xgame.common.api.FutureCall;
import com.xgame.invite.model.FriendRelation;
import com.xgame.invite.model.InvitedUser;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by Albert
 * on 18-1-28.
 */

public interface InviteService {

    @GET("user/friends")
    FutureCall<List<InvitedUser>> getUseFriends();

    @GET("user/detail")
    FutureCall<InvitedUser> getUserDetail(@Query("accountId") String accountId);

    @GET("user/search")
    FutureCall<InvitedUser> searchUser(@Query("keyword") String keyword);

    @GET("user/phone/match")
    FutureCall<List<InvitedUser>> matchUser(@Query("contacts") String contacts);

    @POST("friend/add")
    FutureCall<FriendRelation> addFriend(@Query("accountId") String accountId);

    @POST("friend/delete")
    FutureCall<FriendRelation> deleteFriend(@Query("accountId") String accountId);

    @POST("friend/accept")
    FutureCall<FriendRelation> acceptFriend(@Query("accountId") String accountId);
}