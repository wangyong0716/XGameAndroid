
package com.xgame.account.presenter;

import java.lang.ref.WeakReference;
import java.util.Map;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.xgame.R;
import com.xgame.account.UserManager;
import com.xgame.account.base.BasePresenter;
import com.xgame.account.event.AccountEventController;
import com.xgame.account.event.AccountEventController.LoginEvent;
import com.xgame.account.event.AccountEventController.UploadEvent;
import com.xgame.account.model.LoginModel;
import com.xgame.account.model.LoginModelImpl;
import com.xgame.account.view.BaseLoginView;
import com.xgame.account.view.BaseUserProfileView;
import com.xgame.social.SocialConstants;
import com.xgame.util.Analytics;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

public class LoginPresenter implements BasePresenter {
    public String TAG = "LoginPresenter";
    public static boolean HAVE_LOGIN_ACTIVITY = false;

    public static final int REQUEST_LOGIN = 1000;
    public static final int REQUEST_MAIN_LOGIN = 1001;
    public static final int REQUEST_SETTING_LOGIN = 1002;
    public static final int REQUEST_USER_PROFILE = 1003;

    private WeakReference<Activity> mActivity;
    private WeakReference<BaseLoginView> mLoginView;
    private WeakReference<BaseUserProfileView> mUserProfileView;
    private LoginModel mLoginModel;

    private int mFrom = REQUEST_MAIN_LOGIN;

    public LoginPresenter(Activity activity, int from) {
        mActivity = new WeakReference<>(activity);
        mLoginModel = new LoginModelImpl();
        mFrom = from;
        if (from == REQUEST_LOGIN) {
            HAVE_LOGIN_ACTIVITY = true;
        }
        TAG += this.hashCode();
    }

    public void setBaseLoginView(BaseLoginView baseLoginView) {
        mLoginView = new WeakReference<>(baseLoginView);
    }

    public void setBaseUserProfileView(BaseUserProfileView baseUserProfileView) {
        mUserProfileView = new WeakReference<>(baseUserProfileView);
    }

    public void getVerificationCode(String phoneNum) {
        mLoginModel.getVerificationCode(phoneNum);
    }

    public void onPhoneLogin(String phoneNum, String verificationCode) {
        mLoginModel.onPhoneLogin(phoneNum, verificationCode);
    }

    public void onPhoneBind(String phoneNum, String verificationCode) {
        mLoginModel.onPhoneBind(phoneNum, verificationCode);
    }

    public void onThirdAppLogin(int loginType) {
        mLoginModel.onThirdAppLogin(mActivity.get(), loginType);
        BaseLoginView loginView = mLoginView.get();
        if (loginView != null) {
            loginView.onThirdAppLoginStart(loginType);
        }
    }

    public void onThirdAppBind(int loginType) {
        mLoginModel.onThirdAppBind(mActivity.get(), loginType);
    }

    public void uploadAvatar(String avatarBase64, Map<String, String> map) {
        mLoginModel.uploadAvatar(avatarBase64, map);
    }

    public void modifyUserInfo(Map<String, String> infoMap) {
        mLoginModel.modifyUserInfo(infoMap);
    }

