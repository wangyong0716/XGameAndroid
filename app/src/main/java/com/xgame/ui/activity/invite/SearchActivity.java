package com.xgame.ui.activity.invite;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.xgame.R;
import com.xgame.base.ServiceFactory;
import com.xgame.common.api.OnCallback;
import com.xgame.common.util.ToastUtil;
import com.xgame.invite.model.InvitedUser;
import com.xgame.ui.activity.BaseActivity;
import com.xgame.util.StringUtil;

/**
 * Created by Albert
 * on 18-2-6.
 */

public class SearchActivity extends BaseActivity {

    private EditText mSearchEditor;
    private TextView mSearchTips;
    private ImageView mLoadingIcon;
    private ViewGroup mLoadingLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initView();
        onCreateToolbar();
    }

    private void onCreateToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getTitle());
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    protected void hideKeyboard() {
        InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (im != null && !isDestroyed() && getCurrentFocus() != null) {
            im.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void initView() {
        mLoadingIcon = findViewById(R.id.loading_icon);
        mLoadingLayout = findViewById(R.id.loading_layout);
        findViewById(R.id.search_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchUser();
            }
        });
        final View clear = findViewById(R.id.clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchEditor.setText("");
            }
        });
        mSearchTips = findViewById(R.id.search_tips);
        mSearchTips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchUser();
            }
        });
        mSearchEditor = findViewById(R.id.search_editor);
        mSearchEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mSearchTips.setText(getString(R.string.search_tips, String.valueOf(s)));
                clear.setVisibility(TextUtils.isEmpty(s) ? View.GONE : View.VISIBLE);
            }
        });
        mSearchEditor.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_ENTER:
                            return searchUser();
                    }
                }
                return false;
            }
        });
    }

    private void showLoading() {
        Animation rotateAnim = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnim.setRepeatCount(Animation.INFINITE);
        rotateAnim.setInterpolator(new LinearInterpolator());
        rotateAnim.setDuration(1000);
        mLoadingIcon.startAnimation(rotateAnim);
        mLoadingLayout.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        mLoadingIcon.clearAnimation();
        mLoadingLayout.setVisibility(View.GONE);
    }

    private boolean searchUser() {
        String keyword = String.valueOf(mSearchEditor.getText()).trim();
        return searchUser(keyword);
    }

    private boolean searchUser(final String keyword) {
        if (StringUtil.isEmpty(keyword)) {
            return false;
        }
        showLoading();
        ServiceFactory.inviteService().searchUser(keyword).enqueue(new OnCallback<InvitedUser>() {
            @Override
            public void onResponse(InvitedUser user) {
                if (user == null || StringUtil.isEmpty(user.getAccountId())) {
                    searchFailed(keyword);
                    return;
                }
                Intent intent = new Intent(SearchActivity.this, StrangerDetailActivity.class);
                intent.putExtra("title", getString(R.string.search_title));
                intent.putExtra("user", user);
                startActivity(intent);
                hideLoading();
            }

            @Override
            public void onFailure(InvitedUser user) {
                searchFailed(keyword);
            }
        });
        return true;
    }

    private void searchFailed(String keyword) {
        ToastUtil.showToast(SearchActivity.this, getString(R.string.no_search_result, keyword), false);
        hideLoading();
    }
}
