package me.duncanruns.fsgmod;

import com.google.gson.JsonObject;
import me.duncanruns.fsgmod.util.GrabUtil;
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

    public static FSGFilterResult runFilter() throws IOException, InterruptedException {
        String onlineFilterCode = FSGModConfig.getInstance().onlineFilterCode;
        if (onlineFilterCode != null) {
            return runFilterOnline(onlineFilterCode);
        }
        return runFilterOffline();
    }

    private static FSGFilterResult runFilterOnline(String onlineFilterCode) throws IOException, InterruptedException {
        JsonObject json;

        do {
            try {
                json = GrabUtil.grabJson("https://fsgonlinedb.duncanruns.xyz/getSeed/" + onlineFilterCode);
            } catch (Exception e) {
                // Wrap syntax exception in an io exception, which can technically be correct in this case.
                throw new IOException(e);
            }
        } while (checkCooldown(json));

        String responseType = json.get("type").getAsString();
        switch (responseType) {
            case "SUCCESS":
                return new FSGFilterResult(json.getAsJsonObject("data").get("seed").getAsString(), json.getAsJsonObject("data").get("token").getAsString(), System.currentTimeMillis());
            case "ERROR":
                throw new IOException("Error from fsgonlinedb: " + json.get("errorMessage").getAsString());
            default:
                throw new IOException("Unexpected response from fsgonlinedb");
        }
    }

    private static boolean checkCooldown(JsonObject jsonObject) throws InterruptedException {
        if (jsonObject.get("type").getAsString().equals("COOLDOWN")) {
            sleep(jsonObject.get("cooldown").getAsLong());
            return true;
        }
        return false;
    }

    private static @Nullable FSGFilterResult runFilterOffline() throws IOException, InterruptedException {
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
}
