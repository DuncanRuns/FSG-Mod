package me.duncanruns.fsgwrappermod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FSGWrapperModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("fsgwm.json");
    private static FSGWrapperModConfig instance = new FSGWrapperModConfig();

    public String lastToken = null;
    public boolean runInBackground = false;
    public String installedFilter = "Unknown Filter";

    public static void trySave() {
        try {
            save();
        } catch (Exception e) {
            FSGWrapperMod.LOGGER.error("Failed to save config!");
            FSGWrapperMod.logError(e);
        }
    }

    private static void save() throws IOException {
        FileUtil.writeString(PATH, GSON.toJson(instance));
    }

    public static void tryLoad() {
        try {
            load();
        } catch (Exception e) {
            FSGWrapperMod.LOGGER.error("Failed to load config!");
            FSGWrapperMod.logError(e);
        }
    }

    private static void load() throws IOException, JsonSyntaxException {
        if (Files.exists(PATH)) {
            String s = FileUtil.readString(PATH);
            instance = GSON.fromJson(s, FSGWrapperModConfig.class);
            if (instance.lastToken != null) {
                FSGWrapperMod.lastTokenHash = instance.lastToken.hashCode();
            }
        }
    }

    public static FSGWrapperModConfig getInstance() {
        return instance;
    }
}
