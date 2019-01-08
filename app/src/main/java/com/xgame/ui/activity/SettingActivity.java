package com.xgame.ui.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xgame.R;
import com.xgame.account.UserManager;
import com.xgame.common.application.ApplicationStatus;
import com.xgame.common.util.ExecutorHelper;
import com.xgame.common.util.FileUtils;
import com.xgame.push.PushManager;
import com.xgame.util.dialog.NormalAlertDialog;

import java.io.File;
import java.lang.ref.WeakReference;

public class SettingActivity extends BaseActivity {


    private RelativeLayout mBindAccountLayout;
    private TextView mPhoneBindHintTxt;
    private RelativeLayout mClearCacheLayout;
    private TextView mCacheSizeTxt;
    private RelativeLayout mPushSwitchLayout;
    private SwitchCompat mPushSwitch;
    private TextView mPushCloseWarningTxt;
    private RelativeLayout mAboutUsLayout;
    private TextView mLoginExitBtn;
    private long mCacheSize = 0;
    private String mCachePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mCachePath = getFilesDir().getPath() + "/games";
        initToolbar();
        initView();
        if (UserManager.getInstance().getUser() != null && !TextUtils.isEmpty(UserManager.getInstance().getUser().getPhone())) {
            mPhoneBindHintTxt.setText("");
        }

    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.setting);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void initView() {
        mBindAccountLayout = findViewById(R.id.layout_bind_account);
        mBindAccountLayout.setOnClickListener(mOnClickListener);
        mAboutUsLayout = findViewById(R.id.layout_about_us);
        mAboutUsLayout.setOnClickListener(mOnClickListener);
        mLoginExitBtn = findViewById(R.id.btn_login_exit);
        mLoginExitBtn.setOnClickListener(mOnClickListener);
        mPhoneBindHintTxt = findViewById(R.id.tv_bind_account);
        mClearCacheLayout = findViewById(R.id.layout_clear_cache);
        mClearCacheLayout.setOnClickListener(mOnClickListener);
        mPushSwitchLayout = findViewById(R.id.layout_push_switch);
        mPushSwitchLayout.setOnClickListener(mOnClickListener);
        mPushSwitch = findViewById(R.id.switch_push);
        mPushCloseWarningTxt = findViewById(R.id.txt_push_close_warning);
        mCacheSizeTxt = findViewById(R.id.tv_cache_size);
        mPushSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mPushCloseWarningTxt.setVisibility(View.GONE);
                } else {
                    mPushCloseWarningTxt.setVisibility(View.VISIBLE);
                }
            }
        });
        new GetCacheSizeTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mCachePath);
    }

    public void updateCacheSize(long cacheSize) {
        this.mCacheSize = cacheSize;
        mCacheSizeTxt.setText(FileUtils.byte2FitMemorySize(cacheSize));
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view == mBindAccountLayout) {
                startActivity(new Intent(view.getContext(), AccountBindingActivity.class));
            } else if (view == mAboutUsLayout) {
                startActivity(new Intent(view.getContext(), AboutUsActivity.class));
            } else if (view == mLoginExitBtn) {
                showLoginExitDialog();
            } else if (view == mClearCacheLayout) {
                if (mCacheSize > 0) {
                    showClearCacheDialog();
                } else {
                    Toast.makeText(view.getContext(), R.string.no_cache_to_clean, Toast.LENGTH_LONG).show();
                }
            } else if (view == mPushSwitchLayout) {
                mPushSwitch.setChecked(!mPushSwitch.isChecked());
            }
        }
    };

    private void showClearCacheDialog() {
        new NormalAlertDialog.Builder(this)
                .setMessage(getString(R.string.clear_cache_warning))
                .setIsOppositeColor(true)
                .setPositiveButton(R.string.sure_text, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateCacheSize(0);
                        ExecutorHelper.runInWorkerThread(new Runnable() {
                            @Override
                            public void run() {
                                FileUtils.deleteFile(new File(mCachePath));
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.cancel_text, null)
                .show();
    }

    @NonNull
    private void showLoginExitDialog() {
        NormalAlertDialog dialog = new NormalAlertDialog.Builder(this)
                .setMessage(getString(R.string.exit_login_tip))
                .setIsOppositeColor(true)
                .setPositiveButton(R.string.sure_text, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PushManager.unRegisterRegIdToServer();
                        UserManager.getInstance().clearUser();
                        LoginActivity.reLogin(SettingActivity.this);
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel_text, null).create();
        dialog.show();
    }

    public static class GetCacheSizeTask extends AsyncTask<String, Void, Long> {
        private WeakReference<SettingActivity> mReference;

        public GetCacheSizeTask(SettingActivity activity) {
            mReference = new WeakReference<>(activity);
        }

        @Override
        protected Long doInBackground(String... strings) {
            if (strings.length > 0) {
                return FileUtils.getDirSize(strings[0]);
            }
            return 0L;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            if (mReference != null && mReference.get() != null && !mReference.get().isFinishing()) {
                mReference.get().updateCacheSize(aLong);
            }
        }
    }
}
