package me.duncanruns.fsgmod.screen;

import me.duncanruns.fsgmod.FSGMod;
import me.duncanruns.fsgmod.FSGModConfig;
import me.duncanruns.fsgmod.LocalFilter;
import me.duncanruns.fsgmod.SeedManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;
import org.apache.commons.io.FileUtils;

import java.io.IOException;

public class ConfigScreen extends Screen {
    private String installedFilterText;
    private int y;
    private ButtonWidget uninstallButton = null;

    public ConfigScreen() {
        super(new LiteralText("FSG Mod Config"));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        int y = 15;
        this.drawCenteredText(matrices, this.textRenderer, this.title, width / 2, y, 0xFFFFFF);
        y += 60;
        this.drawCenteredString(matrices, this.textRenderer, installedFilterText, width / 2, y, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void init(MinecraftClient client, int width, int height) {
        super.init(client, width, height);
        y = 75;
        if (FSGMod.filterSelectedOrInstalled()) {
            initFilterInstalled(client, width);
        } else {
            initFilterNotInstalled(client, width);
        }
        y += 60;
        this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 6 + 168, 200, 20, ScreenTexts.DONE, buttonWidget -> this.client.openScreen(null)));
    }

    private void initFilterInstalled(MinecraftClient client, int width) {
        // Change the text based on whether it's an online filter or local filter
        boolean online = !LocalFilter.isInstalled();
        if (online) {
            if (FSGModConfig.getInstance().selectedOnlineFilters.size() == 1) {
                installedFilterText = "Selected Filter: " + FSGModConfig.getInstance().selectedOnlineFilterName;
            } else {
                installedFilterText = FSGModConfig.getInstance().selectedOnlineFilters.size() + " Filters Selected";
            }
        } else {
            installedFilterText = "Installed Filter: " + LocalFilter.getFilterName();
        }

        y += 10;
        addButton(new ButtonWidget(width / 2 - 100, y + (online ? 11 : 0), 200, 20, new LiteralText(online ? "Select Different Filter(s)..." : "Configure Filter (Open Folder)"), b -> {
            if (online) {
                openOnlineFiltersScreen();
            } else {
                Util.getOperatingSystem().open(LocalFilter.getFsgDir().toFile());
            }
        }));
        y += 25;
        if (online) return;
        uninstallButton = addButton(new ButtonWidget(width / 2 - 100, y, 200, 20, new LiteralText("Uninstall Filter"), b -> {
            SeedManager.clear();
            try {
                if (LocalFilter.isInstalled())
                    FileUtils.deleteDirectory(LocalFilter.getFsgDir().toFile());
                FSGModConfig.trySave();
            } catch (IOException e) {
                FSGMod.logError("Failed to delete fsg directory", e);
            }
            client.openScreen(new ConfigScreen());
        }, (button, matrices, mouseX, mouseY) -> {
            if (button.active) return;
            renderTooltip(matrices, new LiteralText("The filter is running in the background..."), mouseX, mouseY);
        }));
        uninstallButton.active = false;
    }

    private void initFilterNotInstalled(MinecraftClient client, int width) {
        installedFilterText = "No filter selected!";
        y += 21;
        addButton(new ButtonWidget(width / 2 - 100, y, 200, 20, new LiteralText("Select Filter..."), b -> openOnlineFiltersScreen()));
        y += 14;
    }

    private void openOnlineFiltersScreen() {
        assert client != null;
        client.openScreen(new LoadingFiltersScreen(filterInfos ->
                client.openScreen(new OnlineFiltersScreen(filterInfos, FSGModConfig.getInstance().selectedOnlineFilters))
        ));
    }

    @Override
    public void tick() {
        if (uninstallButton == null) return;
        boolean filterRunning = SeedManager.getCurrentlyFiltering() > 0;
        uninstallButton.active = !filterRunning;
    }
}
