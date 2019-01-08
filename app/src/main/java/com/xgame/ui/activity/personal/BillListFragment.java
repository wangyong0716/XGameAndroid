package com.xgame.ui.activity.personal;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.xgame.R;
import com.xgame.app.GlideApp;
import com.xgame.base.api.Pack;
import com.xgame.common.api.OnCallback;
import com.xgame.personal.model.BillItem;
import com.xgame.personal.model.BillList;
import com.xgame.ui.activity.home.view.AbsRefreshLoadLayout;
import com.xgame.ui.activity.home.view.PageFragment;
import com.xgame.ui.activity.personal.view.RefreshLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import static com.xgame.base.ServiceFactory.personalInfoService;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by dingning1
 * on 18-1-30.
 */


public class BillListFragment extends PageFragment {

    public interface IDataLoadListener {
        void onLoad(int type, boolean hasData);
    }

    public static final int BILL_COIN = 1;
    public static final int BILL_CASH = 2;

    private static final char PLUS = '+';
    private static final char MINUS = '-';

    private static final int TYPE_INCREASE = 1;
    private static final int TYPE_DECREASE = 2;

    private static final int EMPTY_NET_ERROR = -1;
    private static final int EMPTY_HIDE = 0;
    private static final int EMPTY_NO_DATA = 1;
    private static final int EMPTY_ERROR_MSG = 2;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private ListView mListView;
    private BillItemAdapter mAdapter;
    private EmptyViewHolder mEmptyHolder;

    private IDataLoadListener mLoadListener;
    private RefreshLayout mRefreshLayout;

    private int mType;
    private String mItemUnit;
    private String mEmptyUnit;

