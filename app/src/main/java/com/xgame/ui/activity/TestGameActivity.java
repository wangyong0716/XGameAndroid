package com.xgame.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.xgame.R;
import com.xgame.account.UserManager;
import com.xgame.battle.BattleManager;
import com.xgame.battle.BattleUtils;
import com.xgame.battle.model.BWBattleMatchResult;
import com.xgame.common.api.OnCallback;
import com.xgame.common.util.GlobalGson;
import com.xgame.push.event.BWBattleMatchResultEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.xgame.base.ServiceFactory.battleService;

/**
 * Created by mi on 18-2-1.
 */

public class TestGameActivity extends Activity {

    private static final String TAG = "TestGameActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testgame);

        EventBus.getDefault().register(this);


        //打雪仗
        ((Button) findViewById(R.id.button3)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BattleManager.getInstance().setGameUrl("http://pkclient.egret-labs.org/h5_mi/v30/index.html?gameId=2&myUserId=1&otherUserId=2&isAi=1&gameAiLevel=3");
                BattleManager.getInstance().setRoomId("test");
                BattleUtils.gotoMatchBattle(TestGameActivity.this, "test");
            }
        });


        //小猪快跑
        ((Button) findViewById(R.id.button4)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BattleManager.getInstance().setGameUrl("http://pkclient.egret-labs.org/h5_mi/v30/index.html?gameId=4&myUserId=1&otherUserId=2&isAi=1&gameAiLevel=3");
                BattleManager.getInstance().setRoomId("test");
                BattleUtils.gotoMatchBattle(TestGameActivity.this, "test");
            }
        });

        //斗兽棋
        ((Button) findViewById(R.id.button5)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BattleManager.getInstance().setGameUrl("http://pkclient.egret-labs.org/h5_mi/v30/index.html?gameId=6&myUserId=1&otherUserId=2&isAi=1&gameAiLevel=3");
                BattleManager.getInstance().setRoomId("test");
                BattleUtils.gotoMatchBattle(TestGameActivity.this, "test");
            }
        });


        //节奏达人
        ((Button) findViewById(R.id.button6)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BattleManager.getInstance().setGameUrl("http://pkclient.egret-labs.org/h5_mi/v30/index.html?gameId=7&myUserId=1&otherUserId=2&isAi=1&gameAiLevel=3");
                BattleManager.getInstance().setRoomId("test");
                BattleUtils.gotoMatchBattle(TestGameActivity.this, "test");
            }
        });


        //BANG
        ((Button) findViewById(R.id.button7)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BattleManager.getInstance().setGameUrl("http://pkclient.egret-labs.org/h5_mi/v30/index.html?gameId=10&myUserId=1&otherUserId=2&isAi=1&gameAiLevel=3");
                BattleManager.getInstance().setRoomId("test");
                BattleUtils.gotoMatchBattle(TestGameActivity.this, "test");
            }
        });

        //跳一跳
        ((Button) findViewById(R.id.button8)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BattleManager.getInstance().setGameUrl("http://pkclient.egret-labs.org/h5_mi/v30/index.html?gameId=11&myUserId=1&otherUserId=2&isAi=1&gameAiLevel=3");
                BattleManager.getInstance().setRoomId("test");
                BattleUtils.gotoMatchBattle(TestGameActivity.this, "test");
            }
        });

        //五子棋
        ((Button) findViewById(R.id.button9)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BattleManager.getInstance().setGameUrl("http://pkclient.egret-labs.org/h5_mi/v30/index.html?gameId=12&myUserId=1&otherUserId=2&isAi=1&gameAiLevel=3");
                BattleManager.getInstance().setRoomId("test");
                BattleUtils.gotoMatchBattle(TestGameActivity.this, "test");
            }
        });


        ((Button) findViewById(R.id.button10)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i(TAG, "joinBWBattle()");
                battleService().joinBWBattle(UserManager.getInstance().getToken(), 123,
                        456, 1).enqueue(new OnCallback<BWBattleMatchResult>() {
                    @Override
                    public void onResponse(BWBattleMatchResult result) {
                        Log.i(TAG, "joinBWBattle() : onResponse - " + result);
                    }

                    @Override
                    public void onFailure(BWBattleMatchResult result) {
                        Log.i(TAG, "joinBWBattle() : onFailure - " + result);
                    }
                });
            }
        });

        ((Button) findViewById(R.id.button11)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i(TAG, "getBWBattleMatchResult()");
                battleService().getBWBattleMatchResult(UserManager.getInstance().getToken(), 123, 456, 1)
                        .enqueue(new OnCallback<BWBattleMatchResult>() {
                            @Override
                            public void onResponse(BWBattleMatchResult result) {
                                Log.i(TAG, "getBWBattleMatchResult() : onResponse - " + result);
                            }

                            @Override
                            public void onFailure(BWBattleMatchResult result) {
                                Log.i(TAG, "getBWBattleMatchResult() : onFailure - " + result);
                            }
                        });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBWBattleMatchPush(BWBattleMatchResultEvent event) {
        Log.i(TAG, "onBWBattleMatchPush() : " + event);

        if (event == null) {
            Log.i(TAG, "onBWBattleMatchPush() : event null");
            return;
        }

        String content = event.getContent();
        final BWBattleMatchResult result = GlobalGson.get().fromJson(content,
                BWBattleMatchResult.class);
        Log.i(TAG, "onBWBattleMatchPush() result : " + result);
    }

}
