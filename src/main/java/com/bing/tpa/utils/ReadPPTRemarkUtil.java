package com.bing.tpa.utils;


import com.aspose.slides.ISlideCollection;
import com.bing.tpa.domain.digital.CommentInfo;
import com.bing.tpa.service.baseImpl.TpaTeachDesignServiceImpl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spire.presentation.*;
import com.spire.presentation.collections.CommentAuthorCollection;
import com.spire.presentation.collections.SlideCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.awt.geom.Point2D;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Component
public class ReadPPTRemarkUtil {
    static Logger logger = LoggerFactory.getLogger(TpaTeachDesignServiceImpl.class);
//    TODO 读取批注
public Map<Integer, List<CommentInfo>> readAllComments(String filePath) throws Exception {
    Map<Integer, List<CommentInfo>> commentsMap = new HashMap<>();
    Presentation ppt = new Presentation();
    ppt.loadFromFile(filePath);

    for (int slideIndex = 0; slideIndex < ppt.getSlides().size(); slideIndex++) {
        ISlide slide = ppt.getSlides().get(slideIndex);
        Comment[] comments = slide.getComments();

        if (comments != null && comments.length > 0) {
            List<CommentInfo> slideComments = new ArrayList<>();
            for (Comment comment : comments) {
                slideComments.add(new CommentInfo(
                        comment.getAuthorName(),
                        comment.getText(),
                        null, // 位置信息无法通过API获取
                        comment.getDateTime()
                ));
            }
            commentsMap.put(slideIndex, slideComments);
        }
    }
    ppt.dispose();
    return commentsMap;
}

//    //    TODO 添加批注
//    public void addComments(String inputPath, String outputPath,
//                            Map<Integer, CommentInfo> commentsMap) throws Exception {
//        try {
//            Presentation ppt = new Presentation();
//            ppt.loadFromFile(inputPath);
//
//            // 创建固定作者 "授课讲词"
//            CommentAuthorCollection authors = ppt.getCommentAuthors();
//            ICommentAuthor fixedAuthor = authors.addAuthor("小智老师", "授课讲词：");
//
//            for (Map.Entry<Integer, CommentInfo> entry : commentsMap.entrySet()) {
//                int slideIndex=entry.getKey();//todo 获取页码
//                CommentInfo info = entry.getValue();// todo 获取批注对象
//                if (slideIndex < 0 ||
//                        slideIndex >= ppt.getSlides().size()||
//                        info.getText()==null||
//                        info.getText().isEmpty()) continue;
//
//                ISlide slide = ppt.getSlides().get(slideIndex);
//                Point2D.Float position = info.getPosition();
//                if (position == null) {
//                    position = new Point2D.Float(30, 30); // 默认位置
//                }
//                if (info.getAuthor()!=null) {
//                    fixedAuthor=authors.addAuthor(info.getAuthor(),"授课");
//                }
//                slide.addComment(fixedAuthor, info.getText(), position, info.getTime());
//            }
////        for (Map.Entry<Integer, List<CommentInfo>> entry : commentsMap.entrySet()) {
////            int slideIndex = entry.getKey();
////            if (slideIndex < 0 || slideIndex >= ppt.getSlides().size()) continue;
////
////            ISlide slide = ppt.getSlides().get(slideIndex);
////            for (CommentInfo comment : entry.getValue()) {
////                // 使用固定作者
////                // 添加批注（使用默认位置如果未指定）
////                Point2D.Float position = comment.getPosition();
////                if (position == null) {
////                    position = new Point2D.Float(30, 30); // 默认位置
////                }
////                if (comment.getAuthor()!=null) {
////                    fixedAuthor=authors.addAuthor(comment.getAuthor(),"授课");
////                }
////                slide.addComment(fixedAuthor, comment.getText(), position, comment.getTime());
////            }
////        }
//
//            ppt.saveToFile(outputPath, FileFormat.PPTX_2019);
//            logger.info("批注添加成功并完成保存，保存路径：{}",outputPath);
//            ppt.dispose();
//        } catch (Exception e) {
//            logger.error("批注添加失败：{}",e.getMessage());
//            throw new RuntimeException(e);
//        }
//    }
@Async("addRemark")
public Future<Void> addComments(String inputPath, String outputPath,
                                Map<Integer, CommentInfo> commentsMap) throws Exception {
    try {
        Presentation ppt = new Presentation();
        ppt.loadFromFile(inputPath);

        // 创建固定作者 "授课讲词"
        CommentAuthorCollection authors = ppt.getCommentAuthors();
        // 确保初始作者的缩写唯一
        ICommentAuthor fixedAuthor = authors.addAuthor("小智老师", "XC"); // 使用唯一缩写

        // 用于记录已使用的作者缩写，确保唯一性
        Set<String> usedInitials = new HashSet<>();
        usedInitials.add("XC"); // 添加初始作者的缩写

        // 获取map中最大的有效页码（最后一个有效页）
        int maxValidSlideIndex = -1;
        if (!commentsMap.isEmpty()) {
            maxValidSlideIndex = commentsMap.keySet().stream()
                    .mapToInt(Integer::intValue)
                    .max()
                    .orElse(-1);
        }

        for (Map.Entry<Integer, CommentInfo> entry : commentsMap.entrySet()) {
            int slideIndex = entry.getKey();
            CommentInfo info = entry.getValue();

            // 【关键修改】加强文本校验：过滤null、空字符串、纯空白字符
            String commentText = info.getText();
            if (slideIndex < 0 || slideIndex >= ppt.getSlides().size() ||
                    commentText == null || commentText.trim().isEmpty()) {
                logger.debug("跳过无效批注：页码={}，文本为空或空白", slideIndex);
                continue;
            }

            ISlide slide = ppt.getSlides().get(slideIndex);
            Point2D.Float position = info.getPosition();
            if (position == null) {
                position = new Point2D.Float(30, 30); // 默认位置
            }

            // 处理作者信息，确保缩写唯一
            if (info.getAuthor() != null && !info.getAuthor().trim().isEmpty()) {
                // 生成基于作者名的基础缩写
                String baseInitials = generateInitials(info.getAuthor());

                // 确保缩写唯一
                String uniqueInitials = baseInitials;
                int counter = 1;
                while (usedInitials.contains(uniqueInitials)) {
                    uniqueInitials = baseInitials + counter++;
                }

                // 添加新作者并记录使用过的缩写
                fixedAuthor = authors.addAuthor(info.getAuthor(), uniqueInitials);
                usedInitials.add(uniqueInitials);
            }

            // 添加批注
            slide.addComment(fixedAuthor, info.getText(), position, info.getTime());
            logger.info("第 {} 页批注添加成功！",entry.getKey()+1);
        }

        // 保存修改后的PPT
        logger.info("保存位置：{}",outputPath);
        // 获取最后一页索引并删除

//        ppt.getSlides().removeAt(1);
        ppt.saveToFile(outputPath, FileFormat.PPTX_2019);
        ppt.dispose();

//        //最后几页没用的删掉，这几页是spire这个自动加上去的
//        // 重新加载PPT，准备删除多余页面
//        Presentation finalPpt = new Presentation();
//        finalPpt.loadFromFile(outputPath);
//        SlideCollection slides = finalPpt.getSlides();
//        int totalSlides = slides.getCount();
//
//        // 从末尾遍历删除无效页（索引 > 最大有效页码的页面）
//        if (maxValidSlideIndex >= 0 && totalSlides > 0) {
//            // 从最后一页开始向前删除，避免索引偏移
//            for (int i = totalSlides - 1; i > maxValidSlideIndex; i--) {
//                slides.removeAt(i);
//                logger.info("已删除无效页（索引：{}）", i);
//            }
//            logger.info("删除后剩余有效页数：{}", slides.getCount());
//        } else if (maxValidSlideIndex == -1) {
//            logger.warn("未找到有效批注页，不删除任何页面");
//        }
//
//        // 保存最终处理后的PPT（覆盖原文件）
//        finalPpt.saveToFile(outputPath, FileFormat.PPTX_2019);
//        finalPpt.dispose();
//        logger.info("最终保存位置：{}", outputPath);
    } catch (Exception e) {
        logger.error("添加批注失败", e);
        throw e;
    }
    return CompletableFuture.completedFuture(null);
}

