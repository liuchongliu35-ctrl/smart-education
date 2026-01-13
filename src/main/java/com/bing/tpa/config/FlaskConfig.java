package com.bing.tpa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlaskConfig {
//    @Value("${flask.base-url}")
    private String baseUrl;

    @Value("${task.timeout}")
    private long taskTimeout;

    @Value("${task.check-interval}")
    private long taskCheckInterval;

    @Value("${storage.location}")
    private String storageLocation;

    @Value("${storage.ppt.location}")
    private String pptStorageLocation;

    public String getBaseUrl() {
        return baseUrl;
    }

    public long getTaskTimeout() {
        return taskTimeout;
    }

    public long getTaskCheckInterval() {
        return taskCheckInterval;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public String getPptStorageLocation() {
        return pptStorageLocation;
    }
}
