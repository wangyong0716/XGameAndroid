package com.xgame.invite.api;

import android.util.Log;

import com.xgame.base.ServiceFactory;
import com.xgame.common.api.FutureCall;
import com.xgame.common.api.OnCallback;
import com.xgame.common.util.LogUtil;
import com.xgame.invite.model.FriendRelation;

/**
 * Created by Albert
 * on 18-2-4.
 */

public class FriendManager {

    private static final String TAG = "FriendManager";

    public static void add(String accountId, FriendLoader loader) {
        touch(FriendAction.Add, accountId, loader);
    }

    public static void accept(String accountId, FriendLoader loader) {
        touch(FriendAction.Accept, accountId, loader);
    }

    public static void delete(String accountId, FriendLoader loader) {
        touch(FriendAction.Delete, accountId, loader);
    }

    private static void touch(final FriendAction action, String accountId, final FriendLoader loader) {
        FutureCall<FriendRelation> call = null;
        switch (action) {
            case Add:
                call = ServiceFactory.inviteService().addFriend(accountId);
                break;
            case Accept:
                call = ServiceFactory.inviteService().acceptFriend(accountId);
                break;
            case Delete:
                call = ServiceFactory.inviteService().deleteFriend(accountId);
                break;
        }
        if (call != null) {
            if (loader != null) {
                loader.onLoading();
            }
            call.enqueue(new OnCallback<FriendRelation>() {
                @Override
                public void onResponse(FriendRelation relation) {
                    LogUtil.d(TAG, "OnResponse, action: " + action + " , relation: " + relation);
                    if (loader != null) {
                        loader.onRelationChanged(relation);
                    }
                }

                @Override
                public void onFailure(FriendRelation relation) {
                    LogUtil.d(TAG, "OnFailure, action: " + action + " , relation: " + relation);
                    if (loader != null) {
                        loader.onRelationFailed(relation);
                    }
                }
            });
        }
    }

    public enum FriendAction {
        Add, Accept, Delete
    }

    public interface FriendLoader {
        void onLoading();
        void onRelationChanged(FriendRelation relation);
        void onRelationFailed(FriendRelation relation);
    }
}
