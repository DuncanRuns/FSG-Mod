package me.duncanruns.fsgmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FSGMod implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("fsg-mod");
    public static final Util.OperatingSystem OPERATING_SYSTEM = Util.getOperatingSystem();
    public static final String VERSION = FabricLoader.getInstance().getModContainer("fsg-mod").get().getMetadata().getVersion().getFriendlyString();

    public static int lastTokenHash = 0;

    public static String getLastToken() {
        return FSGModConfig.getInstance().lastToken;
    }

    public static void setLastToken(String lastToken) {
        FSGModConfig.getInstance().lastToken = lastToken;
        lastTokenHash = lastToken.hashCode();
        FSGModConfig.trySave();
    }

    public static boolean shouldRunInBackground() {
        return FSGModConfig.getInstance().runInBackground;
    }

    public static boolean toggleRunInBackground() {
        FSGModConfig.getInstance().runInBackground = !FSGModConfig.getInstance().runInBackground;
        FSGModConfig.trySave();
        return FSGModConfig.getInstance().runInBackground;
    }

    public static Path getFsgDir() {
        return getGameDir().resolve("fsg");
    }

    public static Path getGameDir() {
        return FabricLoader.getInstance().getGameDir().toAbsolutePath();
    }

    public static Path getRunPath() {
        Path runForThisVersionPath = FSGMod.getFsgDir().resolve("run." + SharedConstants.getGameVersion().getName() + (FSGMod.OPERATING_SYSTEM.equals(Util.OperatingSystem.WINDOWS) ? ".bat" : ".sh"));
        if (Files.exists(runForThisVersionPath)) {
            return runForThisVersionPath;
        }
        return FSGMod.getFsgDir().resolve("run" + (FSGMod.OPERATING_SYSTEM.equals(Util.OperatingSystem.WINDOWS) ? ".bat" : ".sh"));
    }

    public static void logError(Throwable t) {
        LOGGER.error(t);
        for (StackTraceElement stackTraceElement : t.getStackTrace()) {
            LOGGER.error(stackTraceElement.toString());
        }
    }

    public static void setAllInFolderExecutable() throws IOException {
        Files.walk(getFsgDir()).filter(Files::isRegularFile).forEach(path -> path.toFile().setExecutable(true));
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

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing");
        FSGModConfig.tryLoad();
    }
}