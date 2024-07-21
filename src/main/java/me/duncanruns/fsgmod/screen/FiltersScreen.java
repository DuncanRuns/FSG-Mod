package me.duncanruns.fsgmod.screen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.duncanruns.fsgmod.FSGMod;
import me.duncanruns.fsgmod.FSGModConfig;
import me.duncanruns.fsgmod.util.FileUtil;
import me.duncanruns.fsgmod.util.GrabUtil;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;

import java.io.IOException;

public class FiltersScreen extends Screen {
    private static LiteralText failedText = new LiteralText("Failed to retrieve filters!");
    private final String minecraftVersion = SharedConstants.getGameVersion().getName();
    private boolean retrievedFilters = false;
    private boolean addedFilterButtons = false;
    private boolean failed = false;
    private JsonArray filters = null;

    protected FiltersScreen() {
        super(new LiteralText("FSG Mod: Install Filter"));
        new Thread(() -> {
            try {
                JsonObject jsonObject = GrabUtil.grabJson("https://raw.githubusercontent.com/DuncanRuns/FSG-Mod/meta/meta.json");
                filters = jsonObject.getAsJsonArray("filters");
                retrievedFilters = true;
            } catch (Exception e) {
                failed = true;
            }
        }, "").start();
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        renderBackground();
        int y = 15;
        this.drawCenteredString(minecraft.textRenderer, this.title.asString(), width / 2, y, 0xFFFFFF);
        if (failed) {
            y += 60;
            this.drawCenteredString(minecraft.textRenderer, failedText.asString(), width / 2, y, 0xFFFFFF);
            return;
        }
        if (!addedFilterButtons && retrievedFilters) {
            setupButtons();
        }

        super.render(mouseX, mouseY, delta);
    }

    private void setupButtons() {
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
            addButton(new ButtonWidget(width / 2 - 100, y, 200, 20, name, b -> {
                minecraft.openScreen(new DownloadingScreen(download.get(FSGMod.getOS3LetterCode()).getAsString(), new ConfigScreen(), () -> {
                    try {
                        if (filter.has("run.bat")) {
                            FileUtil.writeString(FSGMod.getFsgDir().resolve("run.bat"), filter.get("run.bat").getAsString());
                        }
                        if (filter.has("run.sh")) {
                            FileUtil.writeString(FSGMod.getFsgDir().resolve("run.sh"), filter.get("run.sh").getAsString());
                        }
                        FSGMod.setAllInFolderExecutable();
                        FSGModConfig.getInstance().installedFilter = finalName;
                    } catch (IOException e) {
                        FSGMod.logError(e);
                    }
                    FSGModConfig.trySave();
                }));
            })).active = download.has(FSGMod.getOS3LetterCode());
            y += 25;
        }
        if (anySupportedFilters) {
            addedFilterButtons = true;
        } else {
            failed = true;
            failedText = new LiteralText("No filters are available for this version of Minecraft.");
        }
    }
}
