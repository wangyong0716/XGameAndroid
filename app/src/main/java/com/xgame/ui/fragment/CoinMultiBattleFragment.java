package com.xgame.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.miui.zeus.mario.sdk.MarioSdk;
import com.xgame.R;
import com.xgame.account.AccountConstants;
import com.xgame.base.ServiceFactory;
import com.xgame.battle.BattleConstants;
import com.xgame.battle.BattleManager;
import com.xgame.battle.BattleUtils;
import com.xgame.battle.api.BattleService;
import com.xgame.battle.model.MatchResult;
import com.xgame.common.api.OnCallback;
import com.xgame.common.net.Result;
import com.xgame.common.net.RetrofitHttpClient;
import com.xgame.home.model.XGameItem;

import retrofit2.Retrofit;

/**
 * Created by wangyong on 18-1-28.
 */

public class CoinMultiBattleFragment extends Fragment {
    private View mRoot;
    private TextView mTitleView;
    private TextView mDescriptionView;
    private TextView mJoinButton;
    private int mGameId;
    private String mGameUrl;
    private String mUserToken;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_coin_multi_battle, container, false);
        mTitleView = mRoot.findViewById(R.id.description_title);
        mDescriptionView = mRoot.findViewById(R.id.description_content);
        mJoinButton = mRoot.findViewById(R.id.join_now);

        return mRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        mGameId = args.getInt(XGameItem.EXTRA_GAME_ID);
        mGameUrl = args.getString(XGameItem.EXTRA_GAME_URL);
        mUserToken = args.getString(BattleConstants.TOKEN);
        mTitleView.setText(args.getString(XGameItem.EXTRA_GAME_NAME));
        mDescriptionView.setText(args.getString(BattleConstants.BATTLE_RULE_DESC));
        mJoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applyJoin();
            }
        });
    }

    private void applyJoin() {
        ServiceFactory.battleService().joinCoinMulti(mGameId).enqueue(new OnCallback<Result<MatchResult>>() {
            @Override
            public void onResponse(Result<MatchResult> result) {
                joinGame();
            }

            @Override
            public void onFailure(Result<MatchResult> result) {

            }
        });
    }

    public void joinGame() {
        BattleManager.getInstance().clearAll();
        BattleManager.getInstance().setGameId(mGameId);
        BattleManager.getInstance().setGameUrl(mGameUrl);
        BattleManager.getInstance().setToken(mUserToken);
        BattleUtils.gotoCoinBattle2(getActivity(), mUserToken);
    }

}
