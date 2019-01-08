package com.xgame.ui.activity;

import com.xgame.R;
import com.xgame.account.UserManager;
import com.xgame.account.model.User;
import com.xgame.account.presenter.LoginPresenter;
import com.xgame.ui.activity.home.HomePageActivity;
import com.xgame.ui.fragment.LoginFragment;
import com.xgame.ui.fragment.UserProfileFragment;
import com.xgame.util.Analytics;
import com.xgame.util.PermissionUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * Created by wuyanzhi on 2018/1/24.
 */

public class LoginActivity extends BaseActivity implements LoginFragment.LoginCallback{


    private LoginPresenter mLoginPresenter;
    private LoginFragment mLoginFragment;
    private UserProfileFragment mUserProfileFragment;
    private boolean mLoginDone;

    public static void reLogin(Context context) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
        PermissionUtil.requestNecessaryPermission(this, false);
    }

    private void init() {
        checkJump();
    }

    private void checkJump() {
        if (UserManager.getInstance().isLogin()) {
            startMainActivity();
            finish();
            return;
        }
        if (!mLoginDone) {
            showLogin();
        } else {
            showUserProfile();
        }
    }

    public void showLogin() {
        if (mLoginFragment == null) {
            mLoginFragment = LoginFragment.newInstance(null, true);
        }

        if (mLoginFragment.isAdded()) {
            return;
        }
        if (mLoginPresenter == null) {
            mLoginPresenter = new LoginPresenter(this, 1);
        }
        mLoginPresenter.setBaseLoginView(mLoginFragment);
        mLoginFragment.setPresenter(mLoginPresenter);
        mLoginFragment.setLoginCallback(this);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.content, mLoginFragment, "login_fragment");
        transaction.commit();
        //登录页展现
        Analytics.trackPageShowEvent(Analytics.Constans.URL_TITLE_LOGIN_PAGE, Analytics.Constans.VISIT_TYPE_READY, null);
    }

    public void showUserProfile() {
        if (mUserProfileFragment == null) {
            User user = UserManager.getInstance().getUser();
            mUserProfileFragment = UserProfileFragment.newInstance(user.getHeadimgurl(), user.getNickname(), user.getSex(), user.getBirthday(), null);
        }
        if (mUserProfileFragment.isAdded()) {
            return;
        }
        if (mLoginPresenter == null) {
            mLoginPresenter = new LoginPresenter(this, 1);
        }
        mLoginPresenter.setBaseUserProfileView(mUserProfileFragment);
        mUserProfileFragment.setPresenter(mLoginPresenter);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if (mLoginFragment != null) {
            transaction.remove(mLoginFragment);
        }
        transaction.replace(R.id.content, mUserProfileFragment, "profile_fragment");
        transaction.commit();
    }

    public void startMainActivity() {
        startActivity(new Intent(this, HomePageActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        PermissionUtil.checkPermissionAgain(this);
        if (mLoginPresenter != null) {
            mLoginPresenter.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mLoginPresenter != null) {
            mLoginPresenter.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoginPresenter != null) {
            mLoginPresenter.onDestroy();
        }
    }

    @Override
    public void onLoginSuccess(int loginType) {
        mLoginDone = true;
        checkJump();
    }
}
