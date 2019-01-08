package com.xgame.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.xgame.R;
import com.xgame.battle.BattleUtils;
import com.xgame.common.util.LaunchUtils;
import com.xgame.util.Analytics;
import com.xgame.util.PermissionUtil;
import com.xgame.util.dialog.BirthdayDialog;

public class MainActivity extends BaseActivity {
    BirthdayDialog mChooseBirthdayDialog;
    private static final int CHOOSE_PHOE = 100;
    private static final int CROP_PHOE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((Button) findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BattleUtils.gotoBWBattleCover(MainActivity.this, 123);

            }
        });
        ((Button) findViewById(R.id.login)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, LoginActivity.class));
//                String url = "http://staging.growth.e.mi.srv/#/downloadlist/1.103.a.1";
//                MarioSdk.startWebView(url);

            }
        });

        ((Button) findViewById(R.id.match)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
////                BattleUtils.startMatch(MainActivity.this, "1234567", BattleUtils.EXTRA_BATTLE_TYPE_MATCH);
//                MainActivity.this.startActivity(new Intent(MainActivity.this, BWMatchActivity.class));

              //  BattleUtils.startMatch(MainActivity.this, 27, "http://pkclient.egret-labs.org/h5_mi/v24/index.html?gameId=2&myUserId=1&otherUserId=2&isAi=1&gameAiLevel=3");

            }
        });

        ((Button) findViewById(R.id.battle_result)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, BattleResultActivity.class));
            }
        });

        ((Button) findViewById(R.id.coin_battle)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BattleUtils.gotoCoinBattleDetail(MainActivity.this, "1234567", "http://pkclient.egret-labs.org/h5_mi/v24/index.html?gameId=2&myUserId=1&otherUserId=2&isAi=1&gameAiLevel=3","枪神对决", 5888);
            }
        });

        ((Button) findViewById(R.id.coin_battle_result)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, BWMatchActivity.class));
            }
        });

        ((Button) findViewById(R.id.date)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BirthdayDialog.Builder builder = new BirthdayDialog.Builder(MainActivity.this);
                final String oldBirthday = "2000-01-01";
                mChooseBirthdayDialog = builder.setDate(oldBirthday).setButtonClickListener(new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mChooseBirthdayDialog.dismiss();
                        if (!mChooseBirthdayDialog.getDate().equals(oldBirthday)) {
                            Toast.makeText(MainActivity.this, mChooseBirthdayDialog.getDate(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).create();
                mChooseBirthdayDialog.show();
            }
        });

        ((Button) findViewById(R.id.share)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, ShareTestActivity.class));
            }
        });

        ((Button) findViewById(R.id.button10)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent().setClass(getApplicationContext(), TestGameActivity.class));
            }
        });
        findViewById(R.id.register_push).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //PushManager.registerRegIdToServer();
                Analytics.trackPageShowEvent("登录页", "READY", null);
            }
        });
        findViewById(R.id.test_personal).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                LaunchUtils.startActivity(getApplicationContext(), "xgame://xgame.com/personal/home");
            }
        });
        findViewById(R.id.chat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                intent.putExtra(ChatActivity.EXTRA_OTHER_ID, String.valueOf(279509));
                startActivity(intent);
            }
        });
        PermissionUtil.requestNecessaryPermission(this, false);
    }

    public void goToSetting(View view) {
        startActivity(new Intent(view.getContext(),SettingActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        PermissionUtil.checkPermissionAgain(this);
    }
}
