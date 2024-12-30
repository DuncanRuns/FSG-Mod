package me.duncanruns.fsgmod.compat;

import me.contaria.seedqueue.SeedQueue;

class SeedQueueCompat {
    static int getMaxCapacity() {
        return SeedQueue.config.maxCapacity;
    }

    static int getTotalEntries() {
        return SeedQueue.getEntries().size();
    }

    static void clampMaxCapacity(int max) {
        SeedQueue.config.maxCapacity = Math.min(max, SeedQueue.config.maxCapacity);
    }

    static int getMaxConcurrently() {
        return SeedQueue.config.maxConcurrently;
    }

    static int getMaxConcurrently_onWall() {
        return SeedQueue.config.maxConcurrently_onWall;
    }
}
