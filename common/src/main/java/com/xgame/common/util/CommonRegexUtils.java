package com.xgame.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CommonRegexUtils {

    /** 常字符，包含标点 */
    public static final String COMMON_CHAR = "^[\\w \\p{P}]+$";

    /** 空行 */
    public static final String EMPTY_LINE = "^[\\s\\n]*$";

    /** 可用作人名的字符 */
    public static final String PERSION_NAME = "^[\\w .\u00B7-]+$";

    /** 中国的电话 */
    // public static final String CN_PHONE = "^((13[0-9])|(14[5-9])|(15[^4,//D])|(18[0-9]))\\d{8}$";
    public static final String CN_PHONE = "^0?(13[0-9]|15[012356789]|17[0-9]|18[0-9]|14[57])[0-9]{8}$";

    /** Email: The Official Standard: RFC 2822 **/
    public static final String EMAIL =
            "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";

    /**中文、字母、数字和常用特殊符号*/
    private static final String CH_EN_NUM_CHAR_REGEX ="^[-+=，。？！：、…“”；（）《》～‘’〈〉—.,?':…@/;!()*&\\[\\]`~#$%^_{}|<>a-zA-Z0-9\u4e00-\u9fa5]+$";

    private static final String NUM_CHAR_REGEX ="^[a-zA-Z0-9]+$";

    /**检验昵称、用户名*/
    private static final String USER_NAME_REGEX = "^[a-zA-Z0-9\\u4e00-\\u9fa5._]+$";

    private static final String URL_LINK_REGEX = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

    private static final String IP_ADDR_REGEX = "((([1-9]?|1\\d)\\d|2([0-4]\\d|5[0-5]))\\.){3}(([1-9]?|1\\d)\\d|2([0-4]\\d|5[0-5]))";

    /** 18位身份证号码检验 */
    private static final String ID_CARD_REGEX = "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9]|X)$";

    /** 中文名包括·  包括新疆名 */
    private static final String CHINESE_NAME_REGEX = "[\\u4E00-\\u9FA5]{2,5}(?:·[\\u4E00-\\u9FA5]{2,5})*";

    /**
     * 执行正则表达匹配
     * @param regex 正则表达式
     * @param str 字符内容
     * @return 如果正则表达式匹配成功，返回true，否则返回false。
     */
    public static boolean matcherRegex(String regex, String str) {
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(str).matches();
    }

    /**
     * 匹配有效字符（中英文数字及常用标点）
     * @param str 字符内容
     * @return 如果正则表达式匹配成功，返回true，否则返回false。
     */
    public static boolean matchCommonChar(String str) {
        return matcherRegex(COMMON_CHAR, str);
    }

    /**
     * 判断手机号码
     * @param number 手机号码
     * @return 如果正则表达式匹配成功，返回true，否则返回false。
     */
    public static boolean matchCNMobileNumber(String number) {
        return matcherRegex(CN_PHONE, number);
    }

    /**
     * 匹配空行
     * @param line 字符内容
     * @return 如果正则表达式匹配成功，返回true，否则返回false。
     */
    public static boolean matchEmptyLine(String line) {
        return null == line ? true : matcherRegex(EMPTY_LINE, line);
    }

    /**
     * 清除标点符号
     * @param content 字符内容
     * @return 如果正则表达式匹配成功，返回true，否则返回false。
     */
    public static String cleanPunctuation(String content) {
        return content.replaceAll("[\\p{P}‘’“”]", content);
    }

    /**
     * 判断是否是邮箱格式
     * @param email
     * @return
     */
    public static boolean matchEmail(String email) {
        return null == email ? false : matcherRegex(EMAIL, email);
    }

    /**
     * 判断是否为有效的人物名字，可带·间隔符号
     * @param name 字符内容
     * @return 如果正则表达式匹配成功，返回true，否则返回false。
     */
    public static boolean matchPersionName(String name) {
        return null == name ? false : matcherRegex(PERSION_NAME, name);
    }

    /**
     * @MethodName: matchName
     * @Description: 检验昵称和收获地址用户名
     * @author wangjing
     */
    public static boolean matchName(String name) {
        if(name == null) {
            return false;
        }
        return name.matches(USER_NAME_REGEX) ? true : false;
    }

    /**
     *
     * verifyString:(校验字符串是否符合正则表达式). <br/>
     * @author lxh
     * @param str 要校验的字符串
     * @param regex 匹配的正则表达式
     * @return
     */
    public static boolean verifyString(String str, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    /**
     *
     * verifyString:(校验字符串是否只有中文、英文、数字和常用特殊符号). <br/>
     * @author lxh
     * @param str
     * @return
     */
    public static boolean verifyString(String str){
        return verifyString(str,CH_EN_NUM_CHAR_REGEX);
    }

    /**
     *
     * verifyPassword:(校验字符串是否只有英文、数字). <br/>
     * @author zhengjunfei
     * @param str
     * @return
     */
    public static boolean verifyPassword(String str){
        return verifyString(str,NUM_CHAR_REGEX);
    }

    /**
     *
     * @MethodName: matchUrlLink
     * @Description: 匹配url链接
     * @author wangjing
     */
    public static boolean matchUrlLink(String urlStr) {
        return matcherRegex(URL_LINK_REGEX, urlStr);
    }

    public static boolean matchIP(String ipStr) {
        return matcherRegex(IP_ADDR_REGEX, ipStr);
    }

    /**
     * 匹配18位身份证号码
     *
     * @param idCard
     * @return
     */
    public static boolean mathIdCard(String idCard) {
        return matcherRegex(ID_CARD_REGEX, idCard);
    }

    /**
     * 检验中文姓名，2到10位包括
     *
     * @param name
     * @return
     */
    public static boolean matchChineseName(String name) {
        return matcherRegex(CHINESE_NAME_REGEX, name);
    }

}
