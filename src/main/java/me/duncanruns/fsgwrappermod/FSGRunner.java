package me.duncanruns.fsgwrappermod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FSGRunner {
    private static final Pattern SEED_PATTERN = Pattern.compile("[sS]eed.*: ?(-?\\d+)");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[tT]oken.*?: ?(.+)");

    private FSGRunner() {
    }

    public static FSGFilterResult runFilter() throws IOException, InterruptedException {
        String command = FSGWrapperMod.getRunPath().toString();

        Process process = new ProcessBuilder(command).directory(FSGWrapperMod.getFsgDir().toFile()).start();


        List<String> lines = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String readL;
        while ((readL = reader.readLine()) != null) {
            lines.add(readL.trim());
        }
        process.waitFor();

        String seedOut = null;
        String tokenOut = "Token Unavailable";

        FSGWrapperMod.LOGGER.info("Filter Out:");

        for (String line : lines) {
            FSGWrapperMod.LOGGER.info(line);
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
            return null;
        }

        return new FSGFilterResult(seedOut, tokenOut);
    }
}
