package com.bing.tpa.service.VideoGenerate;

import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Service
public class VideoGenerationService {

    // EXE 文件路径 - 根据实际位置配置
    private static final String CREATE_VIDEO_EXE = "F:\\tpa_system\\CreateVideo.exe";

    // 临时目录 - 用于存储中间文件
    private static final String TEMP_DIR = "src/main/resources/videoFile/";

    public Path convertPptToVideo(Path pptPath, String durationsJson, String outputVideoName)
            throws IOException, InterruptedException {

        // 1. 确保临时目录存在
        Path tempDir = Paths.get(TEMP_DIR);
        Path jsonDir = Paths.get("src/main/resources/Data");
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
        }

        // 2. 创建临时JSON文件
        Path jsonFilePath = jsonDir.resolve("Time.json");
        Files.write(jsonFilePath, durationsJson.getBytes());

        // 3. 准备输出视频路径
        Path outputVideoPath = tempDir.resolve(outputVideoName + ".mp4");

        // 4. 构建命令参数
        String command = String.format("\"%s\" \"%s\" \"%s\" \"%s\"",
                CREATE_VIDEO_EXE,
                pptPath.toString(),
                jsonFilePath.toString(),
                outputVideoPath.toString());

        System.out.println("执行命令: " + command);

        // 5. 执行外部程序
        ProcessBuilder processBuilder = new ProcessBuilder(
                "cmd.exe", "/c", command
        );

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        // 6. 读取输出日志
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[EXE] " + line);
            }
        }

        // 7. 等待程序完成（30分钟超时）
        boolean finished = process.waitFor(30, TimeUnit.MINUTES);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("视频生成超时");
        }

        // 8. 检查退出码
        int exitCode = process.exitValue();
        if (exitCode != 0) {
            throw new RuntimeException("视频生成失败，退出码: " + exitCode);
        }

        // 9. 验证视频文件
        if (!Files.exists(outputVideoPath)) {
            throw new RuntimeException("生成的视频文件不存在: " + outputVideoPath);
        }

        return outputVideoPath;
    }
}
