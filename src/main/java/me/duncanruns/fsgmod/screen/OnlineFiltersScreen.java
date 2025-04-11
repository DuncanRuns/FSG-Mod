package me.duncanruns.fsgmod.screen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.duncanruns.fsgmod.FSGModConfig;
import me.duncanruns.fsgmod.util.GrabUtil;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;

public class OnlineFiltersScreen extends Screen {
    private static LiteralText failedText = new LiteralText("Failed to retrieve filters!");
    private final String minecraftVersion = SharedConstants.getGameVersion().getName();
    private boolean retrievedFilters = false;
    private boolean addedFilterButtons = false;
    private boolean failed = false;
    private JsonArray filters = null;

    protected OnlineFiltersScreen() {
        super(new LiteralText("FSG Mod: Select Online Filter"));
        new Thread(() -> {
            try {
                JsonObject jsonObject = GrabUtil.grabJson("https://fsgonlinedb.duncanruns.xyz/filters");
                if (!jsonObject.get("type").getAsString().equals("SUCCESS")) {
                    failed = true;
                    return;
                }
                filters = jsonObject.getAsJsonArray("filters");
                retrievedFilters = true;
            } catch (Exception e) {
                failed = true;
            }
        }, "").start();
    }

    @Override
    protected void init() {
        Util.OperatingSystem os = Util.getOperatingSystem();
        assert client != null;
        final int buttonWidth = 150;
        final int buttonHeight = 20;
        addButton(new ButtonWidget(
                width - buttonWidth,
                height - buttonHeight,
                buttonWidth,
                buttonHeight,
                new LiteralText("Install Filter Locally..."),
                b -> client.openScreen(new LocalFiltersScreen())
        )).active = (os == Util.OperatingSystem.WINDOWS || os == Util.OperatingSystem.LINUX || os == Util.OperatingSystem.OSX);
        if (retrievedFilters) {
            setupButtons();
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        int y = 15;
        this.drawCenteredText(matrices, this.textRenderer, this.title, width / 2, y, 0xFFFFFF);
        if (failed) {
            y += 60;
            this.drawCenteredText(matrices, this.textRenderer, failedText, width / 2, y, 0xFFFFFF);
            return;
        }
        if (!addedFilterButtons && retrievedFilters) {
            setupButtons();
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void setupButtons() {
        assert client != null;
        boolean anySupportedFilters = false;
        int y = 75;
        for (JsonElement el : filters) {
            JsonObject filter = el.getAsJsonObject();
            boolean supportsThisVersion = false;
            for (JsonElement support : filter.get("supportedVersions").getAsJsonArray()) {
                if (minecraftVersion.equals(support.getAsString())) {
                    supportsThisVersion = true;
                    anySupportedFilters = true;
                    break;
                }
            }
            if (!supportsThisVersion) continue;
            String id = filter.get("id").getAsString();
            String finalName = filter.get("displayName").getAsString();
            int maxGenerating = filter.get("maxGenerating").getAsInt();

            addButton(new ButtonWidget(width / 2 - 100, y, 200, 20, new LiteralText(finalName), b -> {
                FSGModConfig config = FSGModConfig.getInstance();
                config.onlineFilterCode = id;
                config.installedFilter = finalName;
                config.maxGenerating = maxGenerating;
                FSGModConfig.trySave();
                client.openScreen(new ConfigScreen());
            }));
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
