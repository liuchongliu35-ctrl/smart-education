package com.bing.tpa.Thread;

import com.bing.tpa.Thread.pptVideoThread.ConvertToVideoThread;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
public class ConversionQueue {
    // 任务队列（无界队列）
    private final BlockingQueue<ConversionTask> queue = new LinkedBlockingQueue<>();
    // 单线程消费者（确保顺序处理）
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    // 运行状态标志
    private volatile boolean running = true;

    @PostConstruct
    public void init() {
        // 启动队列消费线程
        executor.submit(this::processQueue);
    }

    @PreDestroy
    public void shutdown() {
        running = false;
        executor.shutdownNow();
    }

    /**
     * 添加转换任务到队列
     * @param task 转换任务
     */
    public void addTask(ConversionTask task) {
        queue.offer(task);
        System.out.printf("已添加转换任务到队列: %s (%d 页)%n",
                task.getPptBaseName(), task.getPageCount());
    }

    /**
     * 队列处理主循环
     */
    private void processQueue() {
        while (running) {
            try {
                // 阻塞式获取任务
                ConversionTask task = queue.take();
                System.out.printf("开始处理转换任务: %s (%d 页)%n",
                        task.getPptBaseName(), task.getPageCount());

                // 实际执行转换
                processConversionTask(task);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("队列处理线程被中断");
                break;
            } catch (Exception e) {
                System.err.println("处理任务时出错: " + e.getMessage());
            }
        }
    }

    /**
     * 实际执行转换任务
     * @param task 转换任务
     */
    private void processConversionTask(ConversionTask task) {
        String pptRootPath = task.getPptRootPath();
        String userName = task.getUserName();
        String pptBaseName = task.getPptBaseName();
        int pageCount = task.getPageCount();

        // 创建视频输出目录
        String videoRootPath = "src/main/resources/SplitVideoFile";
        Path videoUserDir = Paths.get(videoRootPath, userName);
        try {
            Files.createDirectories(videoUserDir);
        } catch (Exception e) {
            System.err.println("创建视频目录失败: " + e.getMessage());
            return;
        }

        System.out.printf("开始转换PPT到视频: %s, 共%d页%n", pptBaseName, pageCount);

        // 使用固定大小线程池（控制并发度）
        int threadPoolSize = Math.min(3, pageCount); // 最多3个并发线程
        ExecutorService converterExecutor = Executors.newFixedThreadPool(threadPoolSize);
        ConvertToVideoThread converter = new ConvertToVideoThread();

        // 提交所有转换任务
        for (int i = 1; i <= pageCount; i++) {
            final int pageNumber = i;
            converterExecutor.submit(() -> {
                try {
                    // 构建输入输出路径
                    String pptFileName = String.format("%s-%d.pptx", pptBaseName, pageNumber);
                    Path pptPath = Paths.get(pptRootPath, userName, pptFileName);

                    String videoFileName = String.format("%s-%d.mp4", pptBaseName, pageNumber);
                    Path videoPath = videoUserDir.resolve(videoFileName);

                    System.out.printf("正在转换: %s (页 %d)%n", pptBaseName, pageNumber);

                    converter.convertToVideo(pptPath.toString(), videoPath.toString(),4);

                    System.out.printf("✓ 转换完成: %s%n", videoFileName);
                } catch (Exception e) {
                    System.err.printf("转换失败 [%s-%d]: %s%n",
                            pptBaseName, pageNumber, e.getMessage());
                }
            });
        }

        // 关闭线程池并等待完成
        converterExecutor.shutdown();
        try {
            if (!converterExecutor.awaitTermination(1, TimeUnit.HOURS)) {
                System.err.println("警告：部分任务未在时限内完成");
            }
            System.out.println("所有PPT转换任务已完成!");
        } catch (InterruptedException e) {
            System.err.println("任务等待被中断: " + e.getMessage());
            converterExecutor.shutdownNow();
        }
    }
}
