package com.campuslink.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 通知专用线程池
     *
     * 设计要点:
     * 1. 核心线程数 4,最大 8,适合 CPU 密集型任务(数据库写入)
     * 2. 队列容量 200,峰值时可缓冲一定量级的通知任务
     * 3. CallerRunsPolicy: 队列满时由调用者线程执行,避免丢失通知
     */
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(4);

        // 最大线程数
        executor.setMaxPoolSize(8);

        // 队列容量
        executor.setQueueCapacity(200);

        // 线程名前缀(方便日志追踪)
        executor.setThreadNamePrefix("notification-async-");

        // 线程空闲超时时间(秒)
        executor.setKeepAliveSeconds(60);

        // 拒绝策略: 队列满时由调用线程执行,确保通知不丢失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待时间(秒)
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();

        log.info("通知异步线程池初始化完成: core={}, max={}, queue={}",
            executor.getCorePoolSize(),
            executor.getMaxPoolSize(),
            executor.getQueueCapacity());

        return executor;
    }
}
