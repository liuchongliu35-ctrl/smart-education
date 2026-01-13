package com.bing.tpa.utils;

public class TypeCheck {
    // 判断字符串是否只包含中文字符
    public static boolean isAllChinese(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return str.matches("[\\u4e00-\\u9fa5]+");
    }

    // 判断字符串是否只包含数字
    public static boolean isAllDigits(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return str.matches("\\d+");
    }
}