    private boolean mInit;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            Bundle savedInstanceState) {
        mInit = false;
        View view = inflater.inflate(R.layout.personal_bill_list, container, false);
        mEmptyHolder = new EmptyViewHolder(view.findViewById(R.id.empty_view));
        mListView = view.findViewById(R.id.list);
        mAdapter = new BillItemAdapter();
        mListView.setAdapter(mAdapter);
        mRefreshLayout = new RefreshLayout(getActivity(), view);
        mRefreshLayout.setChecker(new RefreshLayout.IRefreshChecker() {
            @Override
            public boolean canRefresh() {
                if (mListView.getFirstVisiblePosition() == 0) {
                    View first = mListView.getChildAt(0);
                    return first == null || Float.compare(first.getY(), 0) == 0;
                }
                return false;
            }
        });
        mRefreshLayout.setRefreshLoadListener(new AbsRefreshLoadLayout.RefreshLoadListener() {
            @Override
            public void onRefresh() {
                loadData();
            }

            @Override
            public void onLoad() {
            }
        });
        return mRefreshLayout;
    }

    public void setType(int type, IDataLoadListener listener) {
        mType = type;
        mLoadListener = listener;
    }

    public int getType() {
        return mType;
    }

    public boolean hasData() {
        return mAdapter != null && mAdapter.mItems.size() > 0;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mInit) {
            mInit = true;
            loadData();
        }
    }

    private void notifyLoadListener(boolean hasData) {
        if (mLoadListener != null) {
            mLoadListener.onLoad(mType, hasData);
        }
    }

    private void loadData() {
        OnCallback<Pack<BillList>> callback = new OnCallback<Pack<BillList>>() {
            @Override
            public void onResponse(Pack<BillList> result) {
                mRefreshLayout.setRefreshing(false);
                if (result.data == null) {
                    String errMsg = "" + result.code + ", " + result.msg;
                    showEmptyView(EMPTY_ERROR_MSG, errMsg);
                    return;
                }
                if (result.data.details == null || result.data.details.length == 0) {
                    showEmptyView(EMPTY_NO_DATA);
                    notifyLoadListener(false);
                } else {
                    showBillList(result.data);
                    notifyLoadListener(true);
                }
            }

            @Override
            public void onFailure(Pack<BillList> result) {
                mRefreshLayout.setRefreshing(false);
                showEmptyView(EMPTY_NET_ERROR);
                notifyLoadListener(false);
            }
        };

        if (mType == BILL_CASH) {
            personalInfoService().getCashBill().enqueue(callback);
        } else {
            personalInfoService().getCoinBill().enqueue(callback);
        }
    }

    private void showBillList(BillList result) {
        showEmptyView(EMPTY_HIDE);
        mAdapter.setItems(result.details);
    }

    private void showEmptyView(int type) {
        showEmptyView(type, null);
    }

    private void showEmptyView(int type, String msg) {
        if (type == EMPTY_HIDE) {
            mEmptyHolder.emptyView.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            return;
        }
        mEmptyHolder.emptyView.setVisibility(View.VISIBLE);
        if (type == EMPTY_NET_ERROR) {
            GlideApp.with(this).load(R.drawable.coin_battle_lose).into(mEmptyHolder.icon);
            mEmptyHolder.msg.setText(R.string.net_error_text);
        } else if (type == EMPTY_NO_DATA) {
            GlideApp.with(this).load(getBillIcon(true)).into(mEmptyHolder.icon);
            mEmptyHolder.msg.setText(getString(R.string.no_bill, getEmptyUnit()));
        } else {
            GlideApp.with(this).load(R.drawable.coin_battle_lose).into(mEmptyHolder.icon);
            mEmptyHolder.msg.setText(msg);
        }
    }

    private int getBillIcon(boolean useBig) {
        if (mType == BILL_CASH) {
            return useBig ? R.drawable.icon_hongbao_big : R.drawable.icon_hongbao_small;
        } else {
            return useBig ? R.drawable.icon_gold_coin_big : R.drawable.icon_gold_coin_small;
        }
    }

    private String getBillUnit() {
        if (TextUtils.isEmpty(mItemUnit)) {
            int strId = mType == BILL_CASH ? R.string.yuan : R.string.gold_coin;
            mItemUnit = getString(strId);
        }
        return mItemUnit;
    }

    private String getEmptyUnit() {
        if (TextUtils.isEmpty(mEmptyUnit)) {
            int strId = mType == BILL_CASH ? R.string.cash : R.string.gold_coin;
            mEmptyUnit = getString(strId);
        }
        return mEmptyUnit;
    }

    private String getTimeString(long time) {
        return DATE_FORMAT.format(new Date(time));
    }

    private String getCashValue(float cash) {
        return mType == BILL_CASH ? String.valueOf(cash) : String.valueOf((int) cash);
    }

    // nested class definition

    static class EmptyViewHolder {
        @BindView(R.id.empty_icon) ImageView icon;
        @BindView(R.id.empty_msg) TextView msg;

        View emptyView;

        EmptyViewHolder(View view) {
            emptyView = view;
            ButterKnife.bind(this, view);
        }
    }

    class BillItemAdapter extends BaseAdapter {

        List<BillItem> mItems = new ArrayList<>();

        public void setItems(BillItem[] items) {
            mItems.clear();
            if (items != null && items.length > 0) {
                Collections.addAll(mItems, items);
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public BillItem getItem(int position) {
            return position >= 0 && position < mItems.size() ? mItems.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            BillItem item = getItem(position);
            ViewHolder holder;
            View view = convertView;
            if (view != null) {
                holder = (ViewHolder) view.getTag();
            } else {
                view = View.inflate(getActivity(), R.layout.personal_bill_item, null);
                holder = new ViewHolder(view);
                view.setTag(holder);
            }
            holder.name.setText(item.remark);
            holder.time.setText(getTimeString(item.time));
            String count = String.format("%c%s%s", (item.type == TYPE_INCREASE ? PLUS : MINUS),
                    getCashValue(item.cash), getBillUnit());
            holder.info.setText(count);
            GlideApp.with(BillListFragment.this)
                    .load(getBillIcon(false))
                    .into(holder.icon);
            return view;
        }

        class ViewHolder {
            @BindView(R.id.name) TextView name;
            @BindView(R.id.time) TextView time;
            @BindView(R.id.info) TextView info;
            @BindView(R.id.icon) ImageView icon;

            ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }
    }
}
