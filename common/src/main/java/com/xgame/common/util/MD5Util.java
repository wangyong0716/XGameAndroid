package com.xgame.common.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 这里MD5_16取了MD5_32的中间16位
 *
 */
public class MD5Util {

    private static String byte2Hex(byte b) {
        int value = (b & 0x7F) + (b < 0 ? 0x80 : 0);
        return (value < 0x10 ? "0" : "")
                + Integer.toHexString(value).toLowerCase();
    }

    public static String MD5_32(String passwd) {
        if (passwd == null)
            return null;
        return getBytesMD5(passwd.getBytes());
    }

    public static String getFileMD5(File file) {
        return getFileHash(file, "MD5");
    }

    public static String getFileHash(File file, String algorithm) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fileInputStream.read(buffer)) != -1) {
                md5.update(buffer, 0, length);
            }
            byte[] digest = md5.digest();
            StringBuffer strbuf = new StringBuffer();
            for (int i = 0; i < digest.length; i++) {
                strbuf.append(byte2Hex(digest[i]));
            }
            return strbuf.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fileInputStream != null)
                    fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getBytesMD5(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        md5.update(bytes, 0, bytes.length);
        byte[] digest = md5.digest();
        StringBuffer strbuf = new StringBuffer();
        for (int i = 0; i < digest.length; i++) {
            strbuf.append(byte2Hex(digest[i]));
        }
        return strbuf.toString();
    }

    public static String MD5_16(String passwd)  {
        if (passwd == null)
            return null;
        return getBytesMD5(passwd.getBytes()).subSequence(8, 24).toString();
    }

}

