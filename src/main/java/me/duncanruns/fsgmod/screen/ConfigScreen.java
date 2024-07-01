package me.duncanruns.fsgmod.screen;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.duncanruns.fsgmod.FSGMod;
import me.duncanruns.fsgmod.FSGModConfig;
import me.duncanruns.fsgmod.FileUtil;
import me.duncanruns.fsgmod.util.GrabUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigScreen extends Screen {
    private static final String WINDOWS_SEEDBANK_DOWNLOAD = "https://github.com/pmaccamp/FSGOptimizedSeedBank/archive/refs/tags/v1.zip";
    private static final String LINUX_SEEDBANK_DOWNLOAD = "https://github.com/Specnr/FSGOptimizedSeedBank/archive/refs/tags/v1.2.8.zip";
    private String installedFilterText;
    private int y;

    public ConfigScreen() {
        super(new LiteralText("FSG Mod Config"));
    }

    private static String getRSGButGoodDownload() throws IOException {
        JsonObject jsonObject = GrabUtil.grabJson("https://raw.githubusercontent.com/DuncanRuns/RSGButGood/main/latest.json");
        switch (Util.getOperatingSystem()) {
            case WINDOWS:
                return jsonObject.get("win").getAsString();
            case OSX:
                return jsonObject.get("mac").getAsString();
            default:
                return jsonObject.get("lin").getAsString();
        }
    }

    private LiteralText getBackgroundFilterText() {
        return new LiteralText("Filter for next seed while playing: " + (FSGMod.shouldRunInBackground() ? "ON" : "OFF"));
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
        y = 15;
        y += 60;
        if (Files.isDirectory(FSGMod.getFsgDir())) {
            initFilterInstalled(client, width);
        } else {
            initFilterNotInstalled(client, width);
        }
        y += 60;
        addButton(new ButtonWidget(width / 2 - 100, y, 200, 20, getBackgroundFilterText(), b -> {
            FSGMod.toggleRunInBackground();
            b.setMessage(getBackgroundFilterText());
        }));
        this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 6 + 168, 200, 20, ScreenTexts.DONE, buttonWidget -> this.client.openScreen(null)));
    }

    private void initFilterInstalled(MinecraftClient client, int width) {
        installedFilterText = "Installed Filter: " + FSGModConfig.getInstance().installedFilter;
        y += 10;
        addButton(new ButtonWidget(width / 2 - 100, y, 200, 20, new LiteralText("Configure Filter (Open Folder)"), b -> Util.getOperatingSystem().open(FSGMod.getFsgDir().toFile())));
        y += 25;
        addButton(new ButtonWidget(width / 2 - 100, y, 200, 20, new LiteralText("Uninstall Filter"), b -> {
            try {
                FileUtils.deleteDirectory(FSGMod.getFsgDir().toFile());
                FSGModConfig.getInstance().installedFilter = "Unknown Filter";
            } catch (IOException e) {
                FSGMod.logError(e);
            }
            client.openScreen(new ConfigScreen());
        }));
    }

    private void initFilterNotInstalled(MinecraftClient client, int width) {
        Util.OperatingSystem os = Util.getOperatingSystem();
        installedFilterText = "No filter installed!";
        y += 10;
        addButton(new ButtonWidget(width / 2 - 100, y, 200, 20, new LiteralText("Install RSGButGood"), b -> {
            try {
                String rsgButGoodDownload = getRSGButGoodDownload();
                client.openScreen(new DownloadingScreen(new URL(rsgButGoodDownload), new ConfigScreen(), () -> {
                    FSGModConfig.getInstance().installedFilter = "RSGButGood";
                    Matcher matcher = Pattern.compile("v\\d+\\.\\d+\\.\\d+").matcher(rsgButGoodDownload);
                    if (matcher.find()) {
                        FSGModConfig.getInstance().installedFilter += " " + matcher.group();
                    }
                    FSGModConfig.trySave();
                }));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        })).active = (os == Util.OperatingSystem.WINDOWS || os == Util.OperatingSystem.LINUX || os == Util.OperatingSystem.OSX);
        y += 25;
        addButton(new ButtonWidget(width / 2 - 100, y, 200, 20, new LiteralText("Install Seedbank"), b -> {
            try {
                client.openScreen(new DownloadingScreen(new URL(os == Util.OperatingSystem.WINDOWS ? WINDOWS_SEEDBANK_DOWNLOAD : LINUX_SEEDBANK_DOWNLOAD), new ConfigScreen(), () -> {
                    FSGModConfig.getInstance().installedFilter = "SeedBank";
                    FSGModConfig.trySave();
                    try {
                        FileUtil.writeString(FSGMod.getFsgDir().resolve("run.bat"), "findSeed");
                        FileUtil.writeString(FSGMod.getFsgDir().resolve("run.sh"), "python3 findSeed.py");
                        FSGMod.setAllInFolderExecutable();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        })).active = (os == Util.OperatingSystem.WINDOWS || os == Util.OperatingSystem.LINUX);
    }
}
