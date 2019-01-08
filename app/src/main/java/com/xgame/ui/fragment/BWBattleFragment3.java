package com.xgame.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.xgame.R;
import com.xgame.battle.BWBattleManager;
import com.xgame.battle.model.BWBattleDetail;
import com.xgame.common.util.LogUtil;
import com.xgame.common.util.NetworkUtil;
import com.xgame.ui.Router;
import com.xgame.ui.activity.BWBattleActivity;
import com.xgame.util.dialog.BaiWanAlertDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by zhanglianyu on 18-1-29.
 */

public class BWBattleFragment3 extends Fragment {

    @BindView(R.id.txt_online_num)
    TextView mOnlineNum;

    @BindView(R.id.fra_video)
    FrameLayout mFraVideo;

    private static final String TAG = "BWBattleFragment3";
    private Unbinder mUnbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bwbattle3, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        showVideo();
        return view;
    }

    private void showVideo() {
        final int netType = NetworkUtil.getNetworkType(getContext());

        // mobile network
        if (netType == NetworkUtil.NetworkType.Network_2G
                || netType == NetworkUtil.NetworkType.Network_3G
                || netType == NetworkUtil.NetworkType.Network_4G) {

            final Activity activity = getActivity();
            if (activity == null) {
                LogUtil.i(TAG, "showVideo() : activity null");
                return;
            }
            BaiWanAlertDialog.Builder builder = new BaiWanAlertDialog.Builder(activity);
            builder.setMessage(getString(R.string.bw_battle_ask_if_play_video));
            builder.setPositiveButton(R.string.bw_battle_goon_play, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    doShowVideo();
                }
            });
            builder.setNegativeButton(R.string.bw_battle_not_goon_play, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BWBattleManager.getInstance().clearAll();
                    Router.toHome();
                    activity.finish();
                }
            });
            BaiWanAlertDialog dialog = builder.create();
            dialog.setCancelable(false);
            dialog.show();
            return;
        }

        // wifi
        doShowVideo();

    }

    private void doShowVideo() {
        final BWBattleDetail bwBattleDetail = BWBattleManager.getInstance().getBWBattleDetail();
        if (bwBattleDetail == null) {
            LogUtil.i(TAG, "showVideo() : bwBattleDetail null");
            return;
        }
        String url = bwBattleDetail.getVideoTransition();
        // String url = "http://v.mifile.cn/b2c-mimall-media/bba7529de8957c041624bf0b0fb4458f.mp4";

        long pos = bwBattleDetail.getStartInterludeVideoPos();
        LogUtil.i(TAG, "showVideo() : pos - " + pos);

        Fragment fragment = ExoPlayerFragment.newInstance(url, true, pos);
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fra_video, fragment, "exo_player_fragment");
        transaction.commit();
    }

    private void updateOnlineNum(final long num) {
        if (num > 0) {
            mOnlineNum.setText(getContext().getString(R.string.bw_battle_online_num, num));
        } else {
            mOnlineNum.setText("");
        }
    }

    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateOnlineNum(BWBattleActivity.EventUpdateOnlines
                                        eventUpdateOnlines) {
        LogUtil.i(TAG, "onUpdateOnlineNum() : " + eventUpdateOnlines.getNumbers());
        updateOnlineNum(eventUpdateOnlines.getNumbers());
    }
}
