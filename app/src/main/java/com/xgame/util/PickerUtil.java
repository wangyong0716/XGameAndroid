package com.xgame.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.widget.Toast;

import com.xgame.R;
import com.xgame.ui.activity.CropperActivity;

/**
 * Created by wuyanzhi on 2018/1/25.
 */

public class PickerUtil {

    public static void openCropActivity(Activity activity, Uri imageUri, int requestCode) {
        Intent intent = new Intent(activity, CropperActivity.class);
        Bundle data = new Bundle();
        String path;
        if (imageUri == null || TextUtils.isEmpty(path = imageUri.toString())){
            Toast.makeText(activity, R.string.cropper_error_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        data.putString(CropperActivity.EXTRA_IMAGE_URI, path);
        data.putInt(CropperActivity.EXTRA_CROP_TYPE, CropperActivity.TYPE_PHOTO);
        intent.putExtras(data);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void openCropActivity(Fragment fragment, Uri imageUri, int requestCode) {
        if (fragment.getActivity() == null) {
            return;
        }
        Intent intent = new Intent(fragment.getActivity(), CropperActivity.class);
        Bundle data = new Bundle();
        String path;
        if (imageUri == null || TextUtils.isEmpty(path = imageUri.toString())){
            Toast.makeText(fragment.getActivity(), R.string.cropper_error_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        data.putString(CropperActivity.EXTRA_IMAGE_URI, path);
        data.putInt(CropperActivity.EXTRA_CROP_TYPE, CropperActivity.TYPE_PHOTO);
        intent.putExtras(data);
        fragment.startActivityForResult(intent, requestCode);
    }

    public static void openAlbum(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        activity.startActivityForResult(intent, requestCode);
    }

    public static void openAlbum(Fragment fragment, int requestCode) {
        if (fragment.getActivity() == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        fragment.startActivityForResult(intent, requestCode);
    }
}
