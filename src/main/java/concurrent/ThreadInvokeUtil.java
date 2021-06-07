package concurrent;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dataochen
 * @Description 异步执行工具类
 * @date: 2020/11/5 15:48
 */
public class ThreadInvokeUtil {

    private volatile static ThreadInvokeUtil instance;

    private ThreadInvokeUtil() {
    }

    /**
     * 使用单例 防止使用此工具的人每次都不传线程池，导致每次都创建线程池。也为了方便管理。
     *
     * @return
     */
    public static ThreadInvokeUtil getInstance() {
        return getInstance(null);
    }

    public static ThreadInvokeUtil getInstance(Executor executor) {
        if (null == instance) {
            synchronized (ThreadInvokeUtil.class) {
                if (null == instance) {
                    instance = new ThreadInvokeUtil(executor);
                }
            }
        }
        return instance;
    }

    /**
     * 如果自定义了线程池 优先用自定义的 否则使用默认的
     *
     * @param executor
     */
    private ThreadInvokeUtil(Executor executor) {
        if (executor == null) {
            executor = new ThreadPoolExecutor(5, 5, 2000,
                    TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1024),
                    new FastErrorUtilThreadFactory(), (r, e) -> {
                throw new RejectedExecutionException("ThreadInvokeUtilTask " + r.toString() +
                        " rejected from " +
                        e.toString());
            }
            );
        }
        this.executor = executor;
    }

    /**
     * 定义此工具类的线程池的线程名称 便于日志定位和管理
     */
    class FastErrorUtilThreadFactory implements ThreadFactory {
        private final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        FastErrorUtilThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "ThreadInvokeUtilPool-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

    private Executor executor;

    public void call(Runnable runnable) {
        executor.execute(runnable);
    }
}
