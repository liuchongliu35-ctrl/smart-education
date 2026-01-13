package com.bing.tpa.utils;

import java.util.HashSet;
import java.util.Set;

public class MoreSelectQuestionUtil {
    public static Integer checkMultipleChoiceAnswer(String correctAnswers, String userAnswers) {
        // 将答案转换为大写并去重
        Set<Character> correctSet = toCharacterSet(correctAnswers);
        Set<Character> userSet = toCharacterSet(userAnswers);

        // 完全匹配
        if (correctSet.equals(userSet)) {
            return 1;
        }
        // 部分正确判断（用户答案是正确答案的真子集）
        else if (isSubset(userSet, correctSet) && userSet.size() < correctSet.size()) {
            return 0;
        }
        // 错误答案
        else {
            return -1;
        }
    }

    // 将字符串转换为字符集合（自动去重）
    private static Set<Character> toCharacterSet(String str) {
        Set<Character> set = new HashSet<>();
        for (char c : str.toUpperCase().toCharArray()) {
            set.add(c);
        }
        return set;
    }

    // 检查subset是否是superset的子集
    private static boolean isSubset(Set<Character> subset, Set<Character> superset) {
        for (Character c : subset) {
            if (!superset.contains(c)) {
                return false;
            }
        }
        return true;
    }
}
