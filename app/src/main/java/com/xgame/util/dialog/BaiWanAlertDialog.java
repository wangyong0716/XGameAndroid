package com.xgame.util.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.xgame.R;
import com.xgame.common.util.PixelUtil;

public class BaiWanAlertDialog extends Dialog {
    private Builder mBuilder;

    public BaiWanAlertDialog(@NonNull Builder builder) {
        super(builder.mContext, R.style.dialog_theme);
        mBuilder = builder;
        init();
    }

    private void init() {
        super.setTitle(null);
        Window win = getWindow();
        if (win != null) {
            win.requestFeature(Window.FEATURE_NO_TITLE);
            WindowManager.LayoutParams lp = win.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            win.setAttributes(lp);
        }
        setContentView(R.layout.layout_bai_wan_alert_dialog);
        TextView msgTv = findViewById(R.id.tv_msg);
        Button positiveBtn = findViewById(R.id.btn_positive);
        Button negativeBtn = findViewById(R.id.btn_negative);
        if (mBuilder == null) {
            return;
        }
        if (!TextUtils.isEmpty(mBuilder.mMessage)) {
            msgTv.setText(mBuilder.mMessage);
            msgTv.setVisibility(View.VISIBLE);
        } else {
            msgTv.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(mBuilder.mNegativeBtnTxt)) {
            negativeBtn.setVisibility(View.GONE);
            positiveBtn.getLayoutParams().width = PixelUtil.dip2px(getContext(), 166.67f);

        } else {
            positiveBtn.getLayoutParams().width = PixelUtil.dip2px(getContext(), 116.67f);
            negativeBtn.setVisibility(View.VISIBLE);
            negativeBtn.setText(mBuilder.mNegativeBtnTxt);
            negativeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    if (mBuilder.mNegativeBtnListener != null) {
                        mBuilder.mNegativeBtnListener.onClick(v);
                    }
                }
            });
        }

        if (TextUtils.isEmpty(mBuilder.mPositiveBtnTxt)) {
            positiveBtn.setVisibility(View.GONE);
        } else {
            positiveBtn.setVisibility(View.VISIBLE);
            positiveBtn.setText(mBuilder.mPositiveBtnTxt);
            positiveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    if (mBuilder.mPostiveBtnListener != null) {
                        mBuilder.mPostiveBtnListener.onClick(v);
                    }
                }
            });
        }
    }

    public static class Builder {
        private Context mContext;
        private CharSequence mMessage;
        private CharSequence mPositiveBtnTxt;
        private View.OnClickListener mPostiveBtnListener;
        private CharSequence mNegativeBtnTxt;
        private View.OnClickListener mNegativeBtnListener;

        public Builder(Context context) {
            mContext = context;
        }

        public Builder setMessage(CharSequence msg) {
            mMessage = msg;
            return this;
        }

        public Builder setPositiveButton(int resId, View.OnClickListener listener) {
            String text = mContext.getString(resId);
            return setPositiveButton(text, listener);
        }

        public Builder setPositiveButton(CharSequence text, View.OnClickListener listener) {
            mPositiveBtnTxt = text;
            mPostiveBtnListener = listener;
            return this;
        }

        public Builder setNegativeButton(int resId, View.OnClickListener listener) {
            String text = mContext.getString(resId);
            return setNegativeButton(text, listener);
        }

        public Builder setNegativeButton(CharSequence text, View.OnClickListener listener) {
            mNegativeBtnTxt = text;
            mNegativeBtnListener = listener;
            return this;
        }

        public BaiWanAlertDialog create() {
            return new BaiWanAlertDialog(this);
        }

        public BaiWanAlertDialog show() {
            BaiWanAlertDialog alertDialog = create();
            alertDialog.show();
            return alertDialog;
        }
    }
}
