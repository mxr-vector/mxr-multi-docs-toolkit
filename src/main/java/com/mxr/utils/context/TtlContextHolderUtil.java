package com.mxr.utils.context;


import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.threadpool.TtlExecutors;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 基于TransmittableThreadLocal的线程上下文管理工具类
 * <p>
 * 提供线程变量的安全存储、传递和线程池包装能力，支持父子线程间的上下文透传
 * </p>
 * @see TransmittableThreadLocal
 */
@Slf4j
public class TtlContextHolderUtil {

    /**
     * 保存请求线程ID的ThreadLocal
     */
    private static final ThreadLocal<Long> REQUEST_THREAD_ID = new ThreadLocal<>();
    
    /**
     * 线程上下文容器，使用TransmittableThreadLocal实现跨线程池的值传递
     * <p>
     * 初始化为空值的BaseContextImpl对象，避免空指针异常
     * </p>
     */
    private static final TransmittableThreadLocal<BaseContext<Object>> TTL = new TransmittableThreadLocal<>() {
        @Override
        protected BaseContext<Object> initialValue() {
            return new BaseContextImpl<>();
        }
    };
    /**
     * 私有构造方法，防止工具类被实例化
     * @throws UnsupportedOperationException 当尝试反射创建实例时抛出
     */
    private TtlContextHolderUtil() {
        throw new UnsupportedOperationException("TtlContextHolder类禁止实例化");
    }

    /**
     * 设置当前线程为请求线程
     * 在Controller方法开始时调用此方法标记当前线程为请求线程
     */
    public static void markAsRequestThread() {
        REQUEST_THREAD_ID.set(Thread.currentThread().threadId());
    }
    
    /**
     * 清除请求线程标记
     * 在请求结束时调用，防止内存泄漏
     */
    public static void clearRequestThreadMark() {
        REQUEST_THREAD_ID.remove();
    }
    
    /**
     * 判断当前线程是否为请求线程
     * @return 如果是请求线程返回true，否则返回false
     */
    public static boolean isRequestThread() {
        Long requestThreadId = REQUEST_THREAD_ID.get();
        return requestThreadId != null && requestThreadId == Thread.currentThread().threadId();
    }

    /**
     * 设置当前线程上下文对象
     * <p>
     * 当传入非空值时更新上下文，空值时保持原有上下文不变
     * </p>
     * @param context 新的上下文对象，需继承自BaseContext
     */
    public static void setContext(BaseContext<Object> context) {
        if (context != null) TTL.set(context);
    }

    /**
     * 清除当前线程的上下文数据
     * <p>
     * 调用后该线程后续获取的上下文将返回initialValue()初始化的对象
     * </p>
     */
    public static void clearContext() {
        TTL.remove();
    }


    /**
     * 获取当前线程上下文（使用共享线程池）
     * <p>
     * 主线程调用时返回初始空值上下文对象
     * </p>
     * @return 当前线程关联的上下文对象
     * @see #getContext(ExecutorService)
     * @throws UnsupportedOperationException 当线程不是主线程且不是由getSharedTtlExecutor()创建的线程池时抛出
     */
    public static BaseContext<Object> getContext() {
        // 如果是请求线程，直接返回上下文
        if (isRequestThread()) {
            return TTL.get();
        }
        
        // 检查当前线程是否来自共享TTL线程池
        if (!isThreadFromTtlExecutor()) {
            throw new UnsupportedOperationException("当前线程不是由TTLExecutorFactory类创建的线程池提交的任务，无法获取上下文。请使用TTLExecutorFactory创建的线程池提交任务。");
        }
        
        return TTL.get();
    }

    /**
     * 获取指定线程池的上下文对象
     * <p>
     * 该方法会校验线程池是否经过TTL包装，并且必须是由getSharedTtlExecutor()创建的线程池
     * </p>
     * @param executorService 目标线程池实例
     * @return 当前线程的上下文对象
     * @throws UnsupportedOperationException 当线程池未经过TTL包装或不是由getSharedTtlExecutor()创建时抛出
     */
    public static BaseContext<Object> getContext(ExecutorService executorService) {
        // 如果是请求线程，直接返回上下文
        if (isRequestThread()) {
            return TTL.get();
        }
        
        // 检查线程池是否为TTL包装的线程池
        if (executorService != null && !TtlExecutors.isTtlWrapper(executorService)) {
            throw new UnsupportedOperationException("线程池必须是TTL包装的线程池，请使用TTLExecutorFactory.getTtlExecutorService()方法包装线程池");
        }

        // 检查当前线程是否来自TTLExecutorFactory创建的线程池
        if (!isThreadFromTtlExecutor()) {
            throw new UnsupportedOperationException("当前线程不是由TTLExecutorFactory类创建的线程池提交的任务，无法获取上下文。请使用TTLExecutorFactory创建的线程池提交任务。");
        }
        
        return TTL.get();
    }


