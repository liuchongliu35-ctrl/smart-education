package com.bing.tpa.domain.digital;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

// 根实体类：对应整个JSON响应
@Data
public class FlaskStatus {
    // 原有业务字段（兼容旧逻辑）
    private boolean isHealthy;  // 服务是否健康
    private double cpuUsage;    // CPU使用率
    private double memoryUsage; // 内存使用率
    private int availableVitsModels;  // VITS可用实例数
    private int availableWav2LipModels;  // Wav2Lip可用实例数
    private int activeTasks;    // 活跃任务数
    private Map<String, Object> gpuMap;
    // 新增：嵌套字段（对应JSON结构）
    private String status;
    private SystemInfo system;
    private ModelsInfo models;
    private TasksInfo tasks;

    // 嵌套类：系统资源信息（对应JSON中的"system"字段）
    @Data
    public static class SystemInfo {
        @JsonProperty("cpu_usage")
        private double cpuUsage;  // CPU使用率

        @JsonProperty("memory_usage")
        private double memoryUsage;  // 内存使用率

        @JsonProperty("memory_total")
        private long memoryTotal;  // 总内存（字节）

        @JsonProperty("memory_used")
        private long memoryUsed;  // 已用内存（字节）

        private Object gpu;  // GPU信息（可为Map或字符串，兼容错误信息）
    }

    // 嵌套类：模型池信息（对应JSON中的"models"字段）
    @Data
    public static class ModelsInfo {
        private ModelStatus vits;  // VITS模型池状态
        private ModelStatus wav2lip;  // Wav2Lip模型池状态

        // 嵌套类：单个模型池的状态（对应"vits"或"wav2lip"字段）
        @Data
        public static class ModelStatus {
            private int available;  // 可用实例数
            private int used;       // 正在使用实例数
            private int total;      // 总实例数
        }
    }

    // 嵌套类：任务信息（对应JSON中的"tasks"字段）
    @Data
    public static class TasksInfo {
        @JsonProperty("active")
        private int activeTasks;  // 活跃任务数

        @JsonProperty("max_concurrent")
        private int maxConcurrent;  // 最大并发任务数

        @JsonProperty("queue_length")
        private int queueLength;  // 排队任务数
    }
}
