package com.bing.tpa.utils;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class VideoReader {
    public static byte[] readMp4File(String filePath) throws IOException {
        Path path= Paths.get(filePath);
        return Files.readAllBytes(path);
    }
}
