package calebxzhou.libertorch.util

import java.util.concurrent.Executors

object ThreadPool {
    private val exe = Executors.newCachedThreadPool()
    fun run(runnable: Runnable) {
        exe.execute(runnable)
    }
}
