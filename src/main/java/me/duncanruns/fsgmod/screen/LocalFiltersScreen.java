package me.duncanruns.fsgmod.screen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.duncanruns.fsgmod.FSGMod;
import me.duncanruns.fsgmod.FSGModConfig;
import me.duncanruns.fsgmod.LocalFilter;
import me.duncanruns.fsgmod.util.FileUtil;
import me.duncanruns.fsgmod.util.GrabUtil;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.io.IOException;
import java.util.HashSet;

public class LocalFiltersScreen extends Screen {
    private static String failedText = "Failed to retrieve filters!";
    private final String minecraftVersion = SharedConstants.getGameVersion().getName();
    private boolean retrievedFilters = false;
    private boolean addedFilterButtons = false;
    private boolean failed = false;
    private JsonArray filters = null;

    protected LocalFiltersScreen() {
        super(new LiteralText("FSG Mod: Install Local Filter"));
        new Thread(() -> {
            try {
                JsonObject jsonObject = GrabUtil.grabJson("https://raw.githubusercontent.com/DuncanRuns/FSG-Mod/meta/meta.json");
                filters = jsonObject.getAsJsonArray("filters");
                retrievedFilters = true;
            } catch (Exception e) {
                FSGMod.logError("Failed to load filters!", e);
                failed = true;
            }
        }, "").start();
    }

    @Override
    protected void init() {
        if (retrievedFilters) {
            setupButtons();
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        int y = 15;
        this.drawCenteredText(matrices, this.textRenderer, this.title, width / 2, y, 0xFFFFFF);
        if (!addedFilterButtons && retrievedFilters) {
            setupButtons();
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void tick() {
        if (failed) {
            client.openScreen(new SimpleTextScreen(new LiteralText("Failed to load filters!"), failedText, true, s -> {
                // Retry button
                return new ButtonWidget(s.width / 2 - 100, s.height / 6 + 140, 200, 20, new LiteralText("Retry"), b ->
                        client.openScreen(new LocalFiltersScreen())
                );
            }));
        }
    }

    private void setupButtons() {
        assert client != null;
        boolean anySupportedFilters = false;
        int y = 75;
        for (JsonElement el : filters) {
            JsonObject filter = el.getAsJsonObject();
            boolean supportsThisVersion = false;
            for (JsonElement support : filter.get("supports").getAsJsonArray()) {
                if (minecraftVersion.equals(support.getAsString())) {
                    supportsThisVersion = true;
                    anySupportedFilters = true;
                    break;
                }
            }
            if (!supportsThisVersion) continue;
            String name = filter.get("name").getAsString();
            if (filter.has("version")) {
                name += " v" + filter.get("version").getAsString();
            }
            JsonObject download = filter.getAsJsonObject("download");
            String finalName = name;
            int maxGenerating = filter.get("maxGenerating").getAsInt();
            String osCode = FSGMod.getOS3LetterCode() + (FSGMod.onArm() ? "arm" : "");

            addButton(new ButtonWidget(width / 2 - 100, y, 200, 20, new LiteralText(name), b -> {
                client.openScreen(new DownloadingScreen(download.get(osCode).getAsString(), new ConfigScreen(), () -> {
                    try {
                        if (filter.has("run.bat")) {
                            FileUtil.writeString(LocalFilter.getFsgDir().resolve("run.bat"), filter.get("run.bat").getAsString());
                        }
                        if (filter.has("run.sh")) {
                            FileUtil.writeString(LocalFilter.getFsgDir().resolve("run.sh"), filter.get("run.sh").getAsString());
                        }
                        FSGMod.setAllInFolderExecutable();
                        FSGModConfig config = FSGModConfig.getInstance();
                        config.selectedOnlineFilters = new HashSet<>();
                        config.selectedOnlineFilterName = null;
                        FSGModConfig.trySave();
                        LocalFilter.writeData(maxGenerating, finalName);

                    } catch (IOException e) {
                        FSGMod.logError("Failed to install filter!", e);
                    }
                    FSGModConfig.trySave();
                }));
            })).active = download.has(osCode);
            y += 25;
        }
        if (anySupportedFilters) {
            addedFilterButtons = true;
        } else {
            failed = true;
            failedText = "No filters are available for this version of Minecraft.";
            client.openScreen(new SimpleTextScreen(new LiteralText("No Filters Found"), failedText, true));
        }
    }
}
