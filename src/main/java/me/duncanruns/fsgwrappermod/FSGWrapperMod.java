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

    public static String lastToken = null;
    public static int lastTokenHash = 0;

    private static boolean runInBackground = false;

    public static String getLastToken() {
        return lastToken;
    }

    public static void setLastToken(String lastToken) {
        FSGWrapperMod.lastToken = lastToken;
        FSGWrapperMod.lastTokenHash = lastToken.hashCode();
        try {
            FileUtil.writeString(getFsgTokenTxtPath(), lastToken);
        } catch (IOException ignored) {
        }
    }

    public static boolean shouldRunInBackground() {
        return runInBackground;
    }

    public static boolean toggleRunInBackground() {
        runInBackground = !runInBackground;
        updateRunInBGFile();
        return runInBackground;
    }

    private static void updateRunInBGFile() {
        try {
            if (runInBackground) {
                FileUtil.writeString(getFsgBackgroundPath(), "");
            } else {
                Files.delete(getFsgBackgroundPath());
            }
        } catch (IOException ignored) {
        }
    }

    public static int getLastTokenHash() {
        return lastTokenHash;
    }

    private static Path getFsgTokenTxtPath() {
        return getFsgDir().resolve("fsgtoken.txt");
    }

    private static Path getFsgBackgroundPath() {
        return getFsgDir().resolve("fsgwmfb");
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

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing");
        try {
            setLastToken(FileUtil.readString(getFsgTokenTxtPath()));
        } catch (IOException ignored) {
        }
        runInBackground = Files.exists(getFsgBackgroundPath());
    }
}