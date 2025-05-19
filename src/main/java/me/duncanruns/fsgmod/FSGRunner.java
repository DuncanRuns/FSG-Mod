package me.duncanruns.fsgmod;

import me.voidxwalker.autoreset.Atum;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;

public final class FSGRunner {
    private static final Pattern SEED_PATTERN = Pattern.compile("[sS]eed.*: ?(-?\\d+)");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[tT]oken.*?: ?(.+)");

    private FSGRunner() {
    }

    @Nullable
    public static FSGFilterResult runFilter() throws IOException, InterruptedException {
        if (!FSGMod.filterSelectedOrInstalled()) throw new IOException("No filter installed!");
        if (LocalFilter.isInstalled()) {
            return runFilterOffline();
        }
        return runFilterOnline(FSGModConfig.getInstance().selectedOnlineFilters);
    }

    @Nullable
    private static synchronized FSGFilterResult runFilterOnline(Set<String> filterIds) throws IOException, InterruptedException {
        if (!Atum.isRunning()) return null;
        FSGOnlineDB.SeedData data = null;

        do {
            try {
                if (!Atum.isRunning()) return null;
                data = FSGOnlineDB.getSeed(filterIds).join();
            } catch (Exception e) {
                Throwable rootCause = ExceptionUtils.getRootCause(e);
                if (rootCause instanceof FSGOnlineDB.CooldownException) {
                    sleep(((FSGOnlineDB.CooldownException) rootCause).cooldownMs);
                } else {
                    // Wrap syntax exception in an io exception, which can technically be correct in this case.
                    throw new IOException(rootCause);
                }
            }
        } while (data == null);

        return new FSGFilterResult(data.seed, data.token, System.currentTimeMillis());
    }

    @Nullable
    private static FSGFilterResult runFilterOffline() throws IOException, InterruptedException {
        if (!Atum.isRunning()) return null;

        String command = LocalFilter.getRunPath().toString();

        Process process = new ProcessBuilder(command).directory(LocalFilter.getFsgDir().toFile()).start();

        List<String> lines = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String readL;

        while ((readL = reader.readLine()) != null) {
            if (!Atum.isRunning()) {
                process.destroy();
                return null;
            }

            lines.add(readL.trim());
            if ((readL = errReader.readLine()) != null) {
                lines.add(readL.trim());
            }
        }

        if (!Atum.isRunning()) {
            process.destroy();
            return null;
        }

        process.waitFor();

        long generationTime = System.currentTimeMillis();

        if (!Atum.isRunning()) return null;

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
}
