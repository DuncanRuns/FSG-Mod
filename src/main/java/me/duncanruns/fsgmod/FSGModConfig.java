package me.duncanruns.fsgmod;

import com.google.gson.*;
import me.duncanruns.fsgmod.util.FileUtil;
import me.duncanruns.fsgmod.util.GrabUtil;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FSGModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("fsgwm.json"); // wm = wrapper mod, not changing for simplicity
    private static FSGModConfig instance = new FSGModConfig();

    public String installedFilter = "Unknown Filter";
    public int maxGenerating = 1;

    public static void trySave() {
        try {
            save();
        } catch (Exception e) {
            FSGMod.logError("Failed to save config!", e);
        }
    }

    private static void save() throws IOException {
        FileUtil.writeString(PATH, GSON.toJson(instance));
    }

    public static void tryLoad() {
        try {
            load();
        } catch (Exception e) {
            FSGMod.logError("Failed to load config!", e);
            instance = new FSGModConfig();
        }
    }

    private static void resolveMaxGenerating() {
        instance.maxGenerating = 1;
        JsonObject meta;
        try {
            meta = GrabUtil.grabJson("https://raw.githubusercontent.com/DuncanRuns/FSG-Mod/meta/meta.json");
        } catch (IOException e) {
            FSGMod.logError("Could not resolve max generating for filter:", e);
            return;
        }
        String filterName = instance.installedFilter;
        for (JsonElement element : meta.getAsJsonArray("filters")) {
            JsonObject filter = element.getAsJsonObject();
            if (!filterName.startsWith(filter.get("name").getAsString())) continue;
            instance.maxGenerating = filter.get("maxGenerating").getAsInt();
        }
    }

    private static void load() throws IOException, JsonSyntaxException {
        if (Files.exists(PATH)) {
            String s = FileUtil.readString(PATH);
            instance = GSON.fromJson(s, FSGModConfig.class);
        }
        if (!instance.installedFilter.equals("Unknown Filter") && instance.maxGenerating == -1) resolveMaxGenerating();
    }

    public static FSGModConfig getInstance() {
        return instance;
    }
}
