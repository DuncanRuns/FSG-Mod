package me.duncanruns.fsgmod;

import me.voidxwalker.autoreset.Atum;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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
        String onlineFilterCode = FSGModConfig.getInstance().onlineFilterCode;
        if (onlineFilterCode != null) {
            return runFilterOnline(onlineFilterCode);
        }
        return runFilterOffline();
    }

    @Nullable
    private static synchronized FSGFilterResult runFilterOnline(String onlineFilterCode) throws IOException, InterruptedException {
        if (!Atum.isRunning()) return null;
        FsgOnlineDb.SeedData data = null;

        do {
            try {
                if (!Atum.isRunning()) return null;
                data = FsgOnlineDb.getSeed(onlineFilterCode).join();
            } catch (FsgOnlineDb.CooldownException e) {
                sleep(e.cooldownMs);
            } catch (Exception e) {
                // Wrap syntax exception in an io exception, which can technically be correct in this case.
                throw new IOException(ExceptionUtils.getRootCause(e));
            }
        } while (data == null);

        return new FSGFilterResult(data.seed, data.token, System.currentTimeMillis());
    }

    @Nullable
    private static FSGFilterResult runFilterOffline() throws IOException, InterruptedException {
        if (!Atum.isRunning()) return null;

        String command = FSGMod.getRunPath().toString();

        Process process = new ProcessBuilder(command).directory(FSGMod.getFsgDir().toFile()).start();

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
