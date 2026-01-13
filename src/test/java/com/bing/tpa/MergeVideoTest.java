//package com.bing.tpa;
//
//import com.bing.tpa.Thread.pptVideoThread.DigitalHuman.ReadUserData;
//import com.bing.tpa.Thread.pptVideoThread.DigitalHuman.VideoMerge;
//import com.bing.tpa.Thread.pptVideoThread.PPTProcess2;
//import com.bing.tpa.common.InMemoryDataStore;
//import com.bing.tpa.common.ResourceType;
//import com.bing.tpa.domain.VO.LongTextVo;
//import com.bing.tpa.domain.VO.PhotoVo;
//import com.bing.tpa.domain.digital.CommentInfo;
//import com.bing.tpa.domain.digital.LoginRequest;
//import com.bing.tpa.domain.entity.TpaTeachDesign;
//import com.bing.tpa.domain.entity.TpaTeacher;
//import com.bing.tpa.exception.DigitalException;
//import com.bing.tpa.modelcall.chatWithImageCall.ChatWithImage;
//import com.bing.tpa.modelcall.pptCall.AIPPTRequest;
//import com.bing.tpa.service.baseImpl.ResourceService;
//import com.bing.tpa.service.baseImpl.TpaTeachDesignServiceImpl;
//import com.bing.tpa.service.baseService.TpaTeachDesignService;
//import com.bing.tpa.service.baseService.TpaTeacherService;
//import com.bing.tpa.utils.ReadPPTRemarkUtil;
//import com.bing.tpa.utils.SplitPPTUtil;
//import com.bing.tpa.utils.word.PPTToImageUtil;
//import org.apache.commons.io.FilenameUtils;
//import org.apache.poi.xslf.usermodel.XMLSlideShow;
//import org.apache.poi.xslf.usermodel.XSLFSlide;
//import org.jetbrains.annotations.NotNull;
//import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import java.awt.*;
//import java.awt.geom.Point2D;
//import java.io.*;
//import java.nio.file.*;
//import java.nio.file.attribute.BasicFileAttributes;
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.List;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.function.Consumer;
//
//@SpringBootTest
//public class MergeVideoTest {
//
//    @Autowired
//    private VideoMerge videoMerge;
//    @Autowired
//    private ReadUserData readUserData;
//
//    @Autowired
//    private PPTProcess2 process;
//    @Autowired
//    private ResourceService resource;
//
//    @Autowired
//    private TpaTeacherService tpaTeacherService;
//    @Autowired
//    private TpaTeachDesignService tpaTeachDesignService;
//    @Autowired
//    private SplitPPTUtil splitPPTUtil;
//    @Autowired
//    private ReadUserData userData;
//    @Autowired
//    private InMemoryDataStore globalConfig;
//
//    @Autowired
//    private AIPPTRequest aipptRequest;
//
//    @Autowired
//    private TpaTeachDesignService designService;
//    static Logger logger = LoggerFactory.getLogger(TpaTeachDesignServiceImpl.class);
//    @Autowired
//    private ChatWithImage chatWithImage;
//
//    @Autowired
//    private ReadPPTRemarkUtil remarkUtil;
//
//    @Autowired
//    private  SplitPPTUtil pptUtil;
//
//
//    @Test
//    public void mergeVideoTest() {
////        String splitVideoRootPath="src/main/resources/videoFile";
//        String userName = "雪之下的猫";
//        String pptName = "20250722175954_temp";
//        String outName = "17--56--人工智能主要流派.mp4";
//        Integer videoNum = 21;
//        try {
//            videoMerge.merge(resource.getResourcePath(ResourceType.VIDEOFILE, "").toString(), pptName, videoNum, outName, userName);
//        } catch (IOException | DigitalException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Test
//    public void getUserJson() {
//        try {
//            List<LoginRequest> requests = readUserData.readUserConfigs(10);
//            for (LoginRequest request : requests) {
//                System.out.println(request.toString());
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Test
//    public void loadResource() throws IOException {
//        String path = "src/main/resources/pptFile/雪之下的猫-ppt/17--55--q认识人工智能.pptx";
//        InputStream stream = resource.getResourceAsStream(path);
//    }
//
//    @Test
//    public void getPath() throws IOException {
//        Path splitVideoRootPath = resource.getResourcePath(ResourceType.VIDEOFILE, "雪之下的猫");
//        System.out.println(splitVideoRootPath.toString());
//    }
//
//    @Test
//    void batchConvertToVideoTest() throws Exception {
//        TpaTeachDesign design = tpaTeachDesignService.getById(62);
////        获取用户信息
////        TpaTeacher user = tpaTeacherService.getCurrentUser();
//        Path pptRootPath = resource.getResourcePath(ResourceType.PPT, ""); //使用资源管理器获取ppt的根路径
//        String pptName = 17 + "--" + design.getTdId() + "--" + design.getDesignName() + ".pptx";
//        Path path = pptRootPath.resolve("雪之下的猫-ppt");
//        Path pptPath = path.resolve(pptName);
//
//        Path pptTempRoot = pptRootPath.resolve("temp");
////        在该用户的ppt文件夹中创建一个temp临时目录，存放临时命名的ppt
//        if (!Files.exists(pptTempRoot)) {
//            Files.createDirectory(pptTempRoot);
//        }
//        // 2. 获取原PPT文件名和扩展名
//        File originalFile = new File(pptPath.getFileName().toString());
//        String baseName = FilenameUtils.getBaseName(originalFile.getName());
//        String extension = FilenameUtils.getExtension(originalFile.getName());
//        // 3. 生成临时文件名（使用时间戳）
//        String timeName = LocalDateTime.now()
//                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
//        String tempFileName = timeName + "_temp" + "." + extension;//如：20250713153045_temp.pptx
//        Path tempFilePath = pptTempRoot.resolve(tempFileName);// todo 临时ppt的路径
//        // 4. 复制原PPT到临时目录并重命名
//        Files.copy(pptPath, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
//        System.out.println("临时ppt文件已创建：" + tempFilePath);
//
////        todo 3.2 调用ppt拆分的线程池，将临时ppt文件传过去
//        CompletableFuture<Integer> future = splitPPTUtil.splitPptByPages(tempFilePath.toString(), resource.getResourcePath(ResourceType.SPLITPPT, "").toString(), "雪之下的猫");
//        int pptCount = future.get(5, TimeUnit.MINUTES);
//
////        globalConfig.put("");
//        process.batchConvertToVideo(
//                resource.getResourcePath(ResourceType.SPLITPPT, "").toString(),//  ****/splitPPTFile/
//                "雪之下的猫",
//                FilenameUtils.getBaseName(tempFilePath.getFileName().toString()),
//                pptCount, baseName + ".mp4", "src/main/resources/videoFile/Video.mp4");
//    }
//
//    //    /root/miniconda3/bin/gunicorn -w 8 -k gevent --timeout 120 -b 0.0.0.0:5000 wsgi:app
////  todo 测试将ppt的大纲作为授课台词插入到ppt的批注中
//    @Test
//    public void pptTest() throws Exception {
//        TpaTeacher user = new TpaTeacher();
//        user.setAccount("liuc123");
//        user.setUid(17);
//        TpaTeachDesign design = designService.getById(62);
//        String request = "请生成人工智能通识课教学课件ppt，主题为：" + design.getSecondaryTitle() + "(" + design.getDesignTitle() + "),";
//        String pptPath = aipptRequest.generatePPT(user, design, request);
//        System.out.println("生成的ppt路径：" + pptPath);
////        String pptPath="src/main/resources/pptFile/liuc123-ppt/17--55--认识人工智能.pptx";
////        addRemark(pptPath, user,"17--62--联邦学习框架15.pptx");
////        src/main/resources/pptPhoto/"+pptName
////        src/main/resources/pptPhoto/17--62--联邦学习框架2.pptx"
//        System.out.println("ppt制作完成");
////       key: ace0e733-956f-4cc4-8aba-04bf3c332783
////        pass:126c1ef56c4e3928ceae68a25c1b0b7a
//    }
//
//    @Test
//    public void photoTest() throws Exception {
//        String text = "联邦学习技术框架是一种分布式机器学习范式，核心在于实现 “数据不动模型动”。" +
//                "它由中央服务器与多个本地节点构成，中央服务器负责初始化模型并协调全局更新，各节点基于本地数据训练模型，" +
//                "仅上传参数梯度而非原始数据，经聚合优化后形成全局模型，再分发至各节点迭代。该框架在保护数据隐私的同时，" +
//                "能充分利用分散数据训练高性能模型，广泛应用于医疗、金融等数据敏感领域。\n";
//
//        LongTextVo longTextVo = new LongTextVo();
//        longTextVo.setText(text);
//        List<PhotoVo> photo = tpaTeachDesignService.getPhoto(longTextVo);
//        for (PhotoVo photoVo : photo) {
//            System.out.println(photoVo.toString());
//        }
//    }
//
////    public void addRemark(String pptPath, TpaTeacher user,String pptName)throws IOException {
////        Map<Integer,CommentInfo>remarkMap= new HashMap<>();
////        List<CommentInfo> commentInfoList=new ArrayList<>();
////        // 1. 创建图片存储目录
////        Path pptPhotoRootPath = resource.getResourcePath(ResourceType.PPTPHOTO, "");
////        Path pptPhotoUserPath = pptPhotoRootPath.resolve(user.getAccount());
////        Files.createDirectories(pptPhotoUserPath);
////
////        // 2. 预加载PPT获取总页数
////        int totalPages;
////        try (XMLSlideShow xmlSlideShow = new XMLSlideShow(new FileInputStream(pptPath))) {
////            totalPages = xmlSlideShow.getSlides().size();
////            System.err.println("ppt总页数："+totalPages);
////        }
////
////        // 3. 创建调度线程池（核心线程数=CPU核心数）
////        int corePoolSize = Runtime.getRuntime().availableProcessors()*2;
////        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(
////                corePoolSize,
////                new ThreadFactory() {
////                    private final AtomicInteger threadNum = new AtomicInteger(1);
////
////                    @Override
////                    public Thread newThread(Runnable r) {
////                        Thread t = new Thread(r, "ppt-remark-thread-" + threadNum.getAndIncrement());
////                        t.setDaemon(true);
////                        return t;
////                    }
////                }
////        );
////
////        // 4. 使用读写锁实现并发控制
////        ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
////        Lock readLock = rwLock.readLock();    // 用于PPT读取
//////        Lock writeLock = rwLock.writeLock();  // 用于PPT修改
////
////        // 5. 预加载PPT内容到内存（减少文件读取次数）
////        byte[] pptBytes = Files.readAllBytes(Paths.get(pptPath));
////
////        // 6. 提交任务（每页一个任务，间隔2秒启动）
////        CountDownLatch completionLatch = new CountDownLatch(totalPages);
////        List<Future<?>> futures = new ArrayList<>();
////
//////        todo 添加进度监控
////        // 添加进度更新回调
////        AtomicInteger processedPages = new AtomicInteger(0);
////        Consumer<Integer> progressUpdater = pageIndex -> {
////            double baseProgress = 50.0; // 从50%开始
////            double increment = 50.0 / totalPages; // 每页进度增量
////            double newProgress = baseProgress + (processedPages.incrementAndGet() * increment);
////            globalConfig.put("progress", Math.min(newProgress, 99.9)); // 保留最后0.1%给收尾
////        };
////
////
////        for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
////            final int currentPage = pageIndex + 1;
////            // 计算启动延迟：第一个0秒启动，第二个2秒后启动，第三个4秒后启动...
////            long initialDelay = pageIndex * 2L;
////
////            Future<?> future = scheduler.schedule(() -> {
////                try {
////                    // === 阶段1: 转换单页PPT为图片 ===
////                    String imagePath;
////                    try (ByteArrayInputStream bis = new ByteArrayInputStream(pptBytes);
////                         XMLSlideShow xmlSlideShow = new XMLSlideShow(bis)) {
////
////                        readLock.lock();
////                        try {
////                            // 添加页码验证
////                            if (currentPage - 1 >= xmlSlideShow.getSlides().size()) {
////                                logger.error("无效页码: {}(最大页数: {})",
////                                        currentPage, xmlSlideShow.getSlides().size());
////                                return; // 跳过当前任务
////                            }
////
////                            XSLFSlide slide = xmlSlideShow.getSlides().get(currentPage - 1);
////                            imagePath = PPTToImageUtil.convertSlideToImage(
////                                    slide, xmlSlideShow.getPageSize(), pptPhotoUserPath.toString(), currentPage
////                            );
////                        } finally {
////                            readLock.unlock();
////                        }
////                    }
////
//////                    // === 阶段2: 获取讲词 ===
//////                    String require = "使用OCR插件识别这页ppt图片，并生成授课讲词，这是教案ppt的第" + currentPage + "页";
//////                    String remark = chatWithImage.chatWithImageCall(imagePath, require);
////                    // === 阶段2: 获取讲词（带重试）===
////                    String require = "使用OCR插件识别这页ppt图片，并生成授课讲词，这是教案ppt的第" + currentPage + "页";
////                    int maxRetries = 3;
////                    int retryCount = 0;
////                    String remark = null;
////
////                    while (retryCount < maxRetries) {
////                        try {
////                            remark = chatWithImage.chatWithImageCall(imagePath, require);
////                            if (remark.contains("NULL")) throw new Exception("图片识别失败，重试");
////                            break;
////                        } catch (Exception e) {
////                            retryCount++;
////                            if (retryCount < maxRetries) {
////                                logger.warn("第{}页Coze调用超时，5秒后重试({}/{})",
////                                        currentPage, retryCount, maxRetries);
////                                Thread.sleep(3000);
////                            } else {
//////                                throw new RuntimeException("第"+currentPage+"页Coze调用失败", e);
////                                logger.error("第{}页Coze调用失败", currentPage, e);
////                                remark="该页授课讲词获取失败，请自行添加";
////                            }
////                        }
////                    }
//////                    // === 阶段3: 添加批注 ===
//////                    todo 将这个线程获取的批注储存到map中，后续集中添加到ppt中
////                    CommentInfo info = new CommentInfo(user.getAccount(),
////                            remark,
////                            new Point2D.Float(25,8),
////                            Date.from(Instant.now()));
////                    remarkMap.put(currentPage-1,info);//以页码作为key，CommentInfo作为值
////                    logger.info("批注列表更新成功");
//////                    writeLock.lock();
//////                    try {
//////                        Path tempPPTPath = pptPhotoUserPath.resolve(pptName + currentPage + ".pptx");
//////                        remarkUtil.addCommentToPage(pptPath, tempPPTPath.toString(),currentPage-1, remark, null);
//////                        logger.info("成功添加批注: 第{}页", currentPage);
//////                    } finally {
//////                        writeLock.unlock();
//////                    }
////                    // 成功处理一页后更新进度
////                    progressUpdater.accept(currentPage);
////                } catch (Exception e) {
////                    logger.error("第{}页处理失败: {}", currentPage, e.getMessage());
////                } finally {
////                    completionLatch.countDown(); // 无论成功失败都标记完成
////                }
////            }, initialDelay, TimeUnit.SECONDS); // 按计算的延迟启动
////
////            futures.add(future);
////        }
////
////        // 7. 等待所有任务完成
////        try {
////            if (!completionLatch.await(30, TimeUnit.MINUTES)) {
////                logger.warn("部分任务未在30分钟内完成");
////            }
////        } catch (InterruptedException e) {
////            Thread.currentThread().interrupt();
////            logger.error("主线程等待被中断");
////        } finally {
//////            todo 将map中的批注插入到ppt中
////            globalConfig.put("progress", 85.0);
////            Path tempDir = null;
////            Path mergedOutputPath = null;
////            try {
//////                Path tempPPTPath = pptPhotoUserPath.resolve(pptName+ ".pptx");
//////                Path pptRootPath = resource.getResourcePath(ResourceType.PPT, "");
//////                Path pptUserPath = pptRootPath.resolve(user.getName() + "-ppt");
//////                Path pptFinalPath = pptUserPath.resolve(pptName);
//////                todo 先将原来的ppt拆分为多个文件tempPPTPath这个路径下,每个文件有10页（后面不足10页的直接组成一个文件）
//////                todo 再将批注map按照顺序10个一组对应拆分的文件，每一组是一个新的map，并且key重新从0开始编号（要按照原来编号的顺序来）
//////                todo 最后调用addComments方法，指定子ppt文件的路径以及保存路径（保存采取覆盖原ppt的方案），然后将该子ppt对应的批注map传过去，添加批注后的ppt文件覆盖tempPPTPath路径下的原ppt文件
//////                todo 最后将拆分到tempPPTPath这个路径下的经过处理的子ppt合成一个完整的ppt保存到指定位置：pptFinalPath
////
//////                todo 开始后续业务:  创建存放拆分后的ppt的临时目录
////                tempDir = pptPhotoUserPath.resolve("temp-split-" + System.currentTimeMillis());
////                Files.createDirectories(tempDir);
////                logger.info("创建临时目录: {}", tempDir);
//////                  todo 拆分ppt为多个文件
////                List<Path> splitFiles = pptUtil.splitPPT(pptPath, tempDir.toString(), pptName);
////                logger.info("拆分后的ppt的保存路径: {}", splitFiles);
//////                todo 处理每一个拆分文件并添加批注
////                for (int i = 0; i < splitFiles.size(); i++) {
////                    Path subPptPath = splitFiles.get(i);
////                    int startPage = i * 10;
////                    int endPage = Math.min(startPage + 9, totalPages - 1);
////
////                    // 创建当前组的批注映射
////                    Map<Integer, CommentInfo> subComments = new HashMap<>();
////                    for (int page = startPage; page <= endPage; page++) {
////                        if (remarkMap.containsKey(page)) {
////                            // 新key = 原始页码 - 起始页码
////                            subComments.put(page - startPage, remarkMap.get(page));
////                        }
////                    }
////
////                    // 添加批注到子PPT（覆盖原文件）
////                    remarkUtil.addComments(subPptPath.toString(), subPptPath.toString(), subComments);
////                    logger.info("已为第 {} 组添加批注 (页码 {}-{})",
////                            i + 1, startPage + 1, endPage + 1);
////
////                    // 更新进度 (85% → 95%)
////                    double progress = 85.0 + (10.0 * (i + 1) / splitFiles.size());
////                    globalConfig.put("progress", progress);
////                }
////
//////                todo 合并所有子ppt
////                mergedOutputPath = pptPhotoUserPath.resolve(pptName + "-with-comments.pptx");
////                pptUtil.mergePPTs(splitFiles, mergedOutputPath.toString());
////
//////                todo替换原始PPT文件
////                Files.copy(mergedOutputPath, Paths.get(pptPath),
////                        StandardCopyOption.REPLACE_EXISTING);
////                logger.info("PPT文件已更新: {}", pptPath);
//////                remarkUtil.addComments(pptPath,tempPPTPath.toString(),remarkMap);
////            } catch (Exception e) {
////                logger.error("批注添加失败");
////            }
////            globalConfig.put("progress", 99.0);
////            // 8. 关闭线程池并清理资源
////            scheduler.shutdownNow();
////            cleanTempFiles(pptPhotoUserPath, tempDir, mergedOutputPath);
////        }
////    }
////
////    // 清理所有临时文件
////    private void cleanTempFiles(Path pptPhotoUserPath, Path tempDir, Path mergedOutputPath) {
////        // 1. 清理图片文件
////        cleanImageFiles(pptPhotoUserPath);
////
////        // 2. 清理临时拆分目录及其内容
////        if (tempDir != null) {
////            try {
////                FileUtils.deleteDirectory(tempDir.toFile());
////                logger.info("已删除临时拆分目录: {}", tempDir);
////            } catch (IOException e) {
////                logger.error("临时拆分目录删除失败: {}", e.getMessage());
////            }
////        }
////
////        // 3. 清理合并的临时PPT文件
////        if (mergedOutputPath != null) {
////            try {
////                Files.deleteIfExists(mergedOutputPath);
////                logger.info("已删除合并临时文件: {}", mergedOutputPath);
////            } catch (IOException e) {
////                logger.error("合并临时文件删除失败: {}", e.getMessage());
////            }
////        }
////    }
//
////public void addRemark(String pptPath, TpaTeacher user, String pptName) throws IOException {
////    Map<Integer, CommentInfo> remarkMap = new ConcurrentHashMap<>(); // 使用ConcurrentHashMap保证线程安全
////
////    // 1. 创建图片存储目录
////    Path pptPhotoRootPath = resource.getResourcePath(ResourceType.PPTPHOTO, "");
////    Path pptPhotoUserPath = pptPhotoRootPath.resolve(user.getAccount());
////    Files.createDirectories(pptPhotoUserPath);
////
////    // 2. 预加载PPT获取总页数和所有幻灯片
////    int totalPages;
////    List<XSLFSlide> slides = new ArrayList<>();
////    Dimension pageSize = null;
////    byte[] pptBytes = Files.readAllBytes(Paths.get(pptPath));
////
////    try (ByteArrayInputStream bis = new ByteArrayInputStream(pptBytes);
////         XMLSlideShow xmlSlideShow = new XMLSlideShow(bis)) {
////        totalPages = xmlSlideShow.getSlides().size();
////        pageSize = xmlSlideShow.getPageSize();
////
////        // 预加载所有幻灯片到内存
////        for (XSLFSlide slide : xmlSlideShow.getSlides()) {
////            slides.add(slide);
////        }
////
////        logger.info("PPT总页数：{}", totalPages);
////    }
////
////    final Dimension finalPageSize = pageSize; // 用于lambda表达式
////
////    // 3. 创建优化的线程池（IO密集型任务）
////    int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
////    ExecutorService executor = new ThreadPoolExecutor(
////            corePoolSize,
////            corePoolSize * 2,
////            60L, TimeUnit.SECONDS,
////            new LinkedBlockingQueue<>(100),
////            new ThreadFactory() {
////                private final AtomicInteger threadNum = new AtomicInteger(1);
////                @Override
////                public Thread newThread(@NotNull Runnable r) {
////                    Thread t = new Thread(r, "ppt-remark-thread-" + threadNum.getAndIncrement());
////                    t.setDaemon(true);
////                    return t;
////                }
////            },
////            new ThreadPoolExecutor.CallerRunsPolicy() // 队列满时让提交线程执行，避免任务丢失
////    );
////
////    // 4. 任务控制与进度监控
////    CountDownLatch completionLatch = new CountDownLatch(totalPages);
////    List<Future<?>> futures = new ArrayList<>();
////    AtomicInteger processedPages = new AtomicInteger(0);
////
////    Consumer<Integer> progressUpdater = pageIndex -> {
////        double baseProgress = 50.0; // 从50%开始
////        double increment = 50.0 / totalPages; // 每页进度增量
////        double newProgress = baseProgress + (processedPages.incrementAndGet() * increment);
////        globalConfig.put("progress", Math.min(newProgress, 99.9)); // 保留最后0.1%给收尾
////    };
////
////    // 5. 提交任务（无延迟，立即并发执行）
////    for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
////        final int currentPage = pageIndex + 1;
////        final int slideIndex = pageIndex; // 幻灯片索引（0-based）
////
////        Future<?> future = executor.submit(() -> {
////            String imagePath = null;
////            try {
////                // === 阶段1: 转换单页PPT为图片（使用预加载的幻灯片）===
////                // 验证页码有效性
////                if (slideIndex >= slides.size()) {
////                    logger.error("无效页码: {}(最大页数: {})", currentPage, slides.size());
////                    return;
////                }
////
////                XSLFSlide slide = slides.get(slideIndex);
////                imagePath = PPTToImageUtil.convertSlideToImage(
////                        slide, finalPageSize, pptPhotoUserPath.toString(), currentPage
////                );
////
////                // === 阶段2: 获取讲词（带动态重试策略）===
////                String require = "使用OCR插件识别这页ppt图片，并生成授课讲词，这是教案ppt的第" + currentPage + "页";
////                int maxRetries = 4;
////                int retryCount = 0;
////                String remark = null;
////
////                while (retryCount < maxRetries) {
////                    try {
////                        remark = chatWithImage.chatWithImageCall(imagePath, require);
////                        if (remark.contains("NULL")) throw new Exception("图片识别失败，重试");
////                        break;
////                    } catch (Exception e) {
////                        retryCount++;
////                        if (retryCount < maxRetries) {
////                            long sleepTime = (long) (Math.pow(2, retryCount) * 1000); // 指数退避：1s→2s→4s
////                            logger.warn("第{}页调用失败，{}ms后重试({}/{})",
////                                    currentPage, sleepTime, retryCount, maxRetries);
////                            Thread.sleep(sleepTime);
////                        } else {
////                            logger.error("第{}页调用失败", currentPage, e);
////                            remark = "该页授课讲词获取失败，请自行添加";
////                        }
////                    }
////                }
////
////                // === 阶段3: 存储批注信息 ===
////                CommentInfo info = new CommentInfo(
////                        user.getAccount(),
////                        remark,
////                        new Point2D.Float(25, 8),
////                        Date.from(Instant.now())
////                );
////                remarkMap.put(slideIndex, info);
////                logger.info("第{}页批注信息已存储", currentPage);
////
////                // 更新进度
////                progressUpdater.accept(currentPage);
////            } catch (Exception e) {
////                logger.error("第{}页处理失败: {}", currentPage, e.getMessage());
////            } finally {
////                // 清理临时图片
////                if (imagePath != null) {
////                    try {
////                        Files.deleteIfExists(Paths.get(imagePath));
////                    } catch (IOException e) {
////                        logger.warn("第{}页图片清理失败: {}", currentPage, e.getMessage());
////                    }
////                }
////                completionLatch.countDown(); // 无论成功失败都标记完成
////            }
////        });
////
////        futures.add(future);
////    }
////
////    // 6. 等待所有任务完成
////    try {
////        if (!completionLatch.await(30, TimeUnit.MINUTES)) {
////            logger.warn("部分任务未在30分钟内完成");
////        }
////    } catch (InterruptedException e) {
////        Thread.currentThread().interrupt();
////        logger.error("主线程等待被中断");
////    } finally {
////        // 7. 处理批注并合并到PPT
////        globalConfig.put("progress", 85.0);
////        Path tempDir = null;
////        Path mergedOutputPath = null;
////
////        try {
////            // 优化：页数少则无需拆分
////            List<Path> splitFiles;
////            if (totalPages <= 10) {
////                // 页数少，直接使用原文件处理
////                splitFiles = Collections.singletonList(Paths.get(pptPath));
////            } else {
////                // 创建临时目录
////                tempDir = pptPhotoUserPath.resolve("temp-split-" + System.currentTimeMillis());
////                Files.createDirectories(tempDir);
////                logger.info("创建临时目录: {}", tempDir);
////
////                // 拆分PPT
//////                splitFiles = pptUtil.splitPPT(pptPath, tempDir.toString(), pptName);
////                System.out.println("开始拆分");
////                CompletableFuture<List<Path>> future = pptUtil.splitPPT(pptPath, tempDir.toString(), pptName);
////                splitFiles = future.get(5, TimeUnit.MINUTES);
////                logger.info("拆分完成，拆分后的PPT保存路径: {}", splitFiles);
////            }
////
////            // 处理每个拆分文件并添加批注
////            for (int i = 0; i < splitFiles.size(); i++) {
////                Path subPptPath = splitFiles.get(i);
////                int startPage = i * 3;
////                int endPage = Math.min(startPage + 2, totalPages - 1);
////
////                // 创建当前组的批注映射
////                Map<Integer, CommentInfo> subComments = new HashMap<>();
////                for (int page = startPage; page <= endPage; page++) {
////                    if (remarkMap.containsKey(page)) {
////                        // 新key = 原始页码 - 起始页码
////                        subComments.put(page - startPage, remarkMap.get(page));
////                    }
////                }
////
////                // 添加批注到子PPT（覆盖原文件）加到线程池中运行
////                Future<Void> future = remarkUtil.addComments(subPptPath.toString(), subPptPath.toString(), subComments);
////                future.get(5, TimeUnit.MINUTES);
////                logger.info("已为第 {} 组添加批注 (页码 {}-{})",
////                        i + 1, startPage + 1, endPage + 1);
////
////                // 更新进度 (85% → 95%)
////                double progress = 85.0 + (10.0 * (i + 1) / splitFiles.size());
////                globalConfig.put("progress", progress);
////            }
////
////            // 合并所有子PPT
////            mergedOutputPath = pptPhotoUserPath.resolve("with-comments-"+pptName);
////            Future<Void> future = pptUtil.mergePPTs(splitFiles, mergedOutputPath.toString());
////            future.get(5, TimeUnit.MINUTES);
////            // 替换原始PPT文件
////                Files.copy(mergedOutputPath, Paths.get("src/main/resources/pptPhoto/"+pptName),
////                        StandardCopyOption.REPLACE_EXISTING);
////                logger.info("PPT文件已更新: {}", pptPath);
////        } catch (Exception e) {
////            logger.error("批注添加失败", e);
////        }
////        globalConfig.put("progress", 99.0);
////        // 8. 关闭线程池并清理资源
////        executor.shutdown();
////        try {
////            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
////                executor.shutdownNow();
////            }
////        } catch (InterruptedException e) {
////            executor.shutdownNow();
////        }
////
////        // 清理临时文件
////        cleanTempFiles(pptPhotoUserPath, tempDir, mergedOutputPath);
////    }
////}
////
////    // 优化：使用NIO递归清理临时文件
////    private void cleanTempFiles(Path pptPhotoUserPath, Path tempDir, Path mergedOutputPath) {
////        cleanImageFiles(pptPhotoUserPath);
////        // 1. 清理临时拆分目录及其内容
////        if (tempDir != null) {
////            try {
////                Files.walkFileTree(tempDir, new SimpleFileVisitor<Path>() {
////                    @NotNull
////                    @Override
////                    public FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
////                        Files.delete(file);
////                        return FileVisitResult.CONTINUE;
////                    }
////
////                    @NotNull
////                    @Override
////                    public FileVisitResult postVisitDirectory(@NotNull Path dir, IOException exc) throws IOException {
////                        if (exc == null) {
////                            Files.delete(dir);
////                            return FileVisitResult.CONTINUE;
////                        } else {
////                            throw exc;
////                        }
////                    }
////                });
////                logger.info("已删除临时拆分目录: {}", tempDir);
////            } catch (IOException e) {
////                logger.error("临时拆分目录删除失败: {}", e.getMessage());
////            }
////        }
////
////        // 2. 清理合并的临时PPT文件
////        if (mergedOutputPath != null) {
////            try {
////                Files.deleteIfExists(mergedOutputPath);
////                logger.info("已删除合并临时文件: {}", mergedOutputPath);
////            } catch (IOException e) {
////                logger.error("合并临时文件删除失败: {}", e.getMessage());
////            }
////        }
////    }
////
////    // 清理图片文件（增强版）
////    private void cleanImageFiles(Path imageDir) {
////        try {
////            // 清理所有图片格式
////            String[] imageExtensions = {"jpg", "jpeg", "png"};
////
////            for (String ext : imageExtensions) {
////                try (DirectoryStream<Path> stream =
////                             Files.newDirectoryStream(imageDir, "*." + ext)) {
////                    for (Path file : stream) {
////                        Files.deleteIfExists(file);
////                        logger.debug("已删除临时图片: {}", file.getFileName());
////                    }
////                }
////            }
////
////            // 清理可能存在的临时PPT文件
////            try (DirectoryStream<Path> pptStream =
////                         Files.newDirectoryStream(imageDir, "*.pptx")) {
////                for (Path file : pptStream) {
////                    // 只删除临时文件，避免删除最终PPT
////                    if (file.getFileName().toString().contains("-temp-") ||
////                            file.getFileName().toString().endsWith("-with-comments.pptx")) {
////                        Files.deleteIfExists(file);
////                        logger.debug("已删除临时PPT: {}", file.getFileName());
////                    }
////                }
////            }
////        } catch (IOException e) {
////            logger.error("图片清理失败: {}", e.getMessage());
////        }
////    }
//
//
//    @Test
//    public void getRemark() {
//        try {
//            Map<Integer, List<CommentInfo>> map = remarkUtil.readAllComments("src/main/resources/pptPhoto/17--62--联邦学习框架15.pptx");
//            for (Map.Entry<Integer, List<CommentInfo>> entry : map.entrySet()) {
//                entry.getValue().forEach(commentInfo -> {
//                    System.out.println(commentInfo.getText());
//                });
//            }
//        } catch (Exception e) {
//            logger.error("读取失败：", e);
//            throw new RuntimeException(e);
//        }
//    }
//}
