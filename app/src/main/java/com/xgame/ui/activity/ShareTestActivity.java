package com.xgame.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.xgame.R;
import com.xgame.social.ShareUtil;
import com.xgame.social.share.ShareListener;
import com.xgame.social.share.SharePlatform;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ShareTestActivity extends BaseActivity {

    @BindView(R.id.title)
    EditText title;
    @BindView(R.id.link)
    EditText link;
    @BindView(R.id.imageUrl)
    EditText imageUrl;
    @BindView(R.id.summary)
    EditText summary;
    @BindView(R.id.type)
    EditText type;
    @BindView(R.id.shareQQ)
    Button shareQQ;
    @BindView(R.id.shareWx)
    Button shareWx;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_test_layout);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.shareQQ, R.id.shareWx})
    public void onViewClicked(View view) {
        String t = title.getText().toString();
        String u = link.getText().toString();
        String image = imageUrl.getText().toString();
        String s = summary.getText().toString();
        String ty = type.getText().toString();
        switch (view.getId()) {
            case R.id.shareQQ:
                share(Integer.valueOf(ty), SharePlatform.QQ, t, t, u, image, s);
                break;
            case R.id.shareWx:
                share(Integer.valueOf(ty), SharePlatform.WX, t, t, u, image, s);

                break;
        }
    }

    private void share(int type, int platform, String title, String text, String url, String imageUrl, String summary) {
        ShareListener shareListener = new ShareListener() {
            @Override
            public void shareSuccess() {

            }

            @Override
            public void shareFailure(Exception e) {

            }

            @Override
            public void shareCancel() {

            }
        };

        switch (type) {
            case 1:
                ShareUtil.shareImage(this, platform, imageUrl, shareListener);
            break;
            case 2:
                ShareUtil.shareMedia(this, platform, title, summary, url, imageUrl, shareListener);
            break;
            case 3:
                ShareUtil.shareText(this, platform, text, shareListener);
                break;

            default:
                break;
        }

    }
}