    /**
     * 检查当前线程是否来自共享TTL线程池
     * <p>
     * 通过检查线程名称判断是否由getSharedTtlExecutor()创建的线程池提交的任务
     * </p>
     * @return 如果是共享TTL线程池创建的线程返回true，否则返回false
     */
    private static boolean isThreadFromTtlExecutor() {
        Thread currentThread = Thread.currentThread();
        
        // 如果是请求线程，直接返回true
        if (isRequestThread()) {
            return true;
        }
        
        // 获取当前线程的名称信息
        String threadName = currentThread.getName();
        
        // 检查线程是否属于共享TTL线程池
        // 直接检查线程名称是否包含共享TTL线程池的标识前缀
        // 由于在TTLExecutorFactory中，我们为共享线程池的线程设置了特殊的名称前缀
        return threadName.startsWith(TTLExecutorFactory.TTL_EXECUTOR_IDENTIFIER);
    }
    
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 模拟标记当前线程为请求线程
        markAsRequestThread();
        
        ExecutorService sharedTtlExecutor = TTLExecutorFactory.getSharedTtlExecutor();
        // 测试主线程传递子线程
        TtlContextHolderUtil.getContext().addProperty("test", "test");
        TtlContextHolderUtil.getContext().addProperty("x", "x");
        log.info("=========测试TTL线程池处理（正常情况）============");
        sharedTtlExecutor.submit(() -> {
            log.info("TTL线程池 - 子线程的结果: test -> {}", TtlContextHolderUtil.getContext().getProperty("test"));
            log.info("TTL线程池 - 子线程的结果: x -> {}", TtlContextHolderUtil.getContext().getProperty("x"));
        }).get();
        log.info("主线程确认test结果不变 -> {}", TtlContextHolderUtil.getContext().getProperty("test"));
        log.info("主线程确认x结果不变 -> {}", TtlContextHolderUtil.getContext().getProperty("x"));


        log.info("***************测试普通线程池1（异常情况）************");
        // 测试普通线程池
        ExecutorService commonExecutorService = Executors.newFixedThreadPool(1);
        
        // 验证常规线程池内调用无参getContext()的情况
        try {
            commonExecutorService.submit(() -> {
                try {
                    log.info("普通线程池 - 调用无参getContext()方法，应当抛出异常");
                    BaseContext<Object> context = TtlContextHolderUtil.getContext();
                    log.info("如果看到这条消息，说明测试失败，没有抛出预期的异常");
                } catch (UnsupportedOperationException e) {
                    log.info("测试成功 - 捕获到预期异常: {}", e.getMessage());
                }
            }).get();
        } catch (Exception e) {
            log.error("测试过程中发生异常: {}", e.getMessage());
        }

        
        // 验证常规线程池内调用有参getContext()的情况
        log.info("***************测试普通线程池2（异常情况）************");
        try {
            commonExecutorService.submit(() -> {
                try {
                    log.info("普通线程池 - 调用有参getContext()方法，应当抛出异常");
                    BaseContext<Object> context = TtlContextHolderUtil.getContext(commonExecutorService);
                    log.info("如果看到这条消息，说明测试失败，没有抛出预期的异常");
                } catch (UnsupportedOperationException e) {
                    log.info("测试成功 - 捕获到预期异常: {}", e.getMessage());
                }
            }).get();
        } catch (Exception e) {
            log.error("测试过程中发生异常: {}", e.getMessage());
        }
        
        // 测试使用TTL包装线程池的情况
        log.info("***************测试ttl线程池（异常情况） ************");
        try {
            ExecutorService ttlWrappedExecutor = TtlExecutors.getTtlExecutorService(Executors.newFixedThreadPool(1));
            ttlWrappedExecutor.submit(() -> {
                try {
                    log.info("TTL包装其余线程池 - 调用有参getContext()方法");
                    BaseContext<Object> context = TtlContextHolderUtil.getContext(ttlWrappedExecutor);
                    log.info("如果看到这条消息，说明测试失败");
                } catch (UnsupportedOperationException e) {
                    log.info("测试成功，解释：这样做，为了单纯提示用户必须使用工厂的ttl保证掉坑，舍弃了原生ttl创建方式。捕获到预期异常: {}", e.getMessage());
                }
            }).get();
        } catch (Exception e) {
            log.error("测试过程中发生异常: {}", e.getMessage());
        }
        
        log.info("***************测试完成************");
        
        // 清理资源
        clearRequestThreadMark();
        clearContext();
        
        // 关闭线程池
        commonExecutorService.shutdown();
        // 不关闭共享线程池，因为它是单例的
    }
}

