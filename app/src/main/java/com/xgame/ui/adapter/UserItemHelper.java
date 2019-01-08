package com.xgame.ui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;

import com.xgame.R;
import com.xgame.app.GlideApp;
import com.xgame.invite.InviteHelper;
import com.xgame.invite.api.FriendManager;
import com.xgame.invite.model.FriendRelation;
import com.xgame.invite.model.InvitedUser;
import com.xgame.ui.activity.invite.StrangerDetailActivity;
import com.xgame.ui.activity.invite.view.RelationButton;
import com.xgame.util.StringUtil;

/**
 * Created by Albert
 * on 18-2-1.
 */

public class UserItemHelper {

    public static void bindUser(final Context context, View item, ImageView avatar, final RelationButton btn,
                                final InvitedUser user) {
        bindUser(context, item, avatar, btn, user, null);
    }

    public static void bindUser(final Context context, View item, ImageView avatar, final RelationButton btn,
                                final InvitedUser user, final String title) {
        if (context == null || item == null || avatar == null || user == null) {
            return;
        }
        // Avatar view.
        if (StringUtil.isEmpty(user.getAvatar())) {
            avatar.setImageResource(R.drawable.default_avatar);
        } else {
            GlideApp.with(item).load(user.getAvatar()).into(avatar);
        }
        // State view.
        final boolean isMyself = InviteHelper.isAccountUser(user.getAccountId());
        bindState(context, btn, user, isMyself);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeRelation(context, btn, user, isMyself, false);
            }
        });
        // Item view.
        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, StrangerDetailActivity.class);
                intent.putExtra("user", user);
                if (!StringUtil.isEmpty(title)) {
                    intent.putExtra("title", title);
                }
                context.startActivity(intent);
            }
        });
        // Bind delete action if needed.
        // bindDeleteAction(context, item, btn, user, isMyself);
    }

    private static void changeRelation(final Context context, final RelationButton btn,
                                       final InvitedUser user, final boolean isMyself, final boolean deleteFriend) {
        if (user == null || isMyself) {
            return;
        }
        FriendManager.FriendLoader loader = new FriendManager.FriendLoader() {
            @Override
            public void onLoading() {
                btn.showLoading();
            }

            @Override
            public void onRelationChanged(FriendRelation relation) {
                user.setRelative(relation.getStatus());
                bindState(context, btn, user, false);
            }

            @Override
            public void onRelationFailed(FriendRelation relation) {
                btn.showText();
            }
        };
        if (deleteFriend) {
            FriendManager.delete(user.getAccountId(), loader);
        } else if (user.isStranger()) {
            FriendManager.add(user.getAccountId(), loader);
        } else if (user.isWaitConfirm()) {
            FriendManager.accept(user.getAccountId(), loader);
        }
    }

    private static void bindState(Context context, RelationButton btn, InvitedUser user, boolean isMyself) {
        if (isMyself) {
            btn.setBackgroundResource(0);
            btn.setText(R.string.myself, R.color.default_hint_text_color);
        } else {
            btn.setText(user.getRelationString(context));
            if (user.isNeedActive()) {
                btn.setBackgroundResource(R.drawable.purple_round_btn);
                btn.setTextColor(Color.WHITE);
            } else {
                btn.setBackgroundResource(0);
                btn.setTextColor(context.getResources().getColor(R.color.default_hint_text_color));
            }
        }
    }

    // Bind Delete action if needed.
    private static void bindDeleteAction(final Context context, final View item, final RelationButton btn,
                                         final InvitedUser user, final boolean isMySelf) {
        if (user == null) {
            return;
        }
        item.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (user.isCouldDelete()) {
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.delete_dialog_title)
                            .setMessage(R.string.delete_dialog_message)
                            .setPositiveButton(R.string.sure_text, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    changeRelation(context, btn, user, isMySelf, true);
                                }
                            })
                            .setNegativeButton(R.string.cancel_text, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).create().show();
                    return true;
                }
                return false;
            }
        });
    }
}
