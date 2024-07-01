package me.duncanruns.fsgmod;

import org.apache.logging.log4j.Level;

import java.util.concurrent.atomic.AtomicBoolean;

public final class SeedManager {
    private static final AtomicBoolean findingSeed = new AtomicBoolean(false);
    private static final AtomicBoolean seedExists = new AtomicBoolean(false);
    private static final AtomicBoolean failed = new AtomicBoolean(false);
    private static FSGFilterResult currentResult;
    private static final Object LOCK = new Object();

    private SeedManager() {
    }

    public static boolean canTake() {
        return seedExists.get();
    }

    public static boolean isFindingSeed() {
        return findingSeed.get();
    }

    public static boolean hasFailed() {
        return failed.get();
    }

    public static void find() {
        synchronized (LOCK) {
            findInternal();
        }
    }

    private static void findInternal() {
        if (isFindingSeed()) {
            return;
        }
        findingSeed.set(true);
        new Thread(() -> {
            FSGFilterResult out;
            try {
                out = FSGRunner.runFilter();
            } catch (Exception e) {
                FSGMod.LOGGER.error(e);
                failed.set(true);
                out = null;
            } finally {
                findingSeed.set(false);
            }
            if (!failed.get() && out == null) {
                failed.set(true);
                FSGMod.LOGGER.error("Error: The specified process did not return a seed!");
            }
            if (!failed.get()) {
                FSGMod.LOGGER.log(Level.INFO, "Found seed!");
                currentResult = out;
                seedExists.set(true);
            }
        }, "seed-finder").start();
    }

    public static FSGFilterResult take() {
        seedExists.set(false);
        return currentResult;
    }

    public static void acknowledgeFail() {
        failed.set(false);
    }
}
