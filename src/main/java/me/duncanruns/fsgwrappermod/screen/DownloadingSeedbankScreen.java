package me.duncanruns.fsgwrappermod.screen;

import me.duncanruns.fsgwrappermod.FSGWrapperMod;
import me.duncanruns.fsgwrappermod.FileUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipFile;

public class DownloadingSeedbankScreen extends Screen {
    private static final String WINDOWS_SEEDBANK_DOWNLOAD = "https://github.com/pmaccamp/FSGOptimizedSeedBank/archive/refs/tags/v1.zip";
    private static final String LINUX_SEEDBANK_DOWNLOAD = "https://github.com/Specnr/FSGOptimizedSeedBank/archive/refs/tags/v1.2.8.zip";
    private boolean failed = false;
    private Thread thread = null;
    private String displayString = "";

    public DownloadingSeedbankScreen() {
        super(new LiteralText("Downloading Seedbank..."));
    }

    private void downloadAndMove() throws IOException {

        String zipFilePath = FSGWrapperMod.getGameDir().resolve("seedbank.zip").toString();

        File seedbankZipFile = new File(zipFilePath);
        if (!seedbankZipFile.isFile()) {
            URL url = new URL(FSGWrapperMod.usingWindows ? WINDOWS_SEEDBANK_DOWNLOAD : LINUX_SEEDBANK_DOWNLOAD);
            URLConnection connection = url.openConnection();
            connection.connect();
            long fileSize = FSGWrapperMod.usingWindows ? 23_097_697 : 7_812_704; // connection.getContentLength() returns -1 :(
            displayString = "0%";
            long totalDownloaded = 0;
            int i = 0;
            int bufferSize = 1024;
            try (BufferedInputStream in = new BufferedInputStream(url.openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(seedbankZipFile)) {
                byte[] dataBuffer = new byte[bufferSize];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, bufferSize)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                    i += bytesRead;
                    if (i >= fileSize / 100) {
                        totalDownloaded += i;
                        i = 0;
                        displayString = (100 * totalDownloaded / fileSize) + "%";
                    }
                }
            }
        }

        String destDirPath = FSGWrapperMod.getFsgDir() + "/";
        String subDirName = FSGWrapperMod.usingWindows ? "FSGOptimizedSeedBank-1" : "FSGOptimizedSeedBank-1.2.8";

        // Create the destination directory if it doesn't exist
        Path fsgFolderPath = Paths.get(destDirPath);
        if (!Files.isDirectory(fsgFolderPath)) {
            Files.createDirectories(fsgFolderPath);
        }

        // Open the zip file
        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            // Get the entries in the zip file
            zipFile.stream()
                    // Filter the entries to only include those in the subdirectory
                    .filter(entry -> entry.getName().startsWith(subDirName + "/"))
                    .forEach(entry -> {
                        try {
                            // Construct the destination path for the entry
                            String destFilePath = destDirPath + entry.getName().substring(subDirName.length() + 1);
                            Path destPath = Paths.get(destFilePath);

                            // Create any necessary parent directories for the destination path
                            Path parentPath = destPath.getParent();
                            if (entry.isDirectory()) {
                                Files.createDirectories(destPath.toAbsolutePath());
                            } else if (parentPath != null) {
                                Files.createDirectories(parentPath.toAbsolutePath());
                            }

                            // Extract the entry to the destination path
                            Files.copy(zipFile.getInputStream(entry), destPath);
                        } catch (IOException ignored) {
                        }
                    });
        }
        seedbankZipFile.delete();

        FileUtil.writeString(fsgFolderPath.resolve("run.bat"), "findSeed");

        if (!FSGWrapperMod.usingWindows) {
            Path runShPath = fsgFolderPath.resolve("run.sh");
            FileUtil.writeString(runShPath, "python3 findSeed.py");
            runShPath.toFile().setExecutable(true);
            runShPath.resolveSibling("bh").toFile().setExecutable(true);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        this.drawCenteredText(matrices, this.textRenderer, this.title, width / 2, height / 3, 0xFFFFFF);
        this.drawCenteredString(matrices, this.textRenderer, displayString, width / 2, height / 2, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    protected void init() {
        if (thread != null) {
            return;
        }
        thread = new Thread(() -> {
            try {
                downloadAndMove();
            } catch (Exception e) {
                failed = true;
                FSGWrapperMod.LOGGER.error("Error while downloading seedbank!");
                FSGWrapperMod.LOGGER.error(e);
            }
        }, "seedbank-download");
        thread.start();
    }

    @Override
    public void tick() {
        if (thread.isAlive()) {
            return;
        }
        try {
            Thread.sleep(15);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (failed) {
            client.openScreen(new DownloadFailedScreen());
        } else {
            client.openScreen(new TitleScreen());
        }
    }

}
