package com.duan.blogos.util;

import javafx.scene.control.Cell;

import java.util.stream.Stream;

/**
 * Created on 2017/12/20.
 *
 * @author DuanJiaNing
 */
public class StringUtils {


    /**
     * int字符串转为int数组，并去重
     */
    public static int[] intStringDistinctToArray(String sour, String regex) {

        return sour == null ? null :
                Stream.of(sour.split(regex))
                        .mapToInt(Integer::valueOf)
                        .distinct()
                        .toArray();

    }

    /**
     * 对象数组拼接为字符串
     */
    public static String arrayToString(Object[] arr, String join) {
        if (arr == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        for (Object obj : arr) {
            builder.append(obj).append(join);
        }

        String r = builder.toString();
        int len = join.length();
        int sum = r.length();
        return r.substring(0, sum - len);
    }

    /**
     * 字符串切分为字符串数组
     */
    public static String[] stringToStringArray(String sour, String regex) {
        if (sour == null) {
            return null;
        }

        return sour.split(regex);
    }

    /**
     * int数组拼接为字符串
     */
    public static String intArrayToString(int[] arr, String join) {
        Integer[] is = new Integer[arr.length];
        for (int i = 0; i < arr.length; i++) {
            is[i] = arr[i];
        }

        return arrayToString(is, join);
    }

    /**
     * 检查数值字符串是否被指定表达式隔开
     *
     * @param str   字符串
     * @param regex 正则表达式
     * @return 正确返回true
     */
    public static boolean isIntStringSplitByChar(String str, String regex) {
        if (str == null || regex == null) return false;

        try {
            String[] strings = str.split(regex);
            for (String s : strings) {
                int i = Integer.valueOf(s);
            }
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 检查字符串是否为 null
     *
     * @param str 字符串
     * @return 是返回true
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0 || "null".equals(str);
    }

    /**
     * 检查字符串是否为url
     *
     * @param url 字符串
     * @return 是返回true
     */
    public static boolean isURL(String url) {
        return !isEmpty(url) && url.matches("(^http|https)://.*");
    }

    /**
     * 检查字符串是否为电话号码
     * http://www.jb51.net/article/96201.htm
     *
     * @return 是为true
     */
    public static boolean isPhone(String phone) {
        String cellPhone = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(18[0-9]))\\d{8}$";
        String telephone = "^(0\\d{2}-\\d{8}(-\\d{1,4})?)|(0\\d{3}-\\d{7,8}(-\\d{1,4})?)$";
        return !isEmpty(phone) && (phone.matches(cellPhone) || phone.matches(telephone));
    }

    /**
     * 检查字符串是否为邮箱
     *
     * @return 是否true
     */
    public static boolean isEmail(String email) {
        return !isEmpty(email) && email.matches("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
    }
}
