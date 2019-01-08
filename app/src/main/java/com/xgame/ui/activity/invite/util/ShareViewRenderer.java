package com.xgame.ui.activity.invite.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.xgame.R;
import com.xgame.account.UserManager;
import com.xgame.account.model.User;
import com.xgame.app.GlideApp;
import com.xgame.base.ClientSettingManager;
import com.xgame.base.ClientSettingManager.OnLoadResult;
import com.xgame.base.model.ClientSettings;
import com.xgame.common.util.ExecutorHelper;
import com.xgame.common.util.ImageUtils;
import com.xgame.common.util.LogUtil;
import com.xgame.common.util.TaggedTextParser;
import com.xgame.common.util.ToastUtil;
import com.xgame.invite.QrGenerator;
import com.xgame.social.ShareUtil;
import com.xgame.social.share.ShareListener;
import com.xgame.social.share.SharePlatform;
import com.xgame.statistic.ShareStatHelper;
import com.xgame.util.StringUtil;

import java.io.File;

/**
 * Created by Albert
 * on 18-2-3.
 */

public class ShareViewRenderer {

    private static final String TAG = "ShareViewRenderer";

    private static final int MAX_PROGRESS = 2;
    private static final String SHARE_FILE_NAME = "raw_image.jpg";

    private View mShareLayout;
    private ImageView mAvatar;
    private ImageView mQrCode;
    private TextView mShareText;
    private TextView mShareNumber;

    private int mPrepareProgress;

    private String mImagePath;
    private ClientSettings mSettings;

    private QrGenerator mQrGenerator = new QrGenerator();
    private ShareStatHelper mStatHelper = new ShareStatHelper();

    private ShareListener mShareListener;

    public void initView(View shareLayout) {
        mShareLayout = shareLayout;
        mAvatar = mShareLayout.findViewById(R.id.share_avatar);
        mQrCode = mShareLayout.findViewById(R.id.qr_code);
        mShareText = mShareLayout.findViewById(R.id.share_text);
        mShareNumber = mShareLayout.findViewById(R.id.share_number);
        initStat();
        renderView();
    }

    private void initStat() {
        Context context = getContext();
        if (context != null && context instanceof Activity) {
            Intent intent = ((Activity) context).getIntent();
            mStatHelper.setup(intent);
        }
    }

    private Context getContext() {
        return mShareLayout.getContext();
    }

    private static String getShareImagePath(Context ctx) {
        File filePath = ctx.getExternalFilesDir("share");
        if (filePath == null) {
            ToastUtil.showToast(ctx, R.string.external_file_null, false);
            return null;
        }
        return filePath.getPath() + File.separator + SHARE_FILE_NAME;
    }

    public void prepareToShare(@SharePlatform.Platform final int platform) {
        if (isPrepared()) {
            if (mSettings == null || !mSettings.isShareImage()) {
                shareLink(platform);
            } else {
                shareImage(platform);
            }
        } else {
            ToastUtil.showToast(getContext(), R.string.share_preparing);
        }
    }

    private String getShareLink(String baseUrl) {
        User user = UserManager.getInstance().getUser();
        if (user == null) {
            return baseUrl;
        } else {
            return baseUrl
                    + "/#icon=" + user.getHeadimgurl()
                    + "&name=" + user.getReadableNickName()
                    + "&id=" + user.getUserid();
        }
    }

    private void shareLink(@SharePlatform.Platform final int platform) {
        ExecutorHelper.runInBackground(new Runnable() {
            @Override
            public void run() {
                final Context context = getContext();
                final Bitmap thumb = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
                ExecutorHelper.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mSettings == null) {
                            ShareUtil.shareMedia(context, platform,
                                    context.getString(R.string.share_title),
                                    context.getString(R.string.share_summary),
                                    getShareLink(context.getString(R.string.share_link)), thumb, getShareListener(platform));
                        } else {
                            ShareUtil.shareMedia(context, platform,
                                    mSettings.getShareTitle(context),
                                    mSettings.getShareSummary(context),
                                    getShareLink(mSettings.getShareLink(context)), thumb, getShareListener(platform));
                        }
                    }
                });
            }
        });
    }

    private void shareImage(@SharePlatform.Platform final int platform) {
        ExecutorHelper.runInBackground(new Runnable() {
            @Override
            public void run() {
                File imageFile = null;
                if (StringUtil.isEmpty(mImagePath)) {
                    mImagePath = getShareImagePath(getContext());
                }
                try {
                    imageFile = new File(mImagePath);
                } catch (Exception e) {
                    LogUtil.d(TAG, "Share image empty: " + mImagePath);
                }
                if (imageFile == null || !imageFile.exists()) {
                    Bitmap shareImage = drawViewToBitmap(mShareLayout);
                    mImagePath = ImageUtils.saveImageFromBitmap(shareImage, true, mImagePath);
                }
                LogUtil.d(TAG, "Share image created: " + mImagePath);
                ExecutorHelper.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        ShareUtil.shareImage(getContext(), platform, mImagePath, getShareListener(platform));
                    }
                });
            }
        });
    }

    private ShareListener getShareListener(@SharePlatform.Platform final int platform) {
        if (mShareListener == null) {
            mShareListener = new ShareListener() {
                @Override
                public void shareSuccess() {
                    LogUtil.d(TAG, "Share success!");
                    mStatHelper.statTaskShare(platform);
                }

                @Override
                public void shareFailure(Exception e) {
                    LogUtil.d(TAG, "Share failed: " + e.getMessage());
                    ToastUtil.showToast(getContext(), e.getMessage());
                }

                @Override
                public void shareCancel() {
                    LogUtil.d(getClass().getSimpleName(), "Share canceled.");
                }
            };
        }
        return mShareListener;
    }

    private Bitmap drawViewToBitmap(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private void progressIncrease() {
        mPrepareProgress += 1;
        mPrepareProgress = Math.max(mPrepareProgress, MAX_PROGRESS);
    }

    private boolean isPrepared() {
        return mPrepareProgress >= MAX_PROGRESS;
    }

    private void renderView() {
        mPrepareProgress = 0;
        User user = UserManager.getInstance().getUser();
        // Avatar
        GlideApp.with(mShareLayout).load(user.getHeadimgurl()).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                progressIncrease();
                return false;
            }
        }).into(mAvatar);
        // Text
        String nameText = "<span color='#FF7E00'>" + user.getNickname() + "</span>";
        TaggedTextParser.setTaggedText(mShareText,
                mShareText.getResources().getString(R.string.share_text_format, nameText));
        // Number
        mShareNumber.setText(String.valueOf(user.getUserid()));
        // QrCode.
        ClientSettingManager.loadSettings(new OnLoadResult() {
            @Override
            public void onLoaded(ClientSettings settings) {
                mSettings = settings;
                generateQrCode(settings.share);
            }

            @Override
            public void onFailure() {
                ToastUtil.showTip(getContext(), R.string.please_relogin);
            }
        });
    }

    private void generateQrCode(String link) {
        mQrGenerator.generate(link, mQrCode.getResources().getDimensionPixelSize(R.dimen.qr_image_size), new QrGenerator.QrListener() {
            @Override
            public void onGenerated(String url, Bitmap qrBitmap) {
                mQrCode.setImageBitmap(qrBitmap);
                progressIncrease();
            }

            @Override
            public void onGenerateFailed(String error) {
                LogUtil.d(TAG, "Qr code generate failed: " + error);
            }
        });
    }
}
