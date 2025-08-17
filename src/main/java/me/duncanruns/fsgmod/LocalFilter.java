package me.duncanruns.fsgmod;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.MinecraftVersion;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LocalFilter {

    private static final Pattern SEED_PATTERN = Pattern.compile("[sS]eed.*: ?(-?\\d+)");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[tT]oken.*?: ?(.+)");

    public static boolean running = false;
    private static final Path DATA_FILE_PATH = getFsgDir().resolve("fsgmoddata");
    public static final Util.OperatingSystem OPERATING_SYSTEM = Util.getOperatingSystem();

    private LocalFilter() {
    }

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

    private static FilterData loadData() throws IOException, IndexOutOfBoundsException {
        if (!Files.exists(DATA_FILE_PATH)) return null;
        byte[] bytes = Files.readAllBytes(DATA_FILE_PATH);
        int lengthLeft = bytes.length;
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int maxGenerating = buffer.getInt();
        lengthLeft -= 4;
        byte nameLength = buffer.get();
        lengthLeft -= 1;
        byte[] nameBytes = new byte[nameLength];
        buffer.get(nameBytes);
        lengthLeft -= nameLength;
        String filterName = new String(nameBytes);
        boolean runIsRetimed = lengthLeft == 0 || buffer.get() == 1;
        // lengthLeft -= 1;
        return new FilterData(maxGenerating, filterName, runIsRetimed);
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

    public static void writeData(int maxGenerating, String displayName, boolean runIsRetimed) throws IOException {
        Files.write(DATA_FILE_PATH, new FilterData(maxGenerating, displayName, runIsRetimed).toBytes());
    }

    public static String getFilterName() {
        try {
            return Objects.requireNonNull(loadData()).displayName;
        } catch (Exception e) {
            return "Unknown Filter";
        }
    }

    @Nullable
    private static FSGFilterResult runInternal() throws IOException, InterruptedException {
        String command = getRunPath().toString();

        Process process = new ProcessBuilder(command).directory(getFsgDir().toFile()).start();

        List<String> lines = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String readL;

        while ((readL = reader.readLine()) != null) {
            lines.add(readL.trim());
            if ((readL = errReader.readLine()) != null) {
                lines.add(readL.trim());
            }
        }

        process.waitFor();

        long generationTime = System.currentTimeMillis();

        String seedOut = null;
        String tokenOut = "Token Unavailable";

        for (String line : lines) {
            if (!line.contains(":")) {
                continue;
            }

            Matcher matcher;

            matcher = SEED_PATTERN.matcher(line);
            if (matcher.find()) {
                seedOut = matcher.group(1);
                continue;
            }

            matcher = TOKEN_PATTERN.matcher(line);
            if (matcher.find()) {
                tokenOut = matcher.group(1);
            }
        }

        if (seedOut == null) {
            FSGMod.LOGGER.info("No seed was returned, process output:");
            lines.forEach(FSGMod.LOGGER::info);
            return null;
        }

        return new FSGFilterResult(seedOut, tokenOut, generationTime);
    }

    @Nullable
    static synchronized FSGFilterResult run() throws IOException, InterruptedException {
        try {
            running = true;
            return runInternal();
        } finally {
            running = false;
        }
    }

    public static boolean getShouldRetime() throws IOException {
        return Objects.requireNonNull(loadData()).runIsRetimed;
    }

    private static class FilterData {
        public final int maxGenerating;
        public final String displayName;
        public final boolean runIsRetimed;

        FilterData(int maxGenerating, String displayName, boolean runIsRetimed) {
            this.maxGenerating = maxGenerating;
            this.displayName = displayName;
            this.runIsRetimed = runIsRetimed;
        }

        byte[] toBytes() {
            byte[] displayNameBytes = displayName.getBytes();
            ByteBuffer buffer = ByteBuffer.allocate(6 + displayNameBytes.length);
            buffer.putInt(maxGenerating);
            buffer.put((byte) displayNameBytes.length);
            buffer.put(displayNameBytes);
            buffer.put((byte) (runIsRetimed ? 1 : 0));
            return buffer.array();
        }
    }


}
