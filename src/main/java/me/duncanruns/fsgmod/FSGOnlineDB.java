package me.duncanruns.fsgmod;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.duncanruns.fsgmod.util.GrabUtil;
import me.voidxwalker.autoreset.Atum;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.lang.Thread.sleep;

// Class to interact with filteredseed.com
public class FSGOnlineDB {
    private static final String HTTPS_BASE_URL = "https://filteredseed.com";
    private static final String HTTP_BASE_URL = "http://filteredseed.com:8080";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static JsonArray cachedFilters = null;
    private static String urlToUse = null;
    private static final Random RANDOM = new Random();
    private static final Map<String, Queue<String>> practiceSeedCache = Collections.synchronizedMap(new HashMap<>());

    private static String getBaseURL() {
        if (urlToUse != null) return urlToUse;

        for (int i = 0; i < 3; i++) {
            try {
                GrabUtil.grab(HTTPS_BASE_URL);
                urlToUse = HTTPS_BASE_URL;
                return urlToUse;
            } catch (Exception e) {
                FSGMod.LOGGER.warn("Failed to connect to FSGOnlineDB (https), retrying...");
            }
        }
        FSGMod.LOGGER.warn("Failed to connect to FSGOnlineDB with https, falling back to http.");

        for (int i = 0; i < 3; i++) {
            try {
                GrabUtil.grab(HTTP_BASE_URL);
                FSGMod.LOGGER.warn("Failed to connect to FSGOnlineDB with https, falling back to http.");
                urlToUse = HTTP_BASE_URL;
                return urlToUse;
            } catch (IOException ex) {
                FSGMod.LOGGER.warn("Failed to connect to FSGOnlineDB (http), retrying...");
            }
        }

        throw new RuntimeException("Failed to connect to FSGOnlineDB, although this error should not happen.");
    }

