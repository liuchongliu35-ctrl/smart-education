package com.bing.tpa.service.baseImpl;


import com.bing.tpa.common.ResourceType;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


@Service
public class ResourceService {

    private final ResourceLoader resourceLoader;
    private Path externalStoragePath;  // 外部存储路径
    private final boolean isJarRuntime;
    public ResourceService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        this.isJarRuntime = isRunningFromJar();
        initExternalStorage();
    }
    // 更可靠的JAR环境检测
    private boolean isRunningFromJar() {
        try {
            String className = getClass().getName().replace('.', '/');
            String classPath = getClass().getResource("/" + className + ".class").toString();
            return classPath.startsWith("jar:");
        } catch (Exception e) {
            return false;
        }
    }

    private void initExternalStorage() {
        // 从配置读取外部目录，默认使用用户目录下的app-data
        String customPath = System.getProperty("app.external.path",
                System.getProperty("user.home") + "/app-data");

        this.externalStoragePath = Paths.get(customPath);
        try {
            Files.createDirectories(externalStoragePath);
            System.out.println("外部存储目录: " + externalStoragePath.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("无法创建外部存储目录", e);
        }
    }

    /**
     * 智能获取资源路径（按资源类型）
     * @param type 资源类型
     * @param relativePath 相对路径（如："teacher1/file1.pptx"）
     * @return 资源路径
     * @throws IOException
     */
    public Path getResourcePath(ResourceType type, String relativePath) throws IOException {
        // 构建完整路径：类型目录/相对路径
        String fullPath = type.getDirectory() + "/" + relativePath; //sound/xxxxx.pcm
        return getResourcePath(fullPath);
    }

    public Path getResourcePath(String fullPath) throws IOException {
//        // 1. 检查外部存储
//        Path externalPath = externalStoragePath.resolve(fullPath);
//        if (Files.exists(externalPath)) {
//            return externalPath;
//        }
//
//        // 2. 检查类路径资源
//        Resource resource = resourceLoader.getResource("classpath:" + fullPath);
//        if (resource.exists()) {
//            // 开发环境：直接返回文件系统路径
//            if (resource.isFile() && resource.getFile().exists()) {
//                return resource.getFile().toPath();
//            }
//
//            // JAR环境：同步资源到外部存储
//            return ensureExternalResource(fullPath);
//        }
//
//        // 3. 资源不存在，返回外部存储路径（即使文件不存在）
//        return externalPath;
        // 1. 优先检查类路径资源（无论是否JAR环境）
        Resource resource = resourceLoader.getResource("classpath:" + fullPath);
        if (resource.exists()) {
            // 开发环境：直接返回文件系统路径
            if (!isJarRuntime && resource.isFile()) {
                try {
                    return resource.getFile().toPath();
                } catch (IOException e) {
                    // 回退到外部存储方案
                    System.err.println("无法直接访问类路径文件，将使用外部存储: " + e.getMessage());
                }
            }
            // JAR环境或回退情况：同步资源到外部存储
            return ensureExternalResource(fullPath);
        }

        // 2. 检查外部存储（仅当类路径不存在时）
        Path externalPath = externalStoragePath.resolve(fullPath);
        if (Files.exists(externalPath)) {
            return externalPath;
        }

        // 3. 返回外部存储路径（即使文件不存在）
        return externalPath;
    }


    private Path ensureExternalResource(String fullPath) throws IOException {
        Path targetPath = externalStoragePath.resolve(fullPath);

        // 确保目标目录存在（关键修复）
        ensureDirectoryExists(targetPath.getParent());

        if (Files.notExists(targetPath)) {
            Resource resource = resourceLoader.getResource("classpath:" + fullPath);
            try (InputStream in = resource.getInputStream()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("已从JAR同步资源: " + fullPath + " => " + targetPath);
            }
        }
        return targetPath;
    }

    // 确保目录存在（新增辅助方法）
    private void ensureDirectoryExists(Path directory) throws IOException {
        if (directory != null && !Files.exists(directory)) {
            Files.createDirectories(directory);
            System.out.println("创建缺失目录: " + directory.toAbsolutePath());
        }
    }

    // 统一资源访问接口
    public Path getResourceStoragePath(String relativePath) {
        // 在生产环境中优先使用外部存储
        Path externalPath = externalStoragePath.resolve(relativePath);
        if (Files.exists(externalPath)) {
            return externalPath;
        }

        // 开发环境或JAR内部资源
        try {
            Resource resource = resourceLoader.getResource("classpath:" + relativePath);
            if (resource.exists()) {
                return resource.getFile().toPath();
            }
        } catch (IOException e) {
            // 忽略错误
        }

        // 返回外部路径（即使不存在）
        return externalPath;
    }

    //    // 保存文件到资源存储
//    public void saveToResourceStorage(String relativePath, InputStream inputStream) throws IOException {
//        Path targetPath = getResourceStoragePath(relativePath);
//        Files.createDirectories(targetPath.getParent());
//        Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
//        System.out.println("资源保存成功: " + targetPath.toAbsolutePath());
//    }
    public void saveResource(ResourceType type, String relativePath, InputStream inputStream) throws IOException {
        // 构建完整路径：类型目录/相对路径
        String fullPath = type.getDirectory() + "/" + relativePath;
        saveResource(fullPath, inputStream);
    }

    /**
     * 保存资源文件（完整路径）
     * @param fullPath 完整资源路径
     * @param inputStream 文件流
     * @throws IOException
     */
    public void saveResource(String fullPath, InputStream inputStream) throws IOException {
        Path targetPath = externalStoragePath.resolve(fullPath);
        Files.createDirectories(targetPath.getParent());
        Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("资源保存成功: " + targetPath.toAbsolutePath());
    }

    // 读取类路径资源内容
    public String readResourceContent(String classpath) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + classpath);
        try (InputStream inputStream = resource.getInputStream()) {
            byte[] bytes = FileCopyUtils.copyToByteArray(inputStream);
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    // 获取类路径资源输入流
    public InputStream getResourceAsStream(String classpath) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + classpath);
        return resource.getInputStream();
    }

    // 获取外部存储路径（用于写入）
    public Path getExternalStoragePath(String relativePath) {
        return externalStoragePath.resolve(relativePath);
    }



    public boolean existsResource(ResourceType type, String relativePath) {
        try {
            String fullPath = type.getDirectory() + "/" + relativePath;
            Path path = getResourcePath(fullPath);
            return Files.exists(path);
        } catch (Exception e) {
            return false;
        }
    }
}
