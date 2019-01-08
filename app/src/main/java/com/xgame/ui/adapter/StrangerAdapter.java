package com.xgame.ui.adapter;

import com.miui.zeus.utils.CollectionUtils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.xgame.R;
import com.xgame.common.util.ExecutorHelper;
import com.xgame.home.model.MessageSession;
import com.xgame.invite.model.InvitedUser;
import com.xgame.ui.activity.invite.view.RelationButton;
import com.xgame.util.StringUtil;

/**
 * Created by Albert
 * on 18-1-28.
 */

public class StrangerAdapter extends RecyclerView.Adapter<StrangerAdapter.ViewHolder> {

    private Context mContext;
    private List<InvitedUser> mList;

    public StrangerAdapter(Context context) {
        this.mContext = context;
    }

    public void addItemAsync(final List<MessageSession> list, final boolean refresh) {
        if (list == null) {
            mList = null;
            notifyDataSetChanged();
        } else {
            ExecutorHelper.runInBackground(new Runnable() {
                @Override
                public void run() {
                    notifyInUi(convert(list), refresh);
                }
            });
        }
    }

    private List<InvitedUser> convert(List<MessageSession> list) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        List<InvitedUser> users = new ArrayList<InvitedUser>(list.size());
        for (MessageSession record : list) {
            if (!record.isStranger()) {
                continue;
            }
            users.add(new InvitedUser(record));
        }
        return users;
    }

    public void addItems(final List<InvitedUser> list, boolean refresh) {
        if (list == null || list.isEmpty()) {
            return;
        }
        if (mList == null) {
            mList = list;
        } else if (refresh) {
            mList.removeAll(list);
            mList.addAll(0, list);
        } else {
            list.removeAll(mList);
            mList.addAll(list);
        }
        notifyDataSetChanged();
    }

    private void mergeList(List<InvitedUser> list) {
        ArrayMap<String, InvitedUser> map = new ArrayMap<>();
        for (InvitedUser user : mList) {
            map.put(user.getAccountId(), user);
        }
        for (InvitedUser user : list) {
            map.put(user.getAccountId(), user);
        }
        Collection<InvitedUser> values = map.values();
        mList = Arrays.asList(values.toArray(new InvitedUser[values.size()]));
    }

    private void notifyInUi(final List<InvitedUser> list, final boolean refresh) {
        ExecutorHelper.runInUIThread(new Runnable() {
            @Override
            public void run() {
                addItems(list, refresh);
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, R.layout.layout_user_friend_item, null);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        InvitedUser user = CollectionUtils.get(mList, position);
        if (user == null) {
            return;
        }

        holder.topGap.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        holder.nicknameView.setText(user.getNickname());
        holder.nicknameView.setVisibility(StringUtil.isEmpty(user.getNickname()) ? View.GONE : View.VISIBLE);
        String message = user.getGenderString(mContext) + "  " + user.getAgeString(mContext);
        holder.messageView.setText(message);
        holder.messageView.setVisibility(StringUtil.isEmpty(message) ? View.GONE : View.VISIBLE);

        UserItemHelper.bindUser(mContext, holder.itemView, holder.avatarView, holder.relationBtn, user);
    }

    @Override
    public int getItemCount() {
        return CollectionUtils.getSize(mList);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View topGap;
        ImageView avatarView;
        TextView nicknameView;
        TextView messageView;
        RelationButton relationBtn;

        public ViewHolder(View view) {
            super(view);
            topGap = view.findViewById(R.id.top_gap);
            avatarView = view.findViewById(R.id.avatar);
            nicknameView = view.findViewById(R.id.nick_name);
            messageView = view.findViewById(R.id.message);
            relationBtn = view.findViewById(R.id.relation_btn);
        }
    }
}
