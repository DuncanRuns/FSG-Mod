package me.duncanruns.fsgmod;

import me.duncanruns.fsgmod.util.ArchUtil;
import me.voidxwalker.autoreset.Atum;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.MinecraftVersion;
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

    public static final boolean DEBUG = false;

    public static Path getFsgDir() {
        return getGameDir().resolve("fsg");
    }

    public static Path getGameDir() {
        return FabricLoader.getInstance().getGameDir().toAbsolutePath();
    }

    public static Path getRunPath() {
        Path runForThisVersionPath = FSGMod.getFsgDir().resolve("run." + MinecraftVersion.field_25319.getName() + (FSGMod.OPERATING_SYSTEM.equals(Util.OperatingSystem.WINDOWS) ? ".bat" : ".sh"));
        if (Files.exists(runForThisVersionPath)) {
            return runForThisVersionPath;
        }
        return FSGMod.getFsgDir().resolve("run" + (FSGMod.OPERATING_SYSTEM.equals(Util.OperatingSystem.WINDOWS) ? ".bat" : ".sh"));
    }

    public static void logError(String message, Throwable t) {
        LOGGER.error(message, t);
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

    public static boolean onArm() {
        return ArchUtil.getArch() == ArchUtil.Arch.ARM;
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing");
        FSGModConfig.tryLoad();
        FSGModConfig.trySave();

        Atum.setSeedProvider(new FSGSeedProvider());
    }
}