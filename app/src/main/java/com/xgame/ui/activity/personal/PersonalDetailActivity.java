
package com.xgame.ui.activity.personal;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.View;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.xgame.R;
import com.xgame.account.AccountConstants;
import com.xgame.account.UserManager;
import com.xgame.account.model.User;
import com.xgame.account.presenter.LoginPresenter;
import com.xgame.account.view.BaseUserProfileView;
import com.xgame.account.view.LoadingDialog;
import com.xgame.app.GlideApp;
import com.xgame.app.XgameApplication;
import com.xgame.common.util.ImageUtils;
import com.xgame.common.util.LogUtil;
import com.xgame.ui.activity.BaseActivity;
import com.xgame.ui.activity.personal.dialog.PersonalDialogUtils;
import com.xgame.ui.activity.personal.view.ItemAvatarViewHolder;
import com.xgame.ui.activity.personal.view.ItemViewHolder;
import com.xgame.ui.activity.personal.view.PersonalToolbarHolder;
import com.xgame.util.PickerUtil;
import com.xgame.util.dialog.BirthdayDialog;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved. Created by dingning1 on
 * 18-1-29.
 */

public class PersonalDetailActivity extends BaseActivity implements BaseUserProfileView {

    private static final int ALBUM_CODE = Activity.RESULT_FIRST_USER + 198;
    private static final int AVATAR_CODE = Activity.RESULT_FIRST_USER + 199;

    private static final int MAX_NICKNAME_LENGTH = 20;

    @BindView(R.id.item_avatar)
    View mItemAvatar;
    @BindView(R.id.item_nickname)
    View mItemNickName;
    @BindView(R.id.item_age)
    View mItemAge;
    @BindView(R.id.item_gender)
    View mItemGender;

