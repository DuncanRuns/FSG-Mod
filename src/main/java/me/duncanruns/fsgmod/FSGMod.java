package me.duncanruns.fsgmod;

import me.duncanruns.fsgmod.util.ArchUtil;
import me.voidxwalker.autoreset.Atum;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;

public class FSGMod implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("fsg-mod");
    public static final String VERSION = FabricLoader.getInstance().getModContainer("fsg-mod").get().getMetadata().getVersion().getFriendlyString();

    public static final boolean DEBUG = false;

    public static void logError(String message, Throwable t) {
        LOGGER.error(message, t);
    }

    public static void setAllInFolderExecutable() throws IOException {
        Files.walk(LocalFilter.getFsgDir()).filter(Files::isRegularFile).forEach(path -> path.toFile().setExecutable(true));
    }

    public static String getOS3LetterCode() {
        switch (Util.getOperatingSystem()) {
            case WINDOWS:
                return "win";
            case OSX:
                return "mac";
            default:
                return "lin";
        }
    }

    public static boolean onArm() {
        return ArchUtil.getArch() == ArchUtil.Arch.ARM;
    }

    public static boolean filterSelectedOrInstalled() {
        return !FSGModConfig.getInstance().selectedOnlineFilters.isEmpty() || LocalFilter.isInstalled();
    }

    public static int getMaxGenerating() throws IOException {
        if (!filterSelectedOrInstalled()) return 30;
        if (LocalFilter.isInstalled()) return LocalFilter.getMaxGenerating();
        return FSGOnlineDB.getMaxGenerating(FSGModConfig.getInstance().selectedOnlineFilters).join();
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing");
        FSGModConfig.tryLoad();
        FSGModConfig.trySave();

        if (LocalFilter.isInstalled() && !LocalFilter.isValid()) {
            try {
                LocalFilter.writeData(1, "Unknown Filter");
            } catch (IOException e) {
                logError("Failed to write local filter data!", e);
            }
        }

        Atum.setSeedProvider(new FSGSeedProvider());

        FSGOnlineDB.getFilters().exceptionally(throwable -> {
            FSGMod.logError("Failed to load filters!", throwable);
            return null;
        });
    }
}