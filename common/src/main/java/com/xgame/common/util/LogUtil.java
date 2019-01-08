package com.xgame.common.util;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.xgame.common.BuildConfig;

/**
 * Created by wuyanzhi on 2018/1/23.
 */

public class LogUtil {


    public static boolean DEBUG = BuildConfig.DEBUG;

    static {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
                File file = new File("/sdcard/xgame/config");
                BufferedReader reader = null;
                FileReader fileReader = null;
                if (file.exists() && file.isFile()) {
                    try {
                        fileReader = new FileReader(file);
                        reader = new BufferedReader(fileReader);
                        String line = reader.readLine();
                        while (!TextUtils.isEmpty(line)) {
                            if (line.trim().equals("debug=true")) {
                                DEBUG = true;
                            }
                            line = reader.readLine();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (fileReader != null) {
                            try {
                                fileReader.close();
                            } catch (IOException e) {
                            }
                        }
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e) {
                            }
                        }
                    }
                }

//            }
//        }).start();
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .tag("Xgame")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy) {
            @Override
            public boolean isLoggable(int priority, String tag) {
                return DEBUG || priority >= Logger.ERROR;
            }
        });
    }

    public static void v(String tag, String msg) {
        if (DEBUG) {
            Logger.t(tag).v(msg);
        }
    }

    public static void v(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            Logger.t(tag).v(msg + '\n' + getStackTraceString(tr));
        }
    }

    public static void d(String tag, String msg) {
        Log.d("wuyanzhi", "d " + DEBUG);
        if (DEBUG) {
            Logger.t(tag).d(msg);
        }
    }

    public static void d(String tag, String format, Object... args) {
        d(tag, String.format(format, args));
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            Logger.t(tag).d(msg + '\n' + getStackTraceString(tr));
        }
    }

    public static void i(String tag, String format, Object... args) {
        i(tag, String.format(format, args));
    }

    public static void i(String tag, String msg) {
        if (DEBUG) {
            Logger.t(tag).i(msg);
        }
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            Logger.t(tag).i(msg + '\n' + getStackTraceString(tr));
        }
    }

    public static void w(String tag, String format, Object... args) {
        w(tag, String.format(format, args));
    }

    public static void w(String tag, String msg) {
        if (DEBUG) {
            Logger.t(tag).w(msg);
        }
    }

    public static void w(String tag, Throwable tr) {
        if (DEBUG) {
            Logger.t(tag).w(getStackTraceString(tr));
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            Logger.t(tag).w(msg + '\n' + getStackTraceString(tr));
        }
    }

    public static void e(String tag, String format, Object... args) {
        e(tag, String.format(format, args));
    }

    public static void e(String tag, String msg) {
        if (DEBUG) {
            Logger.t(tag).e(msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (DEBUG) {
            Logger.t(tag).e(msg + '\n' + getStackTraceString(tr));
        }
    }

    public static void json(String tag, String json) {
        if (DEBUG) {
            Logger.t(tag).json(json);
        }
    }

    public static void xml(String tag, String xml) {
        if (DEBUG) {
            Logger.t(tag).xml(xml);
        }
    }

    public static void f(String tag, String msg) {
        Logger.t(tag).e(msg);
    }

    public static void f(String tag, String msg, Throwable tr) {
        Logger.t(tag).e(tr, msg);
    }

    static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }

        // This is to reduce the amount of log spew that apps do in the non-error
        // condition of the network being unavailable.
        Throwable t = tr;
        while (t != null) {
            if (t instanceof UnknownHostException) {
                return "";
            }
            t = t.getCause();
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }
}
