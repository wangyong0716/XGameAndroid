package com.xgame.util.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xgame.R;
import com.xgame.uisupport.wheelview.CustomDatePickView;

public class BirthdayDialog extends FrameLayout implements DialogInterface {
    RelativeLayout mRootView;
    CustomDatePickView mDatePicker;
    Button mCompleteBtn;

    private Dialog mDialog;

    public BirthdayDialog(Context context) {
        super(context);
        initDialog();
    }

    public BirthdayDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDialog();
    }

    public BirthdayDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDialog();
    }

    private void initDialog(){
        mDialog = new Dialog(getContext(), R.style.BaseDialog_Padding);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_choose_birthday, null);
        mDialog.setContentView(rootView);
        mRootView =  (RelativeLayout) rootView.findViewById(com.xgame.uisupport.R.id.root_view);
        mDatePicker = (CustomDatePickView) rootView.findViewById(com.xgame.uisupport.R.id.date_picker);
        mDatePicker.setVisibleItems(3);
        mCompleteBtn = (Button) rootView.findViewById(com.xgame.uisupport.R.id.complete_btn);
        Window window = mDialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        int padding = (int) getResources().getDimension(R.dimen.dp_23_33);
        window.getDecorView().setPadding(padding, padding, padding, padding);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        initView();
    }

    private void initView(){
        mDatePicker.setScrollerListener(OnScrollerListener);
    }

    private CustomDatePickView.ScrollListener OnScrollerListener = new CustomDatePickView.ScrollListener() {
        @Override
        public void onScrollerListener() {
        }
    };

    public void setOnButtonClickListener(final DialogInterface.OnClickListener listener){
        mCompleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(mDialog, DialogInterface.BUTTON_POSITIVE);
            }
        });
    }

    public String getDate(){
        return mDatePicker.getDate();
    }

    public void setData(String date){
        mDatePicker.setData(date);
    }

    public void show() {
        mDialog.show();
    }

    @Override
    public void cancel() {
        mDialog.cancel();
    }

    @Override
    public void dismiss() {
        mDialog.dismiss();
    }

    public static class Builder {

        private BirthdayDialog mAlert;

        public Builder(Context context) {
            mAlert = new BirthdayDialog(context);
        }

        public Builder setButtonClickListener(DialogInterface.OnClickListener listener) {
            mAlert.setOnButtonClickListener(listener);
            return this;
        }

        public Builder setDate(String date){
            mAlert.setData(date);
            return this;
        }

        public void show() {
            mAlert.show();
        }

        public BirthdayDialog create() {
            return mAlert;
        }

    }

}
