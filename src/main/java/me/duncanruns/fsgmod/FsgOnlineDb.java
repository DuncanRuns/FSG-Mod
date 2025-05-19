package me.duncanruns.fsgmod;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.duncanruns.fsgmod.util.GrabUtil;
import me.voidxwalker.autoreset.Atum;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.lang.Thread.sleep;

// Class to interact with fsgonlinedb.duncanruns.xyz
public class FSGOnlineDB {
    private static final String BASE_URL = "https://fsgonlinedb.duncanruns.xyz";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static JsonArray cachedFilters = null;

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
                JsonObject response = GrabUtil.grabJson(BASE_URL + "/getSeed/" + filterCode);
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

                JsonObject response = GrabUtil.grabJson(BASE_URL + "/getSeedRandomFilter?" + filtersParam);
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

                JsonObject response = GrabUtil.grabJson(BASE_URL + "/filters");
                if (!"SUCCESS".equals(response.get("type").getAsString())) {
                    throw new IOException("Error from fsgonlinedb: " + response.get("errorMessage").getAsString());
                }

                cachedFilters = response.getAsJsonArray("filters");
                return parseFilters(cachedFilters);
            } catch (IOException e) {
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
                            obj.get("maxGenerating").getAsInt()
                    );
                })
                .collect(Collectors.toList());
    }

    @Nullable
    static synchronized FSGFilterResult runFilterOnline(Set<String> filterIds) throws IOException, InterruptedException {
        if (!Atum.isRunning()) return null;
        SeedData data = null;

        do {
            try {
                if (!Atum.isRunning()) return null;
                data = getSeed(filterIds).join();
            } catch (Exception e) {
                Throwable rootCause = ExceptionUtils.getRootCause(e);
                if (rootCause instanceof CooldownException) {
                    sleep(((CooldownException) rootCause).cooldownMs);
                } else {
                    // Wrap exception in an io exception, which can technically be correct in this case.
                    throw new IOException(rootCause);
                }
            }
        } while (data == null);

        return new FSGFilterResult(data.seed, data.token, System.currentTimeMillis());
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

        public FilterInfo(String id, String displayName, List<String> supportedVersions, int maxGenerating) {
            this.id = id;
            this.displayName = displayName;
            this.supportedVersions = supportedVersions;
            this.maxGenerating = maxGenerating;
        }
    }

    public static class CooldownException extends RuntimeException {
        public final long cooldownMs;

        public CooldownException(long cooldownMs) {
            super("Rate limited, cooldown: " + cooldownMs + "ms");
            this.cooldownMs = cooldownMs;
        }
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
}
