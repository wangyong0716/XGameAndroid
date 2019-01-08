package com.xgame.common.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

/**
 * Created by wuyanzhi on 2018/1/25.
 */

public class FileUtils {
    private static final String ANDROID_N_EXTERNAL_PATH = "/external_files";
    private static final String ANDROID_N_ROOT_PATH = "/root_files";

    // copy a file from srcFile to destFile, return true if succeed, return
    // false if fail
    public static boolean copyFile(File srcFile, File destFile) {
        boolean result = false;
        try {
            InputStream in = new FileInputStream(srcFile);
            try {
                result = copyToFile(in, destFile);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            result = false;
        }
        return result;
    }

    /**
     * Copy data from a source stream to destFile. Return true if succeed,
     * return false if failed.
     */
    public static boolean copyToFile(InputStream inputStream, File destFile) {
        try {
            if (destFile.exists()) {
                destFile.delete();
            }
            FileOutputStream out = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                out.flush();
                try {
                    out.getFD().sync();
                } catch (IOException e) {
                }
                out.close();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static File getSharedPrefsFile(Context context, String fileName) {
        return new File("/data/data/" + context.getPackageName() + "/shared_prefs", fileName + ".xml");
    }

    public static String getFileContent(File file) {
        if (file != null && file.exists() && file.isFile()) {
            InputStreamReader reader = null;
            StringWriter writer = new StringWriter();
            try {
                reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
                char[] buffer = new char[1024];
                int n = 0;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
                return writer.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return "";
    }

    public static boolean deleteFile(File file) {
        if (file == null || !file.exists() || !file.canWrite()) {
            return false;
        }

        if (file.isFile()) {
            return file.delete();
        }

        boolean success = true;
        if (file.isDirectory()) {
            File[] list = file.listFiles();
            if (list == null) {
                return true;
            }
            for (File childFile : list) {
                success &= deleteFile(childFile);
            }
        }
        return success;
    }

    public static String getFileExt(String fileName) {
        if (fileName != null) {
            String name = fileName;
            int pos = fileName.lastIndexOf('/');
            if (pos != -1) {
                name = fileName.substring(pos + 1);
            }
            int dotPosition = name.lastIndexOf('.');
            if (dotPosition > 0) {
                return name.substring(dotPosition + 1);
            }
        }
        return "";
    }

    /**
     * 获取文件名字，绝对路径
     *
     * @param uri
     * @return
     */
    public static String getFilePathFromContentUri(Uri uri) {
        if (uri == null) {
            return null;
        }
        String filePath = uri.getPath();
        if (filePath == null) {
            return null;
        }
        if (filePath.startsWith(ANDROID_N_EXTERNAL_PATH)) {
            filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + filePath.substring(ANDROID_N_EXTERNAL_PATH.length());
        } else if (filePath.startsWith(ANDROID_N_ROOT_PATH)) {
            filePath = filePath.substring(ANDROID_N_ROOT_PATH.length());
        } else {
            if (!new File(filePath).exists()) {
                int root = filePath.indexOf("/", filePath.startsWith("/") ? 1 : 0);
                if (root >= 0) {
                    filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                            + filePath.substring(root);
                }
            }
        }
        return filePath;
    }

    public static long getDirSize(final String dirPath) {
        return getDirSize(new File(dirPath));
    }

    public static long getDirSize(final File dir) {
        return getDirLength(dir);
    }

    private static long getDirLength(final File dir) {
        if (dir == null || !dir.exists()) {
            return 0;
        }
        if (!dir.isDirectory()) {
            return dir.length();
        }
        long len = 0;
        File[] files = dir.listFiles();
        if (files != null && files.length != 0) {
            for (File file : files) {
                len += getDirLength(file);
            }
        }
        return len;
    }

    @SuppressLint("DefaultLocale")
    public static String byte2FitMemorySize(final long byteNum) {
        if (byteNum < 0) {
            return "shouldn't be less than zero!";
        } else if (byteNum == 0) {
            return "0M";
        } else if (byteNum < 1024) {
            return String.format("%.1fB", (double) byteNum);
        } else if (byteNum < 1048576) {
            return String.format("%.1fK", (double) byteNum / 1024);
        } else if (byteNum < 1073741824) {
            return String.format("%.1fM", (double) byteNum / 1048576);
        } else {
            return String.format("%.1fG", (double) byteNum / 1073741824);
        }
    }
}