    /**
     * 根据作者名生成基础缩写
     */
    private String generateInitials(String authorName) {
        // 简单实现：取每个单词的首字母，最多取3个字母
        String[] parts = authorName.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                initials.append(Character.toUpperCase(part.charAt(0)));
                if (initials.length() >= 3) {
                    break;
                }
            }
        }

        // 如果没有生成任何缩写，使用默认值
        return initials.length() > 0 ? initials.toString() : "A";
    }

    // todo 添加批注到指定PPT页码
    public void addCommentToPage(String pptPath, String outputPath,int pageNumber,
                                 String commentText, Point2D.Float position) throws Exception {
        //加载测试文档
        try {
            System.err.println("即将插入第："+pageNumber+"页的批注："+commentText);
            Thread.sleep(10000);
            Presentation ppt  = new Presentation();
            ppt.loadFromFile(pptPath);

            //获取指定页
            ISlide slide = ppt.getSlides().get(pageNumber);

            //添加批注作者
            ICommentAuthor author = ppt.getCommentAuthors().addAuthor("Tom","批注");
            //添加批注内容
            slide.addComment(author,"这是一段批注",new Point2D.Float(25,8), Date.from(Instant.now()));
            //保存文档
            ppt.saveToFile(outputPath, FileFormat.PPTX_2010);
            ppt.dispose();
        } catch (Exception e) {
            logger.error("插入失败，失败原因：{}",e.getMessage());
            throw new RuntimeException(e);
        }
    }

