package me.duncanruns.fsgwrappermod.screen;

import me.duncanruns.fsgwrappermod.FSGWrapperMod;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DownloadingRSGButGoodScreen extends Screen {
    private static final String WINDOWS_DOWNLOAD = "https://github.com/DuncanRuns/RSGButGood/releases/download/alpha.4/RSGButGood.alpha.4.win.zip";
    private static final String LINUX_DOWNLOAD = "https://github.com/DuncanRuns/RSGButGood/releases/download/alpha.4/RSGButGood.alpha.4.lin.zip";
    private static final String MAC_DOWNLOAD = "https://github.com/DuncanRuns/RSGButGood/releases/download/alpha.4/RSGButGood.alpha.4.mac.zip";
    private boolean failed = false;
    private Thread thread = null;
    private String displayString = "";

    public DownloadingRSGButGoodScreen() {
        super(new LiteralText("Downloading RSGButGood..."));
    }

    private static URL getDownloadURL() throws MalformedURLException {
        switch (FSGWrapperMod.OPERATING_SYSTEM) {
            case WINDOWS:
                return new URL(WINDOWS_DOWNLOAD);
            case OSX:
                return new URL(MAC_DOWNLOAD);
            default:
                return new URL(LINUX_DOWNLOAD);
        }
    }

    /**
     * Extracts the contents of a zip file to a specified output directory.
     *
     * @param zipFilePath the path to the zip file
     * @param outputDir   the path to the output directory
     *
     * @throws IOException if an I/O error occurs
     */
    private static void unzip(Path zipFilePath, Path outputDir) throws IOException {
        // Create the output directory if it does not exist
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        // Open the zip file input stream
        try (InputStream fis = Files.newInputStream(zipFilePath);
             ZipInputStream zis = new ZipInputStream(fis)) {

            ZipEntry entry;
            // Iterate through each entry in the zip file
            while ((entry = zis.getNextEntry()) != null) {
                // Resolve the entry path in the output directory
                Path entryPath = outputDir.resolve(entry.getName());

                if (entry.isDirectory()) {
                    // If the entry is a directory, create the directory
                    if (!Files.exists(entryPath)) {
                        Files.createDirectories(entryPath);
                    }
                } else {
                    // If the entry is a file, ensure the parent directories exist
                    if (!Files.exists(entryPath.getParent())) {
                        Files.createDirectories(entryPath.getParent());
                    }

                    // Copy the file from the zip stream to the output directory
                    Files.copy(zis, entryPath, StandardCopyOption.REPLACE_EXISTING);
                }

                // Close the current entry in the zip stream
                zis.closeEntry();
            }
        }
    }

    private void downloadAndMove() throws IOException {

        Path zipFilePath = FSGWrapperMod.getGameDir().resolve("rsgbutgood.zip");

        File zipFile = zipFilePath.toFile();
        if (!zipFile.isFile()) {
            URL url = getDownloadURL();
            URLConnection connection = url.openConnection();
            connection.connect();
            displayString = "Downloading...";
            int bufferSize = 1024;
            try (BufferedInputStream in = new BufferedInputStream(url.openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(zipFile)) {
                byte[] dataBuffer = new byte[bufferSize];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, bufferSize)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
            }
        }

        String destDirPath = FSGWrapperMod.getFsgDir() + "/";

        // Create the destination directory if it doesn't exist
        Path fsgFolderPath = Paths.get(destDirPath);
        if (!Files.isDirectory(fsgFolderPath)) {
            Files.createDirectories(fsgFolderPath);
        }

        unzip(zipFilePath, fsgFolderPath);
        zipFile.delete();

        if (!FSGWrapperMod.USING_WINDOWS) {
            FSGWrapperMod.getRunPath().toFile().setExecutable(true);
            FSGWrapperMod.getFsgDir().resolve("RSGButGood").toFile().setExecutable(true);
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
                FSGWrapperMod.LOGGER.error("Error while downloading RSGButGood!");
                FSGWrapperMod.LOGGER.error(e);
            }
        }, "rsgbutgood-download");
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
