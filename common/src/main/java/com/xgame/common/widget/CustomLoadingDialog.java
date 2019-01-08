package com.xgame.common.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xgame.common.R;
import com.xgame.common.util.LogUtil;

/**
 * Created by wuyanzhi on 2018/1/25.
 */

public class CustomLoadingDialog extends Dialog {

    private static final String TAG = CustomLoadingDialog.class.getSimpleName();

    public CustomLoadingDialog(Context context) {
        super(context);
    }

    public CustomLoadingDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public static class Builder{

        private TextView tvLoadingMsg;
        private Context mContext;
        private String mLoadingText;
        private boolean mIsShowing;
        private CustomLoadingDialog customLoadingDialog;

        public Builder(Context context){
            this.mContext = context;
        }

        public Builder(Context context,String loadingText){
            this.mContext = context;
            this.mLoadingText = loadingText;
        }

        public Builder(Context context,int loadingText){
            this.mContext = context;
            this.mLoadingText = String.valueOf(mContext.getText(loadingText));
        }

        public CustomLoadingDialog create(){
            customLoadingDialog = new CustomLoadingDialog(mContext, R.style.CustomDialog);
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.base_view_custom_loading_dialog,null);
            customLoadingDialog.addContentView(layout,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//            customLoadingDialog.setCanceledOnTouchOutside(false);//点击屏幕不消失
            customLoadingDialog.setCancelable(false);//点击任何地方都不消失
            customLoadingDialog.setCanceledOnTouchOutside(false);
            tvLoadingMsg = layout.findViewById(R.id.loading_text);
            tvLoadingMsg.setText(mLoadingText);
            return customLoadingDialog;
        }

        public void updateLoadingMsg(String msg) {
            if (tvLoadingMsg != null) {
                tvLoadingMsg.setText(msg);
            }
        }

        public void dismissLoadingDialog(){
            if(customLoadingDialog != null && customLoadingDialog.isShowing()){
                try {
                    customLoadingDialog.dismiss();
                } catch (Exception e) {
                    LogUtil.e(TAG, e.getMessage());
                }
            }
        }
    }
}
