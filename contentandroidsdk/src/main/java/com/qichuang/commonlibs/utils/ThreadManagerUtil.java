package com.qichuang.commonlibs.utils;

/**
 * @author Created by SXF on 2021/8/2 11:01 AM.
 * 线程池管理的工具类，封装类
 */

import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadManagerUtil {

    //通过ThreadPoolExecutor的代理类来对线程池的管理
    private static ThreadPollProxy mThreadPollProxy;
    private static int             DEFAULT_KEEP_TIME = 1 * 1000;//1秒
    private static int             DEFAULT_MAX_VALUE = Integer.MAX_VALUE;
    private static int             DEFAULT_CORE_SIZE = 0;

    //单列对象
    public static ThreadPollProxy getThreadPollProxy (int corePoolSize, int maximumPoolSize, int keepAliveTime) {
        synchronized (ThreadPollProxy.class) {
            if (mThreadPollProxy == null) {
                mThreadPollProxy = new ThreadPollProxy(corePoolSize, maximumPoolSize, keepAliveTime);
            }
        }
        return mThreadPollProxy;
    }

    //默认单列对象
    public static ThreadPollProxy getDefaultProxy () {
        return getThreadPollProxy(DEFAULT_CORE_SIZE, DEFAULT_MAX_VALUE, DEFAULT_KEEP_TIME);
    }

    //通过ThreadPoolExecutor的代理类来对线程池的管理
    public static class ThreadPollProxy {
        private ThreadPoolExecutor poolExecutor;//线程池执行者 ，java内部通过该api实现对线程池管理
        private int                corePoolSize;
        private int                maximumPoolSize;
        private long               keepAliveTime;

        public ThreadPollProxy (int corePoolSize, int maximumPoolSize, long keepAliveTime) {
            this.corePoolSize = corePoolSize;
            this.maximumPoolSize = maximumPoolSize;
            this.keepAliveTime = keepAliveTime;
        }

        //对外提供一个执行任务的方法
        public void execute (Runnable r) {
            if (poolExecutor == null || poolExecutor.isShutdown()) {
                poolExecutor = new ThreadPoolExecutor(
                        //核心线程数量
                        corePoolSize,
                        //最大线程数量
                        maximumPoolSize,
                        //当线程空闲时，保持活跃的时间
                        keepAliveTime,
                        //时间单元 ，毫秒级
                        TimeUnit.MILLISECONDS,
                        //线程任务队列
                        new SynchronousQueue<Runnable>(),
                        //创建线程的工厂
                        Executors.defaultThreadFactory());
            }
            poolExecutor.execute(r);
        }
    }
}
