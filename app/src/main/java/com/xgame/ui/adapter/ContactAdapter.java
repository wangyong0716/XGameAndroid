package com.xgame.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.miui.zeus.utils.CollectionUtils;
import com.sina.weibo.sdk.register.mobile.PinyinUtils;
import com.xgame.R;
import com.xgame.common.util.ExecutorHelper;
import com.xgame.invite.model.InvitedUser;
import com.xgame.ui.activity.invite.view.RelationButton;
import com.xgame.util.StringUtil;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Albert
 * on 18-1-30.
 */

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> implements SectionIndexer {

    private static final Integer[] sections = new Integer[]{
            (int) 'a', (int) 'b', (int) 'c', (int) 'd', (int) 'e', (int) 'f', (int) 'g',
            (int) 'h', (int) 'i', (int) 'j', (int) 'k', (int) 'l', (int) 'm', (int) 'n',
            (int) 'o', (int) 'p', (int) 'q', (int) 'r', (int) 's', (int) 't',
            (int) 'u', (int) 'v', (int) 'w', (int) 'x', (int) 'y', (int) 'z',
            (int) '#'};
    private static final int SECTION_LAST = sections.length - 1;
    private static Map<Integer, Integer> sectionIndexMap;

    private Map<Integer, Integer> mSectionMap;

    private Context mContext;
    private List<InvitedUser> mList;
    private PinyinUtils mPyUtils;

    public ContactAdapter(Context context) {
        mContext = context;
        mPyUtils = PinyinUtils.getInstance(mContext);
        createSectionMap();
    }

    public void setItemAsync(final List<InvitedUser> list, final Runnable after) {
        if (list == null) {
            if (!CollectionUtils.isEmpty(mList)) {
                mList = null;
                notifyDataSetChanged();
            }
            if (after != null) {
                after.run();
            }
        } else {
            ExecutorHelper.runInBackground(new Runnable() {
                @Override
                public void run() {
                    mList = separate(list);
                    notifyInUi(after);
                }
            });
        }
    }

    private void notifyInUi(final Runnable after) {
        ExecutorHelper.runInUIThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
                if (after != null) {
                    after.run();
                }
            }
        });
    }

    private List<InvitedUser> separate(List<InvitedUser> list) {
        if (CollectionUtils.getSize(list) == 1) {
            InvitedUser user = list.get(0);
            user.setHead(true);
            mSectionMap.put((int) getFactor(user), 0);
            return list;
        }
        Collections.sort(list, new Comparator<InvitedUser>() {
            @Override
            public int compare(InvitedUser user, InvitedUser anotherUser) {
                return getFactor(user) - getFactor(anotherUser);
            }
        });
        char factor = '~';
        for (int i = 0; i < list.size(); i++) {
            InvitedUser user = list.get(i);
            if (user.getFactor() != factor) {
                user.setHead(true);
                factor = user.getFactor();
                mSectionMap.put((int) factor, i);
            }
        }
        return list;
    }

    private char getFactor(InvitedUser user) {
        if (user == null) {
            return '#';
        }
        if (StringUtil.isEmpty(user.getNickname())) {
            user.setFactor('#');
            return user.getFactor();
        }
        String fl = mPyUtils.getPinyin(user.getNickname().substring(0, 1));
        if (StringUtil.isEmpty(fl)) {
            user.setFactor('#');
            return user.getFactor();
        }
        user.setFactor(fl.toLowerCase().charAt(0));
        return user.getFactor();
    }

    private void createSectionMap() {
        if (sectionIndexMap == null) {
            sectionIndexMap = new HashMap<>(sections.length);
            for (int i = 0; i < sections.length; i++) {
                sectionIndexMap.put(sections[i], i);
            }
        }
        if (mSectionMap == null) {
            mSectionMap = new HashMap<>();
        } else {
            mSectionMap.clear();
        }
    }

    public void clearItem() {
        if (mList != null) {
            mList.clear();
        }
    }

    public InvitedUser getItem(int position) {
        return CollectionUtils.get(mList, position);
    }

    @Override
    public ContactAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, R.layout.layout_contact_item, null);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ContactAdapter.ViewHolder holder, int position) {
        InvitedUser user = getItem(position);
        if (user == null) {
            return;
        }

        holder.gap.setVisibility(user.isHead() ? View.VISIBLE : View.GONE);
        holder.gap.getLayoutParams().height =
                mContext.getResources().getDimensionPixelOffset(position == 0 ? R.dimen.dp_13_3 : R.dimen.dp_10);
        holder.header.setVisibility(user.isHead() ? View.VISIBLE : View.GONE);
        TextView alphabet = holder.header.findViewById(R.id.alphabet);
        alphabet.setText(("" + user.getFactor()).toUpperCase());

        holder.contactNameView.setText(user.getContactName());
        holder.contactNameView.setVisibility(StringUtil.isEmpty(user.getNickname()) ? View.GONE : View.VISIBLE);
        holder.nicknameView.setText(user.getNickname());
        holder.nicknameView.setVisibility(StringUtil.isEmpty(user.getNickname()) ? View.GONE : View.VISIBLE);

        UserItemHelper.bindUser(mContext, holder.contentView, holder.avatarView, holder.relationButton,
                user, mContext.getString(R.string.contacts_title));
    }

    @Override
    public int getItemCount() {
        return CollectionUtils.getSize(mList);
    }

    @Override
    public Object[] getSections() {
        return sections;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        Integer section = CollectionUtils.get(sections, sectionIndex);
        Integer position = mSectionMap.get(section);
        return position == null ? -1 : position;
    }

    @Override
    public int getSectionForPosition(int position) {
        int fl = getItem(position).getFactor();
        Integer section = sectionIndexMap.get(fl);
        return section == null ? SECTION_LAST : section;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View gap;
        View header;
        View contentView;
        ImageView avatarView;
        TextView contactNameView;
        TextView nicknameView;
        RelationButton relationButton;

        public ViewHolder(View itemView) {
            super(itemView);
            gap = itemView.findViewById(R.id.top_gap);
            header = itemView.findViewById(R.id.alphabet_header);
            contentView = itemView.findViewById(R.id.content_layout);
            avatarView = itemView.findViewById(R.id.avatar);
            contactNameView = itemView.findViewById(R.id.contact_name);
            nicknameView = itemView.findViewById(R.id.nick_name);
            relationButton = itemView.findViewById(R.id.relation_btn);
        }
    }
}
