package me.duncanruns.fsgwrappermod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.MinecraftVersion;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FSGWrapperMod implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("fsg-wrapper-mod");
    public static final Util.OperatingSystem OPERATING_SYSTEM = Util.getOperatingSystem();
    public static final boolean USING_WINDOWS = OPERATING_SYSTEM.equals(Util.OperatingSystem.WINDOWS);
    public static final String VERSION = FabricLoader.getInstance().getModContainer("fsg-wrapper-mod").get().getMetadata().getVersion().getFriendlyString();

    public static int lastTokenHash = 0;

    public static String getLastToken() {
        return FSGWrapperModConfig.getInstance().lastToken;
    }

    public static void setLastToken(String lastToken) {
        FSGWrapperModConfig.getInstance().lastToken = lastToken;
        lastTokenHash = lastToken.hashCode();
        FSGWrapperModConfig.trySave();
    }

    public static boolean shouldRunInBackground() {
        return FSGWrapperModConfig.getInstance().runInBackground;
    }

    public static boolean toggleRunInBackground() {
        FSGWrapperModConfig.getInstance().runInBackground = !FSGWrapperModConfig.getInstance().runInBackground;
        FSGWrapperModConfig.trySave();
        return FSGWrapperModConfig.getInstance().runInBackground;
    }

    public static Path getFsgDir() {
        return getGameDir().resolve("fsg");
    }

    public static Path getGameDir() {
        return FabricLoader.getInstance().getGameDir().toAbsolutePath();
    }

    public static Path getRunPath() {
        Path runForThisVersionPath = FSGWrapperMod.getFsgDir().resolve("run." + MinecraftVersion.field_25319.getName() + (FSGWrapperMod.OPERATING_SYSTEM.equals(Util.OperatingSystem.WINDOWS) ? ".bat" : ".sh"));
        if (Files.exists(runForThisVersionPath)) {
            return runForThisVersionPath;
        }
        return FSGWrapperMod.getFsgDir().resolve("run" + (FSGWrapperMod.OPERATING_SYSTEM.equals(Util.OperatingSystem.WINDOWS) ? ".bat" : ".sh"));
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

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing");
        FSGWrapperModConfig.tryLoad();
    }
}