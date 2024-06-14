package me.duncanruns.fsgwrappermod.screen;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.duncanruns.fsgwrappermod.FSGWrapperMod;
import me.duncanruns.fsgwrappermod.FSGWrapperModConfig;
import me.duncanruns.fsgwrappermod.FileUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
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

    public ConfigScreen() {
        super(new LiteralText("FSG Wrapper Mod"));
    }

    private static String getRSGButGoodDownload() throws IOException {
        BufferedInputStream s = new BufferedInputStream(new URL("https://raw.githubusercontent.com/DuncanRuns/RSGButGood/main/latest.json").openStream());
        JsonObject jsonObject = new Gson().fromJson(IOUtils.toString(s, StandardCharsets.UTF_8), JsonObject.class);
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
        return new LiteralText("Filter for next seed while playing: " + (FSGWrapperMod.shouldRunInBackground() ? "ON" : "OFF"));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        int y = height / 3;
        this.drawCenteredText(matrices, this.textRenderer, this.title, width / 2, y, 0xFFFFFF);
        y += 40;
        y += 40;
        this.drawCenteredString(matrices, this.textRenderer, installedFilterText, width / 2, y, 0xFFFFFF);
        y += 10;
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public void init(MinecraftClient client, int width, int height) {
        super.init(client, width, height);
        int y = height / 3;
        y += 40;
        addButton(new ButtonWidget(width / 2 - 100, y, 200, 20, getBackgroundFilterText(), b -> {
            FSGWrapperMod.toggleRunInBackground();
            b.setMessage(getBackgroundFilterText());
        }));
        y += 40;
        if (Files.isDirectory(FSGWrapperMod.getFsgDir())) {
            initFilterInstalled(client, width, y);
        } else {
            initFilterNotInstalled(client, width, y);
        }
        //
    }

    private void initFilterInstalled(MinecraftClient client, int width, int y) {
        installedFilterText = "Installed Filter: " + FSGWrapperModConfig.getInstance().installedFilter;
        y += 10;
        addButton(new ButtonWidget(width / 2 - 100, y, 200, 20, new LiteralText("Configure Filter (Open Folder)"), b -> Util.getOperatingSystem().open(FSGWrapperMod.getFsgDir().toFile())));
        y += 25;
        addButton(new ButtonWidget(width / 2 - 100, y, 200, 20, new LiteralText("Uninstall Filter"), b -> {
            try {
                FileUtils.deleteDirectory(FSGWrapperMod.getFsgDir().toFile());
            } catch (IOException e) {
                FSGWrapperMod.logError(e);
            }
            client.openScreen(new ConfigScreen());
        }));
    }

    private void initFilterNotInstalled(MinecraftClient client, int width, int y) {
        Util.OperatingSystem os = Util.getOperatingSystem();
        installedFilterText = "No filter installed!";
        y += 10;
        addButton(new ButtonWidget(width / 2 - 100, y, 200, 20, new LiteralText("Install RSGButGood"), b -> {
            try {
                String rsgButGoodDownload = getRSGButGoodDownload();
                client.openScreen(new DownloadingScreen(new URL(rsgButGoodDownload), new ConfigScreen(), () -> {
                    FSGWrapperModConfig.getInstance().installedFilter = "RSGButGood";
                    Matcher matcher = Pattern.compile("v\\d+\\.\\d+\\.\\d+").matcher(rsgButGoodDownload);
                    if (matcher.find()) {
                        FSGWrapperModConfig.getInstance().installedFilter += " " + matcher.group();
                    }
                    FSGWrapperModConfig.trySave();
                }));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        })).active = (os == Util.OperatingSystem.WINDOWS || os == Util.OperatingSystem.LINUX || os == Util.OperatingSystem.OSX);
        y += 25;
        addButton(new ButtonWidget(width / 2 - 100, y, 200, 20, new LiteralText("Install Seedbank"), b -> {
            try {
                client.openScreen(new DownloadingScreen(new URL(os == Util.OperatingSystem.WINDOWS ? WINDOWS_SEEDBANK_DOWNLOAD : LINUX_SEEDBANK_DOWNLOAD), new ConfigScreen(), () -> {
                    FSGWrapperModConfig.getInstance().installedFilter = "SeedBank";
                    FSGWrapperModConfig.trySave();
                    try {
                        FileUtil.writeString(FSGWrapperMod.getFsgDir().resolve("run.bat"), "findSeed");
                        FileUtil.writeString(FSGWrapperMod.getFsgDir().resolve("run.sh"), "python3 findSeed.py");
                        FSGWrapperMod.setAllInFolderExecutable();
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
