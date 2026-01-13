package com.bing.tpa.utils.word;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class RichTextParser {
    /**
     * 解析Markdown格式为Word可识别的文本块（区分标题、段落、列表）
     * 输入：原始富文本（含###、-等符号）
     * 输出：结构化列表（每个元素包含类型和内容，如标题、段落、列表项）
     */
    public static List<ContentBlock> parse(String richText) {
        List<ContentBlock> blocks = new ArrayList<>();
        if (StringUtils.isEmpty(richText)) {
            return blocks;
        }

        // 按换行分割行
        String[] lines = richText.split("\n");
        boolean inList = false; // 是否在列表中

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("### ")) { // 三级标题（示例，可扩展#、##）
                blocks.add(new ContentBlock(ContentType.TITLE, line.replace("### ", "")));
                inList = false;
            } else if (line.startsWith("- ")) { // 无序列表
                blocks.add(new ContentBlock(ContentType.LIST_ITEM, line.replace("- ", "")));
                inList = true;
            } else if (line.startsWith("#### ")) { // 四级标题（按需扩展）
                blocks.add(new ContentBlock(ContentType.TITLE, line.replace("#### ", "")));
                inList = false;
            } else { // 普通段落
                blocks.add(new ContentBlock(ContentType.PARAGRAPH, line));
                inList = false;
            }
        }
        return blocks;
    }

    // 内容类型枚举（标题、段落、列表项）
    public enum ContentType { TITLE, PARAGRAPH, LIST_ITEM }

    // 结构化内容块（类型+文本）

    @AllArgsConstructor
    @Data
    public static class ContentBlock {
        private ContentType type;
        private String content;
    }
}
