package me.duncanruns.fsgmod;

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
        String command = FSGMod.getRunPath().toString();

        Process process = new ProcessBuilder(command).directory(FSGMod.getFsgDir().toFile()).start();


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

        return new FSGFilterResult(seedOut, tokenOut);
    }
}
