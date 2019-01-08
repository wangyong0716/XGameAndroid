package com.xgame.common.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtils {

    public static String imgToBase64(String imgPath, Bitmap bitmap, String imgFormat) {
        if (bitmap == null && imgPath != null && imgPath.length() > 0) {
            bitmap = readBitmap(imgPath);
        }
        if (bitmap == null) {
            //bitmap not found!!
            return null;
        }
        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            out.flush();
            out.close();

            byte[] imgBytes = out.toByteArray();
            return Base64.encodeToString(imgBytes, Base64.DEFAULT);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            return null;
        } finally {
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static Bitmap readBitmap(String imgPath) {
        try {
            return BitmapFactory.decodeFile(imgPath);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            return null;
        }
    }

    public static String saveImageFromBitmap(Bitmap bitmap, boolean isLimit, String filePath) {
        Bitmap bitmapNew = null;
        if (isLimit) {
            bitmapNew = getZoomImage(bitmap, 1000);
        }

        String fileName = filePath;
        try {
            FileOutputStream b = new FileOutputStream(fileName);
            bitmapNew.compress(Bitmap.CompressFormat.JPEG, 100, b);
            bitmap.recycle();
            bitmapNew.recycle();
            b.flush();
            b.close();
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        String result = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    /**
     * 获取100*100尺寸的缩略图图片
     */
    public static Bitmap getImageThumbnail(Context context, Uri uri) {
        try {
            return getImageThumbnail(context, uri, 100, 100, 0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据当前屏幕设为缩放宽度,然后以初始宽度和缩放后宽度之比作为缩放比
     */
    public static Bitmap getImageThumbnailScreen(Context context, Uri uri) {
        try {
            return getImageThumbnail(context, uri, ScreenUtil.getScreenWidth(context), ScreenUtil.getScreenHeight(context), 1);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取图片缩略图
     */
    private static Bitmap getImageThumbnail(Context context, Uri uri, int width, int height, int type) throws FileNotFoundException {
        ContentResolver contentResolver = context.getContentResolver();
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高，注意此处的bitmap为null
        bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri), null, options);
        // 计算缩放比
        int h = options.outHeight;
        int w = options.outWidth;
        int beWidth = w / width;
        int beHeight = h / height;
        int scale = 1;
        if (type == 0) {
            if (beWidth < beHeight) {
                scale = beWidth;
            } else {
                scale = beHeight;
            }
            if (scale <= 0) {
                scale = 1;
            }
        } else {
            int screenWidth = ScreenUtil.getScreenWidth(context);
            if (w <= screenWidth) {
                scale = 1;
            } else {
                scale = w / screenWidth;
            }
            width = screenWidth;
            height = h * screenWidth / w;
        }
        options.inSampleSize = scale;
        options.inJustDecodeBounds = false; // 设为 false
        // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
        bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri), null, options);
        // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    public static Bitmap centerSquareScaleBitmap(Bitmap bitmap, int edgeLength) {
        if (null == bitmap || edgeLength <= 0) {
            return null;
        }

        Bitmap result = bitmap;
        int widthOrg = bitmap.getWidth();
        int heightOrg = bitmap.getHeight();

        if (widthOrg > edgeLength && heightOrg > edgeLength) {
            //压缩到一个最小长度是edgeLength的bitmap
            int longerEdge = (int) (edgeLength * Math.max(widthOrg, heightOrg) / Math.min(widthOrg, heightOrg));
            int scaledWidth = widthOrg > heightOrg ? longerEdge : edgeLength;
            int scaledHeight = widthOrg > heightOrg ? edgeLength : longerEdge;
            Bitmap scaledBitmap;

            try {
                scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
            } catch (Exception e) {
                return null;
            }

            //从图中截取正中间的正方形部分。
            int xTopLeft = (scaledWidth - edgeLength) / 2;
            int yTopLeft = (scaledHeight - edgeLength) / 2;

            try {
                result = Bitmap.createBitmap(scaledBitmap, xTopLeft, yTopLeft, edgeLength, edgeLength);
                scaledBitmap.recycle();
            } catch (Exception e) {
                return null;
            }
        }

        return result;
    }

    /**
     * z
     * 将Bitmap压缩成1000*1000大小
     *
     * @author zhengjunfei
     * @time 16-11-18 上午10:42
     */
    public static Bitmap getZoomImage(Bitmap orgBitmap, int limitSize) {
        if (null == orgBitmap) {
            return null;
        }
        if (orgBitmap.getWidth() <= limitSize) {
            limitSize = orgBitmap.getWidth();
        }
        if (orgBitmap.isRecycled()) {
            return null;
        }

        // 获取图片的宽和高
        float width = orgBitmap.getWidth();
        float height = orgBitmap.getHeight();
        // 创建操作图片的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) limitSize) / width;
//        float scaleHeight = ((float) limitSize) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleWidth);
        orgBitmap = Bitmap.createBitmap(orgBitmap, 0, 0, (int) width, (int) height, matrix, true);
//        orgBitmap.recycle();
        return orgBitmap;
    }

    /**
     * 发送广播更新系统相册
     */
    public static void updateAlbum(Context context, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(uri);
        context.sendBroadcast(intent);
    }

}
