package me.duncanruns.fsgmod;

import me.duncanruns.fsgmod.compat.ModCompat;
import me.voidxwalker.autoreset.Atum;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class SeedManager {

    private static Queue<FSGFilterResult> resultQueue = new ConcurrentLinkedQueue<>();
    private static final Queue<FSGFilterResult> resultCache = new ConcurrentLinkedQueue<>();
    public static int currentlyFiltering = 0;

    private SeedManager() {
    }

    public static void clear() {
        synchronized (SeedManager.class) {
            resultQueue = new ConcurrentLinkedQueue<>();
        }
    }

    public static void waitForSeed() {
        Object queueAtStart;
        synchronized (SeedManager.class) {
            queueAtStart = resultQueue;
        }
        while (!hasSeed()) {
            synchronized (SeedManager.class) {
                if (queueAtStart != resultQueue) {
                    return;
                }
                kick(currentlyFiltering == 0);
            }
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Kicks the seed manager into generating seeds.
     */
    private static synchronized void kick(boolean forceOne) {
        if (ModCompat.HAS_SEEDQUEUE) {
            int maxCapacity = MathHelper.clamp(FSGModConfig.getInstance().maxGenerating, 1, ModCompat.seedqueue$getMaxCapacity());
            ModCompat.seedqueue$clampMaxCapacity(maxCapacity);
            int maxGenerating = Math.min(maxCapacity, Math.max(ModCompat.seedqueue$getMaxConcurrently_onWall(), ModCompat.seedqueue$getMaxConcurrently()));

            int toGenerate = Math.min(maxCapacity - ModCompat.seedqueue$getTotalEntries() - resultQueue.size(), maxGenerating) - currentlyFiltering;
            toGenerate = Math.max(toGenerate, forceOne ? 1 : 0);
            if (toGenerate == 0) return;
            FSGMod.LOGGER.info("Starting {} filtering threads...", toGenerate);
            startNewFilterThreads(toGenerate);
        } else {
            FSGMod.LOGGER.info("Starting 1 filtering threads...");
            startNewFilterThreads(1);
        }
    }

    private static synchronized void startNewFilterThreads(int total) {
        for (int i = 0; i < total; i++) {
            currentlyFiltering++;
            startNewFilterThread();
        }
    }

    private static void startNewFilterThread() {
        new Thread(() -> {
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
                    onFail();
                }
                return;
            }
            synchronized (SeedManager.class) {
                resultCache.add(result);
                queueToUse.add(result);
                while (resultCache.size() > 200) resultCache.remove();
                currentlyFiltering--;
                if (Atum.isRunning()) kick(false);
            }
        }, "filter-thread").start();

    }

    private static synchronized void onFail() {
        clear();
        MinecraftClient.getInstance().execute(() -> {
            Atum.stopRunning();
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world != null && client.player != null) {
                client.inGameHud.getChatHud().addMessage(Text.method_30163("(FSG Mod) Filtering has failed!").copy().styled(style -> style.withColor(Formatting.RED).withColor(Formatting.BOLD)));
            }
        });
    }

    public static boolean hasSeed() {
        kick(false);
        return !resultQueue.isEmpty();
    }

    public static FSGFilterResult getResult() {
        long currentTime = System.currentTimeMillis();
        synchronized (SeedManager.class) {
            resultQueue.removeIf(result -> Math.abs(result.generationTime - currentTime) > 60_000);
        }
        return resultQueue.poll();
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
}
