package com.xgame.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.xgame.R;

public class AboutUsActivity extends BaseActivity {

    public static final String USER_AGREEMENT_URL = "file:///android_asset/userAgreement.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        initToolbar();
        findViewById(R.id.tv_user_agreement).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonWebViewActivity.startWeb(view.getContext(), USER_AGREEMENT_URL,
                        view.getContext().getString(R.string.about_us));
            }
        });
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        toolbar.setTitle(getString(R.string.about_us));
    }
}
