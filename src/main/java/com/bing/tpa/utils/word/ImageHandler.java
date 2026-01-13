package com.bing.tpa.utils.word;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageHandler {
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();

    /**
     * 从富文本中提取所有图片URL（格式：![xxx](url)）
     */
    public static List<String> extractImageUrls(String richText) {
        List<String> imageUrls = new ArrayList<>();
        // 正则匹配![任意字符](url)
        Pattern pattern = Pattern.compile("!\\[.*?]\\((.*?)\\)");
        Matcher matcher = pattern.matcher(richText);
        while (matcher.find()) {
            imageUrls.add(matcher.group(1)); // 提取URL
        }
        return imageUrls;
    }

    /**
     * 下载图片为输入流（用于插入Word）
     */
    public static InputStream downloadImage(String imageUrl) throws IOException {
        Request request = new Request.Builder().url(imageUrl).build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        if (response.isSuccessful() && response.body() != null) {
            return response.body().byteStream();
        }
        throw new RuntimeException("图片下载失败：" + imageUrl);
    }

    /**
     * 向Word文档插入图片
     */
    public static void insertImageToWord(XWPFDocument doc, String imageUrl) throws IOException, InvalidFormatException {
        try (InputStream imageIs = downloadImage(imageUrl)) {
            // 插入图片（宽度300px，高度自适应，单位：emu，1px≈9525emu）
            XWPFParagraph paragraph = doc.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.addPicture(imageIs, XWPFDocument.PICTURE_TYPE_JPEG, "image.jpg",
                    Units.toEMU(300), Units.toEMU(200)); // 宽300px，高200px
        }
    }
}
