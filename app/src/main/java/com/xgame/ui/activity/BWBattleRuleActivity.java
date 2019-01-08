package com.xgame.ui.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.xgame.R;
import com.xgame.battle.BWBattleManager;
import com.xgame.battle.BattleManager;
import com.xgame.battle.model.BWBattleDetail;
import com.xgame.ui.fragment.ExoPlayerFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by zhanglianyu on 18-1-25.
 */

public class BWBattleRuleActivity extends AppCompatActivity {

    @BindView(R.id.ic_back)
    ImageView mBack;

    @BindView(R.id.txt_battle_title)
    TextView mTitle;

    @BindView(R.id.txt_battle_des)
    TextView mDes;

    @BindView(R.id.txt_time_value)
    TextView mTime;

    @BindView(R.id.txt_bonus_value)
    TextView mBonus;

    @OnClick({R.id.ic_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ic_back:
                onBackPressed();
                break;
            default:
                break;
        }
    }

    private static final String TAG = "BWBattleRuleActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bwbattle_rule);

        final BWBattleDetail bwBattleDetail = BWBattleManager.getInstance().getBWBattleDetail();
        if (bwBattleDetail == null) {
            finish();
            return;
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        ButterKnife.bind(this);

        String url = bwBattleDetail.getVideoRule();
        Fragment fragment = ExoPlayerFragment.newInstance(url, false);
        fragment.getArguments().putString("title", bwBattleDetail.getRuleVideoTitle());
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.rel_video, fragment, "exo_player_fragment");
        transaction.commit();

        updateView(bwBattleDetail);
    }

    private void updateView(BWBattleDetail bwBattleDetail) {
        mTitle.setText(bwBattleDetail.getTitle());
        mDes.setText(bwBattleDetail.getSubTitle());
        mTime.setText(bwBattleDetail.getShowStartTimeStr());
        mBonus.setText(bwBattleDetail.getShownBonus());
    }

}
