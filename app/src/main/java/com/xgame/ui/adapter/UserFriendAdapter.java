package com.xgame.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xgame.R;
import com.xgame.invite.model.InvitedUser;
import com.xgame.ui.activity.invite.view.RelationButton;
import com.xgame.util.StringUtil;

/**
 * Created by Albert
 * on 18-1-28.
 */

public class UserFriendAdapter extends AbstractListAdapter<InvitedUser> {

    public UserFriendAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        if (convertView == null) {
            convertView = UserFriendItemAdapter.createView(mContext);
        }
        UserFriendItemAdapter.bindView(convertView, getItem(position));
        return convertView;
    }

    static class UserFriendItemAdapter {

        static View createView(Context context) {

            View view = LayoutInflater.from(context).inflate(R.layout.layout_user_friend_item, null);

            ViewHolder holder = new ViewHolder();

            holder.itemView = view;
            holder.avatarView = view.findViewById(R.id.avatar);
            holder.nicknameView = view.findViewById(R.id.nick_name);
            holder.messageView = view.findViewById(R.id.message);
            holder.relationButton = view.findViewById(R.id.relation_btn);

            view.setTag(holder);

            return view;
        }

        static void bindView(View view, final InvitedUser user) {
            if (user == null) {
                return;
            }
            final Context context = view.getContext();
            final ViewHolder holder = (ViewHolder) view.getTag();
            holder.nicknameView.setText(user.getNickname());
            holder.nicknameView.setVisibility(StringUtil.isEmpty(user.getNickname()) ? View.GONE : View.VISIBLE);
            holder.messageView.setText(user.getMessage());
            holder.messageView.setVisibility(StringUtil.isEmpty(user.getMessage()) ? View.GONE : View.VISIBLE);
            holder.relationButton.setText(user.getRelationString(context));

            UserItemHelper.bindUser(context, holder.itemView, holder.avatarView, holder.relationButton, user);
        }
    }

    private static class ViewHolder {
        View itemView;
        ImageView avatarView;
        TextView nicknameView;
        TextView messageView;
        RelationButton relationButton;
    }
}
