package com.xgame.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xgame.R;
import com.xgame.battle.BattleConstants;
import com.xgame.battle.model.ServerCoinItem;
import com.xgame.common.util.ToastUtil;
import com.xgame.home.model.XGameItem;
import com.xgame.ui.activity.CommonWebViewActivity;
import com.xgame.ui.view.CoinBattleItemView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangyong on 18-1-28.
 */

public class CoinMatchBattleFragment extends Fragment {
    private static final int MAX_ITEM_DISPLAY_NUM = 2;
    private View mRoot;
    private int mGameId;
    private int mGameType;
    private String mGameUrl;
    private String mGameName;
    private String mRuleDesc;
    private int mCoin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_coin_match_battle, container, false);

        return mRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        mGameId = args.getInt(XGameItem.EXTRA_GAME_ID);
        mGameType = args.getInt(XGameItem.EXTRA_GAME_TYPE);
        mCoin = args.getInt(XGameItem.EXTRA_GOLD_COIN);
        mGameUrl = args.getString(XGameItem.EXTRA_GAME_URL);
        mGameName = args.getString(XGameItem.EXTRA_GAME_NAME);
        mRuleDesc = args.getString(BattleConstants.BATTLE_RULE_DESC);
        mRoot.findViewById(R.id.read_rule).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonWebViewActivity.startWeb(getContext(), mRuleDesc,
                        getResources().getString(R.string.bw_battle_rule_title));
            }
        });
        fillItems(getItemsFromBundle(args));
    }

    private ServerCoinItem[] getItemsFromBundle(Bundle args) {
        if (args == null) {
            return null;
        }
        List<ServerCoinItem> itemList = (ArrayList<ServerCoinItem>) args.getSerializable("coin_items");
        ServerCoinItem[] items = null;
        if (itemList != null) {
            items = new ServerCoinItem[itemList.size()];
            items = itemList.toArray(items);
        }
        return items;
    }

    private void fillItems(final ServerCoinItem[] items) {
        if (items == null) {
            return;
        }
        if (items.length >= 1) {
            bindItem(items[0], (CoinBattleItemView) mRoot.findViewById(R.id.battle_item1));
        }
        if (items.length >= 2) {
            bindItem(items[1], (CoinBattleItemView) mRoot.findViewById(R.id.battle_item2));
        }
    }

    private void bindItem(final ServerCoinItem item, CoinBattleItemView itemView) {
        itemView.setVisibility(View.VISIBLE);
        itemView.setCoins(item.getTicketGold(), item.getWinnerGold());
        itemView.setTitle(item.getTitle());
        itemView.setStyleType(item.getStyleType());
        itemView.setJoinNowListener(new CoinBattleItemView.JoinNowListener() {
            @Override
            public void join() {
                if (mCoin < item.getTicketGold() && getContext() != null) {
                    ToastUtil.showToast(getContext(), R.string.match_coin_lack);
                    return;
                }
                Intent intent = new Intent();
                Uri uri = new Uri.Builder().scheme("xgame").authority("xgame.com").path("/battle/match").build();
                intent.putExtra(XGameItem.EXTRA_GAME_ID, String.valueOf(mGameId));
                intent.putExtra(XGameItem.EXTRA_GAME_TYPE, mGameType);
                intent.putExtra(XGameItem.EXTRA_GAME_URL, mGameUrl);
                intent.putExtra(XGameItem.EXTRA_GAME_NAME, mGameName);
                intent.putExtra(BattleConstants.BATTLE_RULE_ID, item.getId());
                intent.putExtra(BattleConstants.BATTLE_WIN_COIN, item.getWinnerGold());
                intent.putExtra(BattleConstants.BATTLE_LOSE_COIN, item.getTicketGold());
                intent.putExtra(BattleConstants.BATTLE_RULE_TITLE, item.getTitle());
                intent.setData(uri);
                getContext().startActivity(intent);
            }
        });
    }
}
