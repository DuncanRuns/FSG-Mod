package me.duncanruns.fsgmod.screen;

import me.duncanruns.fsgmod.FSGMod;
import me.duncanruns.fsgmod.FSGModConfig;
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
import java.nio.file.Files;

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
        if (FSGMod.filterIsInstalled()) {
            initFilterInstalled(client, width);
        } else {
            initFilterNotInstalled(client, width);
        }
        y += 60;
        this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 6 + 168, 200, 20, ScreenTexts.DONE, buttonWidget -> this.client.openScreen(null)));
    }

    private void initFilterInstalled(MinecraftClient client, int width) {
        // Change the text based on whether it's an online filter or local filter
        boolean online = FSGModConfig.getInstance().onlineFilterCode != null;
        if (online) {
            installedFilterText = "Selected Filter: " + FSGModConfig.getInstance().installedFilter;
        } else {
            installedFilterText = "Installed Filter: " + FSGModConfig.getInstance().installedFilter;
        }

        y += 10;
        addButton(new ButtonWidget(width / 2 - 100, y, 200, 20, new LiteralText(online ? "Select Different Filter" : "Configure Filter (Open Folder)"), b -> {
            if (online) {
                client.openScreen(new OnlineFiltersScreen());
            } else {
                Util.getOperatingSystem().open(FSGMod.getFsgDir().toFile());
            }
        }));
        y += 25;
        uninstallButton = addButton(new ButtonWidget(width / 2 - 100, y, 200, 20, new LiteralText("Uninstall Filter"), b -> {
            SeedManager.clear();
            try {
                if (Files.exists(FSGMod.getFsgDir()))
                    FileUtils.deleteDirectory(FSGMod.getFsgDir().toFile());
                FSGModConfig.getInstance().installedFilter = "Unknown Filter";
                FSGModConfig.getInstance().onlineFilterCode = null;
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
        addButton(new ButtonWidget(width / 2 - 100, y, 200, 20, new LiteralText("Select Filter..."), b -> client.openScreen(new OnlineFiltersScreen())));
        y += 14;
    }

    @Override
    public void tick() {
        if (uninstallButton == null) return;
        boolean filterRunning = SeedManager.getCurrentlyFiltering() > 0;
        uninstallButton.active = !filterRunning;
    }
}