    public void completeUserInfo(Map<String, String> infoMap) {
        mLoginModel.completeUserInfo(infoMap);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(final LoginEvent event) {
        if (event == null || mActivity == null) {
            return;
        }
        Context context = mActivity.get();
        if (context == null) {
            return;
        }
        switch (event.getEventType()) {
            case LoginEvent.EVENT_TYPE_THIRD_APP_LOGIN_SUCCESS:
                if (mLoginView.get() != null) {
                    mLoginView.get().onThirdAppLoginEnd(event.getLoginType());
                }

                break;
            case LoginEvent.EVENT_TYPE_THIRD_APP_LOGIN_FAILED:
                if (mLoginView.get() != null) {
                }

                break;

            case LoginEvent.EVENT_TYPE_LOGIN_SUCCESS:
                if (mLoginView.get() != null) {
                    mLoginView.get().onLoginSuccess(event.getLoginType());
                }
                trackLoginEvent(event.getLoginType());
                break;
            case LoginEvent.EVENT_TYPE_LOGIN_CANCEL:

                break;
            case LoginEvent.EVENT_TYPE_LOGIN_FAILED:
                Toast.makeText(context,
                        context.getResources().getString(
                                UserManager.getInstance().isLogin() ? R.string.bind_failed
                                        : R.string.login_failed),
                        Toast.LENGTH_SHORT).show();
                if (mLoginView.get() != null) {
                    mLoginView.get().onLoginFailed(event.getLoginType(), event.getMsg());
                }

                break;
            case LoginEvent.EVENT_TYPE_LOGIN_EXCEPTION:

                break;
            case LoginEvent.EVENT_TYPE_GET_VERIFICATION_FAILED:
                Toast.makeText(context,
                        context.getResources()
                                .getString(R.string.toast_virification_code_send_fail_text),
                        Toast.LENGTH_SHORT).show();

                break;
            case LoginEvent.EVENT_TYPE_GET_VERIFICATION_SUCCESS:
                Toast.makeText(context,
                        context.getResources().getString(
                                R.string.toast_virification_code_sended_text),
                        Toast.LENGTH_SHORT).show();

                break;
            default:
                break;
        }
    }

    private void trackLoginEvent(int loginType) {
        String actionName = null;
        if (loginType == SocialConstants.LOGIN.TYPE_PHONE_NUM_VERIFICATION_CODE) {
            actionName = Analytics.Constans.CUSTOM_ACTION_NAME_MOBILE_LOGIN;
        } else if (loginType == SocialConstants.LOGIN.TYPE_QQ) {
            actionName = Analytics.Constans.CUSTOM_ACTION_NAME_QQ_LOGIN;
        } else if (loginType == SocialConstants.LOGIN.TYPE_WECHAT) {
            actionName = Analytics.Constans.CUSTOM_ACTION_NAME_WX_LOGIN;
        } else {
            actionName = "Other";
        }
        Analytics.trackCustomEvent(Analytics.Constans.ACTION_PATH_LOGIN_SUCCESS, Analytics.Constans.ACTION_TYPE_LOGIN_SUCCESS,
                actionName, null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(final AccountEventController.UploadEvent event) {
        if (event == null || mActivity == null) {
            return;
        }
        Context activity = mActivity.get();
        if (activity == null) {
            return;
        }
        BaseUserProfileView profileView = mUserProfileView.get();
        if (profileView == null) {
            return;
        }
        switch (event.getEventType()) {
            case UploadEvent.EVENT_TYPE_UPLOAD_SUCCESS:
                if (event.getUploadType() == UploadEvent.UPLOAD_TYPE_AVATAR) {
                    profileView.uploadAvatarSuccess();
                } else if (event.getUploadType() == UploadEvent.UPLOAD_TYPE_INFO) {
                    profileView.uploadUserInfoSuccess();
                }
                break;

            case UploadEvent.EVENT_TYPE_UPLOAD_CANCEL:
            case UploadEvent.EVENT_TYPE_UPLOAD_FAILED:
                if (event.getUploadType() == UploadEvent.UPLOAD_TYPE_AVATAR) {
                    profileView.uploadAvatarFailed(
                            event.getData() == null ? null : event.getData().toString());
                } else if (event.getUploadType() == UploadEvent.UPLOAD_TYPE_INFO) {
                    profileView.uploadUserInfoFailed(
                            event.getData() == null ? null : event.getData().toString());
                }
                break;
            default:
                break;
        }
    }

    public void onResume() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void onDestroy() {
        // EventBus.getDefault().unregister(this);
    }

    public void onPause() {
        EventBus.getDefault().unregister(this);
    }

}
