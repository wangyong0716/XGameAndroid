package com.xgame.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xgame.R;
import com.xgame.battle.BattleConstants;

/**
 * Created by wangyong on 18-1-28.
 */

public class CoinBattleItemView extends RelativeLayout {

    public CoinBattleItemView(Context context) {
        super(context);
        init();
    }

    public CoinBattleItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CoinBattleItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.coin_battle_item, this);
    }


    public interface JoinNowListener {
        public void join();
    }

    public void setCoins(int fee, int award) {
        ((TextView) findViewById(R.id.item_requirement)).setText(getResources().getString(R.string.coin_battle_fee, fee));
        ((TextView) findViewById(R.id.item_award)).setText(getResources().getString(R.string.coin_battle_award, award));

    }

    public void setJoinNowListener(final JoinNowListener listener) {
        findViewById(R.id.join_now).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.join();
            }
        });
    }

    public void setTitle(String title) {
        ((TextView)findViewById(R.id.item_title)).setText(title);
    }

    public void setStyleType(int styleType) {
        if (styleType == BattleConstants.COIN_TYPE_BLUE) {
            setBackgroundResource(R.drawable.round_blue_gradient_background);
        } else {
            setBackgroundResource(R.drawable.round_yellow_gradient_background);
        }
    }
}
