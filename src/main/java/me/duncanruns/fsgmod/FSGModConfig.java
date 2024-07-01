package me.duncanruns.fsgmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FSGModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("fsgwm.json"); // wm = wrapper mod, not changing for simplicity
    private static FSGModConfig instance = new FSGModConfig();

    public String lastToken = null;
    public boolean runInBackground = false;
    public String installedFilter = "Unknown Filter";

    public static void trySave() {
        try {
            save();
        } catch (Exception e) {
            FSGMod.LOGGER.error("Failed to save config!");
            FSGMod.logError(e);
        }
    }

    private static void save() throws IOException {
        FileUtil.writeString(PATH, GSON.toJson(instance));
    }

    public static void tryLoad() {
        try {
            load();
        } catch (Exception e) {
            FSGMod.LOGGER.error("Failed to load config!");
            FSGMod.logError(e);
        }
    }

    private static void load() throws IOException, JsonSyntaxException {
        if (Files.exists(PATH)) {
            String s = FileUtil.readString(PATH);
            instance = GSON.fromJson(s, FSGModConfig.class);
            if (instance.lastToken != null) {
                FSGMod.lastTokenHash = instance.lastToken.hashCode();
            }
        }
    }

    public static FSGModConfig getInstance() {
        return instance;
    }
}
