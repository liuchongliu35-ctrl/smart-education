package com.bing.tpa.Thread.pptVideoThread;

import com.bing.tpa.Thread.pptVideoThread.DigitalHuman.*;
import com.bing.tpa.Thread.pptVideoThread.sdk.ApiException;
import com.bing.tpa.common.InMemoryDataStore;
import com.bing.tpa.common.ResourceType;
import com.bing.tpa.domain.digital.BaseSetupInfo;
import com.bing.tpa.domain.digital.LoginRequest;
import com.bing.tpa.exception.DigitalException;
import com.bing.tpa.service.baseImpl.ResourceService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class PPTProcess2 {

    @Autowired
    private AfterConvertTask afterConvertTask;

    @Autowired
    private BeforeConvertTask beforeConvertTask;

    @Autowired
    private CommonTask commonTask;

    @Autowired
    private VideoMerge videoMerge;

    @Autowired
    private ResourceService resource;

    @Autowired
    private InMemoryDataStore globalConfig;

    @Autowired
    private SmartTaskScheduler smartTaskScheduler; // 新增智能调度器



    public void batchConvertToVideo(String pptRootPath, String userName,
                                    String pptBaseName, int pageCount,
                                    String pptTrueName, String avatarPath) throws IOException {
        // 创建视频输出目录
        Path videoUserDir = resource.getResourcePath(ResourceType.SPLITVIDEO, userName);
        try {
            Files.createDirectories(videoUserDir);
        } catch (IOException e) {
            System.err.println("创建视频目录失败: " + e.getMessage());
            globalConfig.put(userName,false);//出错就改状态
            globalConfig.put("status","fail");
            return;
        }
        globalConfig.put("progress",0.1);
        System.out.printf("开始转换PPT到视频: %s, 共%d页%n", pptBaseName, pageCount);

        // 创建完成计数器
        CountDownLatch completionLatch = new CountDownLatch(pageCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        BaseSetupInfo baseSetupInfo = new BaseSetupInfo();
        baseSetupInfo.setGender("1");
        baseSetupInfo.setEnhancer(false);
        baseSetupInfo.setDigitalMotion(2);

//        // 登录并配置数字人（只做一次）
//        LoginRequest userConfig1 = new LoginRequest();
//        userConfig1.setUsername("Test");
//        userConfig1.setPassword("123000");
//        commonTask.loginAndSetup(
//                userConfig1,
//                baseSetupInfo,
//                avatarPath
//        );

        // 进度监控
//        AtomicInteger processedCount = new AtomicInteger(0);
//        ScheduledExecutorService progressMonitor = Executors.newSingleThreadScheduledExecutor();
//        progressMonitor.scheduleAtFixedRate(() -> {
//            int done = processedCount.get();
//            System.out.printf("[进度] 已启动任务: %d/%d (%.1f%%)%n",
//                    done, pageCount, (done * 100.0 / pageCount));
//        }, 0, 10, TimeUnit.SECONDS);

        // 创建进度监控器（更细粒度）
        AtomicInteger processedCount = new AtomicInteger(0);
        ScheduledExecutorService progressMonitor = Executors.newSingleThreadScheduledExecutor();
        // 修改进度监控器
        progressMonitor.scheduleAtFixedRate(() -> {
            int active = smartTaskScheduler.getActiveTaskCount();  // 当前运行中的任务
            int pending = smartTaskScheduler.getPendingStarts();   // 等待启动的任务
            int totalActive = active + pending;                   // 总活跃任务
            int done = processedCount.get();

            System.out.printf("[状态] 运行中: %d, 等待启动: %d, 已完成: %d/%d (%.1f%%)%n",
                    active, pending, done, pageCount, (done * 100.0 / pageCount));
        }, 0, 5, TimeUnit.SECONDS);



        // 提交任务
        for (int i = 1; i <= pageCount; i++) {
            final int pageNumber = i;
            long initialDelay = (i - 1) * 500;

            // 创建视频任务
            VideoTask task = new VideoTask("PPT-Page-" + pageNumber) {
                @Override
                public void execute() throws Exception {
                    processedCount.incrementAndGet();
                    System.out.printf("准备启动任务: 页 %d (用户名: %s)%n", pageNumber, "liuc");

                    // 新增重试机制参数
                    int maxRetries = 3;
                    int retryCount = 0;
                    boolean isSuccess = false;

                    // 循环重试机制
                    while (retryCount < maxRetries && !isSuccess) {
                        try {
                            System.out.printf("用户 %s 开始处理: %s (页 %d)，尝试次数: %d/%d%n",
                                    "liuc", pptBaseName, pageNumber, retryCount + 1, maxRetries);
                            String pptFileName = String.format("%s-%d.pptx", pptBaseName, pageNumber);
                            Path pptPath = Paths.get(pptRootPath, userName, pptFileName);

                            String videoFileName = String.format("%s-%d.mp4", pptBaseName, pageNumber);
                            Path pptVideoPath = videoUserDir.resolve(videoFileName);

                            // 执行转换前任务
                            Path jsonPath = beforeConvertTask.BeforeConvert(pptPath.toString(), pageNumber, userName, pptBaseName);
                            System.out.println("当前任务的id为："+globalConfig.get("task-"+pageNumber));
                            System.out.println("音频推理成功，开始ppt处理");
                            // 读取JSON时间
                            String jsonContent = new String(Files.readAllBytes(jsonPath));
                            ObjectMapper mapper = new ObjectMapper();
                            Map<String, Double> jsonMap = mapper.readValue(jsonContent,
                                    new TypeReference<Map<String, Double>>() {});
                            Double timeValue = jsonMap.get("0");

                            // 视频转换
                            try {
                                smartTaskScheduler.increaseMP4counter();
                                ConvertToVideoThread converter = new ConvertToVideoThread();
                                converter.convertToVideo(pptPath.toString(), pptVideoPath.toString(), (int) Math.round(timeValue) - 1);
                                System.out.printf("✓ 转换完成: %s%n", videoFileName);
                            }finally {
                                smartTaskScheduler.decreaseMP4counter();
                            }
                            // ================== 后处理任务（带重试机制）==================
                            int afterMaxRetries = 3;
                            int afterRetryCount = 0;
                            boolean afterTaskSuccess = false;
                            String videoPath = null;
//                          重试机制
                            while (afterRetryCount < afterMaxRetries && !afterTaskSuccess) {
                                try {
                                    System.out.println("开始合成视频");
                                    videoPath = afterConvertTask.digitalHumanVideo(pptVideoPath.toString(), userName, pptBaseName, pageNumber);
                                    System.out.printf("✓ 视频生产完成: %s%n", videoPath);
//                                   todo 视频生成完成任务数量减一：
                                    smartTaskScheduler.downTaskCount();
                                    afterTaskSuccess = true;
                                } catch (Exception e) {
                                    afterRetryCount++;
                                    if (afterRetryCount < afterMaxRetries) {
                                        System.err.printf("后处理任务失败 [页 %d]，尝试重试 (%d/%d): %s%n",
                                                pageNumber, afterRetryCount, afterMaxRetries, e.getMessage());
                                        Thread.sleep(5000); // 等待5秒后重试
                                    } else {
                                        System.err.printf("❌ 后处理任务最终失败 [页 %d]: %s%n", pageNumber, e.getMessage());
                                        globalConfig.put(userName,false);// todo 最终失败就修改视频制作状态
                                        globalConfig.put("status","fail");
                                        throw new RuntimeException("后处理任务达到最大重试次数", e);
                                    }
                                }
                            }
                            // ================== 后处理任务结束 ==================

                            if (afterTaskSuccess) {
                                isSuccess = true;
                                successCount.incrementAndGet();
                            }
                        } catch (Exception e) {
                            retryCount++;
                            if (retryCount < maxRetries) {
                                System.err.printf("处理失败 [页 %d]，尝试重试 (%d/%d): %s%n",
                                        pageNumber, retryCount, maxRetries, e.getMessage());
                                Thread.sleep(5000); // 等待5秒后重试
                            } else {
                                globalConfig.put(userName,false);// todo 最终失败就修改视频制作状态
                                globalConfig.put("status","fail");
                                throw e; // 最终失败
                            }
                        }
                    }

                    // 所有重试结束后判断最终结果
                    if (!isSuccess) {
                        failureCount.incrementAndGet();
                        if (globalConfig.get(userName) != Boolean.FALSE) {
                            globalConfig.put(userName, false);
                            globalConfig.put("status","fail");
                        }// todo 最终失败就修改视频制作状态
                        System.err.printf("❌ 页 %d 所有重试均失败，已标记为失败%n", pageNumber);
                    }

                    completionLatch.countDown();
                    // 新增：计算已完成页数并更新进度
                    int donePages = Math.toIntExact(pageCount - completionLatch.getCount()); // 已完成的总页数（含当前页）
                    double perPageProgress = 0.8 / pageCount; // 每页占总进度的比例（0.8为单页处理阶段总占比）
                    double currentProgress = 0.1 + (donePages * perPageProgress); // 初始0.1 + 已完成页进度
                    currentProgress = Math.min(currentProgress, 0.9); // 限制最大为0.9（预留合并阶段的0.1）
                    globalConfig.put("progress", currentProgress); // 更新进度
                    System.out.printf("任务完成: 页 %d (剩余: %d)%n",
                            pageNumber, completionLatch.getCount());
                }
            };
//            // 根据页面位置设置优先级
//            TaskPriority priority = TaskPriority.NORMAL;
//            if (pageNumber <= 3) {
//                priority = TaskPriority.HIGH; // 前3页高优先级
//            } else if (pageNumber > pageCount - 2) {
//                priority = TaskPriority.LOW; // 最后2页低优先级
//            }
            // 设置优先级（首页高，尾页低）
            TaskPriority priority = (pageNumber == 1) ? TaskPriority.HIGH :
                    (pageNumber > pageCount - 2) ? TaskPriority.LOW :
                            TaskPriority.NORMAL;
            // 使用智能调度器提交任务（优先级设为NORMAL）
         smartTaskScheduler.submitTask(task, priority);
        }

        System.out.println("所有任务已提交到智能调度器");

        // 等待所有任务完成
        try {
            long maxWaitTime = pageCount * 120L + 300; // 额外5分钟缓冲
            System.out.println("等待任务完成，最长等待: " + maxWaitTime + "秒");

            if (completionLatch.await(maxWaitTime, TimeUnit.SECONDS)) {
                System.out.printf("所有PPT转换任务已完成! 成功: %d, 失败: %d%n",
                        successCount.get(), failureCount.get());

                // ================== 所有任务完成后执行视频拼接 ==================
                if (successCount.get() > 0) { // 至少有一个成功才进行合并
                    try {
                        Path splitVideoRootPath = resource.getResourcePath(ResourceType.VIDEOFILE, "");
                        globalConfig.put("progress", 0.9);
                        videoMerge.merge(splitVideoRootPath.toString(), pptBaseName, pageCount, pptTrueName, userName);
                        globalConfig.put("progress", 1.0);
                        System.out.println("视频合并完成，总进度100%");
                    } catch (IOException | DigitalException e) {
                        globalConfig.put(userName,false);
                        globalConfig.put("status","fail");
                        throw new RuntimeException("视频拼接失败: " + e.getMessage());
                    }
                }
            } else {
                globalConfig.put(userName,false);// todo 失败就修改视频制作状态
                globalConfig.put("status","fail");
                System.err.println("警告：部分任务未在时限内完成");
                System.err.println("已完成: " + successCount.get() + ", 失败: " + failureCount.get() +
                        ", 未完成: " + completionLatch.getCount());
            }
        } catch (InterruptedException e) {
            System.err.println("任务等待被中断: " + e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            shutdownExecutor(progressMonitor, "进度监控器");
        }
    }

    // 重试处理工具方法
    private void handleRetry(int pageNumber, int retryCount, int maxRetries, String errorType, Exception e) {
        if (retryCount < maxRetries) {
            System.err.printf("%s 失败 [页 %d]，尝试重试 (%d/%d): %s%n",
                    errorType, pageNumber, retryCount, maxRetries, e.getMessage());
            try {
                Thread.sleep(5000); // 等待5秒后重试
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        } else {
            System.err.printf("❌ %s 最终失败 [页 %d]: %s%n", errorType, pageNumber, e.getMessage());
            e.printStackTrace();
        }
    }

    // 优雅关闭线程池
    private void shutdownExecutor(ExecutorService executor, String name) {
        try {
            System.out.println("正在关闭 " + name + "...");
            executor.shutdown();
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                System.err.println(name + " 未能在30秒内关闭，强制终止");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.err.println(name + " 关闭过程被中断");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}


//public void batchConvertToVideo(String pptRootPath, String userName,
//                                    String pptBaseName, int pageCount,
//                                    String pptTrueName, List<LoginRequest> userConfigs, String avatarPath) throws IOException {
//        // 创建视频输出目录
//        Path videoUserDir = resource.getResourcePath(ResourceType.SPLITVIDEO, userName);
//        try {
//            Files.createDirectories(videoUserDir);
//        } catch (IOException e) {
//            System.err.println("创建视频目录失败: " + e.getMessage());
//            return;
//        }
//
//        // 创建线程池
//        int threadPoolSize = Math.min(4, pageCount);
//        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(threadPoolSize, r -> {
//            Thread t = new Thread(r);
//            t.setDaemon(true);
//            return t;
//        });
//
//        System.out.printf("开始转换PPT到视频: %s, 共%d页%n", pptBaseName, pageCount);
//
//        // 创建完成计数器
//        CountDownLatch completionLatch = new CountDownLatch(pageCount);
//        AtomicInteger successCount = new AtomicInteger(0);
//        AtomicInteger failureCount = new AtomicInteger(0);
//        BaseSetupInfo baseSetupInfo = new BaseSetupInfo();
//        baseSetupInfo.setGender("1");
//        baseSetupInfo.setEnhancer(false);
//        baseSetupInfo.setDigitalMotion(2);
//
//        // 任务间隔时间
//        final long taskInterval = 60;
//        long initialDelay = 0;
//
//        // 进度监控
//        AtomicInteger processedCount = new AtomicInteger(0);
//        ScheduledExecutorService progressMonitor = Executors.newSingleThreadScheduledExecutor();
//        progressMonitor.scheduleAtFixedRate(() -> {
//            int done = processedCount.get();
//            System.out.printf("[进度] 已启动任务: %d/%d (%.1f%%)%n",
//                    done, pageCount, (done * 100.0 / pageCount));
//        }, 0, 60, TimeUnit.SECONDS);
//
//        LoginRequest userConfig1 = new LoginRequest();
//        userConfig1.setUsername("Test");
//        userConfig1.setPassword("123000");
//        commonTask.loginAndSetup(
//                userConfig1,
//                baseSetupInfo,
//                avatarPath
//        );
//
//        // 提交任务
//        for (int i = 1; i <= pageCount; i++) {
//            final int pageNumber = i;
//            scheduler.schedule(() -> {
//                processedCount.incrementAndGet();
//                System.out.printf("准备启动任务: 页 %d (用户名: %s)%n", pageNumber, "liuc");
//
//                // 新增重试机制参数
//                int maxRetries = 3;
//                int retryCount = 0;
//                boolean isSuccess = false;
//
//                // 循环重试机制
//                while (retryCount < maxRetries && !isSuccess) {
//                    try {
//                        // 为每个任务创建独立的处理逻辑
//                        System.out.printf("用户 %s 开始处理: %s (页 %d)，尝试次数: %d/%d%n",
//                                "liuc", pptBaseName, pageNumber, retryCount + 1, maxRetries);
//
//                        String pptFileName = String.format("%s-%d.pptx", pptBaseName, pageNumber);
//                        Path pptPath = Paths.get(pptRootPath, userName, pptFileName);
//
//                        String videoFileName = String.format("%s-%d.mp4", pptBaseName, pageNumber);
//                        Path pptVideoPath = videoUserDir.resolve(videoFileName);
//
//                        // 执行转换前任务
//                        Path jsonPath = beforeConvertTask.BeforeConvert(pptPath.toString(), pageNumber, userName,pptBaseName);
//
//                        // 读取JSON时间
//                        String jsonContent = new String(Files.readAllBytes(jsonPath));
//                        ObjectMapper mapper = new ObjectMapper();
//                        Map<String, Double> jsonMap = mapper.readValue(jsonContent,
//                                new TypeReference<Map<String, Double>>() {});
//                        Double timeValue = jsonMap.get("0");
//
//                        // 视频转换
//                        ConvertToVideoThread converter = new ConvertToVideoThread();
//                        converter.convertToVideo(pptPath.toString(), pptVideoPath.toString(), (int) Math.round(timeValue) - 1);
//                        System.out.printf("✓ 转换完成: %s%n", videoFileName);
//
//                        // ================== 后处理任务（带重试机制）==================
//                        int afterMaxRetries = 3;
//                        int afterRetryCount = 0;
//                        boolean afterTaskSuccess = false;
//                        String videoPath = null;
//
//                        while (afterRetryCount < afterMaxRetries && !afterTaskSuccess) {
//                            try {
//                                videoPath = afterConvertTask.digitalHumanVideo(pptVideoPath.toString(), userName,pptBaseName,pageNumber);
//                                System.out.printf("✓ 视频生产完成: %s%n", videoPath);
//                                afterTaskSuccess = true;
//                            } catch (Exception e) {
//                                afterRetryCount++;
//                                if (afterRetryCount < afterMaxRetries) {
//                                    System.err.printf("后处理任务失败 [页 %d]，尝试重试 (%d/%d): %s%n",
//                                            pageNumber, afterRetryCount, afterMaxRetries, e.getMessage());
//                                    try {
//                                        Thread.sleep(5000); // 等待5秒后重试
//                                    } catch (InterruptedException ie) {
//                                        Thread.currentThread().interrupt();
//                                    }
//                                } else {
//                                    System.err.printf("❌ 后处理任务最终失败 [页 %d]: %s%n", pageNumber, e.getMessage());
//                                    throw new RuntimeException("后处理任务达到最大重试次数", e);
//                                }
//                            }
//                        }
//                        // ================== 后处理任务结束 ==================
//
//                        if (afterTaskSuccess) {
//                            isSuccess = true;
//                            successCount.incrementAndGet();
//                        }
//                    } catch (IOException | DigitalException | ApiException e) {
//                        retryCount++;
//                        handleRetry(pageNumber, retryCount, maxRetries, "已知异常", e);
//                    } catch (Exception e) {
//                        retryCount++;
//                        handleRetry(pageNumber, retryCount, maxRetries, "未知异常", e);
//                    }
//                }
//
//                // 所有重试结束后判断最终结果
//                if (!isSuccess) {
//                    failureCount.incrementAndGet();
//                    System.err.printf("❌ 页 %d 所有重试均失败，已标记为失败%n", pageNumber);
//                }
//
//                completionLatch.countDown();
//                System.out.printf("任务完成: 页 %d (剩余: %d)%n",
//                        pageNumber, completionLatch.getCount());
//
//            }, initialDelay, TimeUnit.SECONDS);
//
//            initialDelay += taskInterval;
//        }
//
//        System.out.println("所有任务已调度，启动间隔: " + taskInterval + "秒");
//        System.out.println("预计全部启动完成时间: " + initialDelay + "秒后");
//
//        // 等待所有任务完成
//        try {
//            long maxWaitTime = initialDelay + (pageCount * 120L);
//            System.out.println("等待任务完成，最长等待: " + maxWaitTime + "秒");
//
//            if (completionLatch.await(maxWaitTime, TimeUnit.SECONDS)) {
//                System.out.printf("所有PPT转换任务已完成! 成功: %d, 失败: %d%n",
//                        successCount.get(), failureCount.get());
//
//                // ================== 所有任务完成后执行视频拼接 ==================
//                try {
//                    Path splitVideoRootPath = resource.getResourcePath(ResourceType.VIDEOFILE, "");
//                    videoMerge.merge(splitVideoRootPath.toString(), pptBaseName, pageCount, pptTrueName, userName);
//                } catch (IOException | DigitalException e) {
//                    throw new RuntimeException("视频拼接失败: " + e.getMessage());
//                }
//            } else {
//                System.err.println("警告：部分任务未在时限内完成");
//                System.err.println("已完成: " + successCount.get() + ", 失败: " + failureCount.get() +
//                        ", 未完成: " + completionLatch.getCount());
//            }
//        } catch (InterruptedException e) {
//            System.err.println("任务等待被中断: " + e.getMessage());
//            Thread.currentThread().interrupt();
//        } finally {
//            shutdownExecutor(scheduler, "任务调度器");
//            shutdownExecutor(progressMonitor, "进度监控器");
//        }
//    }
