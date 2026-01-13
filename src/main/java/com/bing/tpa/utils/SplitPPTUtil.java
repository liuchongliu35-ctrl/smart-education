package com.bing.tpa.utils;

import com.groupdocs.cloud.merger.api.DocumentApi;
import com.groupdocs.cloud.merger.api.FileApi;
import com.groupdocs.cloud.merger.client.ApiException;
import com.groupdocs.cloud.merger.client.Configuration;
import com.groupdocs.cloud.merger.model.*;
import com.groupdocs.cloud.merger.model.requests.DeleteFileRequest;
import com.groupdocs.cloud.merger.model.requests.DownloadFileRequest;
import com.groupdocs.cloud.merger.model.requests.JoinRequest;
import com.groupdocs.cloud.merger.model.requests.UploadFileRequest;
import com.spire.presentation.FileFormat;
import com.spire.presentation.Presentation;
import com.spire.presentation.packages.sprssp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Component
public class SplitPPTUtil {

    private static final Presentation BLANK_TEMPLATE;
    // 静态变量存储Configuration，确保全局唯一
    private static final Configuration GROUPDOCS_CONFIG;
    private static final String STORAGE_NAME = "tpa"; // 您的存储名称
    private static final String BASE_FOLDER = "ppt-merge-temp"; // 基础文件夹
    Logger logger = LoggerFactory.getLogger(SplitPPTUtil.class);
    static {
        // 1. 初始化GroupDocs的Configuration（只执行一次）
        String clientId = "030ec066-946e-4d9a-8dbd-a34203f476e4";
        String clientSecret = "97a7bf160f1d01ce6ecef0d5996ff3fa";
//        预备的账号：id：030ec066-946e-4d9a-8dbd-a34203f476e4
//        密码：97a7bf160f1d01ce6ecef0d5996ff3fa
        GROUPDOCS_CONFIG = new Configuration(clientId, clientSecret);
        // 预加载空白模板（只执行一次）
        BLANK_TEMPLATE = new Presentation();
        if (BLANK_TEMPLATE.getSlides().getCount() > 0) {
            BLANK_TEMPLATE.getSlides().removeAt(0);
        }
    }

    // 获取全局唯一的Configuration实例
    public static Configuration getGroupDocsConfig() {
        return GROUPDOCS_CONFIG;
    }

    /**
     * 将PPT文件按页拆分为单独的文件
     * @param sourcePath 原始PPT文件路径（改了名字存放在临时的目录下）
     * @param rootDir 根目录路径
     * @param userName 用户名（用于创建子目录）
     * @throws Exception 可能抛出文件操作异常
     */
    @Async("pptSplit")
    public CompletableFuture<Integer> splitPptByPages(String sourcePath, String rootDir, String userName) throws Exception {
        // 1. 创建用户专属目录
        Path userDir = Paths.get(rootDir, userName);
        if (!Files.exists(userDir)) {
            Files.createDirectories(userDir);
        }
        // 2. 获取原始文件名（不含扩展名）
        File sourceFile = new File(sourcePath);
        String fileName = sourceFile.getName();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));

        // 3. 加载原始PPT
        Presentation originalPpt = new Presentation();
        originalPpt.loadFromFile(sourcePath);
        int slideCount = originalPpt.getSlides().getCount();

        System.out.printf("开始拆分PPT: %s, 共%d页%n", fileName, slideCount);
        // 4. 遍历每一页并保存
        for (int i = 0; i < slideCount; i++) {
            // 创建新PPT并移除默认空白页
            Presentation newPpt = new Presentation();
            if (newPpt.getSlides().getCount() > 0) {
                newPpt.getSlides().removeAt(0);
            }

            // 添加当前页
            newPpt.getSlides().append(originalPpt.getSlides().get(i));

            // 构建输出路径
            String outputName = String.format("%s-%d.pptx", baseName, i + 1);//ppt名字
            Path outputPath = userDir.resolve(outputName);

            // 保存文件
            newPpt.saveToFile(outputPath.toString(), FileFormat.PPTX_2019);
            System.out.println("已保存: " + outputPath);

            // 释放资源
            newPpt.dispose();
        }

        // 5. 释放原始PPT资源
        originalPpt.dispose();
        System.out.println("PPT拆分完成!");
        return CompletableFuture.completedFuture(slideCount);
    }

//    todo 拆分ppt(10页一组)
    @Async("pptSplit")
    public CompletableFuture<List<Path>> splitPPT(String sourcePath, String outputDir, String baseName) throws Exception {
        List<Path> splitFiles = new ArrayList<>();

        try {
            Presentation originalPpt = new Presentation();
            originalPpt.loadFromFile(sourcePath);
            int slideCount = originalPpt.getSlides().getCount();
            int groupCount = (int) Math.ceil(slideCount / 3.0);

            for (int group = 0; group < groupCount; group++) {
                int startIndex = group * 3;
                int endIndex = Math.min(startIndex + 2, slideCount - 1);

                try  {
                    Presentation newPpt = new Presentation();
                    // 移除默认空白页
                    if (newPpt.getSlides().getCount() > 0) {
                        newPpt.getSlides().removeAt(0);
                    }

                    // 添加当前组的幻灯片
                    for (int i = startIndex; i <= endIndex; i++) {
                        newPpt.getSlides().append(originalPpt.getSlides().get(i));
                    }

                    // 保存子PPT文件
                    String fileName = String.format("%s-part%d.pptx", baseName, group + 1);
                    Path outputPath = Paths.get(outputDir, fileName);
                    newPpt.saveToFile(outputPath.toString(), FileFormat.PPTX_2019);
                    splitFiles.add(outputPath);
                    logger.info("<UNK>子ppt拆分成功，保存路径：<UNK>: {}", outputPath);
                }catch (Exception e){
                    logger.error("ppt拆分失败 {}",e.getMessage());
                }
            }
        }catch (Exception e){
            logger.error("ppt拆分失败(外):{}",e.getMessage());
        }
        logger.info("拆分PPT完成，共{}组文件", splitFiles.size());
        return CompletableFuture.completedFuture(splitFiles);
    }

