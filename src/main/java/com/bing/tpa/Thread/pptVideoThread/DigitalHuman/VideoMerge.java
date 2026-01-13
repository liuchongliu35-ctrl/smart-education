package com.bing.tpa.Thread.pptVideoThread.DigitalHuman;

import com.bing.tpa.common.ResourceType;
import com.bing.tpa.exception.DigitalException;
import com.bing.tpa.service.baseImpl.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

//TODO 该类用于将视频片段进行整合
@Component
public class VideoMerge {

    @Autowired
    private ResourceService resource;
    /**
     *
     * @param splitVideoRootPath 碎视频的根路径
     * @param pptName 临时ppt的名字，因为拆分的视频的名字就是以临时ppt的名字作为根名字的
     * @param videoNum 视频的数量
     * @param outName 合成视频的名字，以原ppt的名字命名，如：17--55--认识人工智能
     */
    public void merge(String splitVideoRootPath,String pptName,Integer videoNum,String outName,String userName) throws IOException, DigitalException {
        Path path = Paths.get(splitVideoRootPath);
        // 判断目录是否存在且为目录
        if (Files.exists(path) && Files.isDirectory(path)) {
//            该目录存在就获取该目录下得物视频片段
//            todo 1、创建融合视频的保存目录
//            String mergeVideoRootPath = "src/main/resources/mergeVideo";
//            Path videoPathWithoutName = Paths.get(mergeVideoRootPath,userName);
            Path videoPathWithoutName = resource.getResourcePath(ResourceType.VIDEO, userName);
            if(!Files.exists(videoPathWithoutName)){
                Files.createDirectory(videoPathWithoutName);
            }
//            todo 2、构建保存的视频的完整路径
            Path videoPath=videoPathWithoutName.resolve(outName);
            List<String> fromVideoFileList=new ArrayList<>();


//            todo 遍历视频碎片将所有碎片视频存到集合中
            for (int i = 1; i <= videoNum; i++) {
//                获取地i个碎片视频的完整地址
                Path smallVideoPath=Paths.get(splitVideoRootPath,userName);
                Path trueVideoPath = smallVideoPath.resolve(pptName + "-" + i + ".mp4");
                fromVideoFileList.add(trueVideoPath.toString());
            }

//            调用视频拼接的方法
            boolean success = convetor(fromVideoFileList, "C:\\Users\\LC335\\ffmpeg\\ffmpeg-full\\bin\\ffmpeg.exe", videoPath.toString());
//            boolean success = convetor(fromVideoFileList, "/usr/bin/ffmpeg", videoPath.toString());
            if (!success) {
                throw new DigitalException("视频合并失败");
            }

        } else {
            System.out.println("目录不存在或不是目录：" + splitVideoRootPath);
            throw new DigitalException("视频目录不存在，保存视频失败");
        }

    }

    /**
     ** 参数：
     *	**List<String> fromVideoFileList 需要合并的多视频url地址以List存放**
     *	**String ffmpeg 此处是ffmpeg 配置地址，可写死如“E:/ffmpeg/bin/ffmpeg.exe”**
     *	**String NewfilePath 合并后的视频存放地址，如：src/main/resources/mergeVideo/liuc/17--55--认识人工智能.mp4
     */
    public static boolean convetor(List<String> fromVideoFileList, String ffmpegPath,
                                   String outputFilePath) {
        try {
            // 0. 预处理：确保所有路径都是绝对路径且使用正确分隔符
            List<Path> absolutePaths = new ArrayList<>();
            for (String filePath : fromVideoFileList) {
                // 解析为绝对路径并标准化
                Path absPath = Paths.get(filePath).toAbsolutePath().normalize();
                // 确保文件存在
                if (!Files.exists(absPath)) {
                    System.err.println("[ERROR] 视频文件不存在: " + absPath);
                    return false;
                }
                absolutePaths.add(absPath);
            }

            // 1. 创建临时文件列表（使用绝对路径）
            Path listFile = Files.createTempFile("video-list", ".txt");
            List<String> fileLines = absolutePaths.stream()
                    .map(Path::toString)
                    // 转义Windows路径分隔符和特殊字符
                    .map(path -> "file '" + path.replace("\\", "\\\\").replace("'", "'\\''") + "'")
                    .collect(Collectors.toList());

            Files.write(listFile, fileLines);

            // 调试：打印临时文件内容
            System.out.println("临时列表文件内容:\n" + Files.readString(listFile));

            // 2. 构建FFmpeg命令（使用标准化路径）
            List<String> command = new ArrayList<>();
            command.add(ffmpegPath.replace("/", "\\")); // 确保FFmpeg路径使用Windows格式
            command.add("-y");
            command.add("-f");
            command.add("concat");
            command.add("-safe");
            command.add("0");
            command.add("-i");
            command.add(listFile.toString().replace("/", "\\")); // 标准化路径分隔符
            command.add("-c");
            command.add("copy");
            command.add("-movflags");
            command.add("+faststart");
            command.add(outputFilePath.replace("/", "\\")); // 输出路径标准化

            // 3. 打印完整命令（调试用）
            System.out.println("执行FFmpeg命令: " + String.join(" ", command));

            // 4. 执行命令（其余部分保持不变）
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[FFmpeg] " + line);
                }
            }

            int exitCode = process.waitFor();
            Files.deleteIfExists(listFile);

            if (exitCode != 0) {
                System.err.println("[ERROR] FFmpeg退出码: " + exitCode);
            }

            return exitCode == 0;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

}
