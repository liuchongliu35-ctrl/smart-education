package com.bing.tpa.Thread.pptVideoThread.DigitalHuman;

import com.bing.tpa.common.InMemoryDataStore;
import com.bing.tpa.domain.digital.FlaskStatus;
import lombok.Getter;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Comparator;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class SmartTaskScheduler {
    // 配置参数
    @Value("${scheduler.flask.status.url}")
    private String flaskStatusUrl;

    @Value("${scheduler.core.pool.size}")
    private int corePoolSize;

    @Value("${scheduler.max.pool.size}")
    private int maxPoolSize;

    @Value("${scheduler.max.concurrent.tasks}")
    private int maxConcurrentTasks;


    @Value("${scheduler.high.load.threshold}")
    private int highLoadThreshold;

    @Value("${scheduler.min.start.interval}")
    private long minStartInterval; // 任务最小启动间隔(ms)

    @Value("${scheduler.max.start.interval}")
    private long maxStartInterval; // 最大启动间隔(ms)
    // 新增配置参数
    @Value("${scheduler.min.start.delay}")
    private long minStartDelay; // 默认10秒最小启动间隔

    @Value("${scheduler.max.start.delay}")
    private long maxStartDelay; // 默认13秒最大启动间隔

    @Autowired
    private InMemoryDataStore globalConfig;

    @Autowired
    private PerformanceMonitor performanceMonitor;

    @Autowired
    private BeforeConvertTask flaskStatusService;

    // 新增状态跟踪
    private final AtomicInteger pptConversionCount = new AtomicInteger(0);
    private final AtomicBoolean triggerFlag = new AtomicBoolean(false);

    private final ScheduledExecutorService fastStartExecutor =
            Executors.newSingleThreadScheduledExecutor();

    public  void increaseMP4counter() {
        int newCount = pptConversionCount.incrementAndGet();
        System.out.printf("[PPT转MP4] 开始转换，当前数量: %d%n", newCount);

        // 首次检测到PPT转MP4任务时触发新任务
        if (newCount == 1 && !triggerFlag.get()) {
//            triggerNextTask();
            triggerFlag.set(true); // 设置触发标志
        }
    }
    public  void decreaseMP4counter() {
        int newCount = pptConversionCount.decrementAndGet();
        System.out.printf("[PPT转MP4] 完成转换，当前数量: %d%n", newCount);

        // 当PPT转MP4任务全部完成时重置触发标志
        if (newCount == 0) {
            triggerFlag.set(false);
            System.out.println("[调度] PPT转MP4任务清零，重置触发标志");
        }
    }

    // 线程池组件
    private ThreadPoolExecutor executor;
    private ScheduledExecutorService monitorScheduler;
    @Getter
    private Semaphore resourceSemaphore; // 改为声明但不初始化
//    todo 记录最近三个used的值，即模型实例使用的数量
    private final CircularFifoBuffer recentUsedValues = new CircularFifoBuffer(3);
    // 状态跟踪
    private final AtomicInteger activeTasks = new AtomicInteger(0);
    private final AtomicLong totalTasksProcessed = new AtomicLong(0);
    private final AtomicLong totalDelayTime = new AtomicLong(0);
    private volatile FlaskStatus lastFlaskStatus = new FlaskStatus();

    // 自适应参数
    private volatile int dynamicCoreSize;
    private volatile long baseWaitTime = 500; // ms
    private final Lock adjustmentLock = new ReentrantLock();


    // 新增状态跟踪
    private volatile long lastTaskStartTime = System.currentTimeMillis();
    private final AtomicInteger pendingStarts = new AtomicInteger(0);
    private final Lock startLock = new ReentrantLock();
    private volatile long averageTaskTime = 60000; // 默认60秒

    // 状态跟踪
    private final ConcurrentLinkedQueue<Long> taskDurations = new ConcurrentLinkedQueue<>();
    private volatile long averageTaskDuration = 60000; // 默认60秒

    // 双重控制机制
    private Semaphore globalConcurrencySemaphore;
    private final Semaphore timeWindowSemaphore = new Semaphore(1);

    public SmartTaskScheduler() {
//        // 强制最大并发为2
//        this.maxConcurrentTasks = Math.min(2, Math.min(4, maxConcurrentTasks));
//        this.globalConcurrencySemaphore = new Semaphore(maxConcurrentTasks);
    }

    public int getActiveTaskCount() {
        return activeTasks.get();
    }
    public void downTaskCount() {
        activeTasks.decrementAndGet();
    }
    @PostConstruct
    public void init() {
//        todo 血的教训：maxConcurrentTasks和globalConcurrencySemaphore要在init里面初始化，不要在构造函数初始化！！
        System.out.println("Max concurrent tasks: " + maxConcurrentTasks);
        // 初始化信号量，使用注入后的maxConcurrentTasks值
        this.globalConcurrencySemaphore = new Semaphore(maxConcurrentTasks);
        this.resourceSemaphore = new Semaphore(maxConcurrentTasks);
        System.out.println("初始化信号量，许可数: " + maxConcurrentTasks);

        // 其余初始化代码...
        dynamicCoreSize = Math.max(1, corePoolSize);
        executor = new ThreadPoolExecutor(
                dynamicCoreSize,
                maxPoolSize,
                30L, TimeUnit.SECONDS,
                new PriorityBlockingQueue<>(100, new TaskPriorityComparator()),
                new ResourceAwareThreadFactory(),
                new SmartRejectionHandler()
        );

        // 启动监控调度器
        monitorScheduler = Executors.newScheduledThreadPool(2);
        monitorScheduler.scheduleAtFixedRate(this::updateFlaskStatus, 0, 3, TimeUnit.SECONDS);
        monitorScheduler.scheduleAtFixedRate(this::adjustParameters, 5, 5, TimeUnit.SECONDS);
        monitorScheduler.scheduleAtFixedRate(this::logSystemStatus, 1, 1, TimeUnit.MINUTES);
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
        monitorScheduler.shutdownNow();
        fastStartExecutor.shutdownNow();
    }

//    public Future<?> submitTask(VideoTask task) {
//        return submitTask(task, TaskPriority.NORMAL);
//    }

//    // 触发快速任务启动
//    private void triggerFastStart() {
//        mp4TaskLock.lock();
//        try {
//            int running = MP4counter.get();
//            int availableSlots = Math.min(2, maxConcurrentTasks - activeTasks.get());
//
//            if (running > 0 && availableSlots > 0 && !fastStartQueue.isEmpty()) {
//                System.out.printf("[快速启动] 检测到 %d 个PPT转MP4任务执行中，可启动 %d 个快速任务%n",
//                        running, availableSlots);
//
//                // 启动快速任务（错开启动）
//                for (int i = 0; i < availableSlots; i++) {
//                    if (!fastStartQueue.isEmpty()) {
//                        Runnable task = fastStartQueue.poll();
//                        fastStartExecutor.schedule(task, i * 2000, TimeUnit.MILLISECONDS);
//                    }
//                }
//            }
//        } finally {
//            mp4TaskLock.unlock();
//        }
//    }
//
//    // 处理快速启动队列
//    private void processFastStartQueue() {
//        if (!fastStartQueue.isEmpty()) {
//            triggerFastStart();
//        }
//    }
//    // 提交任务（重载方法，支持快速启动）
//    public Future<?> submitTask(VideoTask task, TaskPriority priority, boolean fastStart) {
//        if (fastStart) {
//            System.out.println("任务加入快速启动队列: " + task.getTaskId());
//            fastStartQueue.add(() -> {
//                // 实际执行任务
//                executor.execute(() -> processTask(task));
//            });
//            return null;
//        }
//        return submitTask(task, priority);
//    }

    // 新增方法：触发下一个任务（确保只触发一次）
//    private void triggerNextTask() {
//        // 检查当前活跃任务数，活跃数大于等于3就不行（等于2时表示有2个或者一个在执行ppt转mp4，就可以在启动一个，如果有3个活跃则不能再启动）
//        if (activeTasks.get() >=3) {
//            System.out.println("[调度] 已达最大并发，不触发新任务");
//            return;
//        }
//
//        // 获取线程池中的等待任务，使用线程池对象获取线程池的等待队列，然后获取等待队列中的下一个任务，获取后执行
//        BlockingQueue<Runnable> queue = executor.getQueue();
//        if (!queue.isEmpty()) {
//            // 找到并执行下一个任务
//            Runnable nextTask = queue.poll();
//            if (nextTask != null) {
//                System.out.println("[调度] 检测到PPT转MP4任务，触发下一个任务执行");
//                executor.execute(nextTask);
//            }
//        } else {
//            System.out.println("[调度] 队列中无等待任务");
//        }
//    }


    public Future<?> submitTask(VideoTask task, TaskPriority priority) {
        System.out.println("提交任务: " + task.getTaskId() + "，优先级: " + priority);
        task.setPriority(priority);
        return executor.submit(() -> {
            System.out.println("开始处理任务: " + task.getTaskId());
            processTask(task);
        });

    }

//    private void processTask(VideoTask task) {
//        long startTime = System.currentTimeMillis();
//        boolean acquired = false;
//        System.out.println("[" + task.getTaskId() + "] 进入任务处理流程");
//        try {
////            // 智能等待策略
////            long waitTime = calculateWaitTime();
////            System.out.println("[" + task.getTaskId() + "] 计算等待时间: " + waitTime + "ms");
////            if (waitTime > 0) {
////                System.out.println("需要等待"+waitTime+"ms");
////                totalDelayTime.addAndGet(waitTime);
////                Thread.sleep(waitTime);
////            }
////
////            // 获取资源许可
////            resourceSemaphore.acquire();
////            acquired = true;
////            activeTasks.incrementAndGet();
////
////            // 执行任务
////            task.execute();
////            totalTasksProcessed.incrementAndGet();
//            // 1. 智能等待策略（包含间隔控制）
//            long waitTime = calculateWaitTime();
//            Thread.sleep(waitTime);
//
//            // 2. 获取资源许可前检查时间间隔
//            enforceStartInterval();
//
//            // 3. 获取资源许可
//            resourceSemaphore.acquire();
//            acquired = true;
//            activeTasks.incrementAndGet();
//            updateLastStartTime(); // 记录本次启动时间
//
//            // 执行任务
//            long taskStart = System.currentTimeMillis();
//            task.execute();
//
//            // 记录任务执行时间
//            long duration = System.currentTimeMillis() - taskStart;
//            updateAverageTime(duration);
//
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            task.onInterrupted();
//        } catch (Exception e) {
//            task.onError(e);
//        } finally {
//            if (acquired) {
//                resourceSemaphore.release();
//                activeTasks.decrementAndGet();
//            }
//            performanceMonitor.recordTaskTime(System.currentTimeMillis() - startTime);
//        }
//    }
// 修改后的任务处理流程
//private void processTask(VideoTask task) {
//    long taskStartTime = 0;
//    try {
//        // 1. 全局并发控制（最多2个）
//        globalConcurrencySemaphore.acquire();
//
//        // 2. 时间窗口控制（确保启动间隔）
//        enforceStartInterval();
//
//        // 3. 记录任务启动时间
//        taskStartTime = System.currentTimeMillis();
//        lastTaskStartTime=taskStartTime;
//        activeTasks.incrementAndGet();
//
//        // 4. 执行任务
//        task.execute();
//
//    } catch (InterruptedException e) {
//        Thread.currentThread().interrupt();
//        task.onInterrupted();
//    } catch (Exception e) {
//        task.onError(e);
//    } finally {
//        // 5. 记录任务持续时间
//        if (taskStartTime > 0) {
//            long duration = System.currentTimeMillis() - taskStartTime;
//            recordTaskDuration(duration);
//            activeTasks.decrementAndGet();
//        }
//        // 6. 释放信号量
//        activeTasks.decrementAndGet();
//        globalConcurrencySemaphore.release();
//    }
//}

private void processTask(VideoTask task) {
    long taskStartTime = 0;
    try {
        // 1. 全局并发控制（最多3个）
        globalConcurrencySemaphore.acquire();
        // 2. 增加待启动计数
        pendingStarts.incrementAndGet();
        // 提前递增activeTasks，确保状态监控正确
        activeTasks.incrementAndGet();
        // 3. 时间窗口控制（确保启动间隔）
        enforceStartInterval();

        // 4. 减少待启动计数，增加活跃计数
        pendingStarts.decrementAndGet();

        // 5. 记录任务启动时间
        taskStartTime = System.currentTimeMillis();
        lastTaskStartTime = taskStartTime;

        // 6. 执行任务
        task.execute();

    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        task.onInterrupted();
    } catch (Exception e) {
        task.onError(e);
    } finally {
        // 7. 减少活跃计数
        if (taskStartTime > 0) {
            long duration = System.currentTimeMillis() - taskStartTime;
            recordTaskDuration(duration);
            activeTasks.decrementAndGet(); // 递减
        }
        // 8. 释放信号量
        globalConcurrencySemaphore.release();
    }
}
//    // 严格的时间间隔控制
//    private void enforceStartInterval() throws InterruptedException {
//        // 获取时间窗口锁（确保只有一个任务在计算间隔）
//        timeWindowSemaphore.acquire();
//        try {
//            long elapsed = System.currentTimeMillis() - lastTaskStartTime;
//            long requiredWait = calculateRequiredWait(elapsed);
//
//            if (requiredWait > 0) {
//                System.out.printf("[调度] 强制等待 %dms (已等待 %dms)%n", requiredWait, elapsed);
//                Thread.sleep(requiredWait);
//            }
//        } finally {
//            timeWindowSemaphore.release();
//        }
//    }

//    private void enforceStartInterval() throws InterruptedException {
//        // 获取时间窗口锁（确保只有一个任务在计算间隔）
//        timeWindowSemaphore.acquire();
//        try {
//            long currentTime = System.currentTimeMillis();
//            long elapsed = currentTime - lastTaskStartTime;
//
//            // 计算基础等待时间（5-7秒随机）
//            long baseWait = ThreadLocalRandom.current().nextLong(minStartDelay, maxStartDelay + 1);
//
//            // 计算实际需要等待的时间
//            long requiredWait = Math.max(0, baseWait - elapsed);
//
//            // 考虑待启动任务数的影响
//            if (pendingStarts.get() > 1) {
//                // 如果有多个任务在等待启动，缩短等待时间
//                requiredWait = Math.max(1000, requiredWait / 2); // 至少等待1秒
//            }
//            //如果检测到当前正在运行得到线程数量小于最大并发或者有线程在执行ppt转换的任务就间隔2秒启动其他线程
//            if (activeTasks.get() <= maxConcurrentTasks||pptConversionCount.get()>=1) {
//                if (activeTasks.get()==0) {
//                    return;
//                }else{
//                    Thread.sleep(5000);
//                    return;
//                }
//            }
//            if (requiredWait > 0) {
//                System.out.printf("[调度] 强制等待 %dms (已等待 %dms, 待启动:%d)%n",
//                        requiredWait, elapsed, pendingStarts.get());
//                Thread.sleep(requiredWait);
//            }
//        } finally {
//            timeWindowSemaphore.release();
//        }
//    }
private void enforceStartInterval() throws InterruptedException {
    timeWindowSemaphore.acquire();
    try {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastTaskStartTime;

        // 基础等待时间（5-7秒随机）
        long baseWait = ThreadLocalRandom.current().nextLong(minStartDelay, maxStartDelay + 1);

        // 计算实际需要等待的时间
        long requiredWait = Math.max(0, baseWait - elapsed);

        // 考虑待启动任务数的影响
        if (pendingStarts.get() > 1) {
            // 如果有多个任务在等待启动，缩短等待时间
            requiredWait = Math.max(1000, requiredWait / 2); // 至少等待1秒
        }

        // 特殊处理：当有PPT转换任务时，适当缩短等待时间
        if (pptConversionCount.get() >= 1 && requiredWait > 2000) {
            requiredWait = 2000; // 有PPT任务时最大等待2秒
        }

        if (requiredWait > 0) {
            System.out.printf("[调度] 强制等待 %dms (已等待 %dms, 待启动:%d)%n",
                    requiredWait, elapsed, pendingStarts.get());
            Thread.sleep(requiredWait);
        }

        // 更新最后启动时间
        lastTaskStartTime = System.currentTimeMillis();
    } finally {
        timeWindowSemaphore.release();
    }
}
//private void enforceStartInterval() throws InterruptedException {
//    long currentTime = System.currentTimeMillis();
//    long elapsed = currentTime - lastTaskStartTime;
//    long baseWait = ThreadLocalRandom.current().nextLong(minStartDelay, maxStartDelay + 1);
//    long requiredWait = Math.max(0, baseWait - elapsed);
//
//    if (requiredWait > 0) {
//        System.out.println("任务[" + Thread.currentThread().getName() + "]等待" + requiredWait + "ms");
//        Thread.sleep(requiredWait);
//    }
//    // 直接更新最后启动时间，无需锁
//    lastTaskStartTime = currentTime + requiredWait;
//}

    public int getPendingStarts() {
        return  pendingStarts.get();
    }

    public void addTaskCount() {
        activeTasks.incrementAndGet();
    }

    // 动态计算所需等待时间
    private long calculateRequiredWait(long elapsed) {
        FlaskStatus status = lastFlaskStatus;
        int smoothedUsed = getSmoothedUsed();

        // 基础等待 = 最小间隔 - 已等待时间
        long baseWait = ThreadLocalRandom.current().nextLong(minStartDelay, maxStartDelay + 1);

        if(activeTasks.get() < maxConcurrentTasks){
            if (elapsed >= baseWait) {
                return 0;
            }
        }

        // 模型使用量因子（核心控制）
        double modelFactor = 1.0;
        if (smoothedUsed >= 2) {
            modelFactor = 2.0 + (smoothedUsed - 2) * 0.5; // 使用量2→2x, 3→2.5x, 4→3x
        }

        // 资源负载因子
        double resourceFactor = 1.0;
        if (status.getSystem().getCpuUsage() > 70) {
            resourceFactor += (status.getSystem().getCpuUsage() - 70) * 0.02;
        }
        if (status.getSystem().getMemoryUsage() > 75) {
            resourceFactor += (status.getSystem().getMemoryUsage() - 75) * 0.03;
        }

        // 预测因子（基于任务完成情况）
        double predictionFactor = 1.0;
        if (activeTasks.get() > 0 && isTaskNearingCompletion()) {
            predictionFactor = 0.6; // 有任务即将完成时减少等待
        }

        // 最终等待时间 = 基础等待 * 模型因子 * 资源因子 * 预测因子
        long calculatedWait = (long) (baseWait * modelFactor * resourceFactor * predictionFactor);

        // 限制在最小和最大间隔之间
        return Math.min(Math.max(minStartInterval, calculatedWait), maxStartInterval);
//        baseWait - elapsed
    }

    // 新增方法：更新启动时间
    private void updateLastStartTime() {
        startLock.lock();
        try {
            lastTaskStartTime = System.currentTimeMillis();
        } finally {
            startLock.unlock();
        }
    }

    // 记录任务持续时间（移动平均）
    private void recordTaskDuration(long duration) {
        // 保留最近10个任务的持续时间
        if (taskDurations.size() >= 10) {
            taskDurations.poll();
        }
        taskDurations.offer(duration);

        // 计算移动平均
        long sum = 0;
        for (long d : taskDurations) {
            sum += d;
        }
        averageTaskDuration = sum / taskDurations.size();
    }

    // 检查是否有任务即将完成
    private boolean isTaskNearingCompletion() {
        // 平均任务时间的70%作为"接近完成"阈值
        long nearingThreshold = (long) (averageTaskDuration * 0.7);

        // 检查所有活动任务
        long currentTime = System.currentTimeMillis();
        return currentTime - lastTaskStartTime > nearingThreshold;
    }

    // 新增方法：更新平均任务时间
    private void updateAverageTime(long duration) {
        // 指数平滑: 新值=0.2*当前 + 0.8*历史
        averageTaskTime = (long) (0.2 * duration + 0.8 * averageTaskTime);
    }


    // 新增方法：检查是否有任务即将完成
    private boolean hasTaskNearingCompletion() {
        if (activeTasks.get() == 0) return false;

        // 计算预估剩余时间 = 平均时间 * 1.2 - 已运行时间
        long estimatedRemaining = (long)(averageTaskTime * 1.2);
        return estimatedRemaining < 15000; // 剩余时间<15秒视为即将完成
    }

    private long calculateWaitTime() {
//        FlaskStatus status = lastFlaskStatus;
//        int currentActive = activeTasks.get();
//
//        // 当所有资源充足时立即返回0（不等待）
//        if (isResourceAbundant(status)) {
//            System.out.println("[调度] 资源充足，立即执行任务");
//            return 0;
//        }else System.out.println("[调度] 资源紧张，启动等待计算");
//
//        // 基础算法：等待时间 = 基础等待 + 负载因子 + 资源因子
//        long waitTime = baseWaitTime;
//
//        // 1. 负载因子：基于活动任务数
//        int maxConcurrent = status.getTasks().getMaxConcurrent();
//        if (currentActive > maxConcurrent * 0.7) {
//            waitTime += (currentActive - (int)(maxConcurrent * 0.7)) * 200;
//        }
//
//        // 2. 资源因子：基于Flask状态
//        if (status.getSystem().getCpuUsage() > 70) {
//            waitTime += (long) ((status.getSystem().getCpuUsage() - 70) * 10);
//        }
//
//        if (status.getSystem().getMemoryUsage() > 75) {
//            waitTime += (long) ((status.getSystem().getMemoryUsage() - 75) * 15);
//        }
////        gpu状态
//
//        if (status.getSystem().getGpu() instanceof Map) {
//            Map<String, Object> gpu = status.getGpuMap();//经过校验的GPU数据
//            // 安全获取utilization字段（判断存在性和类型）
//            if (gpu.containsKey("utilization") && gpu.get("utilization") instanceof Number) {
//                double gpuUtil = ((Number) gpu.get("utilization")).doubleValue(); // 兼容整数/浮点数
//                if (gpuUtil > 80) {
//                    waitTime += (long) ((gpuUtil - 80) * 40);
//                }
//            }
//        }
//
////        int minAvailableModels = Math.min(
////                status.getModels().getVits().getAvailable(),
////                status.getModels().getWav2lip().getAvailable()
////        );
////        int modelUsed=Math.max(status.getModels().getVits().getUsed(),status.getModels().getWav2lip().getUsed());
//
////        if (minAvailableModels < 2) {
////            waitTime += (2 - minAvailableModels) * 300L;
////        }
////        若当前flask正在使用的模型实例数量大于等于2就让后面的线程等待，否者就可以执行
////        if (modelUsed>=2) {
////            waitTime += (status.getModels().getVits().getTotal()-modelUsed) * 300L;
////        }
//
//        // 3. 模型可用性因子（优化后）
//        int smoothedUsed = getSmoothedUsed();
////        int vitsUsed = status.getModels().getVits().getUsed();
////        int wav2lipUsed = status.getModels().getWav2lip().getUsed();
//        int vitsTotal = status.getModels().getVits().getTotal(); // 固定为4
//        int wav2lipTotal = status.getModels().getWav2lip().getTotal(); // 固定为4
//
//// 计算剩余可用实例（取两个模型的最小值，即瓶颈模型）
//  /*      int vitsRemaining = vitsTotal - vitsUsed;
//        int wav2lipRemaining = wav2lipTotal - wav2lipUsed;*/
//        int minTotal= Math.min(vitsTotal, wav2lipTotal);
//        int minRemaining = minTotal-smoothedUsed;
//
//// 剩余越少，等待时间越长（剩余0时等待最久）
//        if (minRemaining <= 2) { // 剩余≤1时增加等待
//            waitTime += (3 - minRemaining) * 1000L; // 剩余1→+1000ms，剩余0→+2000ms
//        }
//
//        // 4. 队列长度因子
//        int queueLength = status.getTasks().getQueueLength();
//        if (queueLength > 5) {
//            waitTime += queueLength * 50L;
//        }
//        System.out.println("当前等待时间："+waitTime);
//        return Math.min(waitTime, 10000); // 最大等待10秒

        FlaskStatus status = lastFlaskStatus;
        int smoothedUsed = getSmoothedUsed();
        long baseWait = 0;

        // 核心控制：基于模型实例使用量
        if (smoothedUsed >= 3) {
            baseWait = 10000; // 高负载
        } else if (smoothedUsed == 2) {
            baseWait = 3000;  // 中等负载

            // 检查任务完成情况
            if (hasTaskNearingCompletion()) {
                baseWait = Math.max(500, baseWait / 2);
            }
        }
        // smoothedUsed <=1 时不增加基础等待
        // 添加资源因子
        long resourceWait = calculateResourceWait(status);

        // 最终等待时间 = 基础等待 + 资源等待
        return Math.min(baseWait + resourceWait, 15000);
    }


    private long calculateResourceWait(FlaskStatus status) {
        long resourceWait = 0;
        final double cpuWeight = 0.4;   // CPU权重
        final double gpuWeight = 0.5;   // GPU权重
        final double queueWeight = 0.1; // 队列权重

        // 1. CPU因子（非线性计算）
        double cpuUsage = status.getSystem().getCpuUsage();
        if (cpuUsage > 60) {
            // 指数增长：超过60%后影响急剧增加
            double cpuFactor = Math.pow(1.1, cpuUsage - 60) - 1;
            resourceWait += (long) (cpuFactor * 500 * cpuWeight);
        }

        // 2. GPU因子
        if (status.getGpuMap() != null && status.getGpuMap().containsKey("utilization")) {
            double gpuUtil = ((Number) status.getGpuMap().get("utilization")).doubleValue(); // 兼容整数/浮点数
            if (gpuUtil > 50) {
                // GPU影响比CPU更大
                double gpuFactor = Math.pow(1.15, gpuUtil - 50) - 1;
                resourceWait += (long) (gpuFactor * 800 * gpuWeight);
            }
        }

        // 3. 队列长度因子（动态调整）
        int queueLength = status.getTasks().getQueueLength();
        if (queueLength > 3) {
            resourceWait += (long) (Math.min(queueLength * 100, 2000) * queueWeight);
        }

        // 4. 内存因子（作为熔断机制）
        if (status.getSystem().getMemoryUsage() > 85) {
            // 内存超过85%时大幅增加等待
            double memFactor = (status.getSystem().getMemoryUsage() - 85) / 5.0;
            resourceWait += (long) (memFactor * 3000);
        }

        return resourceWait;
    }

//    private void updateFlaskStatus() {
//        try {
//            FlaskStatus status = flaskStatusService.getCurrentStatus(flaskStatusUrl);
//            lastFlaskStatus = status;
//
//            // 紧急熔断机制
//            if (status.getSystem().getCpuUsage() > 90 ||
//                    status.getSystem().getMemoryUsage() > 90) {
//                emergencyThrottle();
//            }
//
//        } catch (Exception e) {
//            // 状态获取失败时使用衰减策略
//            decayFlaskStatus();
//        }
//    }

    // 判断资源是否充足
    private boolean isResourceAbundant(FlaskStatus status) {
        // 1. 检查系统资源
        if (status.getSystem().getCpuUsage() > 70 ||
                status.getSystem().getMemoryUsage() > 75) {
            return false;
        }

        // 2. 检查GPU资源
        if (status.getGpuMap() != null) {
            Object utilObj = status.getGpuMap().get("utilization");
            if (utilObj instanceof Number) {
                double gpuUtil = ((Number) utilObj).doubleValue();
                if (gpuUtil > 30) { // GPU利用率超过30%即认为紧张
                    return false;
                }
            }
        }

        // 3. 检查模型实例
        int smoothedUsed = getSmoothedUsed();
        int minTotal = Math.min(
                status.getModels().getVits().getTotal(),
                status.getModels().getWav2lip().getTotal()
        );
        int minRemaining = minTotal - smoothedUsed;

        // 剩余实例少于3个认为资源紧张
        return minRemaining >= 3;
    }


    // 更新Flask状态时记录历史used值
    private void updateFlaskStatus() {
        try {
            FlaskStatus status = flaskStatusService.getCurrentStatus(flaskStatusUrl);
            lastFlaskStatus = status;
            // 记录VITS和Wav2Lip的used最大值
            int currentUsed = Math.max(
                    status.getModels().getVits().getUsed(),
                    status.getModels().getWav2lip().getUsed()
            );
            recentUsedValues.add(currentUsed);

//            紧急熔断机制
            if (status.getSystem().getCpuUsage() > 90 ||
                    status.getSystem().getMemoryUsage() > 90) {
                emergencyThrottle();
            }

        } catch (Exception e) {
            decayFlaskStatus();
        }
    }
    // 计算等待时间时使用平均used值
    private int getSmoothedUsed() {
        if (recentUsedValues.isEmpty()) return 0;
        int sum = 0;
        for (Object val : recentUsedValues) {
            sum += (Integer) val;
        }
        return sum / recentUsedValues.size();
    }

    private void adjustParameters() {
        adjustmentLock.lock();
        try {
            FlaskStatus status = lastFlaskStatus;
            int currentActive = activeTasks.get();
            int smoothedUsed = getSmoothedUsed(); // 调用平滑方法

            // 动态调整核心线程数
            if (status.isHealthy()) {
//                根据cpu调整
                if (status.getSystem().getCpuUsage() < 50 && currentActive > dynamicCoreSize * 0.8) {
                    dynamicCoreSize = Math.min(maxPoolSize, dynamicCoreSize + 1);//增加线程
                } else if (status.getSystem().getCpuUsage() > highLoadThreshold) {
                    dynamicCoreSize = Math.max(corePoolSize, dynamicCoreSize - 1);//减少线程
                }

//                根据used调整
                // 若平滑后的used值较低（系统负载轻），增加核心线程数
                if (smoothedUsed < status.getModels().getVits().getTotal() * 0.5) {
                    dynamicCoreSize = Math.min(maxPoolSize, dynamicCoreSize + 1);//增加线程
                }
                // 若平滑后的used值较高（系统负载重），减少核心线程数
                else if (smoothedUsed > status.getModels().getVits().getTotal() * 0.8) {
                    dynamicCoreSize = Math.max(corePoolSize, dynamicCoreSize - 1);//减少线程
                }

                // 动态调整基础等待时间（新增逻辑）
                if (isResourceAbundant(status)) {
                    // 资源充足时进一步降低基础等待
                    baseWaitTime = (long) Math.max(50, baseWaitTime * 0.8); // 最低50ms
                } else {
                    // 资源紧张时适当增加基础等待
                    baseWaitTime = (long) Math.min(1000, baseWaitTime * 1.2); // 最高1000ms
                }

            } else {
                dynamicCoreSize = Math.max(1, dynamicCoreSize - 1);
            }
            executor.setCorePoolSize(dynamicCoreSize);

            // 调整基础等待时间
            if (totalTasksProcessed.get() > 0) {
                double avgDelay = (double) totalDelayTime.get() / totalTasksProcessed.get();
                if (avgDelay > 1000) {
                    baseWaitTime = (long) (baseWaitTime * 0.9); // 降低延迟
                } else if (avgDelay < 300) {
                    baseWaitTime = (long) (baseWaitTime * 1.1); // 增加延迟
                }
            }

        } finally {
            adjustmentLock.unlock();
        }
    }

    private void emergencyThrottle() {
        // 1. 减少核心线程数
        dynamicCoreSize = Math.max(1, dynamicCoreSize / 2);
        executor.setCorePoolSize(dynamicCoreSize);

        // 2. 增加基础等待时间
        baseWaitTime = Math.min(5000, baseWaitTime * 2);

        // 3. 记录熔断事件
        performanceMonitor.recordEmergencyEvent("FLASK_OVERLOAD");
    }

    private void decayFlaskStatus() {
        // 状态衰减：假设资源使用率线性增加
        FlaskStatus.SystemInfo system = lastFlaskStatus.getSystem();
        system.setCpuUsage(Math.min(100, system.getCpuUsage() + 5));
        system.setMemoryUsage(Math.min(100, system.getMemoryUsage() + 3));

        FlaskStatus.ModelsInfo models = lastFlaskStatus.getModels();
        models.getVits().setAvailable(Math.max(0, models.getVits().getAvailable() - 1));
        models.getWav2lip().setAvailable(Math.max(0, models.getWav2lip().getAvailable() - 1));
    }

    private void logSystemStatus() {
        System.out.printf("[System Status] Core: %d, Active: %d/%d, Queue: %d, AvgDelay: %.1fms%n",
                dynamicCoreSize,
                activeTasks.get(),
                lastFlaskStatus.getTasks().getMaxConcurrent(),
                lastFlaskStatus.getTasks().getQueueLength(),
                totalTasksProcessed.get() > 0 ?
                        (double) totalDelayTime.get() / totalTasksProcessed.get() : 0
        );
        System.out.printf("[Flask Status] CPU: %.1f%%, Mem: %.1f%%, VITS: %d, Wav2Lip: %d%n",
                lastFlaskStatus.getSystem().getCpuUsage(),
                lastFlaskStatus.getSystem().getMemoryUsage(),
                lastFlaskStatus.getModels().getVits().getAvailable(),
                lastFlaskStatus.getModels().getWav2lip().getAvailable()
        );
    }


//    public long getLastTaskStartTime() {
//        return lastTaskStartTime;
//    }

    // === 内部类 ===
    private static class ResourceAwareThreadFactory implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "DH-Worker-" + counter.getAndIncrement());
            t.setPriority(Thread.NORM_PRIORITY - 1);
            t.setUncaughtExceptionHandler((thread, ex) -> {
                System.err.println("Uncaught exception in " + thread.getName() + ": " + ex.getMessage());
            });
            return t;
        }
    }

    private static class SmartRejectionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (r instanceof VideoTask) {
                ((VideoTask) r).onRejected();
            }
            System.err.println("Task rejected due to overload: " + r.toString());
        }
    }

    private static class TaskPriorityComparator implements Comparator<Runnable> {
        @Override
        public int compare(Runnable r1, Runnable r2) {
            int p1 = (r1 instanceof VideoTask) ? ((VideoTask) r1).getPriority().value : TaskPriority.NORMAL.value;
            int p2 = (r2 instanceof VideoTask) ? ((VideoTask) r2).getPriority().value : TaskPriority.NORMAL.value;
            return Integer.compare(p2, p1); // 更高优先级值表示更优先
        }
    }
}

