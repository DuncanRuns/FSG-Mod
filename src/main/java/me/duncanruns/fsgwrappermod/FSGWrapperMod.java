package me.duncanruns.fsgwrappermod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

public class FSGWrapperMod implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("fsg-wrapper-mod");
    public static final boolean usingWindows = Util.getOperatingSystem().equals(Util.OperatingSystem.WINDOWS);
    public static final String VERSION = FabricLoader.getInstance().getModContainer("fsg-wrapper-mod").get().getMetadata().getVersion().getFriendlyString();

    public static String lastToken = null;
    public static int lastTokenHash = 0;

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

    public static int getLastTokenHash() {
        return lastTokenHash;
    }

    private static Path getFsgTokenTxtPath() {
        return getFsgDir().resolve("fsgtoken.txt");
    }

    public static Path getFsgDir() {
        return getGameDir().resolve("fsg");
    }

    public static Path getGameDir() {
        return FabricLoader.getInstance().getGameDir().toAbsolutePath();
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing");
        try {
            lastToken = FileUtil.readString(getFsgTokenTxtPath());
        } catch (IOException ignored) {
        }
    }


}