package com.xgame.util;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ArrayRes;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.xgame.R;
import com.xgame.common.util.CommonRegexUtils;
import com.xgame.common.util.LogUtil;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    /**
     * stringFilter:过滤非法字符串 .<br/>
     *
     * @param input
     * @param replacement
     * @return
     * @author zhengjunfei
     */
    public static String stringFilter(String input, String replacement) {
        // 只允许字母和数字
        // String regEx = "[^a-zA-Z0-9]";
        // 清除掉所有特殊字符
        String regEx = "[`~!@#$%^&*()+=|{}':;',//[//].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(input);
        return m.replaceAll(replacement).trim();
    }

    /**
     * 格式化视频大小
     */
    public static String getFormatSize(double size) {
        if (size < 1024 * 1024) {
            return null;
        }
        DecimalFormat df = new DecimalFormat("#0.00");
        int sizeLevel = 0;
        String[] levelString = new String[]{"B", "KB", "MB", "GB", "TB"};
        do {
            size = size / 1024;
            sizeLevel++;
        } while (size / 1024L > 1);

        return df.format(size) + levelString[sizeLevel];
    }

    public static String checkUsernameValid(Context context, String name) {
        if (TextUtils.isEmpty(name))
            return getValidString(context, R.string.string_utils_input_nickname);
        if (!CommonRegexUtils.matchName(name)) {
            return getValidString(context, R.string.string_utils_input_right_nickname);
        }
        return null;
    }

    public static String checkSexValid(Context context, int sex) {
        if (sex == -1)
            return getValidString(context, R.string.string_utils_choose_sex);
        return null;
    }

    public static String checkLoginOrRegisterValid(Context context, String phone) {
        if (TextUtils.isEmpty(phone))
            // 手机号空
            return getValidString(context, R.string.string_utils_input_phone_num);
        if (!CommonRegexUtils.matchCNMobileNumber(phone)) {
            return getValidString(context, R.string.string_utils_input_right_phone_num);
        } else {
            return null;
        }
    }

    public static String checkVerificationCodeValid(Context context, String verificationCode) {
        if (TextUtils.isEmpty(verificationCode))
            // 验证码空
            return getValidString(context, R.string.string_utils_input_verification_code);
        if (verificationCode.length() != 6) {
            return getValidString(context, R.string.string_utils_input_six_verification_code);
        } else {
            return null;
        }
    }

    public static String checkLoginOrRegisterValid(Context context, String phone, String verificationCodeOrPassword, int type) {
        if (TextUtils.isEmpty(phone))
            // 手机号空
            return getValidString(context, R.string.string_utils_input_phone_num);
        if (type == 0) {
            if (TextUtils.isEmpty(verificationCodeOrPassword))
                // 验证码空
                return getValidString(context, R.string.string_utils_input_verification_code);
        } else if (type == 1) {
            if (TextUtils.isEmpty(verificationCodeOrPassword))
                // 验证码空
                return getValidString(context, R.string.string_utils_input_password);
            if (!CommonRegexUtils.verifyPassword(verificationCodeOrPassword))
                return getValidString(context, R.string.string_utils_input_right_password);
        }
        if (!CommonRegexUtils.matchCNMobileNumber(phone))
            return getValidString(context, R.string.string_utils_input_right_phone_num);
        return null;
    }

    public static String checkLoginOrRegisterValid(Context context, String phone, String verificationCode, String password) {
        if (TextUtils.isEmpty(phone))
            // 手机号空
            return getValidString(context, R.string.string_utils_input_phone_num);
        if (TextUtils.isEmpty(verificationCode))
            // 验证码空
            return getValidString(context, R.string.string_utils_input_verification_code);
        if (TextUtils.isEmpty(password))
            // 密码为空
            return getValidString(context, R.string.string_utils_input_password);

        if (!CommonRegexUtils.matchCNMobileNumber(phone))
            return getValidString(context, R.string.string_utils_input_right_phone_num);
        if (password.length() < 6)
            return getValidString(context, R.string.string_utils_input_right_phone_num);
        if (password.length() > 12)
            return getValidString(context, R.string.string_utils_input_right_phone_num);
        if (!CommonRegexUtils.verifyPassword(password))
            return getValidString(context, R.string.string_utils_input_right_phone_num);

        return null;
    }

    public static String checkBirthdayStringValid(Context context, String birthday) {
        if (TextUtils.isEmpty(birthday)) {
            // 生日为空
            return getValidString(context, R.string.string_utils_choose_birthday);
        }
        return null;
    }

    public static String checkPhotoStringValid(Context context, String photoBase64String) {
        if (TextUtils.isEmpty(photoBase64String)) {
            // 头像为空
            return getValidString(context, R.string.string_utils_choose_photo);
        }
        return null;
    }

    public static boolean showErrorMsgIfNeeded(Context context, String message) {
        if (!TextUtils.isEmpty(message)) {
            try {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                LogUtil.e("showErrorMsgIfNeeded", e.getMessage());
                return false;
            }
            return true;
        }
        return false;
    }

    public static String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    private static String getValidString(Context context, int resId) {
        return context.getString(resId);
    }

    public static boolean isEmpty(String text) {
        if (text == null || text.length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public static String getAge(Context context, String birthday) {
        String[] date = birthday.split("-");
        if (date.length == 3) {
            int age = getAge(Integer.parseInt(date[0]), Integer.parseInt(date[1]), Integer.parseInt(date[2]));
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(age);
            stringBuilder.append(context.getString(R.string.age_unit_text));
            return stringBuilder.toString();
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(18);
            stringBuilder.append(context.getString(R.string.age_unit_text));
            return stringBuilder.toString();
        }
    }

    public static String getConstellation(String birthday) {
        String[] date = birthday.split("-");
        if (date.length == 3) {
            String constellation = getConstellation(Integer.parseInt(date[1]), Integer.parseInt(date[2]));
            return constellation;
        } else {
            Calendar c = Calendar.getInstance();
            int curMonth = c.get(Calendar.MONTH) + 1;//通过Calendar算出的月数要+1
            int curDay = c.get(Calendar.DAY_OF_MONTH);
            return getConstellation(curMonth, curDay);
        }
    }

    public static String getConstellation(int month, int day) {
        int[] dayArr = new int[]{20, 19, 21, 20, 21, 22, 23, 23, 23, 24, 23, 22};
        String[] constellationArr = new String[]{"摩羯座", "水瓶座", "双鱼座", "白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座"};
        return day < dayArr[month - 1] ? constellationArr[month - 1] : constellationArr[month];
    }

    /**
     * 根据出生日期算年龄
     */
    private static int getAge(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        int curYear = c.get(Calendar.YEAR);
        int curMonth = c.get(Calendar.MONTH) + 1;//通过Calendar算出的月数要+1
        int curDay = c.get(Calendar.DAY_OF_MONTH);

        int age = curYear - year;
        if (curMonth < month) {
            age = age - 1;
        } else if (month == curMonth) {
            if (curDay < day) {
                age = age - 1;
            }
        }

        if (age == 0) {
            age = 0;
        }
        return age;
    }

    public static int[] getArraysList(Context context, @ArrayRes int arrays) {
        TypedArray array = context.getResources().obtainTypedArray(arrays);
        int[] list = new int[array.length()];
        for (int i = 0; i < array.length(); i++) {
            list[i] = array.getResourceId(i, 0);
        }
        array.recycle();
        return list;
    }

    public static String listToString(List<String> list) {
        if (list == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            stringBuilder.append(list.get(i) + ",");
        }
        return stringBuilder.toString();
    }

    public static void removeItem(List<String> list, String tag) {
        if (list == null || list.size() <= 0) {
            return;
        }
        int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            if (tag.equals(list.get(i))) {
                list.remove(i);
                return;
            }
        }
    }

    public static void copyStringToClipboard(Context context, String text) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        // 将文本内容放到系统剪贴板里。
        cm.setText(text);
    }

    public static String getPhoneFormat(String phone) {
        if (phone == null) return "";

        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }
}
