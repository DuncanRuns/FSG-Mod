package me.duncanruns.fsgmod;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.MinecraftVersion;
import net.minecraft.util.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class LocalFilter {

    private LocalFilter() {
    }

    private static final Path DATA_FILE_PATH = getFsgDir().resolve("fsgmoddata");

    public static Path getFsgDir() {
        return getGameDir().resolve("fsg");
    }

    public static Path getRunPath() {
        Path runForThisVersionPath = getFsgDir().resolve("run." + MinecraftVersion.field_25319.getName() + (OPERATING_SYSTEM.equals(Util.OperatingSystem.WINDOWS) ? ".bat" : ".sh"));
        if (Files.exists(runForThisVersionPath)) {
            return runForThisVersionPath;
        }
        return getFsgDir().resolve("run" + (OPERATING_SYSTEM.equals(Util.OperatingSystem.WINDOWS) ? ".bat" : ".sh"));
    }

    public static Path getGameDir() {
        return FabricLoader.getInstance().getGameDir().toAbsolutePath();
    }

    public static final Util.OperatingSystem OPERATING_SYSTEM = Util.getOperatingSystem();

    private static FilterData loadData() throws IOException, IndexOutOfBoundsException {
        if (!Files.exists(DATA_FILE_PATH)) return null;
        byte[] bytes = Files.readAllBytes(DATA_FILE_PATH);
        byte maxGenerating = bytes[0];
        byte nameLength = bytes[1];
        String filterName = new String(bytes, 2, nameLength);
        return new FilterData(maxGenerating, filterName);
    }

    public static boolean isInstalled() {
        return Files.isDirectory(getFsgDir());
    }

    public static boolean isValid() {
        try {
            return loadData() != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static int getMaxGenerating() throws IOException {
        return Objects.requireNonNull(loadData()).maxGenerating;
    }

    public static void writeData(int maxGenerating, String displayName) throws IOException {
        Files.write(DATA_FILE_PATH, new FilterData(maxGenerating, displayName).toBytes());
    }

    public static String getFilterName() {
        try {
            return Objects.requireNonNull(loadData()).displayName;
        } catch (Exception e) {
            return "Unknown Filter";
        }
    }

    private static class FilterData {
        public final int maxGenerating;
        public final String displayName;

        FilterData(int maxGenerating, String displayName) {
            this.maxGenerating = maxGenerating;
            this.displayName = displayName;
        }

        byte[] toBytes() {
            byte[] bytes = new byte[2 + displayName.length()];
            bytes[0] = (byte) maxGenerating;
            bytes[1] = (byte) displayName.length();
            System.arraycopy(displayName.getBytes(), 0, bytes, 2, displayName.length());
            return bytes;
        }
    }
}
