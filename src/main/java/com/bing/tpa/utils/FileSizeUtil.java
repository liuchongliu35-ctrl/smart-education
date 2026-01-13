package com.bing.tpa.utils;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class FileSizeUtil {
    public static  String getSize(Path filePath){
        try {
            // 直接获取文件大小（更高效）
            long sizeInBytes = Files.size(filePath);
            System.out.println("文件大小（字节）: " + sizeInBytes);

            // 转换为人类可读的格式
            String humanReadableSize = formatSize(sizeInBytes);
            System.out.println("文件大小: " + humanReadableSize);
            return humanReadableSize;
        } catch (IOException e) {
            System.err.println("读取文件大小失败: " + e.getMessage());
        }
        return null;
    }

    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