    /**
     * Gets a seed from the specified filter
     *
     * @param filterCode The filter code to get a seed for
     * @return A future that will complete with the seed data
     */
    public static CompletableFuture<SeedData> getSeed(String filterCode) {
        if (filterCode == null) {
            throw new IllegalArgumentException("No filter code provided");
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonObject response = GrabUtil.grabJson(getBaseURL() + "/getSeed/" + filterCode);
                String type = response.get("type").getAsString();

                if ("SUCCESS".equals(type)) {
                    JsonObject data = response.getAsJsonObject("data");
                    return new SeedData(
                            data.get("seed").getAsString(),
                            data.get("token").getAsString(),
                            null
                    );
                } else if ("COOLDOWN".equals(type)) {
                    throw new CooldownException(response.get("cooldown").getAsLong());
                } else {
                    throw new IOException("Error from fsgonlinedb: " + response.get("errorMessage").getAsString());
                }
            } catch (IOException e) {
                urlToUse = null;
                throw new RuntimeException(e);
            }
        }, EXECUTOR);
    }

    /**
     * Gets a seed from a random filter among the provided list
     *
     * @param filterCodes List of filter codes to choose from
     * @return A future that will complete with the seed data
     */
    public static CompletableFuture<SeedData> getSeed(Collection<String> filterCodes) {
        if (filterCodes == null || filterCodes.isEmpty()) {
            throw new IllegalArgumentException("No filter codes provided");
        }
        if (filterCodes.size() == 1) {
            return getSeed(filterCodes.iterator().next());
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                String filtersParam = filterCodes.stream()
                        .map(code -> "filters=" + code)
                        .collect(Collectors.joining("&"));

                JsonObject response = GrabUtil.grabJson(getBaseURL() + "/getSeedRandomFilter?" + filtersParam);
                String type = response.get("type").getAsString();

                if ("SUCCESS".equals(type)) {
                    JsonObject data = response.getAsJsonObject("data");
                    return new SeedData(
                            data.get("seed").getAsString(),
                            data.get("token").getAsString(),
                            data.get("filter").getAsString()
                    );
                } else if ("COOLDOWN".equals(type)) {
                    throw new CooldownException(response.get("cooldown").getAsLong());
                } else {
                    throw new IOException("Error from fsgonlinedb: " + response.get("errorMessage").getAsString());
                }
            } catch (IOException e) {
                urlToUse = null;
                throw new RuntimeException(e);
            }
        }, EXECUTOR);
    }

    public static CompletableFuture<List<FilterInfo>> getFilters() {
        return getFilters(false);
    }

    /**
     * Gets the list of available filters
     *
     * @param refresh Whether to refresh the cache
     * @return A future that will complete with the list of filters
     */
    public static CompletableFuture<List<FilterInfo>> getFilters(boolean refresh) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (cachedFilters != null && !refresh) {
                    return parseFilters(cachedFilters);
                }

                JsonObject response = GrabUtil.grabJson(getBaseURL() + "/filters");
                if (!"SUCCESS".equals(response.get("type").getAsString())) {
                    throw new IOException("Error from fsgonlinedb: " + response.get("errorMessage").getAsString());
                }

                cachedFilters = response.getAsJsonArray("filters");
                return parseFilters(cachedFilters);
            } catch (IOException e) {
                urlToUse = null;
                throw new RuntimeException(e);
            }
        }, EXECUTOR);
    }

    private static List<FilterInfo> parseFilters(JsonArray filtersArray) {
        return StreamSupport.stream(filtersArray.spliterator(), false)
                .map(JsonObject.class::cast)
                .map(obj -> {
                    // Parse supportedVersions array
                    List<String> supportedVersions = new ArrayList<>();
                    if (obj.has("supportedVersions")) {
                        JsonArray versionsArray = obj.getAsJsonArray("supportedVersions");
                        versionsArray.forEach(element -> supportedVersions.add(element.getAsString()));
                    }

                    return new FilterInfo(
                            obj.get("id").getAsString(),
                            obj.get("displayName").getAsString(),
                            supportedVersions,
                            obj.get("maxGenerating").getAsInt(),
                            obj.get("runIsRetimed").getAsBoolean()
                    );
                })
                .collect(Collectors.toList());
    }

    @Nullable
    static synchronized FSGFilterResult runFilterOnline(Set<String> filterIds, boolean practiceMode) throws IOException, InterruptedException {
        if (!Atum.isRunning()) return null;
        SeedData data = null;

        do {
            try {
                if (!Atum.isRunning()) return null;
                data = practiceMode ? getRandomUsedSeed(filterIds).join() : getSeed(filterIds).join();
            } catch (Exception e) {
                Throwable rootCause = ExceptionUtils.getRootCause(e);
                if (rootCause instanceof CooldownException) {
                    FSGMod.LOGGER.info("Rate limited, cooldown: {}ms", ((CooldownException) rootCause).cooldownMs);
                    long timeDone = System.currentTimeMillis() + ((CooldownException) rootCause).cooldownMs;
                    while (System.currentTimeMillis() < timeDone) {
                        sleep(10);
                        if (!Atum.isRunning()) return null;
                    }
                } else {
                    // Wrap exception in an io exception, which can technically be correct in this case.
                    throw new IOException(rootCause);
                }
            }
        } while (data == null);

        return new FSGFilterResult(data.seed, data.token, System.currentTimeMillis());
    }

    /**
     * Gets the display name for a filter code
     *
     * @param filterCode The filter code to get the display name for
     * @return A future that will complete with the display name, or null if not found
     */
    public static CompletableFuture<String> getFilterDisplayName(String filterCode) {
        return getFilters(false).thenApply(filters ->
                filters.stream()
                        .filter(filter -> filter.id.equals(filterCode))
                        .map(filter -> filter.displayName)
                        .findFirst()
                        .orElse(null)
        );
    }

    public static CompletableFuture<Integer> getMaxGenerating(Collection<String> filterCodes) {
        return getFilters(false)
                .thenApply(
                        filters -> filters.stream()
                                .filter(filter -> filterCodes.contains(filter.id))
                                .mapToInt(filter -> filter.maxGenerating)
                                .min().orElse(1)
                );
    }

    public static CompletableFuture<Boolean> getShouldRetime(Set<String> selectedOnlineFilters) {
        return getFilters(false)
                .thenApply(
                        filters -> filters.stream()
                                .filter(filter -> selectedOnlineFilters.contains(filter.id))
                                .anyMatch(filter -> filter.runIsRetimed)
                );
    }

    public static CompletableFuture<String> getFilterInfoDoc() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonObject jsonObject = GrabUtil.grabJson(getBaseURL() + "/getFilterInfoDoc");
                return jsonObject.get("doc").getAsString();
            } catch (Exception e) {
                urlToUse = null;
                throw new RuntimeException(e);
            }
        }, EXECUTOR);
    }

    public static CompletableFuture<SeedData> getRandomUsedSeed(Collection<String> filterCodes) {
        if (filterCodes == null || filterCodes.isEmpty()) {
            throw new IllegalArgumentException("No filter codes provided");
        }
        List<String> filterCodesList = new ArrayList<>(filterCodes);
        String filterCode = filterCodes.size() == 1 ? filterCodesList.get(0) : filterCodesList.get(RANDOM.nextInt(filterCodes.size()));
        Queue<String> cache = practiceSeedCache.computeIfAbsent(filterCode, k -> new LinkedList<>());
        if (!cache.isEmpty()) {
            return CompletableFuture.completedFuture(new SeedData(cache.poll(), null, filterCode));
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonObject jsonObject = GrabUtil.grabJson(getBaseURL() + "/getRandomUsedSeeds/" + filterCode + "/50");
                if ("SUCCESS".equals(jsonObject.get("type").getAsString())) {
                    JsonArray seeds = jsonObject.getAsJsonArray("seeds");
                    for (JsonElement seed : seeds) {
                        cache.add(seed.getAsString());
                    }
                    return new SeedData(cache.poll(), null, filterCode);
                } else {
                    throw new IOException("Error from fsgonlinedb: " + jsonObject.get("errorMessage").getAsString());
                }
            } catch (Exception e) {
                urlToUse = null;
                throw new RuntimeException(e);
            }
        });
    }

    public static class SeedData {
        public final String seed;
        public final String token;
        public final String filter; // null for getSeed endpoint

        public SeedData(String seed, String token, String filter) {
            this.seed = seed;
            this.token = token;
            this.filter = filter;
        }
    }

    public static class FilterInfo {
        public final String id;
        public final String displayName;
        public final List<String> supportedVersions;
        public final int maxGenerating;
        public final boolean runIsRetimed;

        public FilterInfo(String id, String displayName, List<String> supportedVersions, int maxGenerating, boolean runIsRetimed) {
            this.id = id;
            this.displayName = displayName;
            this.supportedVersions = supportedVersions;
            this.maxGenerating = maxGenerating;
            this.runIsRetimed = runIsRetimed;
        }
    }

    public static class CooldownException extends RuntimeException {
        public final long cooldownMs;

        public CooldownException(long cooldownMs) {
            super("Rate limited, cooldown: " + cooldownMs + "ms");
            this.cooldownMs = cooldownMs;
        }
    }
}
