
package com.xgame.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xgame.R;
import com.xgame.account.presenter.LoginPresenter;
import com.xgame.account.view.BaseLoginView;
import com.xgame.account.view.LoadingDialog;
import com.xgame.common.os.WeakHandler;
import com.xgame.common.util.NetworkUtil;
import com.xgame.social.SocialConstants;
import com.xgame.ui.activity.AboutUsActivity;
import com.xgame.ui.activity.CommonWebViewActivity;
import com.xgame.util.Analytics;
import com.xgame.util.StringUtil;
import com.xgame.util.ThirdAppUtil;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class LoginFragment extends Fragment implements BaseLoginView {

    @BindView(R.id.register_phone)
    TextView mRegisterTitle;
    @BindView(R.id.input_phone)
    EditText mInputPhone;
    @BindView(R.id.input_verification)
    EditText mInputVerification;
    @BindView(R.id.get_verification)
    TextView mGetVerification;
    @BindView(R.id.login)
    Button mLogin;
    @BindView(R.id.third_app_accounts)
    RecyclerView mThirdAppAccounts;
    @BindView(R.id.privacy)
    TextView privacy;
    private LoadingDialog mLoadingDialog;

    private static final String ARG_TITLE = "title";
    private static final String ARG_THIRD_APP_SHOW = "third_app_show";
    private static final int WAIT_TIME = 60;

    private ThirdAppAccountsAdapter mThirdAppAccountsAdapter;

    private List<ThirdAppData> mThirdAppList = new ArrayList<>();

    protected final int TIME_UPDATE = 1;
    private int mTime;
    private int mPhoneNumSize;
    private int mVerificationCodeSize;

    private LoginPresenter mLoginPresenter;
    private LoginCallback mLoginCallback;

    private WeakHandler mTimeHandler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == TIME_UPDATE && getContext() != null) {
                if (mTime > 0) {
                    mGetVerification.setText(
                            (--mTime) + getContext().getString(R.string.can_send_again_text));
                    Message message = Message.obtain();
                    message.what = TIME_UPDATE;
                    mTimeHandler.sendMessageDelayed(message, 1000);
                } else {
                    mGetVerification.setEnabled(true);
                    mGetVerification.setText(getContext().getString(R.string.send_again_text));
                    mTime = WAIT_TIME;
                }
            }
            return false;
        }
    });

    private Unbinder unbinder;

    public static LoginFragment newInstance(String title, boolean showThirdApp) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putBoolean(ARG_THIRD_APP_SHOW, showThirdApp);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        unbinder = ButterKnife.bind(this, view);
        Bundle data = getArguments();
        if (data != null) {
            init(data.getString(ARG_TITLE), data.getBoolean(ARG_THIRD_APP_SHOW));
        } else {
            init(null, true);
        }
        mLoadingDialog = new LoadingDialog(getActivity());
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onStop() {
        super.onStop();
        mLoadingDialog.dismissLoadingDialog();
    }

    private void init(String title, boolean showThirdApp) {
        if (!TextUtils.isEmpty(title)) {
            mRegisterTitle.setText(title);
        }
        mInputPhone.addTextChangedListener(OnPhoneNumEditTextChangeListener);
        mInputVerification.addTextChangedListener(OnVerificationCodeEditTextChangeListener);
        mTime = WAIT_TIME;
        if (showThirdApp) {
            initThirdApp();
        }
    }

    private void initThirdApp() {
        mThirdAppList.add(
                new ThirdAppData("qq", R.drawable.third_login_qq, SocialConstants.LOGIN.TYPE_QQ));
        mThirdAppList.add(new ThirdAppData("微信", R.drawable.third_login_weixin,
                SocialConstants.LOGIN.TYPE_WECHAT));

        mThirdAppAccounts.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mThirdAppAccountsAdapter = new ThirdAppAccountsAdapter(mThirdAppList);
        mThirdAppAccounts.setAdapter(mThirdAppAccountsAdapter);
        final int padding = getResources()
                .getDimensionPixelSize(R.dimen.third_app_account_login_padding);
        mThirdAppAccounts.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                    RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.left = padding;
                outRect.right = padding;
            }
        });
        mThirdAppAccountsAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                onThirdAppLoginClicked(view);
            }
        });
    }

    @OnClick({
            R.id.get_verification, R.id.login, R.id.privacy
    })
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.get_verification:
                onGetVerificationClicked();
                break;
            case R.id.login:
                onLoginClicked();
                break;
            case R.id.privacy:
                CommonWebViewActivity.startWeb(view.getContext(),
                        AboutUsActivity.USER_AGREEMENT_URL,
                        view.getContext().getString(R.string.privacy_title));
                break;
            default:
                break;
        }
    }

    private void onThirdAppLoginClicked(View view) {
        Object tag = view.getTag();
        if (tag != null && tag instanceof ThirdAppData) {
            int type = ((ThirdAppData) tag).loginType;
            if (checkAppInstalled(type) && mLoginPresenter != null) {
                mLoginPresenter.onThirdAppLogin(type);
            }
            if (type == SocialConstants.LOGIN.TYPE_QQ) {
                //qq登录按钮点击
                Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_CLICK, Analytics.Constans.STOCK_ID_QQ,
                        Analytics.Constans.STOCK_NAME_QQ, Analytics.Constans.STOCK_TYPE_BTN,
                        Analytics.Constans.PAGE_LOGIN, Analytics.Constans.SECTION_LOGIN, null);
            } else if (type == SocialConstants.LOGIN.TYPE_WECHAT) {
                //微信登录按钮点击
                Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_CLICK, Analytics.Constans.STOCK_ID_WECHAT,
                        Analytics.Constans.STOCK_NAME_WECHAT, Analytics.Constans.STOCK_TYPE_BTN,
                        Analytics.Constans.PAGE_LOGIN, Analytics.Constans.SECTION_LOGIN, null);
            }
        }
    }

    private boolean checkAppInstalled(int type) {
        switch (type) {
            case SocialConstants.LOGIN.TYPE_QQ:
                return ThirdAppUtil.isQQInstalled(true);
            case SocialConstants.LOGIN.TYPE_WECHAT:
                return ThirdAppUtil.isWXInstalled(true);
            default:
                return true;
        }
    }

    private void onGetVerificationClicked() {
        String phoneNum = mInputPhone.getText().toString().trim();
        if (NetworkUtil.hasNetwork(getContext())) {
            String phoneNumCheckHint = StringUtil.checkLoginOrRegisterValid(getContext(), phoneNum);
            if (StringUtil.showErrorMsgIfNeeded(getContext(), phoneNumCheckHint)) {
                return;
            }
            mTime = WAIT_TIME;
            mTimeHandler.sendMessage(mTimeHandler.obtainMessage(TIME_UPDATE));
            mInputVerification.setText("");
            mInputVerification.requestFocus();
            mGetVerification.setEnabled(false);
            if (mLoginPresenter != null) {
                mLoginPresenter.getVerificationCode(phoneNum);
            }
        } else {
            Toast.makeText(getContext(), getString(R.string.net_error_text), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void onLoginClicked() {
        String mVerificationCodeNum = mInputVerification.getText().toString().trim();
        String phoneNum = mInputPhone.getText().toString().trim();
        String phoneNumCheckHint = StringUtil.checkLoginOrRegisterValid(getContext(), phoneNum);
        String verificationCodeNumCheckHint = StringUtil.checkVerificationCodeValid(getContext(),
                mVerificationCodeNum);
        if (StringUtil.showErrorMsgIfNeeded(getContext(), phoneNumCheckHint)) {
            return;
        }
        if (StringUtil.showErrorMsgIfNeeded(getContext(), verificationCodeNumCheckHint)) {
            return;
        }
        mLoginPresenter.onPhoneLogin(phoneNum, mVerificationCodeNum);
        mLoadingDialog.showLoadingDialog(this.getString(R.string.logining_text));
        //“登录”按钮点击
        Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_CLICK, Analytics.Constans.STOCK_ID_MOBILE,
                Analytics.Constans.STOCK_NAME_MOBILE, Analytics.Constans.STOCK_TYPE_BTN,
                Analytics.Constans.PAGE_LOGIN, Analytics.Constans.SECTION_LOGIN, null);
    }

    private void changeLoginBtnEnable() {
        if (mPhoneNumSize == 11 && mVerificationCodeSize == 6) {
            mLogin.setEnabled(true);
        } else {
            mLogin.setEnabled(false);
        }
    }

    private TextWatcher OnPhoneNumEditTextChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mPhoneNumSize = s.length();
            changeLoginBtnEnable();
            if (mPhoneNumSize == 11 && mTime == WAIT_TIME) {
                mGetVerification.setEnabled(true);
            } else {
                mGetVerification.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private TextWatcher OnVerificationCodeEditTextChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mVerificationCodeSize = s.length();
            changeLoginBtnEnable();
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    public void setPresenter(LoginPresenter presenter) {
        mLoginPresenter = presenter;
    }

    public void setLoginCallback(LoginCallback callback) {
        mLoginCallback = callback;
    }

    @Override
    public void onLoginSuccess(int type) {
        mLoadingDialog.dismissLoadingDialog();
        if (mLoginCallback != null) {
            mLoginCallback.onLoginSuccess(type);
        }
    }

    @Override
    public void onLoginFailed(int type, String msg) {
        mLoadingDialog.dismissLoadingDialog();
    }

    @Override
    public void onThirdAppLoginStart(int loginType) {
        if (getActivity() == null) {
            return;
        }
        StringBuilder loginDialogText;
        switch (loginType) {
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
    public void onThirdAppLoginEnd(int loginType) {
        mLoadingDialog.dismissLoadingDialog();
        if (getActivity() == null) {
            return;
        }
        switch (loginType) {
            case SocialConstants.LOGIN.TYPE_QQ:
                break;
            case SocialConstants.LOGIN.TYPE_WECHAT:
                break;
            default:
                break;
        }
        mLoadingDialog.showLoadingDialog(this.getString(R.string.logining_text));
    }

    private class ThirdAppData {
        public String loginName;
        public int iconRes;
        public int loginType;

        ThirdAppData(String loginName, int iconRes, int loginType) {
            this.loginName = loginName;
            this.iconRes = iconRes;
            this.loginType = loginType;
        }
    }

    private class ThirdAppAccountsAdapter extends BaseQuickAdapter<ThirdAppData, BaseViewHolder> {

        public ThirdAppAccountsAdapter(@Nullable List<ThirdAppData> data) {
            super(R.layout.third_app_login_item, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, ThirdAppData item) {
            TextView view = (TextView) helper.itemView;
            view.setCompoundDrawablesRelativeWithIntrinsicBounds(0, item.iconRes, 0, 0);
            view.setTag(item);
        }
    }

    public interface LoginCallback {
        public void onLoginSuccess(int loginType);
    }
}
