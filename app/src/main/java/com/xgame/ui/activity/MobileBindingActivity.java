package com.xgame.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.xgame.R;
import com.xgame.account.presenter.LoginPresenter;
import com.xgame.account.view.BaseLoginView;
import com.xgame.account.view.LoadingDialog;
import com.xgame.common.os.WeakHandler;
import com.xgame.common.util.NetworkUtil;
import com.xgame.util.StringUtil;

public class MobileBindingActivity extends BaseActivity implements BaseLoginView {

    private EditText mInputPhoneEt;
    private EditText mInputVerificationEt;
    private TextView mGetVerificationTv;
    private Button mLoginBtn;
    protected final int TIME_UPDATE = 1;
    private int mTime;
    private static final int WAIT_TIME = 60;
    private LoginPresenter mLoginPresenter;
    private LoadingDialog mLoadingDialog;

    private WeakHandler mTimeHandler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == TIME_UPDATE) {
                if (mTime > 0) {
                    mGetVerificationTv.setText(
                            (--mTime) + getString(R.string.can_send_again_text));
                    Message message = Message.obtain();
                    message.what = TIME_UPDATE;
                    mTimeHandler.sendMessageDelayed(message, 1000);
                } else {
                    mGetVerificationTv.setEnabled(true);
                    mGetVerificationTv.setText(getString(R.string.send_again_text));
                    mTime = WAIT_TIME;
                }
            }
            return false;
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_binding);
        initToolbar();
        initView();
        mLoginPresenter = new LoginPresenter(this, 1);
        mLoginPresenter.setBaseLoginView(this);
        mTime = WAIT_TIME;
        mLoadingDialog = new LoadingDialog(this);
    }

    private void initView() {
        mInputPhoneEt = findViewById(R.id.input_phone);
        mInputVerificationEt = findViewById(R.id.input_verification);
        mGetVerificationTv = findViewById(R.id.get_verification);
        mLoginBtn = findViewById(R.id.login);
        mInputPhoneEt.addTextChangedListener(mOnPhoneNumEditTextChangeListener);
        mInputVerificationEt.addTextChangedListener(mOnVerificationCodeEditTextChangeListener);
        mGetVerificationTv.setOnClickListener(mGetVerificationListener);
        mLoginBtn.setOnClickListener(mLoginListener);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.bind_account);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private View.OnClickListener mGetVerificationListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String phoneNum = mInputPhoneEt.getText().toString().trim();
            if (NetworkUtil.hasNetwork(v.getContext())) {
                String phoneNumCheckHint = StringUtil.checkLoginOrRegisterValid(v.getContext(), phoneNum);
                if (StringUtil.showErrorMsgIfNeeded(v.getContext(), phoneNumCheckHint)) {
                    return;
                }
                mTime = WAIT_TIME;
                mTimeHandler.sendMessage(mTimeHandler.obtainMessage(TIME_UPDATE));
                mInputVerificationEt.setText("");
                mInputVerificationEt.requestFocus();
                mGetVerificationTv.setEnabled(false);
                if (mLoginPresenter != null) {
                    mLoginPresenter.getVerificationCode(phoneNum);
                }
            } else {
                Toast.makeText(v.getContext(), getString(R.string.net_error_text), Toast.LENGTH_SHORT)
                        .show();
            }
        }
    };
    private View.OnClickListener mLoginListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String mVerificationCodeNum = mInputVerificationEt.getText().toString().trim();
            String phoneNum = mInputPhoneEt.getText().toString().trim();
            String phoneNumCheckHint = StringUtil.checkLoginOrRegisterValid(v.getContext(),
                    phoneNum);
            String verificationCodeNumCheckHint =
                    StringUtil.checkVerificationCodeValid(v.getContext(), mVerificationCodeNum);
            if (StringUtil.showErrorMsgIfNeeded(v.getContext(), phoneNumCheckHint)) {
                return;
            }
            if (StringUtil.showErrorMsgIfNeeded(v.getContext(),
                    verificationCodeNumCheckHint)) {
                return;
            }
            mLoadingDialog.showLoadingDialog(R.string.binding_text);
            mLoginPresenter.onPhoneBind(phoneNum, mVerificationCodeNum);
        }
    };

    private TextWatcher mOnPhoneNumEditTextChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            changeLoginBtnEnable();
            if (s.length() == 11 && mTime == WAIT_TIME) {
                mGetVerificationTv.setEnabled(true);
            } else {
                mGetVerificationTv.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private TextWatcher mOnVerificationCodeEditTextChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            changeLoginBtnEnable();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void changeLoginBtnEnable() {
        if (mInputPhoneEt.getText().length() == 11 && mInputVerificationEt.getText().length() == 6) {
            mLoginBtn.setEnabled(true);
        } else {
            mLoginBtn.setEnabled(false);
        }
    }

    @Override
    public void setPresenter(LoginPresenter presenter) {

    }

    @Override
    public void onLoginSuccess(int type) {
        mLoadingDialog.dismissLoadingDialog();
        setResult(Activity.RESULT_OK);
        finish();
    }

    @Override
    public void onLoginFailed(int type, String msg) {
        mLoadingDialog.dismissLoadingDialog();
    }

    @Override
    public void onThirdAppLoginStart(int type) {

    }

    @Override
    public void onThirdAppLoginEnd(int type) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoginPresenter != null) {
            mLoginPresenter.onDestroy();
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
}
