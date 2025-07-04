package me.duncanruns.fsgmod;

import me.duncanruns.fsgmod.compat.ModCompat;
import me.duncanruns.fsgmod.util.SeedUtil;
import me.voidxwalker.autoreset.Atum;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Queue;
import java.util.concurrent.*;

public class SeedManager {
    private static boolean running = false;
    public static boolean filtering = false;

    private static CompletableFuture<String> mainThreadSF = null;
    private static CompletableFuture<String> sqThreadSF = null;

    private static final Queue<FSGFilterResult> resultCache = new ConcurrentLinkedQueue<>();

    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    public static void tick() {
        synchronized (SeedManager.class) {
            if (!running) return;
            if (!Atum.isRunning()) {
                clearStaleFutures();
                if (mainThreadSF != null) mainThreadSF.cancel(true);
                if (sqThreadSF != null) sqThreadSF.cancel(true);
                mainThreadSF = null;
                sqThreadSF = null;
                running = false;
                return;
            }
        }
        int maxGenerating;
        try {
            maxGenerating = FSGMod.getMaxGenerating();
        } catch (IOException e) {
            completeFailureAll(e);
            return;
        }

        if (mainThreadSF == null && sqThreadSF == null) return;

        ModCompat.seedqueue$clampMaxCapacity(maxGenerating);

        FSGFilterResult result;
        try {
            filtering = true;
            result = runFilter();
            filtering = false;
        } catch (IOException | InterruptedException e) {
            filtering = false;
            synchronized (SeedManager.class) {
                FSGMod.logError("Failed to run filter!", e);
                completeFailure(e);
            }
            return;
        }

        if (result == null) return;

        synchronized (SeedManager.class) {
            resultCache.add(result);
            while (resultCache.size() > 200) resultCache.remove();
            if (mainThreadSF != null) {
                mainThreadSF.complete(result.seed);
                mainThreadSF = null;
            } else if (sqThreadSF != null) {
                sqThreadSF.complete(result.seed);
                sqThreadSF = null;
            } else {
                completeFailure(new IllegalStateException("No request"));
            }
        }
    }

    private static synchronized void clearStaleFutures() {
        if (mainThreadSF != null && (mainThreadSF.isDone() || mainThreadSF.isCancelled() || mainThreadSF.isCompletedExceptionally())) {
            mainThreadSF = null;
        }
        if (sqThreadSF != null && (sqThreadSF.isDone() || sqThreadSF.isCancelled() || sqThreadSF.isCompletedExceptionally())) {
            sqThreadSF = null;
        }
    }

    private static synchronized void completeFailure(Exception e) {
        clearStaleFutures();
        if (mainThreadSF != null) mainThreadSF.completeExceptionally(e);
        else if (sqThreadSF != null) sqThreadSF.completeExceptionally(e);
        else if (Atum.isRunning()) Atum.SEED_FAILURES.add(e);
    }

    private static synchronized void completeFailureAll(Exception e) {
        clearStaleFutures();
        if (mainThreadSF != null) mainThreadSF.completeExceptionally(e);
        if (sqThreadSF != null) sqThreadSF.completeExceptionally(e);
        else if (mainThreadSF == null && Atum.isRunning()) Atum.SEED_FAILURES.add(e);
    }

    public static synchronized void requestSeed(boolean mainThread, CompletableFuture<String> sf) {
        clearStaleFutures();
        if (mainThread && mainThreadSF == null) {
            mainThreadSF = sf;
            onRequest();
        } else if (!mainThread && sqThreadSF == null) {
            sqThreadSF = sf;
            onRequest();
        } else {
            sf.completeExceptionally(new IllegalStateException("Already have a request"));
        }
    }

    private static void onRequest() {
        running = true;
        EXECUTOR.execute(SeedManager::tick);
    }

    @Nullable
    public static FSGFilterResult runFilter() throws IOException, InterruptedException {
        if (!FSGMod.filterSelectedOrInstalled()) throw new IOException("No filter installed!");
        if (LocalFilter.isInstalled()) {
            return LocalFilter.run();
        }
        return FSGOnlineDB.runFilterOnline(FSGModConfig.getInstance().selectedOnlineFilters);
    }

    public static void start() {
        EXECUTOR.scheduleWithFixedDelay(SeedManager::tick, 0, 1, TimeUnit.SECONDS);
    }

    public static void stop() {
        EXECUTOR.shutdownNow();
    }

    public static Optional<FSGFilterResult> getResultForSeed(long seed) {
        return resultCache.stream().filter(result -> {
            OptionalLong optionalLong = SeedUtil.getSeedFromString(result.seed);
            if (!optionalLong.isPresent()) return false;
            return optionalLong.getAsLong() == seed;
        }).findAny();
    }
}
