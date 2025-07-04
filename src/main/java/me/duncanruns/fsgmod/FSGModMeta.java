package me.duncanruns.fsgmod;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.duncanruns.fsgmod.util.GrabUtil;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FSGModMeta {
    private static final String META_URL = "https://raw.githubusercontent.com/DuncanRuns/FSG-Mod/refs/heads/meta/meta.json";
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static JsonArray cachedFilters = null;

    /**
     * Gets the list of available local filters from the meta.json file
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

                JsonObject response = GrabUtil.grabJson(META_URL);
                cachedFilters = response.getAsJsonArray("filters");
                return parseFilters(cachedFilters);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, EXECUTOR);
    }

    /**
     * Gets the list of available local filters from the meta.json file
     *
     * @return A future that will complete with the list of filters
     */
    public static CompletableFuture<List<FilterInfo>> getFilters() {
        return getFilters(false);
    }

    private static List<FilterInfo> parseFilters(JsonArray filtersArray) {
        return StreamSupport.stream(filtersArray.spliterator(), false)
                .map(JsonObject.class::cast)
                .map(obj -> {
                    // Parse supported versions array
                    List<String> supportedVersions = new ArrayList<>();
                    if (obj.has("supports")) {
                        JsonArray versionsArray = obj.getAsJsonArray("supports");
                        versionsArray.forEach(element -> supportedVersions.add(element.getAsString()));
                    }

                    // Parse download links
                    JsonObject downloadObj = obj.has("download") ? obj.getAsJsonObject("download") : new JsonObject();
                    
                    // Parse run scripts if present
                    String runBat = obj.has("run.bat") ? obj.get("run.bat").getAsString() : null;
                    String runSh = obj.has("run.sh") ? obj.get("run.sh").getAsString() : null;

                    return new FilterInfo(
                            obj.get("name").getAsString(),
                            obj.has("version") ? obj.get("version").getAsString() : null,
                            supportedVersions,
                            obj.get("maxGenerating").getAsInt(),
                            downloadObj,
                            runBat,
                            runSh
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Gets filters that support the specified Minecraft version
     *
     * @param minecraftVersion The Minecraft version to check for
     * @return A future that will complete with the list of compatible filters
     */
    public static CompletableFuture<List<FilterInfo>> getFiltersForVersion(String minecraftVersion) {
        return getFilters().thenApply(filters ->
                filters.stream()
                        .filter(filter -> filter.supportedVersions.contains(minecraftVersion))
                        .collect(Collectors.toList())
        );
    }

    /**
     * Gets the download URL for a filter based on the current OS
     *
     * @param filter The filter to get the download URL for
     * @return The download URL, or null if not available for this OS
     */
    @Nullable
    public static String getDownloadUrl(FilterInfo filter) {
        String osCode = FSGMod.getOS3LetterCode() + (FSGMod.onArm() ? "arm" : "");
        return filter.downloadLinks.has(osCode) ? filter.downloadLinks.get(osCode).getAsString() : null;
    }

    public static class FilterInfo {
        public final String name;
        public final String version;
        public final List<String> supportedVersions;
        public final int maxGenerating;
        public final JsonObject downloadLinks;
        public final String runBatScript;
        public final String runShScript;

        public FilterInfo(String name, String version, List<String> supportedVersions, 
                         int maxGenerating, JsonObject downloadLinks, 
                         String runBatScript, String runShScript) {
            this.name = name;
            this.version = version;
            this.supportedVersions = supportedVersions;
            this.maxGenerating = maxGenerating;
            this.downloadLinks = downloadLinks;
            this.runBatScript = runBatScript;
            this.runShScript = runShScript;
        }

        /**
         * Gets the display name of the filter, including version if available
         *
         * @return The display name
         */
        public String getDisplayName() {
            return version != null ? name + " v" + version : name;
        }

        @Override
        public String toString() {
            return "FilterInfo{" +
                    "name='" + name + '\'' +
                    ", version='" + version + '\'' +
                    ", supportedVersions=" + supportedVersions +
                    ", maxGenerating=" + maxGenerating +
                    ", downloadLinks=" + downloadLinks +
                    ", runBatScript='" + runBatScript + '\'' +
                    ", runShScript='" + runShScript + '\'' +
                    '}';
        }
    }
}