//// todo 添加批注到指定PPT页码
//    public void addCommentToPage(String pptPath, int pageNumber,
//                                 String commentText, Point2D.Float position) throws Exception {
//        Presentation ppt = new Presentation();
//        ppt.loadFromFile(pptPath);
//
//        // 验证页码有效性（注意：PPT页码从1开始，slide索引从0开始）
//        int slideIndex = pageNumber - 1;
//        if (slideIndex < 0 || slideIndex >= ppt.getSlides().size()) {
//            throw new IllegalArgumentException("无效的PPT页码：" + pageNumber +
//                    "，总页数为：" + ppt.getSlides().size());
//        }
//
//        // 创建固定作者 "虚拟教师"
//        CommentAuthorCollection authors = ppt.getCommentAuthors();
//        ICommentAuthor fixedAuthor = authors.addAuthor("虚拟教师小智", "授课讲词：");
//
//        // 获取指定页码的幻灯片
//        ISlide slide = ppt.getSlides().get(slideIndex);
//
//        // 设置默认位置（如果未指定）
//        Point2D.Float commentPosition = position;
//        if (commentPosition == null) {
//            commentPosition = new Point2D.Float(30, 30); // 默认左上角位置
//        }
//
//        // 添加批注到指定幻灯片
//        slide.addComment(fixedAuthor, commentText, commentPosition, new Date());
//
//        // 保存修改后的PPT，还是保存到原来的路径（覆盖原文件）
//        ppt.saveToFile(pptPath, FileFormat.PPTX_2019);
//        ppt.dispose();
//    }


//    TODO 将批注转为json串
public static String convertToSortedJson(Map<Integer, List<CommentInfo>> commentMap) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();//使用这个就可以实现有序的json串！！
//    TODO 调用了setPrettyPrinting()这个就会触发Gson 内部使用LinkedTreeMap来存储 JSON 结构，实现按照map顺序来生成json串

    // 1. 提取所有键并排序
    List<Integer> keys = new ArrayList<>(commentMap.keySet());
    Collections.sort(keys); // 升序排序

    // 2. 使用LinkedHashMap按顺序存储键值对
    Map<String, String> sortedMap = new LinkedHashMap<>();
    for (Integer key : keys) {
        List<CommentInfo> comments = commentMap.get(key);
        StringBuilder textBuilder = new StringBuilder();
        for (CommentInfo info : comments) {
            textBuilder.append(info.getText()).append(" ");
        }
        sortedMap.put(key.toString(), textBuilder.toString().trim());
    }

    // 3. 转换为JSON
    return gson.toJson(sortedMap);
}

//
}
