package com.bing.tpa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class ThreadPoolConfig {
//    @Bean("analysisData")
//    public Executor analysisExecutor(){
//        ThreadPoolTaskExecutor executor=new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(8);
//        executor.setMaxPoolSize(30);
//        executor.setQueueCapacity(Integer.MAX_VALUE);
//        executor.setKeepAliveSeconds(60);
//        executor.setThreadNamePrefix("AI分析作业数据池");
//        executor.setWaitForTasksToCompleteOnShutdown(true);
//        executor.initialize();
//        return executor;
//    }

    @Bean("markAndGenerate")
    public Executor markAndGenerate(){
//        ThreadPoolTaskExecutor executor=new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(5);
//        executor.setMaxPoolSize(20);
//        executor.setQueueCapacity(Integer.MAX_VALUE);
//        executor.setKeepAliveSeconds(60);
//        executor.setThreadNamePrefix("预习任务批改以及生成个性化资源线程池");
//        executor.setWaitForTasksToCompleteOnShutdown(true);
//        executor.initialize();
//        return executor;
        return null;
    }

    @Bean("marking")
    public Executor markingExecutor(){
//        ThreadPoolTaskExecutor executor=new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(5);
//        executor.setMaxPoolSize(20);
//        executor.setQueueCapacity(Integer.MAX_VALUE);
//        executor.setKeepAliveSeconds(40);
//        executor.setThreadNamePrefix("预习任务批改以及生成个性化资源线程池");
//        executor.setWaitForTasksToCompleteOnShutdown(true);
//        executor.initialize();
//        return executor;
        return null;
    }
}
