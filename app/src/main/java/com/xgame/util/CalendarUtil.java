package com.xgame.util;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.xgame.R;

import static java.text.DateFormat.SHORT;
import static java.text.DateFormat.getTimeInstance;

/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 *
 * Created by jackwang
 * on 18-1-30.
 */


public final class CalendarUtil {

    private CalendarUtil() {

    }

    public static Calendar getTodayStartCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public static String parseToDateString(Context cnt, final long time) {
        final long now = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        if (isSameDay(time, now)) {
            return getTimeInstance(SHORT).format(new Date(time));
        }
        cal.add(Calendar.DATE, -1);
        if (isSameDay(time, cal.getTimeInMillis())) {
            return cnt.getString(R.string.yesterday);
        } else {
            return SimpleDateFormat.getInstance().format(new Date(time));
        }

    }

    public static boolean isSameDay(Date d1, Date d2) {
        return isSameDay(d1.getTime(), d2.getTime());
    }

    public static boolean isSameDay(long d1, long d2) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(d1);
        final int dy = cal.get(Calendar.YEAR);
        final int dd = cal.get(Calendar.DAY_OF_YEAR);
        cal.setTimeInMillis(d2);
        final int ty = cal.get(Calendar.YEAR);
        final int td = cal.get(Calendar.DAY_OF_YEAR);
        return dy == ty && dd == td;
    }
}