    private ItemAvatarViewHolder mAvatarHolder;
    private ItemViewHolder mNickNameHolder;
    private ItemViewHolder mAgeHolder;
    private ItemViewHolder mGenderHolder;
    private LoginPresenter mLoginPresenter;
    private LoadingDialog mLoadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_detail);
        ButterKnife.bind(this);

        initToolBar();
        initView();
        loadUserData();
        initPresenter();
    }

    private void initToolBar() {
        new PersonalToolbarHolder(findViewById(R.id.toolbar))
                .setBackground(R.color.color_white)
                .enableDivider(true)
                .setTitle(R.string.personal_detail_title, 0)
                .setBackIcon(R.drawable.icon_back_black, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onBackPressed();
                    }
                });
    }

    private void initView() {
        mAvatarHolder = new ItemAvatarViewHolder(mItemAvatar);
        mAvatarHolder.setName(R.string.photo_text);
        mAvatarHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PickerUtil.openAlbum(PersonalDetailActivity.this, ALBUM_CODE);
            }
        });

        mNickNameHolder = new ItemViewHolder(mItemNickName).setName(R.string.nickname_text);
        mNickNameHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersonalDialogUtils.showInputDialog(PersonalDetailActivity.this,
                        UserManager.getInstance().getUser().getNickname(), R.string.nickname_hint,
                        MAX_NICKNAME_LENGTH, new PersonalDialogUtils.IDialogCallback<String>() {
                            @Override
                            public void onResult(String result) {
                                if (!TextUtils.isEmpty(result)) {
                                    mNickNameHolder.setInfo(result);
                                    if (!TextUtils.equals(
                                            UserManager.getInstance().getUser().getNickname(),
                                            result)) {
                                        ArrayMap<String, String> map = new ArrayMap<>();
                                        map.put(AccountConstants.AccountKey.KEY_NICKNAME, result);
                                        uploadUserInfo(map);
                                    }
                                }
                            }
                        });
            }
        });

        mAgeHolder = new ItemViewHolder(mItemAge).setName(R.string.age_text);
        mAgeHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String birthDay = UserManager.getInstance().getUser().getBirthday();
                final String oldBirthday = TextUtils.isEmpty(birthDay) ? "2000-01-01" : birthDay;
                final BirthdayDialog dlg = new BirthdayDialog.Builder(PersonalDetailActivity.this)
                        .setDate(oldBirthday).create();
                dlg.setOnButtonClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        onSelectAge(dlg, oldBirthday);
                    }
                });
                dlg.show();
            }
        });
        mGenderHolder = new ItemViewHolder(mItemGender).setName(R.string.sex_text);
        mGenderHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectGender();
            }
        });
        mLoadingDialog = new LoadingDialog(this);
    }

    private void initPresenter() {
        mLoginPresenter = new LoginPresenter(this, LoginPresenter.REQUEST_USER_PROFILE);
        mLoginPresenter.setBaseUserProfileView(this);
    }

    private void onSelectAge(BirthdayDialog dlg, String oldBirthday) {
        String day = dlg.getDate();
        if (!day.equals(oldBirthday)) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date date = format.parse(day);
                Calendar cd = Calendar.getInstance();
                cd.setTime(date);
                Calendar now = Calendar.getInstance();
                int age = now.get(Calendar.YEAR) - cd.get(Calendar.YEAR);
                mAgeHolder.setInfo(String.valueOf(age));
                ArrayMap<String, String> map = new ArrayMap<>();
                map.put(AccountConstants.AccountKey.KEY_BIRTHDAY, day);
                uploadUserInfo(map);
            } catch (ParseException e) {
                LogUtil.d(PersonalDetailActivity.this.toString(),
                        "parse data failed, " + e + ", " + day);
            }
        }
    }

    private void onSelectGender() {
        final int defaultGender = UserManager.getInstance().getUser().getSex();
        PersonalDialogUtils.showGenderSelectDialog(PersonalDetailActivity.this,
                defaultGender > 0 ? defaultGender : User.GENDER_MALE,
                new PersonalDialogUtils.IDialogCallback<Integer>() {
                    @Override
                    public void onResult(Integer result) {
                        mGenderHolder.setInfo(getGenderText(result));
                        if (result != defaultGender) {
                            ArrayMap<String, String> map = new ArrayMap<>();
                            map.put(AccountConstants.AccountKey.KEY_SEX, String.valueOf(result));
                            uploadUserInfo(map);
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }
        if (requestCode == ALBUM_CODE) {
            PickerUtil.openCropActivity(this, data.getData(), AVATAR_CODE);
        } else if (requestCode == AVATAR_CODE) {
            String imagePath = data.getStringExtra("imagePath");
            GlideApp.with(this).load(imagePath).into(mAvatarHolder.avatar);
            uploadAvatar(imagePath);
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
    public void uploadAvatarFailed(String msg) {
        showUploadFailedToast();
    }

    @Override
    public void uploadAvatarSuccess() {
        showUploadSuccessToast();
    }

    @Override
    public void uploadUserInfoFailed(String msg) {
        showUploadFailedToast();
    }

    @Override
    public void uploadUserInfoSuccess() {
        showUploadSuccessToast();
    }

    @Override
    public void setPresenter(LoginPresenter presenter) {

    }

    private void uploadUserInfo(Map<String, String> map) {
        mLoadingDialog.showLoadingDialog(R.string.uploading_text);
        mLoginPresenter.modifyUserInfo(map);
    }

    private void loadUserData() {
        User user = UserManager.getInstance().getUser();

        GlideApp.with(this).load(user.getHeadimgurl()).placeholder(R.drawable.default_avatar)
                .into(mAvatarHolder.avatar);
        mNickNameHolder.setInfo(user.getNickname());
        mAgeHolder.setInfo(String.valueOf(user.getAge()));
        mGenderHolder.setInfo(getGenderText(user.getSex()));
    }

    private String getGenderText(int gender) {
        int strId = gender == User.GENDER_FEMALE ? R.string.female_text : R.string.male_text;
        return getString(strId);
    }

    private void uploadAvatar(String imagePath) {
        String imageBase64Data = ImageUtils.imgToBase64(imagePath, null, null);
        if (TextUtils.isEmpty(imageBase64Data)) {
            Toast.makeText(this, R.string.upload_avatar_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        mLoadingDialog.showLoadingDialog(R.string.uploading_text);
        mLoginPresenter.uploadAvatar(imageBase64Data, null);
    }

    private void showUploadFailedToast() {
        mLoadingDialog.dismissLoadingDialog();
        Toast.makeText(XgameApplication.getApplication(), R.string.uploading_failed_text,
                Toast.LENGTH_SHORT).show();

    }

    private void showUploadSuccessToast() {
        mLoadingDialog.dismissLoadingDialog();
        Toast.makeText(XgameApplication.getApplication(), R.string.uploading_success_text,
                Toast.LENGTH_SHORT).show();

    }
}
