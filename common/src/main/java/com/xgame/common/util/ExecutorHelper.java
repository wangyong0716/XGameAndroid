package com.xgame.common.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.NonNull;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.xgame.common.var.LazyVarHandle;
import com.xgame.common.var.VarHandle;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public final class ExecutorHelper {

    private static final String TAG = ExecutorHelper.class.getSimpleName();

    private static final int KEEP_ALIVE = 10;

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    private static final int KEEP_POOL_SIZE = (CPU_COUNT < 4 ? 0 : CPU_COUNT / 2 + 1);

    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;

    private static final VarHandle<Handler> sUIHandlerVar = new LazyVarHandle<Handler>() {
        @Override
        protected Handler constructor() {
            return new Handler(Looper.getMainLooper());
        }
    };

    private static final int TASK_QUEUE_CAPACITY = 20;

    private static final Comparator<? super Runnable> sPriorityComparator
            = new PriorityComparator();

    private static final VarHandle<ExecutorService> sSingleWorkVar
            = new LazyVarHandle<ExecutorService>() {
        @Override
        protected ExecutorService constructor() {
            return createSingleThreadPool("WorkThread");
        }
    };

    private static final ThreadPoolExecutor sCacheThreadPool
            = new InnerThreadPoolExecutor(
            KEEP_POOL_SIZE,
            MAXIMUM_POOL_SIZE,
            KEEP_ALIVE,
            TimeUnit.SECONDS,
            new PriorityBlockingQueue<>(TASK_QUEUE_CAPACITY, sPriorityComparator),
            getThreadFactory("CacheThread", THREAD_PRIORITY_BACKGROUND - 2),
            new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    sSingleWorkVar.get().execute(r);
                }
            });

    private ExecutorHelper() {
    }

    private static ThreadFactory getWorkerThreadFactory(String name) {
        return getThreadFactory(name, THREAD_PRIORITY_BACKGROUND);
    }

    private static ThreadFactory getThreadFactory(final String factoryName, final int priority) {
        return new ThreadFactory() {
            @Override
            public Thread newThread(@NonNull Runnable runnable) {
                return new InnerThread(priority, factoryName, runnable);
            }
        };
    }

    public static ExecutorService getCacheThreadPool() {
        return sCacheThreadPool;
    }

    public static ExecutorService createThreadPool(String threadName) {
        return new ThreadPoolExecutor(0, 3,
                1, TimeUnit.MINUTES,
                new LinkedBlockingDeque<Runnable>(),
                getWorkerThreadFactory(threadName),
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        LogUtil.i(TAG,
                                "runnable %s is rejected by current pool %s", r, executor);
                    }
                });
    }

    public static ExecutorService createSingleThreadPool(String threadName) {
        return new ThreadPoolExecutor(0, 1,
                1, TimeUnit.MINUTES,
                new LinkedBlockingDeque<Runnable>(),
                getWorkerThreadFactory(threadName), new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                LogUtil.i(TAG, "runnable %s is rejected by current pool %s", r, executor);
            }
        });
    }

    public static void runInUIThread(Runnable task) {
        sUIHandlerVar.get().post(task);
    }

    public static void runInUIThread(Runnable task, long millis) {
        sUIHandlerVar.get().postDelayed(task, millis);
    }

    public static void removeFromUIThread(Runnable task) {
        sUIHandlerVar.get().removeCallbacks(task);
    }

    public static void runInWorkerThread(Runnable task) {
        sSingleWorkVar.get().execute(new TaskFuture(task));
    }

    public static void runInBackground(Runnable task) {
        runInBackground(task, Priority.LOW);
    }

    public static void runInBackground(Runnable task, @Priority int priority) {
        sCacheThreadPool.submit(new TaskFuture(task, priority));
    }

    private static class InnerThreadPoolExecutor extends ThreadPoolExecutor {

        InnerThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                TimeUnit unit, BlockingQueue<Runnable> workQueue,
                ThreadFactory threadFactory, RejectedExecutionHandler handler) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                    threadFactory, handler);
        }

        @NonNull
        @Override
        public Future<?> submit(Runnable task) {
            if (!(task instanceof TaskFuture)) {
                task = new TaskFuture(task, Priority.NORMAL);
            }
            this.execute(task);
            return (Future<?>) task;
        }
    }


    private static class InnerThread extends Thread {

        private final int nPriority;

        InnerThread(int priority, String namePrefix, Runnable target) {
            super(target);
            nPriority = priority;
            String name = namePrefix + "-" + getId();
            setName(name);
        }

        @Override
        public void run() {
            Process.setThreadPriority(nPriority);
            super.run();
        }
    }

    private static class TaskFuture<V> extends FutureTask<V>
            implements Comparable<TaskFuture> {

        private int nPriority;

        TaskFuture(Runnable run) {
            this(run, Priority.LOW);
        }

        TaskFuture(Runnable run, @Priority int priority) {
            super(run, null);
            nPriority = priority;
        }

        @Override
        public int compareTo(@NonNull TaskFuture other) {
            return this.nPriority - other.nPriority;
        }
    }

    private static class PriorityComparator implements Comparator<Runnable> {

        @Override
        public int compare(Runnable first, Runnable second) {
            if (first instanceof TaskFuture && second instanceof TaskFuture) {
                return ((TaskFuture) first).compareTo((TaskFuture) second);
            }
            return first instanceof TaskFuture ? 1 : (second instanceof TaskFuture ? -1 : 0);
        }
    }
}
