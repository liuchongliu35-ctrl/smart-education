package com.bing.tpa.utils.word;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkHandler {

    /**
     * 向Word段落插入可点击超链接（兼容所有POI版本）
     */
    public static void insertLinkToParagraph(XWPFDocument doc, XWPFParagraph paragraph, String linkText) {
        try {
            // 1. 生成唯一关系ID
            String relationId = "rId_" + System.currentTimeMillis();

            // 3. 创建超链接对象
            CTHyperlink ctHyperlink = paragraph.getCTP().addNewHyperlink();
            ctHyperlink.setId(relationId);

            // 4. 设置超链接文本（兼容低版本POI）
            CTText ctText = CTText.Factory.newInstance();
            ctText.setStringValue(linkText);

            CTR ctr = CTR.Factory.newInstance();
            // 兼容低版本：使用addNewT()替代setT()
            ctr.addNewT().setStringValue(linkText);

            // 兼容低版本：使用setRArray()替代setR()
            ctHyperlink.setRArray(new CTR[]{ctr});

            // 5. 设置样式（蓝色下划线）
            XWPFRun run = paragraph.createRun();
            run.setText(linkText);
            run.setColor("0000FF");
            run.setUnderline(UnderlinePatterns.SINGLE);

        } catch (Exception e) {
            throw new RuntimeException("插入超链接失败: " + e.getMessage(), e);
        }
    }

    /**
     * 提取文本中的链接
     */
    public static Map<String, String> extractLinks(String text) {
        Map<String, String> links = new HashMap<>();
        if (text == null || text.isEmpty()) {
            return links;
        }
        Pattern pattern = Pattern.compile("\\[(.*?)]\\((.*?)\\)");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            links.put(matcher.group(1), matcher.group(2));
        }
        return links;
    }
}