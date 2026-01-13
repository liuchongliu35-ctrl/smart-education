package com.bing.tpa.Thread.pptVideoThread.DigitalHuman;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

@Component
public class PerformanceMonitor {
    private final LongAdder totalTasks = new LongAdder();
    private final LongAdder succeededTasks = new LongAdder();
    private final LongAdder failedTasks = new LongAdder();
    private final AtomicLong totalProcessingTime = new AtomicLong();
    private final ConcurrentMap<String, LongAdder> eventCounters = new ConcurrentHashMap<>();

    public void recordTaskSuccess(long processingTime) {
        totalTasks.increment();
        succeededTasks.increment();
        totalProcessingTime.addAndGet(processingTime);
    }

    public void recordTaskFailure() {
        totalTasks.increment();
        failedTasks.increment();
    }

    public void recordTaskTime(long time) {
        // 用于统计处理时间分布
        System.out.println("处理时间分布:"+time);
    }

    public void recordEmergencyEvent(String eventType) {
        eventCounters.computeIfAbsent(eventType, k -> new LongAdder()).increment();
    }

    public double getSuccessRate() {
        long total = totalTasks.sum();
        return total > 0 ? (double) succeededTasks.sum() / total : 0.0;
    }

    public double getAvgProcessingTime() {
        long total = totalTasks.sum();
        return total > 0 ? (double) totalProcessingTime.get() / total : 0.0;
    }
}
