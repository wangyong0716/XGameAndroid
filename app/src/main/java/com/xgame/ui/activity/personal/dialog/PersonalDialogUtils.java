package com.xgame.ui.activity.personal.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.concurrent.TimeUnit;

import com.xgame.R;
import com.xgame.account.model.User;
import com.xgame.common.util.ToastUtil;
import com.xgame.common.util.UiUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by dingning1
 * on 18-1-31.
 */


public class PersonalDialogUtils {

    public interface IDialogCallback<T> {
        void onResult(T result);
    }

    // single input field dialog
    static class InputViewHolder {
        @BindView(R.id.edit) EditText mEdit;
        @BindView(R.id.confirm) View mConfirm;

        InputViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public static void showInputDialog(Activity act, String defaultText, int hintId, int limit,
            final IDialogCallback<String> callback) {
        final View view = View.inflate(act, R.layout.personal_input, null);
        final InputViewHolder holder = new InputViewHolder(view);

        final Dialog dlg = createInputDialog(act, view, callback);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });
        holder.mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
                if (callback != null) {
                    callback.onResult(holder.mEdit.getText().toString());
                }
            }
        });
        initEdit(holder.mEdit, defaultText, hintId, limit);

        dlg.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dlg.show();
    }

    // gender selection dialog
    static class GenderViewHolder {
        @BindView(R.id.male_check) View maleCheck;
        @BindView(R.id.female_check) View femaleCheck;
        @BindView(R.id.male_img) ImageView maleImg;
        @BindView(R.id.female_img) ImageView femaleImg;
        @BindView(R.id.confirm) View btn;

        GenderViewHolder(View view) {
            ButterKnife.bind(this, view);
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Integer gender = UiUtil.getTag(v, 0);
                    if (gender != null) {
                        setGender(gender);
                    }
                }
            };
            maleCheck.setTag(User.GENDER_MALE);
            femaleCheck.setTag(User.GENDER_FEMALE);
            maleCheck.setOnClickListener(listener);
            femaleCheck.setOnClickListener(listener);
        }

        void setGender(int gender) {
            check(maleImg, gender == User.GENDER_MALE);
            check(femaleImg, gender == User.GENDER_FEMALE);
        }

        void check(ImageView view, boolean isCheck) {
            view.setTag(isCheck);
            view.setImageResource(isCheck ? R.drawable.icon_selected : R.drawable.icon_unselected);
        }

        int getSelectGender() {
            boolean maleSelected = UiUtil.getTag(maleImg, 0);
            return maleSelected ? User.GENDER_MALE : User.GENDER_FEMALE;
        }
    }

    public static void showGenderSelectDialog(Activity act, int default_gender, final IDialogCallback<Integer> callback) {
        final View view = View.inflate(act, R.layout.personal_gender_select, null);
        final GenderViewHolder holder = new GenderViewHolder(view);
        holder.setGender(default_gender);

        final Dialog dlg = new Dialog(act);
        dlg.setContentView(view);
        setDialogWindowAttributes(dlg);

        holder.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
                callback.onResult(holder.getSelectGender());
            }
        });
        dlg.show();
    }

    // feedback input dialog
    private static final int MAX_FEEDBACK_LENGTH = 500;
    private static final int MAX_CONTACT_LENGTH = 64;
    private static final long EMPTY_PROMPT_INTERVAL = TimeUnit.SECONDS.toMillis(5);

    static class FeedbackInputHolder {
        @BindView(R.id.feedback_edit) EditText feedbackEdit;
        @BindView(R.id.contact_edit) EditText contactEdit;
        @BindView(R.id.confirm) View confirm;

        long lastPromptEmpty;

        FeedbackInputHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public static void showFeedbackInputDialog(final Activity act, final IDialogCallback<String[]> callback) {
        final View view = View.inflate(act, R.layout.personal_feedback_input, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onResult(null);
            }
        });
        final FeedbackInputHolder holder = new FeedbackInputHolder(view);
        initEdit(holder.feedbackEdit, "", R.string.feedback_hint, MAX_FEEDBACK_LENGTH);
        initEdit(holder.contactEdit, "", R.string.contact_hint, MAX_CONTACT_LENGTH);

        final Dialog dlg = createInputDialog(act, view, callback);

        holder.feedbackEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                holder.lastPromptEmpty = 0;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        holder.confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String feedback = String.valueOf(holder.feedbackEdit.getText());
                if (TextUtils.isEmpty(feedback.trim())) {
                    long now = System.currentTimeMillis();
                    if (now - holder.lastPromptEmpty > EMPTY_PROMPT_INTERVAL) {
                        ToastUtil.showToast(act, R.string.feedback_empty, false);
                        holder.lastPromptEmpty = now;
                    }
                    return;
                }
                String contact = String.valueOf(holder.contactEdit.getText());
                if (TextUtils.isEmpty(contact.trim())) {
                    long now = System.currentTimeMillis();
                    if (now - holder.lastPromptEmpty > EMPTY_PROMPT_INTERVAL) {
                        ToastUtil.showToast(act, R.string.contact_empty, false);
                        holder.lastPromptEmpty = now;
                    }
                    return;
                }
                dlg.dismiss();
                String[] result = new String[]{
                        feedback,
                        contact
                };
                callback.onResult(result);
            }
        });
        dlg.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dlg.show();
    }

    private static Dialog createInputDialog(Activity act, View view, final IDialogCallback callback) {
        Dialog dlg = new Dialog(act, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dlg.setContentView(view);
        dlg.setCancelable(true);
        dlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                callback.onResult(null);
            }
        });
        setDialogWindowAttributes(dlg);
        return dlg;
    }

    private static void initEdit(EditText edit, String defaultText, int hintId, int maxLength) {
        if (maxLength > 0) {
            InputFilter[] filters = new InputFilter[1];
            filters[0] = new InputFilter.LengthFilter(maxLength);
            edit.setFilters(filters);
        }
        if (hintId > 0) {
            edit.setHint(hintId);
        }
        if (!TextUtils.isEmpty(defaultText)) {
            edit.setText(defaultText);
        }
    }

    private static void setDialogWindowAttributes(Dialog dlg) {
        Window window = dlg.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(wlp);
    }
}
