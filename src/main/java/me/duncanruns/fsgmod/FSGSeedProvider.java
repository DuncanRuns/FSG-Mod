package me.duncanruns.fsgmod;

import me.duncanruns.fsgmod.screen.FilteringScreen;
import me.voidxwalker.autoreset.Atum;
import me.voidxwalker.autoreset.api.seedprovider.AtumWaitingScreen;
import me.voidxwalker.autoreset.api.seedprovider.SeedProvider;
import net.minecraft.client.MinecraftClient;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class FSGSeedProvider implements SeedProvider {
    // To be replaced by a better system later
    ThreadLocal<CompletableFuture<String>> sftl = new ThreadLocal<>();

    @Override
    public Optional<String> getSeed() {
        CompletableFuture<String> sf = sftl.get();
        if (sf != null && sf.isDone()) {
            sftl.remove();
            try {
                return Optional.of(sf.join());
            } catch (Exception e) {
                FSGMod.logError("Failed to get seed from completable future", e);
                return Optional.of("");
            }
        }

        sf = new CompletableFuture<>();
        SeedManager.requestSeed(MinecraftClient.getInstance().isOnThread(), sf);
        sftl.set(sf);
        return Optional.empty();
    }

    @Override
    public boolean shouldShowSeed() {
        return false;
    }

    @Override
    public void waitForSeed() {
        sftl.get().join();
    }

    @Override
    public AtumWaitingScreen getWaitingScreen() {
        return new FilteringScreen();
    }

    public static CompletableFuture<String> getSeedFuture() {
        return ((FSGSeedProvider) Atum.getSeedProvider()).sftl.get();
    }
}
