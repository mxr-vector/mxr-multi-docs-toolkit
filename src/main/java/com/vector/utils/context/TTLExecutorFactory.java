package com.vector.utils.context;

import com.alibaba.ttl.threadpool.TtlExecutors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * @ClassName TtlFactory
 * @description: TODO
 * @author YuanJie
 * @date 2025/6/4 16:14
 */
public class TTLExecutorFactory {
    /** 默认线程池核心线程数 */
    private static final int DEFAULT_THREAD_POOL_SIZE = 2;

    /** TTL线程池标识 */
    protected static final String TTL_EXECUTOR_IDENTIFIER = "TTL_EXECUTOR";
    
    /** 共享TTL线程池单例 */
    private static final ExecutorService SHARED_TTL_EXECUTOR;
    /**
     * 创建固定大小的TTL线程池
     * <p>
     * 该线程池会自动传递上下文到子线程，建议用于需要上下文传递的业务场景
     * 注意：该线程池使用固定数量线程，长期闲置时不会自动回收
     * 注意：此方法创建的线程池不是共享TTL线程池，无法获取上下文
     * </p>
     * @param corePoolSize 线程池核心线程数
     * @return 经过TTL包装的固定大小线程池
     */
    public static ExecutorService newFixedThreadPool(int corePoolSize) {
        // 创建自定义线程工厂
        ThreadFactory threadFactory = r -> {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            // 设置线程名称包含TTL线程池标识
            thread.setName(TTL_EXECUTOR_IDENTIFIER + "-" + thread.getName());
            return thread;
        };
        return TtlExecutors.getTtlExecutorService(Executors.newFixedThreadPool(corePoolSize, threadFactory));
    }

    // 静态初始化块，用于创建并配置共享TTL线程池
    static {
        // 创建自定义线程工厂，为共享线程池的线程添加特殊标识
        ThreadFactory sharedThreadFactory = r -> {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            // 设置线程名称包含共享TTL线程池标识
            thread.setName(TTL_EXECUTOR_IDENTIFIER + "-" + thread.getName());
            return thread;
        };
        
        // 使用自定义线程工厂创建线程池，并用TTL包装
        SHARED_TTL_EXECUTOR = TtlExecutors.getTtlExecutorService(
                Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE, sharedThreadFactory));
    }
    
    /**
     * 获取默认的共享TTL线程池
     * <p>
     * 注意：由于使用固定大小线程池，长期闲置时可能造成资源浪费，建议根据业务需求调整实现方式
     * </p>
     * @return 默认的TTL包装线程池实例（单例）
     */
    public static ExecutorService getSharedTtlExecutor() {
        return SHARED_TTL_EXECUTOR;
    }

    /**
     * 创建缓存的TTL线程池
     * <p>
     * 注意： 该线程池使用缓存线程池，长期闲置时线程池会自动回收，建议根据业务需求调整实现方式
     * 注意：此方法创建的线程池不是共享TTL线程池，无法获取上下文
     * </p>
     * @return 默认的TTL包装线程池实例
     */
    public static ExecutorService newCachedThreadPool() {
        // 创建自定义线程工厂
        ThreadFactory threadFactory = r -> {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            // 设置线程名称包含TTL线程池标识
            thread.setName(TTL_EXECUTOR_IDENTIFIER + "-" + thread.getName());
            return thread;
        };
        return TtlExecutors.getTtlExecutorService(Executors.newCachedThreadPool(threadFactory));
    }

    /**
     * 创建调度TTL线程池
     * <p>
     * 注意： 该线程池使用调度线程池，建议根据业务需求调整实现方式
     * 注意：此方法创建的线程池不是共享TTL线程池，无法获取上下文
     * </p>
     * @return 默认的TTL包装线程池实例
     */
    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
        // 创建自定义线程工厂
        ThreadFactory threadFactory = r -> {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            // 设置线程名称包含TTL线程池标识
            thread.setName(TTL_EXECUTOR_IDENTIFIER + "-" + thread.getName());
            return thread;
        };
        return TtlExecutors.getTtlScheduledExecutorService(
                Executors.newScheduledThreadPool(corePoolSize, threadFactory)
        );
    }

}
