package me.duncanruns.fsgmod;

import me.duncanruns.fsgmod.screen.FilteringScreen;
import me.voidxwalker.autoreset.api.seedprovider.AtumWaitingScreen;
import me.voidxwalker.autoreset.api.seedprovider.SeedProvider;

import java.util.Optional;

class FSGSeedProvider implements SeedProvider {
    @Override
    public Optional<String> getSeed() {
        return SeedManager.hasSeed() ? Optional.of(SeedManager.getResult().seed) : Optional.empty();
    }

    @Override
    public boolean shouldShowSeed() {
        return false;
    }

    @Override
    public void waitForSeed() {
        SeedManager.waitForSeed();
    }

    @Override
    public AtumWaitingScreen getWaitingScreen() {
        return new FilteringScreen();
    }
}
