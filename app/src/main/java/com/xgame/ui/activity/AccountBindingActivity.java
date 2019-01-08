
package com.xgame.ui.activity;

import static com.xgame.account.presenter.LoginPresenter.REQUEST_SETTING_LOGIN;

import com.xgame.R;
import com.xgame.account.UserManager;
import com.xgame.account.model.User;
import com.xgame.account.presenter.LoginPresenter;
import com.xgame.account.view.BaseLoginView;
import com.xgame.account.view.LoadingDialog;
import com.xgame.social.SocialConstants;
import com.xgame.util.StringUtil;
import com.xgame.util.ThirdAppUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AccountBindingActivity extends AppCompatActivity implements BaseLoginView {

    private RelativeLayout mBindMobileLayout;
    private TextView mBindMobileTv;
    private RelativeLayout mBindWechatLayout;
    private TextView mBindWechatTv;
    private RelativeLayout mBindQQLayout;
    private TextView mBindQQtv;
    private LoginPresenter mLoginPresenter;
    private LoadingDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_binding);
        initToolbar();
        initView();
        setDate();
        mLoginPresenter = new LoginPresenter(this, REQUEST_SETTING_LOGIN);
        mLoginPresenter.setBaseLoginView(this);
        mLoadingDialog = new LoadingDialog(this);
    }

    private void initView() {
        mBindMobileLayout = findViewById(R.id.layout_bind_mobile);
        mBindMobileTv = findViewById(R.id.tv_bind_mobile);
        mBindWechatLayout = findViewById(R.id.layout_bind_wechat);
        mBindWechatTv = findViewById(R.id.tv_bind_wechat);
        mBindWechatLayout.setOnClickListener(mOnClickListener);
        mBindQQLayout = findViewById(R.id.layout_bind_qq);
        mBindQQtv = findViewById(R.id.tv_bind_qq);
        mBindQQLayout.setOnClickListener(mOnClickListener);
        mBindMobileLayout.setOnClickListener(mOnClickListener);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.bind_account);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void setDate() {
        User user = UserManager.getInstance().getUser();
        if (user == null) {
            return;
        }
        if (TextUtils.isEmpty(user.getPhone())) {
            mBindMobileTv.setText(R.string.to_binding);
        } else {
            mBindMobileTv.setText(
                    getString(R.string.had_bind, StringUtil.getPhoneFormat(user.getPhone())));
        }
        if (TextUtils.isEmpty(user.getWxNickname())) {
            mBindWechatTv.setText(R.string.to_binding);
        } else {
            mBindWechatTv.setText(getString(R.string.had_bind, user.getWxNickname()));
        }
        if (TextUtils.isEmpty(user.getQqNickname())) {
            mBindQQtv.setText(R.string.to_binding);
        } else {
            mBindQQtv.setText(getString(R.string.had_bind, user.getQqNickname()));
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == mBindQQLayout) {
                if (UserManager.getInstance().getUser() == null
                        || TextUtils.isEmpty(UserManager.getInstance().getUser().getQqNickname())) {
                    if (ThirdAppUtil.isQQInstalled(true)) {
                        mLoginPresenter.onThirdAppBind(SocialConstants.LOGIN.TYPE_QQ);
                    }
                }
            } else if (v == mBindWechatLayout) {
                if (UserManager.getInstance().getUser() == null
                        || TextUtils.isEmpty(UserManager.getInstance().getUser().getWxNickname())) {
                    if (ThirdAppUtil.isWXInstalled(true)) {
                        mLoginPresenter.onThirdAppBind(SocialConstants.LOGIN.TYPE_WECHAT);
                    }
                }
            } else if (v == mBindMobileLayout) {
                if (UserManager.getInstance().getUser() == null
                        || TextUtils.isEmpty(UserManager.getInstance().getUser().getPhone())) {
                    startActivityForResult(new Intent(v.getContext(), MobileBindingActivity.class),
                            10001);
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10001 && resultCode == Activity.RESULT_OK) {
            User user = UserManager.getInstance().getUser();
            if (user == null) {
                return;
            }
            if (TextUtils.isEmpty(user.getPhone())) {
                mBindMobileTv.setText(R.string.to_binding);
            } else {
                mBindMobileTv.setText(
                        getString(R.string.had_bind, StringUtil.getPhoneFormat(user.getPhone())));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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
    public void onLoginSuccess(int type) {
        mLoadingDialog.dismissLoadingDialog();
        switch (type) {
            case SocialConstants.LOGIN.TYPE_QQ:
                mBindQQtv.setText(getString(R.string.had_bind,
                        UserManager.getInstance().getUser().getQqNickname()));
                break;
            case SocialConstants.LOGIN.TYPE_WECHAT:

                mBindWechatTv.setText(getString(R.string.had_bind,
                        UserManager.getInstance().getUser().getWxNickname()));
                break;
            case SocialConstants.LOGIN.TYPE_PHONE_NUM_VERIFICATION_CODE:
                mBindMobileTv.setText(getString(R.string.had_bind,
                        StringUtil.getPhoneFormat(UserManager.getInstance().getUser().getPhone())));
                break;
            default:
                break;
        }
    }

    @Override
    public void onLoginFailed(int type, String msg) {
        mLoadingDialog.dismissLoadingDialog();
    }

    @Override
    public void onThirdAppLoginStart(int type) {
        StringBuilder loginDialogText;
        switch (type) {
            case SocialConstants.LOGIN.TYPE_QQ:
                loginDialogText = new StringBuilder();
                loginDialogText.append(this.getString(R.string.starting_text));
                loginDialogText.append(this.getString(R.string.qq));
                mLoadingDialog.showLoadingDialog(loginDialogText.toString());
                break;
            case SocialConstants.LOGIN.TYPE_WECHAT:
                loginDialogText = new StringBuilder();
                loginDialogText.append(this.getString(R.string.starting_text));
                loginDialogText.append(this.getString(R.string.wechat));
                mLoadingDialog.showLoadingDialog(loginDialogText.toString());
                break;
            default:
                break;
        }
    }

    @Override
    public void onThirdAppLoginEnd(int type) {
        mLoadingDialog.dismissLoadingDialog();
        mLoadingDialog.showLoadingDialog(this.getString(R.string.binding_text));
    }

    @Override
    public void setPresenter(LoginPresenter presenter) {

    }
}
