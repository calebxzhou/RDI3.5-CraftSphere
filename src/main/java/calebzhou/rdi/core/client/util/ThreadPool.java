package calebzhou.rdi.core.client.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPool {
    private final static ExecutorService exe = Executors.newCachedThreadPool();
    public static void newThread(Runnable runnable){
        exe.execute(runnable);
    }

}
