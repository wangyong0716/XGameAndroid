package com.xgame.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.xgame.R;
import com.xgame.battle.BattleManager;
import com.xgame.battle.BattleUtils;
import com.xgame.common.util.LogUtil;
import com.xgame.util.dialog.BaiWanAlertDialog;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 逗游游戏界面
 *
 * Created by zhanglianyu on 18-1-30.
 */

public class BattleActivity2 extends Activity {

    @BindView(R.id.web_view)
    WebView mWebView;

    private static final String TAG = "BattleActivity2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final Intent intent = getIntent();
        final String gameUrl = BattleManager.getInstance().getGameUrl();
        final String token = intent.getStringExtra(BattleUtils.EXTRA_SELF_TOKEN);

        if (TextUtils.isEmpty(gameUrl) || TextUtils.isEmpty(token)) {
            LogUtil.i(TAG, "onCreate() : param is empty");
            finish();
            return;
        }

        final Uri.Builder builder = Uri.parse(gameUrl).buildUpon();
        builder.appendQueryParameter(BattleUtils.URL_PARAM_TOKEN, token);
        String url = builder.build().toString();
        LogUtil.i(TAG, "onCreate() : url - " + url);

        setContentView(R.layout.activity_battle2);
        ButterKnife.bind(this);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new NativeInterface(this), "NativeInterface");
        mWebView.loadUrl(url);
    }

    @Override
    protected void onDestroy() {
        mWebView.destroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        BaiWanAlertDialog.Builder builder = new BaiWanAlertDialog.Builder(this);
        builder.setMessage(getString(R.string.battle_back_title));
        builder.setPositiveButton(R.string.battle_back_yes, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BattleActivity2.super.onBackPressed();
            }
        });
        builder.setNegativeButton(R.string.battle_back_no, null);
        builder.create().show();
    }


    private void onGameOver(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // TODO: 18-1-30
                LogUtil.i(TAG, "onGameOver() : " + message);
                // finish();
            }
        });
    }

    private static class NativeInterface {

        private WeakReference<BattleActivity2> activityRef;

        public NativeInterface(BattleActivity2 activity) {
            activityRef = new WeakReference<BattleActivity2>(activity);
        }

        @JavascriptInterface
        public void callNativeGameOver(final String message) {
            if (activityRef == null) {
                return;
            }
            final BattleActivity2 activity = activityRef.get();
            if (activity == null) {
                return;
            }
            activity.onGameOver(message);
        }
    }
}
