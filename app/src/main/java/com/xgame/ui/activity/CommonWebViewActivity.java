package com.xgame.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.xgame.R;
import com.xgame.account.view.LoadingDialog;
import com.xgame.ui.view.CommonWebView;

public class CommonWebViewActivity extends BaseActivity {

    private LoadingDialog mLoadingDialog;

    public static void startWeb(Context context, String url, String title) {
        Intent intent = new Intent(context, CommonWebViewActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("title",
                title != null ? title : context.getString(R.string.app_name));
        context.startActivity(intent);
    }

    public static void startWeb(Context context, String url) {
        Intent intent = new Intent(context, CommonWebViewActivity.class);
        intent.putExtra("url", url);
        context.startActivity(intent);
    }

    private String mUrl;
    private String mTitle;
    private CommonWebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_web_view);
        getIntentData();
        initToolbar();
        mLoadingDialog = new LoadingDialog(CommonWebViewActivity.this);
        mWebView = findViewById(R.id.web_view);
        WebSettings settings = this.mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        this.mWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });
        this.mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (mLoadingDialog != null) {
                    mLoadingDialog.updateLoadingDialog(getString(R.string.loading_progress,
                            String.valueOf(newProgress)));
                    if (newProgress == 100) {
                        mLoadingDialog.dismissLoadingDialog();
                        mLoadingDialog = null;
                    }
                }
            }
        });
        if (!TextUtils.isEmpty(mUrl) && URLUtil.isValidUrl(mUrl)) {
            mWebView.loadUrl(mUrl);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mLoadingDialog != null) {
            mLoadingDialog.showLoadingDialog(R.string.loading);
        }
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        if (!TextUtils.isEmpty(mTitle)) {
            toolbar.setTitle(mTitle);
        } else {
            toolbar.setVisibility(View.GONE);
        }
    }

    private void getIntentData() {
        mUrl = getIntent().getStringExtra("url");
        mTitle = getIntent().getStringExtra("title");
    }

    @Override
    public void onBackPressed() {
        if (this.mWebView != null && this.mWebView.canGoBack()) {
            this.mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
