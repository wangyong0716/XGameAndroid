package com.xgame.util;

import com.xgame.common.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;


public class StreamHelper {

    static final String TAG = "StreamHelper";

    public static JSONObject toJSONObject(InputStream is) throws JSONException, IOException {
        return new JSONObject(toString(is));
    }

    public static String toString(InputStream is) throws IOException {
        final ByteArrayOutputStream baos = toByteArrayOutputStream(is);
        return (baos != null) ? baos.toString("UTF-8") : null;
    }

    public static byte[] toByteArray(InputStream is) throws IOException {
        final ByteArrayOutputStream baos = toByteArrayOutputStream(is);
        return (baos != null) ? baos.toByteArray() : null;
    }

    public static final class DirectlyOutputStream extends ByteArrayOutputStream {

        @Override
        public byte[] toByteArray() {
            return buf.length == count ? buf : super.toByteArray();
        }
    }

    private static ByteArrayOutputStream toByteArrayOutputStream(InputStream is)
            throws IOException {
        final ByteArrayOutputStream baos = new DirectlyOutputStream();
        final byte[] buffer = new byte[512];
        int length = 0;
        while ((length = is.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }

        return baos;
    }

    public static void wirteStringToStream(OutputStream out, String src) throws IOException {
        final OutputStreamWriter writer = new OutputStreamWriter(out);
        writer.write(src);
        writer.flush();
    }

    public static boolean closeSafe(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
                return true;
            } catch (IOException e) {
                LogUtil.i(TAG, "close error, obj=" + closeable, e);
            }
        }

        return false;
    }
}