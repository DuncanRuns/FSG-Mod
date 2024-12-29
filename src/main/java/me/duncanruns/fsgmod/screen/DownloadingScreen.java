package me.duncanruns.fsgmod.screen;

import me.duncanruns.fsgmod.FSGMod;
import me.duncanruns.fsgmod.util.GrabUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DownloadingScreen extends Screen {
    private boolean failed = false;
    private Thread thread = null;
    private int totalBytesRead = 0;
    private final String downloadURL;
    private final Screen screenOnCompletion;
    private final Runnable runOnCompletion;

    public DownloadingScreen(String downloadURL, Screen screenOnCompletion, Runnable runOnCompletion) {
        super(new LiteralText("Filter Download"));
        this.downloadURL = downloadURL;
        this.screenOnCompletion = screenOnCompletion;
        this.runOnCompletion = runOnCompletion;
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
        Path zipFilePath = FSGMod.getGameDir().resolve("downloaded.zip");

        File zipFile = zipFilePath.toFile();
        if (!zipFile.isFile()) {
            GrabUtil.download(downloadURL, zipFilePath, i -> totalBytesRead = i);
        }

        String destDirPath = FSGMod.getFsgDir() + "/";

        // Create the destination directory if it doesn't exist
        Path fsgFolderPath = Paths.get(destDirPath);
        if (!Files.isDirectory(fsgFolderPath)) {
            Files.createDirectories(fsgFolderPath);
        }

        unzip(zipFilePath, fsgFolderPath);
        zipFile.delete();
        // Elevate folders
        List<Path> files;
        while ((files = Files.list(FSGMod.getFsgDir()).collect(Collectors.toList())).size() == 1 && Files.isDirectory(files.get(0))) {
            File temp = FSGMod.getFsgDir().resolveSibling("fsg-temp").toFile();
            FileUtils.moveDirectory(files.get(0).toFile(), temp);
            FileUtils.deleteDirectory(FSGMod.getFsgDir().toFile());
            FileUtils.moveDirectory(temp, FSGMod.getFsgDir().toFile());
            FileUtils.deleteDirectory(temp);
        }
    }

    private String getDisplay() {
        if (totalBytesRead == 0) {
            return "Starting download...";
        } else {
            return String.format("Downloading (%d)...", totalBytesRead);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        this.drawCenteredText(matrices, this.textRenderer, this.title, width / 2, height / 3, 0xFFFFFF);
        this.drawCenteredString(matrices, this.textRenderer, getDisplay(), width / 2, height / 2, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        if (thread != null) {
            return;
        }
        thread = new Thread(() -> {
            try {
                downloadAndMove();
                runOnCompletion.run();
            } catch (Exception e) {
                failed = true;
                FSGMod.logError("Error while downloading!", e);
            }
        }, "fsg-download");
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
            client.openScreen(screenOnCompletion);
        }
    }
}
