package com.bing.tpa.Thread.pptVideoThread;

import com.bing.tpa.Thread.pptVideoThread.DigitalHuman.AfterConvertTask;
import com.bing.tpa.Thread.pptVideoThread.DigitalHuman.BeforeConvertTask;
import com.bing.tpa.Thread.pptVideoThread.DigitalHuman.VideoMerge;
import com.bing.tpa.common.InMemoryDataStore;
import com.bing.tpa.common.ResourceType;
import com.bing.tpa.exception.DigitalException;
import com.bing.tpa.service.baseImpl.ResourceService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Component
public class PPTProcess {

    @Autowired
    private AfterConvertTask afterConvertTask;

    @Autowired
    private BeforeConvertTask beforeConvertTask;

    @Autowired
    private VideoMerge videoMerge;

    @Autowired
    private ResourceService resource;

    @Autowired
    private InMemoryDataStore globalConfig;

    @Async("convertToVideoExecutor")
    public void batchConvertToVideo(String pptRootPath, String userName,
                                    String pptBaseName, int pageCount,
                                    String pptTrueName) throws IOException {
        // 创建视频输出目录
//        String videoRootPath = "src/main/resources/splitPPTVideoFile";
//        Path videoUserDir = Paths.get(videoRootPath, userName);
        Path videoUserDir = resource.getResourcePath(ResourceType.SPLITVIDEO, userName);
        try {
            Files.createDirectories(videoUserDir);
        } catch (IOException e) {
            globalConfig.put(userName,false);//出错就改状态
            System.err.println("创建视频目录失败: " + e.getMessage());
            return;
        }
        globalConfig.put("progress",0.1);
        System.out.printf("开始转换PPT到视频: %s, 共%d页%n", pptBaseName, pageCount);

        int completedCount = 0;
        int totalErrors = 0;

        // 顺序处理每一页
        for (int pageNumber = 1; pageNumber <= pageCount; pageNumber++) {
            int retryAttempts = 0;
            final int maxRetries = 3;
            boolean success = false;

            while (retryAttempts <= maxRetries && !success) {
                try {
                    System.out.printf("正在转换: %s (页 %d)%n", pptBaseName, pageNumber);

                    // 创建转换器实例
                    ConvertToVideoThread converter = new ConvertToVideoThread();
                    String pptFileName = String.format("%s-%d.pptx", pptBaseName, pageNumber);//17--5512ppt_temp-1.pptx
                    Path pptPath = Paths.get(pptRootPath, userName, pptFileName);//****/splitPPTFile/雪之下的猫/aaa.pptx
                    String videoFileName = String.format("%s-%d.mp4", pptBaseName, pageNumber);//17--5512ppt_temp-1.mp4
                    Path pptVideoPath = videoUserDir.resolve(videoFileName);

                    // 1. 预处理任务
                    Path jsonPath = beforeConvertTask.BeforeConvert(pptPath.toString(), pageNumber, userName,pptBaseName);
//                    将jsonPath中的时间读取出来
                    String jsonContent = new String(Files.readAllBytes(jsonPath));

                    // 2. 使用Jackson解析JSON
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Double> jsonMap = mapper.readValue(jsonContent,
                            new TypeReference<Map<String, Double>>(){});

                    // 3. 获取键"0"对应的值
                    Double timeValue = jsonMap.get("0");
                    // 2. 转换视频
                    converter.convertToVideo(pptPath.toString(), pptVideoPath.toString(), (int) Math.round(timeValue)-1);
                    System.out.printf("✓ 转换完成: %s%n", videoFileName);

                    // 3. 后处理任务
                    String videoPath = afterConvertTask.digitalHumanVideo(pptVideoPath.toString(), userName,pptBaseName,pageCount);
                    System.out.printf("✓ 视频生产完成: %s%n", videoPath);

                    completedCount++;
                    success = true;
                    globalConfig.put("progress",(double)completedCount/pageCount);//设置进度调
//                    Path splitVideoRootPath = resource.getResourcePath(ResourceType.VIDEOFILE, "");
//                    System.out.println(splitVideoRootPath);
                } catch (Exception e) {
                    retryAttempts++;
                    totalErrors++;

                    if (retryAttempts <= maxRetries) {
                        System.err.printf("转换失败 [%s-%d]: %s (尝试 %d/%d)%n",
                                pptBaseName, pageNumber, e.getMessage(), retryAttempts, maxRetries);
                        try {
                            // 延迟重试，避免立即重试导致服务器压力过大
                            Thread.sleep(5000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        globalConfig.put(userName,false);//最终转化失败就重置视频生成状态
                        System.err.printf("❌ 转换最终失败 [%s-%d]: %s%n",
                                pptBaseName, pageNumber, e.getMessage());
                    }
                }
            }
        }

        System.out.printf("所有PPT转换任务已完成! 成功: %d, 失败: %d%n",
                completedCount, totalErrors);
//        todo 将所有视频进行拼接
//        String splitVideoRootPath="src/main/resources/videoFile";//视频片段保存的地址
        Path splitVideoRootPath = resource.getResourcePath(ResourceType.VIDEOFILE, "");
        try {
            videoMerge.merge(splitVideoRootPath.toString(),pptBaseName,pageCount,pptTrueName,userName);
//            todo 视频生成完成，将该用户视频生成状态变为false，表示该用户没有在制作视频
            globalConfig.put(userName,false);
        } catch (IOException | DigitalException e) {
            globalConfig.put(userName,false);//重置视频生成状态，根据名字来重置状态
            throw new RuntimeException(e);
        }
    }
    }
//    @Async("convertToVideoExecutor")
//    public CompletableFuture<Void> batchConvertToVideo(String pptRootPath, String userName,
//                                                       String pptBaseName, int pageCount) {
//        // 创建转换任务对象
//        ConversionTask task = new ConversionTask(
//                pptRootPath,
//                userName,
//                pptBaseName,
//                pageCount
//        );
//
//        // 将任务加入队列（非阻塞）
//        conversionQueue.addTask(task);
//        return CompletableFuture.completedFuture(null);
//    }


// // 创建视频输出目录
//        String videoRootPath = "src/main/resources/SplitVideoFile";
//        Path videoUserDir = Paths.get(videoRootPath, userName);
//        try {
//            Files.createDirectories(videoUserDir);
//        } catch (IOException e) {
//            System.err.println("创建视频目录失败: " + e.getMessage());
//            return ;
//        }
//
//        // 创建线程池 (根据实际需求调整线程数)
//        int threadPoolSize = Math.min(10, pageCount); // 最多10个线程
//        ScheduledExecutorService executor =
//                Executors.newScheduledThreadPool(threadPoolSize);
//
//        System.out.printf("开始转换PPT到视频: %s, 共%d页%n", pptBaseName, pageCount);
//
//        // 创建任务队列
//        BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
/// /        // 创建转换任务线程
/// /        ConvertToVideoThread converter = new ConvertToVideoThread();
//        AtomicReference<AtomicInteger> retryCount= new AtomicReference<>(new AtomicInteger(0));
//        AtomicReference<AtomicInteger> completedCount=new AtomicReference<>(new AtomicInteger(0));
//        // 提交所有转换任务
//        // 填充任务队列
//        for (int i = 1; i <= pageCount; i++) {
//            final int pageNumber = i;
//            taskQueue.add(() -> {
//                try {
//                    System.out.printf("正在转换: %s (页 %d)%n", pptBaseName, pageNumber);
//
//                    // 创建独立的转换器实例
//                    ConvertToVideoThread converter = new ConvertToVideoThread();
//
//                    String pptFileName = String.format("%s-%d.pptx", pptBaseName, pageNumber);
//                    Path pptPath = Paths.get(pptRootPath, userName, pptFileName);
//
//                    String videoFileName = String.format("%s-%d.mp4", pptBaseName, pageNumber);
//                    Path videoPath = videoUserDir.resolve(videoFileName);
//
//                    converter.convertToVideo(pptPath.toString(), videoPath.toString());
//
//                    System.out.printf("✓ 转换完成: %s%n", videoFileName);
//                    completedCount.get().incrementAndGet();//表示完成任务的数量
//                } catch (Exception e) {
//                    retryCount.get().incrementAndGet();
//                    System.err.printf("转换失败 [%s-%d]: %s%n",
//                            pptBaseName, pageNumber, e.getMessage());
//                    // 失败后重试（最多3次）
//                    if (retryCount.get().getAndIncrement() < 3) {
//                        taskQueue.add((Runnable) this);
//                        System.out.printf("↻ 重试任务: %s-%d (尝试 %d/3)%n",
//                                pptBaseName, pageNumber, retryCount.get().get());
//                    }
//                }
//            });
//        }
//
//        // 智能调度任务
//        while (!taskQueue.isEmpty()) {
//            executor.execute(taskQueue.poll());
//            try {
//                // 动态调整等待时间：每10个任务增加等待
//                int waitTime = 10 + (completedCount.get().get() / 10) * 5;
//                TimeUnit.SECONDS.sleep(waitTime);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        }
//
//        // 关闭线程池（所有任务完成后）
//        executor.shutdown();
//
//        try {
//            // 等待所有任务完成（最多1小时）
//            if (!executor.awaitTermination(1, TimeUnit.HOURS)) {
//                System.err.println("警告：部分任务未在时限内完成");
//            }
//            System.out.println("所有PPT转换任务已完成!");
//        } catch (InterruptedException e) {
//            System.err.println("任务等待被中断: " + e.getMessage());
//            Thread.currentThread().interrupt();
//        }


