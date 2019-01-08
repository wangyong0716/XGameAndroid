
package com.xgame.ui.fragment;

import java.util.HashMap;

import com.bumptech.glide.Glide;
import com.miui.zeus.mario.sdk.util.ClientInfoUtil;
import com.xgame.R;
import com.xgame.account.AccountConstants;
import com.xgame.account.UserManager;
import com.xgame.account.model.User;
import com.xgame.account.presenter.LoginPresenter;
import com.xgame.account.view.BaseUserProfileView;
import com.xgame.account.view.LoadingDialog;
import com.xgame.app.XgameApplication;
import com.xgame.common.util.ImageUtils;
import com.xgame.common.util.NetworkUtil;
import com.xgame.common.util.ToastUtil;
import com.xgame.ui.activity.home.HomePageActivity;
import com.xgame.util.Analytics;
import com.xgame.util.PickerUtil;
import com.xgame.util.dialog.BirthdayDialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.xgame.R;
import com.xgame.account.AccountConstants;
import com.xgame.account.model.User;
import com.xgame.account.presenter.LoginPresenter;
import com.xgame.account.view.BaseUserProfileView;
import com.xgame.account.view.LoadingDialog;
import com.xgame.app.XgameApplication;
import com.xgame.common.util.ImageUtils;
import com.xgame.common.util.NetworkUtil;
import com.xgame.common.util.ToastUtil;
import com.xgame.ui.activity.home.HomePageActivity;
import com.xgame.util.PickerUtil;
import com.xgame.util.dialog.BirthdayDialog;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by wuyanzhi on 2018/1/26.
 */

public class UserProfileFragment extends Fragment implements BaseUserProfileView {

    private static final String ARG_AVATAR = "avatar_path";
    private static final String ARG_NICK_NAME = "nickname";
    private static final String ARG_GENDER = "gender";
    private static final String ARG_BIRTHDAY = "birthday";
    private static final String ARG_INVITE_CODE = "invite_code";
    private static final int CHOOSE_PHOTO = 100;
    private static final int CROP_PHOTO = 101;

    @BindView(R.id.avatar)
    CircleImageView mAvatar;
    @BindView(R.id.nick_input)
    EditText mNickInput;
    @BindView(R.id.male)
    TextView mMale;
    @BindView(R.id.female)
    TextView mFemale;
    @BindView(R.id.age)
    TextView mAge;
    @BindView(R.id.invite_code)
    EditText mInviteCode;
    @BindView(R.id.go_btn)
    ImageView mGoBtn;
    Unbinder mUnBinder;
    private BirthdayDialog mChooseBirthdayDialog;
    private LoadingDialog mLoadingDialog;
    private int mGender = -1;
    private LoginPresenter mLoginPresenter;
    private String mImagePath;
    private boolean mGoNextStep;

