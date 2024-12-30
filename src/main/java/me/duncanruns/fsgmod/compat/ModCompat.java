package me.duncanruns.fsgmod.compat;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Intermediate class that allows safe access to other mods methods/fields from code paths that may be run without the mod present.
 * This class provides wrapper methods for compat classes which should only be classloaded if the mod in question is loaded.
 * <p>
 * Original by KingContaria: <a href="https://github.com/KingContaria/seedqueue/blob/main/src/main/java/me/contaria/seedqueue/compat/ModCompat.java">me.contaria.seedqueue.compat.ModCompat</a>
 */
public class ModCompat {
    public static final boolean HAS_SEEDQUEUE = FabricLoader.getInstance().isModLoaded("seedqueue");

    public static int seedqueue$getMaxCapacity() {
        return HAS_SEEDQUEUE ? SeedQueueCompat.getMaxCapacity() : 1;
    }

    public static int seedqueue$getTotalEntries() {
        return HAS_SEEDQUEUE ? SeedQueueCompat.getTotalEntries() : 0;
    }

    public static void seedqueue$clampMaxCapacity(int max) {
        if (!HAS_SEEDQUEUE) return;
        SeedQueueCompat.clampMaxCapacity(max);
    }

    public static int seedqueue$getMaxConcurrently() {
        return HAS_SEEDQUEUE ? SeedQueueCompat.getMaxConcurrently() : -1;
    }

    public static int seedqueue$getMaxConcurrently_onWall() {
        return HAS_SEEDQUEUE ? SeedQueueCompat.getMaxConcurrently_onWall() : -1;
    }
}