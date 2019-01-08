package com.xgame.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import com.theartofdev.edmodo.cropper.CropImageView;
import com.xgame.R;
import com.xgame.common.util.ImageUtils;
import com.xgame.common.util.PixelUtil;
import com.xgame.common.util.ScreenUtil;
import com.xgame.common.util.StatusBarUtil;
import com.xgame.common.util.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by wuyanzhi on 2018/1/25.
 */

public class CropperActivity extends BaseActivity {
    @BindView(R.id.cropImageView)
    CropImageView mCropImageView;
    @BindView(R.id.cancel)
    TextView mCancel;
    @BindView(R.id.rotate_imageview)
    ImageView mRotateImageview;
    @BindView(R.id.sure)
    TextView mSure;

    public static final String EXTRA_IMAGE_URI = "uri";
    public static final String EXTRA_CROP_TYPE = "type";
    public static final int TYPE_PHOTO = 0;
    public static final int TYPE_BACKGROUND = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_cropper);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setTranslucent(this);
    }

    private void initView() {
        String path = getIntent().getStringExtra(EXTRA_IMAGE_URI);

        if (TextUtils.isEmpty(path)) {
            Toast.makeText(this, R.string.cropper_error_toast, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Uri imageUri = Uri.parse(path);
        int type = getIntent().getIntExtra(EXTRA_CROP_TYPE, -1);
        mCropImageView = (CropImageView) findViewById(R.id.cropImageView);

        mCropImageView.setImageUriAsync(imageUri);
        if (TYPE_PHOTO == type) {
            mCropImageView.setAspectRatio(1, 1);
        } else {
            mCropImageView.setAspectRatio(ScreenUtil.getScreenWidth(getApplicationContext()), PixelUtil.dip2px(getApplicationContext(), 206.7f));
        }

        mCropImageView.setFixedAspectRatio(true);
        mCropImageView.setGuidelines(CropImageView.Guidelines.ON);
        mCropImageView.setCropShape(CropImageView.CropShape.RECTANGLE);
        mCropImageView.setScaleType(CropImageView.ScaleType.FIT_CENTER);
        mCropImageView.setMinCropResultSize(500, 600);
        mCropImageView.setAutoZoomEnabled(true);
        mCropImageView.setShowProgressBar(true);

        mCancel.setOnClickListener(OnClickListener);
        mRotateImageview.setOnClickListener(OnClickListener);
        mSure.setOnClickListener(OnClickListener);

    }

    private View.OnClickListener OnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.cancel:
                    finish();
                    break;
                case R.id.sure:
                    mSure.setEnabled(false);
                    Bitmap cropperBitmap = mCropImageView.getCroppedImage();
                    if (cropperBitmap == null) {
                        return;
                    }
                    String imagePath = ImageUtils.saveImageFromBitmap(cropperBitmap, true,
                            getPhotopath(getBaseContext()));
                    Intent intent = getIntent();
                    intent.putExtra("imagePath", imagePath);
                    setResult(RESULT_OK, intent);
                    finish();
                    break;
                case R.id.rotate_imageview:
                    mCropImageView.rotateImage(90);
                    break;
                default:
                    break;
            }

        }
    };

    public static String getPhotopath(Context ctx) {
        File filePath = ctx.getExternalFilesDir("avatar");
        if (filePath == null) {
            ToastUtil.showToast(ctx, R.string.external_file_null, false);
            return null;
        }
        // 照片全路径
        return filePath.getPath() + File.separator + System.currentTimeMillis() + ".jpg";
    }
}
