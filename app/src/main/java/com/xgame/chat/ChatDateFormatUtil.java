package com.xgame.chat;

import android.content.Context;

import com.xgame.R;
import com.xgame.common.application.ApplicationStatus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ChatDateFormatUtil {

    private static Context sContext = ApplicationStatus.getApplicationContext();

    public static String getFormatTextForTime(long time) {
        Calendar today = Calendar.getInstance();
        Calendar showingTime = Calendar.getInstance();
        showingTime.setTimeInMillis(time);

        int formatStrRes;
        if (today.get(Calendar.YEAR) != showingTime.get(Calendar.YEAR)) {
            formatStrRes = R.string.chat_date_format_other_year;
        } else {
            int days_count_of_today = today.get(Calendar.DAY_OF_YEAR);
            int days_count_of_showing = showingTime.get(Calendar.DAY_OF_YEAR);
            if (days_count_of_showing == days_count_of_today) {
                formatStrRes = R.string.chat_date_format_today;
            } else if (days_count_of_showing == days_count_of_today - 1) {
                formatStrRes = R.string.chat_date_format_yesterday;
            } else if (days_count_of_showing == days_count_of_today - 2) {
                formatStrRes = R.string.chat_date_format_the_day_before_yesterday;
            } else {
                formatStrRes = R.string.chat_date_format_this_year;
            }
        }
        Date date = new Date(time);
        return new SimpleDateFormat(sContext.getString(formatStrRes)).format(date);
    }
}
