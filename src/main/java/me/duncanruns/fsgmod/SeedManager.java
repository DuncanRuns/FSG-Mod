package me.duncanruns.fsgmod;

import me.duncanruns.fsgmod.compat.ModCompat;
import me.voidxwalker.autoreset.Atum;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class SeedManager {

    private static Queue<FSGFilterResult> resultQueue = new ConcurrentLinkedQueue<>();
    private static final Queue<FSGFilterResult> resultCache = new ConcurrentLinkedQueue<>();
    private static int currentlyFiltering = 0;

    private static CompletableFuture<String> mainThreadSF = null;
    private static CompletableFuture<String> sqThreadSF = null;

    private SeedManager() {
    }

    public static int getCurrentlyFiltering() {
        return currentlyFiltering;
    }

    public static synchronized void clear() {
        resultQueue = new ConcurrentLinkedQueue<>();
    }

    /**
     * Kicks the seed manager into generating seeds.
     */
    private static synchronized void kick() {
        if (!ModCompat.HAS_SEEDQUEUE) return;
        if (!Atum.isRunning()) return;

        int filterMaxGenerating;
        try {
            filterMaxGenerating = FSGMod.getMaxGenerating();
        } catch (Exception e) {
            completeFailure(new IOException("Failed to get max generating!", e));
            return;
        }
        int maxCapacity = MathHelper.clamp(filterMaxGenerating, 1, ModCompat.seedqueue$getMaxCapacity());
        ModCompat.seedqueue$clampMaxCapacity(maxCapacity);
        int maxGenerating = Math.min(maxCapacity, Math.max(ModCompat.seedqueue$getMaxConcurrently_onWall(), ModCompat.seedqueue$getMaxConcurrently()));

        int toGenerate = Math.min(maxCapacity - ModCompat.seedqueue$getTotalEntries() - resultQueue.size(), maxGenerating) - currentlyFiltering;
        if (toGenerate <= 0) return;
        FSGMod.LOGGER.info("Starting {} filtering thread{}...", toGenerate, toGenerate > 1 ? "s" : "");
        startNewFilterThreads(toGenerate);
    }

    private static void startNewFilterThreads(int total) {
        for (int i = 0; i < total; i++) {
            startNewFilterThread();
        }
    }

    private static synchronized void startNewFilterThread() {
        currentlyFiltering++;
        Thread thread = new Thread(() -> {
            Queue<FSGFilterResult> queueToUse;
            synchronized (SeedManager.class) {
                queueToUse = resultQueue;
            }
            FSGFilterResult result;
            try {
                result = FSGRunner.runFilter();
            } catch (IOException | InterruptedException e) {
                synchronized (SeedManager.class) {
                    currentlyFiltering--;
                    if (resultQueue != queueToUse) return;
                    FSGMod.logError("Failed to run filter!", e);
                    completeFailure(e);
                }
                return;
            }
            synchronized (SeedManager.class) {
                if (result == null) {
                    currentlyFiltering--;
                    return;
                }
                resultCache.add(result);
                queueToUse.add(result);
                while (resultCache.size() > 200) resultCache.remove();
                currentlyFiltering--;
                onSeedAvailable();
            }
        }, "filter-thread");
        thread.setDaemon(true);
        thread.start();

    }

    private static synchronized void clearStaleFutures() {
        if (mainThreadSF != null && (mainThreadSF.isDone() || mainThreadSF.isCancelled() || mainThreadSF.isCompletedExceptionally())) {
            mainThreadSF = null;
        }
        if (sqThreadSF != null && (sqThreadSF.isDone() || sqThreadSF.isCancelled() || sqThreadSF.isCompletedExceptionally())) {
            sqThreadSF = null;
        }
    }

    private static void completeFailure(Exception e) {
        clearStaleFutures();
        if (mainThreadSF != null) mainThreadSF.completeExceptionally(e);
        else if (sqThreadSF != null) sqThreadSF.completeExceptionally(e);
        else if (Atum.isRunning()) Atum.SEED_FAILURES.add(e);
    }

    private static synchronized void onSeedAvailable() {
        clearStaleFutures();
        if (!hasSeed()) return;
        if (mainThreadSF != null) {
            mainThreadSF.complete(Objects.requireNonNull(resultQueue.poll()).seed);
            mainThreadSF = null;
        }
        if (sqThreadSF != null) {
            sqThreadSF.complete(Objects.requireNonNull(resultQueue.poll()).seed);
            sqThreadSF = null;
        }
    }

    public static boolean hasSeed() {
        long currentTime = System.currentTimeMillis();
        synchronized (SeedManager.class) {
            resultQueue.removeIf(result -> Math.abs(result.generationTime - currentTime) > 60_000);
        }
        kick();
        return !resultQueue.isEmpty();
    }

    public static Optional<FSGFilterResult> getResultForSeed(long seed) {
        return resultCache.stream().filter(result -> {
            OptionalLong optionalLong = getSeedFromString(result.seed);
            if (!optionalLong.isPresent()) return false;
            return optionalLong.getAsLong() == seed;
        }).findAny();
    }

    private static OptionalLong getSeedFromString(String string) {
        OptionalLong optionalLong;
        if (StringUtils.isEmpty(string)) {
            optionalLong = OptionalLong.empty();
        } else {
            OptionalLong optionalLong2 = tryParseLong(string);
            if (optionalLong2.isPresent() && optionalLong2.getAsLong() != 0L) {
                optionalLong = optionalLong2;
            } else {
                optionalLong = OptionalLong.of(string.hashCode());
            }
        }
        return optionalLong;
    }


    private static OptionalLong tryParseLong(String string) {
        try {
            return OptionalLong.of(Long.parseLong(string));
        } catch (NumberFormatException e) {
            return OptionalLong.empty();
        }
    }

    public static synchronized void requestSeed(boolean mainThread, CompletableFuture<String> sf) {
        clearStaleFutures();
        if (hasSeed()) { // hasSeed will also kick it if needed
            sf.complete(Objects.requireNonNull(resultQueue.poll()).seed);
            return;
        } else if (!ModCompat.HAS_SEEDQUEUE && currentlyFiltering == 0) {
            startNewFilterThread();
        }
        if (mainThread) {
            mainThreadSF = sf;
        } else {
            sqThreadSF = sf;
        }
    }
}
