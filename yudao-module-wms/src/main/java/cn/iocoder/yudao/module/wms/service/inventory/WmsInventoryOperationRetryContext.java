package cn.iocoder.yudao.module.wms.service.inventory;

/** 标记当前线程是否正在执行补偿重试，避免重试失败时再次创建重复任务。 */
public final class WmsInventoryOperationRetryContext {

    private static final ThreadLocal<Boolean> RETRYING = ThreadLocal.withInitial(() -> false);

    private WmsInventoryOperationRetryContext() {
    }

    public static boolean isRetrying() {
        return RETRYING.get();
    }

    public static void run(Runnable action) {
        boolean previous = RETRYING.get();
        RETRYING.set(true);
        try {
            action.run();
        } finally {
            RETRYING.set(previous);
        }
    }
}
