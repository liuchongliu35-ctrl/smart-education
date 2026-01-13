package com.bing.tpa.utils;

import com.bing.tpa.domain.VO.PointLink;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KnowledgeSortUtil {
    // 正则表达式匹配中文数字（支持一到九十九）
    private static final Pattern CHAPTER_PATTERN = Pattern.compile("第(\\S+?)讲");
    private static final String[] CN_NUMBERS = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};

    public static void sortByChapterTitle(List<PointLink> pointLinks) {
        if (pointLinks == null || pointLinks.isEmpty()) return;

        Collections.sort(pointLinks, new Comparator<PointLink>() {
            @Override
            public int compare(PointLink o1, PointLink o2) {
                return extractChapterNumber(o1.getTitle()) - extractChapterNumber(o2.getTitle());
            }
        });
    }

    /**
     * 从标题中提取章节数字
     * @param title 标题字符串（如 "第七讲人工智能前沿技术"）
     * @return 章节数字（如 7），无法解析时返回 Integer.MAX_VALUE（排在最后）
     */
    private static int extractChapterNumber(String title) {
        if (title == null) return Integer.MAX_VALUE;

        Matcher matcher = CHAPTER_PATTERN.matcher(title);
        if (matcher.find()) {
            String cnNum = matcher.group(1);
            return convertChineseNumber(cnNum);
        }
        return Integer.MAX_VALUE; // 无法解析的标题排到最后
    }

    /**
     * 将中文数字转换为整数
     * @param cnNum 中文数字字符串（如 "七"）
     * @return 对应的整数值
     */
    private static int convertChineseNumber(String cnNum) {
        // 处理十以内的简单数字
        for (int i = 1; i < CN_NUMBERS.length; i++) {
            if (CN_NUMBERS[i].equals(cnNum)) {
                return i;
            }
        }

        // 处理复合数字（如 "十一"）
        if (cnNum.startsWith("十")) {
            if (cnNum.length() == 1) return 10; // "十"
            String rest = cnNum.substring(1);   // "十一" -> "一"
            for (int i = 1; i < CN_NUMBERS.length; i++) {
                if (CN_NUMBERS[i].equals(rest)) {
                    return 10 + i;
                }
            }
        }

        return Integer.MAX_VALUE; // 无法解析的数字
    }
}
