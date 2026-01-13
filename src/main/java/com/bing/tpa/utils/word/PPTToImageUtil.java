package com.bing.tpa.utils.word;

import org.apache.poi.xslf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PPTToImageUtil {
    private static final Logger log = LoggerFactory.getLogger(PPTToImageUtil.class);

    /**
     * 将后缀为.pptx的PPT转换为图片
     * @param pptFile PPT的路径（File对象）
     * @param imgDir 图片存放的目标文件夹（绝对路径字符串）
     * @return 生成的图片文件名列表（如["1.jpeg", "2.jpeg"]）
     */
    public static List<String> doPPT2007toImage(File pptFile, String imgDir) {
        List<String> imagePaths= new ArrayList<>();
        FileInputStream is = null;
        try {
            is = new FileInputStream(pptFile);
            XMLSlideShow xmlSlideShow = new XMLSlideShow(is);
            is.close();

            // 获取幻灯片大小
            Dimension pgsize = xmlSlideShow.getPageSize();
            // 获取所有幻灯片
            List<XSLFSlide> slides = xmlSlideShow.getSlides();

            // 遍历每页幻灯片
            for (int i = 0; i < slides.size(); i++) {
                // 解决中文乱码问题：强制设置字体为宋体
                List<XSLFShape> shapes = slides.get(i).getShapes();
                for (XSLFShape shape : shapes) {
                    if (shape instanceof XSLFTextShape) {
                        XSLFTextShape textShape = (XSLFTextShape) shape;
                        for (XSLFTextParagraph paragraph : textShape.getTextParagraphs()) {
                            for (XSLFTextRun run : paragraph.getTextRuns()) {
                                run.setFontFamily("宋体"); // 确保中文正常显示
                            }
                        }
                    }
                }

                // 创建图片缓冲区（按幻灯片大小）
                BufferedImage img = new BufferedImage(
                        pgsize.width,
                        pgsize.height,
                        BufferedImage.TYPE_INT_RGB
                );
                Graphics2D graphics = img.createGraphics();
                graphics.setPaint(Color.WHITE);
                graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width, pgsize.height));

                // 绘制幻灯片内容到图片
                slides.get(i).draw(graphics);

                // 生成图片文件名：页码.jpeg（如1.jpeg、2.jpeg）
                int pageNum = i + 1; // 页码从1开始
                String imageName = pageNum + ".jpeg";
                // 图片完整路径：目标文件夹 + 文件名
                File imageFile = new File(imgDir, imageName);

                // 若图片已存在，跳过生成
                if (imageFile.exists()) {
                    imagePaths.add(imageFile.getPath());
                    continue;
                }

                // 写入图片文件
                try (FileOutputStream out = new FileOutputStream(imageFile)) {
                    ImageIO.write(img, "jpeg", out);
                    imagePaths.add(imageFile.getPath()); // 记录生成的文件路径
                }
            }

            log.info("PPT转换成图片成功！共生成 {} 张图片，路径：{}", imagePaths.size(), imgDir);
            return imagePaths;

        } catch (Exception e) {
            log.error("PPT转换成图片失败：", e); // 修正日志格式，避免字符串拼接错误
            throw new RuntimeException("PPT转图片失败", e);
        }
    }

    /**
     * 转换单页PPT为图片
     * @param slide 幻灯片页
     * @param pgsize 页面尺寸
     * @param imgDir 图片存储目录
     * @param pageNum 页码
     * @return 图片完整路径
     */
    public static String convertSlideToImage(XSLFSlide slide, Dimension pgsize, String imgDir, int pageNum) {
        // 1. 设置中文字体
        for (XSLFShape shape : slide.getShapes()) {
            if (shape instanceof XSLFTextShape) {
                XSLFTextShape textShape = (XSLFTextShape) shape;
                for (XSLFTextParagraph para : textShape.getTextParagraphs()) {
                    for (XSLFTextRun run : para.getTextRuns()) {
                        run.setFontFamily("宋体");
                    }
                }
            }
        }

        // 2. 创建图片缓冲区
        BufferedImage img = new BufferedImage(
                pgsize.width,
                pgsize.height,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D graphics = img.createGraphics();
        try {
            // 3. 渲染设置
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setPaint(Color.WHITE);
            graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width, pgsize.height));

            // 4. 绘制幻灯片
            slide.draw(graphics);
        } finally {
            graphics.dispose();
        }

        // 5. 保存图片
        String fileName = pageNum + ".jpeg";
        File output = new File(imgDir, fileName);
        try {
            ImageIO.write(img, "jpeg", output);
            System.err.println("页"+pageNum+"图片生成完成并保存");
            return output.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("图片保存失败: " + fileName, e);
        }
    }
}