    public static UserProfileFragment newInstance(String avatarPath, String nickName, int gender,
            String birthday, String inviteCode) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_AVATAR, avatarPath);
        args.putString(ARG_NICK_NAME, nickName);
        args.putInt(ARG_GENDER, gender);
        args.putString(ARG_BIRTHDAY, birthday);
        args.putString(ARG_INVITE_CODE, inviteCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        mUnBinder = ButterKnife.bind(this, view);
        mLoadingDialog = new LoadingDialog(getActivity());
        init();
        return view;
    }

    private void init() {
        Bundle data = getArguments();
        if (data != null) {
            mImagePath = data.getString(ARG_AVATAR);
            if (!TextUtils.isEmpty(mImagePath)) {
                Glide.with(this).load(mImagePath)
                        .apply(new RequestOptions().placeholder(R.drawable.default_avatar).error(R.drawable.default_avatar))
                        .into(mAvatar);
            }
            mNickInput.setText(data.getString(ARG_NICK_NAME, ""));
            mGender = data.getInt(ARG_GENDER, -1);
            mMale.setSelected(mGender == 1);
            mFemale.setSelected(mGender == 2);
            mAge.setText(data.getString(ARG_BIRTHDAY, "2000-01-01"));
            mInviteCode.setText(data.getString(ARG_INVITE_CODE, ""));
        }
        mNickInput.addTextChangedListener(mOnInputTextChangeListener);
        mAge.addTextChangedListener(mOnInputTextChangeListener);
        changeGoNextBtnEnable();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnBinder.unbind();
        mLoginPresenter = null;
        mOnInputTextChangeListener = null;
    }

    @OnClick({
            R.id.avatar, R.id.male, R.id.female, R.id.age, R.id.go_btn
    })
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.avatar:
                pickPhoto();
                break;
            case R.id.male:
                mMale.setSelected(true);
                mFemale.setSelected(false);
                mGender = User.GENDER_MALE;
                changeGoNextBtnEnable();
                break;
            case R.id.female:
                mMale.setSelected(false);
                mFemale.setSelected(true);
                mGender = User.GENDER_FEMALE;
                changeGoNextBtnEnable();
                break;
            case R.id.age:
                chooseBirthday();
                break;
            case R.id.go_btn:
                onGoBtnClick();
                break;
        }
    }

    private void chooseBirthday() {
        BirthdayDialog.Builder builder = new BirthdayDialog.Builder(getActivity());
        String date = mAge.getText().toString();
        final String oldBirthday = TextUtils.isEmpty(date) ? "2000-01-01" : date;
        mChooseBirthdayDialog = builder.setDate(oldBirthday)
                .setButtonClickListener(new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mChooseBirthdayDialog.dismiss();
                        mAge.setText(mChooseBirthdayDialog.getDate());
                    }
                }).create();
        mChooseBirthdayDialog.show();
    }

    private void pickPhoto() {
        PickerUtil.openAlbum(this, CHOOSE_PHOTO);
    }

    private void changeGoNextBtnEnable() {

        if (TextUtils.isEmpty(mImagePath) || mGender < User.GENDER_MALE
                || mGender > User.GENDER_FEMALE || TextUtils.isEmpty(mAge.getText())
                || TextUtils.isEmpty(mNickInput.getText())) {
            mGoBtn.setBackgroundResource(R.drawable.go_next_d);
            mGoNextStep = false;
        } else {
            mGoNextStep = true;
            mGoBtn.setBackgroundResource(R.drawable.go_next_n);
        }
    }

    private void onGoBtnClick() {
        if (getActivity() == null) {
            return;
        }
        if (!mGoNextStep) {
            showToast();
            return;
        }
        if (NetworkUtil.hasNetwork(getActivity())) {
            mLoadingDialog.showLoadingDialog(R.string.uploading_text);
            HashMap<String, String> infoMap = new HashMap<>();
            infoMap.put(AccountConstants.AccountKey.KEY_NICKNAME, mNickInput.getText().toString());
            infoMap.put(AccountConstants.AccountKey.KEY_SEX, String.valueOf(mGender));
            infoMap.put(AccountConstants.AccountKey.KEY_BIRTHDAY, mAge.getText().toString());
            String inviteCode = mInviteCode.getText().toString();
            if (!TextUtils.isEmpty(inviteCode)) {
                infoMap.put(AccountConstants.AccountKey.KEY_INVITATION, inviteCode);
            }
            if (mImagePath != null && mImagePath.startsWith("http")) {
                infoMap.put(AccountConstants.AccountKey.KEY_HEAD_IMG_URL, mImagePath);
                mLoginPresenter.completeUserInfo(infoMap);
            } else {
                String imageBase64Data = ImageUtils.imgToBase64(mImagePath, null, null);
                if (TextUtils.isEmpty(imageBase64Data)) {
                    Toast.makeText(getActivity(), R.string.upload_avatar_failed, Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                mLoginPresenter.uploadAvatar(imageBase64Data, infoMap);
            }
            //“下一步”按钮点击
            Analytics.trackClickEvent(Analytics.Constans.SUBTYPE_CLICK, Analytics.Constans.STOCK_ID_NEXT,
                    Analytics.Constans.STOCK_NAME_NEXT, Analytics.Constans.STOCK_TYPE_BTN,
                    Analytics.Constans.PAGE_PROFILE, Analytics.Constans.SECTION_PROFILE, null);
        } else {
            Toast.makeText(getContext(), getString(R.string.net_error_text), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        switch (requestCode) {
            case CHOOSE_PHOTO:
                Uri uri = data.getData();
                if (uri != null) {
                    PickerUtil.openCropActivity(this, uri, CROP_PHOTO);
                }
                break;
            case CROP_PHOTO:
                if (requestCode == CROP_PHOTO && data != null) {
                    String imagePath = data.getStringExtra("imagePath");
                    if (!TextUtils.isEmpty(imagePath)) {
                        Glide.with(this).load(imagePath).into(mAvatar);
                        mImagePath = imagePath;
                        changeGoNextBtnEnable();
                    }
                }
                break;
            default:
                break;
        }
    }

    private TextWatcher mOnInputTextChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            changeGoNextBtnEnable();
        }
    };

    @Override
    public void uploadUserInfoSuccess() {
        mLoadingDialog.dismissLoadingDialog();
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent(activity, HomePageActivity.class);
            intent.putExtra(AccountConstants.FIRST_REGISTER, 1);
            activity.startActivity(intent);
            activity.finish();
        }
    }

    @Override
    public void uploadUserInfoFailed(String msg) {
        showUploadFailedToast();
    }

    @Override
    public void uploadAvatarSuccess() {

    }

    @Override
    public void uploadAvatarFailed(String msg) {
        showUploadFailedToast();
    }

    @Override
    public void setPresenter(LoginPresenter presenter) {
        mLoginPresenter = presenter;
    }

    private void showUploadFailedToast() {
        mLoadingDialog.dismissLoadingDialog();
        Toast.makeText(XgameApplication.getApplication(), R.string.uploading_failed_text,
                Toast.LENGTH_SHORT).show();
    }

    private void showToast() {
        Context context = getContext();
        if (TextUtils.isEmpty(mImagePath)) {
            ToastUtil.showTip(context, R.string.string_utils_choose_photo);
            return;
        }
        if (mGender < User.GENDER_MALE || mGender > User.GENDER_FEMALE) {
            ToastUtil.showTip(context, R.string.string_utils_choose_sex);
            return;
        }
        if (TextUtils.isEmpty(mAge.getText())) {
            ToastUtil.showTip(context, R.string.string_utils_choose_birthday);
            return;
        }
        if (TextUtils.isEmpty(mNickInput.getText())) {
            ToastUtil.showTip(context, R.string.string_utils_input_nickname);
            return;
        }
    }
}
