package me.duncanruns.fsgmod;

import com.google.gson.*;
import me.duncanruns.fsgmod.util.FileUtil;
import me.duncanruns.fsgmod.util.GrabUtil;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class FSGModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("fsg-mod.json");
    private static FSGModConfig instance = new FSGModConfig();

    public Set<String> selectedOnlineFilters = new HashSet<>();
    public String selectedOnlineFilterName = null;

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

    private static void load() throws IOException, JsonSyntaxException {
        if (Files.exists(PATH)) {
            String s = FileUtil.readString(PATH);
            instance = GSON.fromJson(s, FSGModConfig.class);
        } else {
            OldConfig.check();
        }
    }

    public static FSGModConfig getInstance() {
        return instance;
    }

    static class OldConfig {
        private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("fsgwm.json");

        public String onlineFilterCode = null;
        public String installedFilter = "Unknown Filter";
        public int maxGenerating = 1;

        public static void check() {
            OldConfig oldConfig;
            try {
                oldConfig = GSON.fromJson(FileUtil.readString(PATH), OldConfig.class);
            } catch (Exception e) {
                return;
            }

            if (!oldConfig.installedFilter.equals("Unknown Filter") && oldConfig.maxGenerating == -1)
                oldConfig.resolveMaxGenerating();

            FSGModConfig config = FSGModConfig.getInstance();
            if (oldConfig.onlineFilterCode != null) {
                config.selectedOnlineFilters.add(oldConfig.onlineFilterCode);
                config.selectedOnlineFilterName = oldConfig.installedFilter;
                trySave();
            } else if (LocalFilter.isInstalled()) {
                config.selectedOnlineFilters = new HashSet<>();
                config.selectedOnlineFilterName = null;
                try {
                    LocalFilter.writeData(oldConfig.maxGenerating, oldConfig.installedFilter);
                } catch (IOException e) {
                    FSGMod.logError("Failed to write local filter data!", e);
                }
                trySave();
            }

            try {
                Files.delete(PATH);
            } catch (IOException e) {
                FSGMod.logError("Failed to delete old config file!", e);
            }
        }

        private void resolveMaxGenerating() {
            maxGenerating = 1;
            JsonObject meta;
            try {
                meta = GrabUtil.grabJson("https://raw.githubusercontent.com/DuncanRuns/FSG-Mod/meta/meta.json");
            } catch (IOException e) {
                FSGMod.logError("Could not resolve max generating for filter:", e);
                return;
            }
            String filterName = LocalFilter.getFilterName();
            for (JsonElement element : meta.getAsJsonArray("filters")) {
                JsonObject filter = element.getAsJsonObject();
                if (!filterName.startsWith(filter.get("name").getAsString())) continue;
                maxGenerating = filter.get("maxGenerating").getAsInt();
            }
        }
    }
}
