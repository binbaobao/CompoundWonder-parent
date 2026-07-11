//package com.compoundwonder.core.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.annotation.EnableAsync;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//
//import java.util.concurrent.Executor;
//
///**
// * 配置用于下单执行的异步线程池（@Async 使用）
// */
//@Configuration
//@EnableAsync
//public class AsyncConfig {
//
//    @Bean("orderAsyncExecutor")
//    public Executor orderAsyncExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(2);
//        executor.setMaxPoolSize(4);
//        executor.setQueueCapacity(200);
//        executor.setThreadNamePrefix("order-exec-");
//        executor.initialize();
//        return executor;
//    }
//
//    @Bean("orderDataExecutor")
//    public Executor orderDataExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(4);
//        // 交易，lv1，lv2，增值
//        executor.setMaxPoolSize(6);
//        executor.setQueueCapacity(200);
//        executor.setThreadNamePrefix("order-Data-");
//        executor.initialize();
//        return executor;
//    }
//}