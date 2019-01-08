package com.xgame.uisupport.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xgame.uisupport.R;
import com.xgame.uisupport.wheelview.CustomDatePickView;

public class ChooseBirthdayDialog extends FrameLayout implements DialogInterface {
    RelativeLayout mRootView;
    CustomDatePickView mDatePicker;
    TextView mCompleteBtn;

    private Dialog mDialog;

    public ChooseBirthdayDialog(Context context) {
        super(context);
        initDialog();
    }

    public ChooseBirthdayDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDialog();
    }

    public ChooseBirthdayDialog(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDialog();
    }

    private void initDialog(){
        mDialog = new Dialog(getContext(), R.style.BaseBottomDialog_Padding);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.view_dialog_choose_birthday, null);
        mDialog.setContentView(rootView);
        mRootView =  (RelativeLayout) rootView.findViewById(R.id.root_view);
        mDatePicker = (CustomDatePickView) rootView.findViewById(R.id.date_picker);
        mCompleteBtn = (TextView) rootView.findViewById(R.id.complete_btn);
        Window window = mDialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        int padding = (int) getResources().getDimension(R.dimen.dp_14_6);
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

        private ChooseBirthdayDialog mAlert;

        public Builder(Context context) {
            mAlert = new ChooseBirthdayDialog(context);
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

        public ChooseBirthdayDialog create() {
            return mAlert;
        }

    }

}
