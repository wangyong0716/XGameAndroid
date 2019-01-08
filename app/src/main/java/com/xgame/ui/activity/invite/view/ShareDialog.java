package com.xgame.ui.activity.invite.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.xgame.R;
import com.xgame.ui.activity.invite.util.ShareInvoker;
import com.xgame.util.dialog.NormalAlertDialog;

/**
 * Created by Albert
 * on 18-2-6.
 */

public class ShareDialog extends NormalAlertDialog {

    private View mShareLayout;

    public ShareDialog(@NonNull Builder builder) {
        super(builder);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.layout_share_dialog;
    }

    @Override
    protected void init() {
        super.init();
        LinearLayout customLayout = findViewById(R.id.custom_layout);
        customLayout.setPadding(0, 0, 0, getDimenPx(R.dimen.dp_30));
        mShareLayout = View.inflate(getContext(), R.layout.layout_share, customLayout);
    }

    private int getDimenPx(int res) {
        return getContext().getResources().getDimensionPixelSize(res);
    }

    public void initShareLayout(final ShareInvoker invoker) {
        if (invoker == null || mShareLayout == null) {
            return;
        }
        View.OnClickListener onShareListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invoker.onInviteThirdUser(v);
            }
        };
        mShareLayout.findViewById(R.id.share_qq).setOnClickListener(onShareListener);
        mShareLayout.findViewById(R.id.share_wechat).setOnClickListener(onShareListener);
        mShareLayout.findViewById(R.id.share_timeline).setOnClickListener(onShareListener);
    }

    public static class Builder extends NormalAlertDialog.Builder {

        public Builder(Context context) {
            super(context);
        }

        @Override
        public ShareDialog create() {
            return new ShareDialog(this);
        }
    }
}
