package com.xgame.invite;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.xgame.common.util.ExecutorHelper;
import com.xgame.util.StringUtil;

import java.util.Hashtable;

/**
 * Created by Albert
 * on 18-2-2.
 */

public class QrGenerator {

    private static final String CHARSET_UTF8 = "utf-8";

    public void generate(final String url, final int size, final QrListener listener) {
        ExecutorHelper.runInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    generateInternal(url, size, listener);
                } catch (WriterException e) {
                    listener.onGenerateFailed("Qr writer exception: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void generateInternal(String url, int imageSize, QrListener listener) throws WriterException {
        if (StringUtil.isEmpty(url)) {
            listener.onGenerateFailed("Url for qr code is empty");
            return;
        }
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.CHARACTER_SET, CHARSET_UTF8);
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, imageSize,
                imageSize, hints);
        int[] pixels = new int[imageSize * imageSize];
        for (int y = 0; y < imageSize; y++) {
            for (int x = 0; x < imageSize; x++) {
                if (bitMatrix.get(x, y)) {
                    pixels[y * imageSize + x] = Color.BLACK;
                } else {
                    pixels[y * imageSize + x] = Color.WHITE;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, imageSize, 0, 0, imageSize, imageSize);

        // Generated.
        listener.onGenerated(url, bitmap);
    }

    public interface QrListener {
        void onGenerated(String url, Bitmap qrBitmap);

        void onGenerateFailed(String error);
    }
}