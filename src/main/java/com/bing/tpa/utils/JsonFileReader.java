package com.bing.tpa.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonFileReader {
    public static String readJsonFile(String filePath) throws IOException {
        // 读取整个文件内容为字符串
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }
}