//    todo 合并ppt
// 合并多个PPT文件
@Async("pptSplit")
public Future<Void> mergePPTs(List<Path> inputPaths, String outputPath) throws Exception {
    Configuration configuration = getGroupDocsConfig();
    DocumentApi apiInstance = new DocumentApi(configuration);
    FileApi fileApi = new FileApi(configuration);
    try {
        // 1. 上传所有输入文件到云端
        List<JoinItem> joinItems = inputPaths.stream().map(path -> {
            String cloudPath = BASE_FOLDER + "/input/" + path.getFileName().toString();
            uploadFile(fileApi, path.toString(), cloudPath);
            return createJoinItem(cloudPath);
        }).collect(Collectors.toList());

        // 2. 设置合并选项
        String outputCloudPath = BASE_FOLDER + "/output/" + Path.of(outputPath).getFileName().toString();
        JoinOptions options = new JoinOptions();
        options.setJoinItems(joinItems);
        options.setOutputPath(outputCloudPath);

        // 3. 执行合并
        JoinRequest request = new JoinRequest(options);
        DocumentResult response = apiInstance.join(request);
        System.out.println("合并成功! 输出路径: " + response.getPath());

        // 4. 下载合并后的文件
        downloadFile(fileApi, outputCloudPath, outputPath);

        // 5. 清理云端临时文件
        cleanCloudFiles(fileApi, joinItems, outputCloudPath);
        logger.info("合并完成！保存位置: {} ", outputPath);
    } catch (ApiException e) {
        System.err.println("GroupDocs API 异常: ");
        throw new RuntimeException("PPT合并失败: " + e.getMessage(), e);
    }
        return CompletableFuture.completedFuture(null);
    }

    private JoinItem createJoinItem(String filePath) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFilePath(filePath);
        fileInfo.setStorageName(STORAGE_NAME);

        JoinItem item = new JoinItem();
        item.setFileInfo(fileInfo);
        return item;
    }

    private void uploadFile(FileApi fileApi, String localPath, String cloudPath) {
        try {
            UploadFileRequest request = new UploadFileRequest(cloudPath, new File(localPath), STORAGE_NAME);
            FilesUploadResult response = fileApi.uploadFile(request);
            System.out.println("文件上传成功: " + cloudPath + ", 大小: " + response.getUploaded().size() + " bytes");
        } catch (ApiException e) {
            throw new RuntimeException("文件上传失败: " + cloudPath, e);
        }
    }

    private void downloadFile(FileApi fileApi, String cloudPath, String localPath) {
        try {
            System.out.println("开始下载");
            DownloadFileRequest request = new DownloadFileRequest(cloudPath, STORAGE_NAME, null);
            File response = fileApi.downloadFile(request);

            // 保存到本地
            Files.copy(response.toPath(), Path.of(localPath), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("文件下载成功: " + localPath + ", 大小: " + response.length() + " bytes");
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败: " + cloudPath, e);
        }
    }

    private void cleanCloudFiles(FileApi fileApi, List<JoinItem> joinItems, String outputPath) {
        try {
            // 删除输入文件
            for (JoinItem item : joinItems) {
                String filePath = item.getFileInfo().getFilePath();
                deleteFile(fileApi, filePath);
            }

            // 删除输出文件
            deleteFile(fileApi, outputPath);

            System.out.println("云端临时文件清理完成");
        } catch (Exception e) {
            System.err.println("云端文件清理失败: " + e.getMessage());
        }
    }

    private void deleteFile(FileApi fileApi, String filePath) {
        try {
            DeleteFileRequest request = new DeleteFileRequest(filePath, STORAGE_NAME, null);
            fileApi.deleteFile(request);
            System.out.println("云端文件已删除: " + filePath);
        } catch (ApiException e) {
            System.err.println("删除云端文件失败: " + filePath);
        }
    }
}
//    try  {
//        Presentation mergedPpt = new Presentation();
//        // 移除默认空白页
//        if (mergedPpt.getSlides().getCount() > 0) {
//            mergedPpt.getSlides().removeAt(0);
//        }
//
//        // 按顺序合并所有PPT文件
//        for (Path inputPath : inputPaths) {
//            try {
//                Presentation subPpt = new Presentation();
//                subPpt.loadFromFile(inputPath.toString());
//
//                // 添加所有幻灯片
//                for (int i = 0; i < subPpt.getSlides().getCount(); i++) {
//                    mergedPpt.getSlides().append(subPpt.getSlides().get(i));
//                }
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }
//
//        // 保存合并后的PPT
//        mergedPpt.saveToFile(outputPath, FileFormat.PPTX_2019);
//        logger.info("PPT合并完成，保存至：{}", outputPath);
//    }catch (Exception e){
//        logger.error("ppt合并失败：{}",e.getMessage());
//    }
