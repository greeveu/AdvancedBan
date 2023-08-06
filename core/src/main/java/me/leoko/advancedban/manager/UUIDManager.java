package me.leoko.advancedban.manager;

import lombok.Getter;
import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The UUID Manager used to resolve and cache the UUIDs.
 */
@Getter
public class UUIDManager {
    private static UUIDManager instance = null;

    private final Map<String, String> activeUUIDs = new HashMap<>();

    private FetcherMode mode;

    private MethodInterface getUniversalMethods() {
        return Universal.get().getMethods();
    }

    /**
     * Get the uuid manager.
     *
     * @return the uuid manager instance
     */
    public static synchronized UUIDManager get() {
        if (instance == null) instance = new UUIDManager();

        return instance;
    }

    /**
     * Initially setup the uuid manager by determening which {@link FetcherMode} should be used
     * based on the configured preference and the servers capabilities.
     */
    public void setup() {
        MethodInterface mi = getUniversalMethods();
        if (mi.getBoolean(mi.getConfig(), "UUID-Fetcher.Dynamic", true)) {
            if (!mi.isOnlineMode()) {
                mode = FetcherMode.DISABLED;
            } else {
                if (Universal.get().isBungee()) {
                    mode = FetcherMode.MIXED;
                } else {
                    mode = FetcherMode.INTERN;
                }
            }
        } else {
            if (!mi.getBoolean(mi.getConfig(), "UUID-Fetcher.Enabled", true)) {
                mode = FetcherMode.DISABLED;
            } else if (mi.getBoolean(mi.getConfig(), "UUID-Fetcher.Intern", false)) {
                mode = FetcherMode.INTERN;
            } else {
                mode = FetcherMode.RESTFUL;
            }
        }
    }

    /**
     * Initially request the uuid bypassing the cache.<br>
     * If request succeeds the uuid will be automatically entered into the cache.
     *
     * @param name the name
     * @return the uuid
     */
    public String getInitialUUID(String name) {
        MethodInterface mi = getUniversalMethods();
        name = name.toLowerCase();
        if (mode == FetcherMode.DISABLED) {
            return name;
        }

        if (mode == FetcherMode.INTERN || mode == FetcherMode.MIXED) {
            String internUUID = mi.getInternUUID(name);
            if (mode == FetcherMode.INTERN || internUUID != null) {
                return internUUID;
            }
        }

        String uuid = null;
        try {
            uuid = askAPI(mi.getString(mi.getConfig(), "UUID-Fetcher.REST-API.URL"), name, mi.getString(mi.getConfig(), "UUID-Fetcher.REST-API.Key"));
        } catch (IOException e) {
            Universal.get().log("Error -> " + e.getMessage());
            Universal.get().log("!! Failed fetching UUID of " + name);
            Universal.get().log("!! Could not connect to REST-API under " + mi.getString(mi.getConfig(), "UUID-Fetcher.REST-API.URL"));
        }

        if (uuid == null) {
            Universal.get().log("Trying to fetch UUID form BackUp-API...");
            try {
                uuid = askAPI(mi.getString(mi.getConfig(), "UUID-Fetcher.BackUp-API.URL"), name, mi.getString(mi.getConfig(), "UUID-Fetcher.BackUp-API.Key"));
            } catch (IOException e) {
                Universal.get().log("!! Failed fetching UUID of " + name);
                Universal.get().log("!! Could not connect to REST-API under " + mi.getString(mi.getConfig(), "UUID-Fetcher.BackUp-API.URL"));
            }
        }

        if (uuid == null) {
            Universal.get().log("!! !! Warning we have not been able to fetch the UUID of the Player " + name);
            Universal.get().log("!! Make sure that the name is spelled correctly and if it is change your UUID-Fetcher settings!");
        }

        return uuid;
    }

    /**
     * Adds uuid to the cache
     *
     * @param name the name
     * @param uuid the uuid
     */
    public void supplyInternUUID(String name, UUID uuid) {
        if (mode == FetcherMode.INTERN || mode == FetcherMode.MIXED) {
            activeUUIDs.put(name.toLowerCase(), uuid.toString().replace("-", ""));
        }
    }

    /**
     * Convert String to UUID even if dashes are missing
     *
     * @param uuid
     */
    public UUID fromString(String uuid) {
        if (!uuid.contains("-") && uuid.length() == 32) {
            uuid = uuid.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5");
        }

        if (uuid.length() != 36 || !uuid.contains("-")) {
            return null;
        }

        return UUID.fromString(uuid);
    }

    /**
     * Get the uuid to a name.
     *
     * @param name the name
     * @return the uuid
     */
    public String getUUID(String name) {
        String inMemoryUuid = getInMemoryUUID(name);
        return (inMemoryUuid != null) ? inMemoryUuid : getInitialUUID(name);
    }

    /**
     * Gets a uuid from a name only if AdvancedBan
     * already has the uuid/name mapping in memory.
     *
     * @param name the player name
     * @return the nonhyphenated uuid or null if not found
     */
    public String getInMemoryUUID(String name) {
        return activeUUIDs.get(name.toLowerCase());
    }

    private String askAPI(String url, String name, String key) throws IOException {
        MethodInterface mi = getUniversalMethods();
        name = name.toLowerCase();
        HttpURLConnection request = (HttpURLConnection) new URL(url.replace("%NAME%", name).replace("%TIMESTAMP%", new Date().getTime() + "")).openConnection();
        request.connect();

        String uuid = mi.parseJSON(new InputStreamReader(request.getInputStream()), key);

        if (uuid == null) {
            Universal.get().log("!! Failed fetching UUID of " + name);
            Universal.get().log("!! Could not find key '" + key + "' in the servers response");
            Universal.get().log("!! Response: " + request.getResponseMessage());
        } else {
            activeUUIDs.put(name, uuid);
        }
        return uuid;
    }

    /**
     * The fetcher-mode describes how the {@link UUIDManager} resolves UUIDs.
     */
    public enum FetcherMode {
        /**
         * No UUID Fetcher is used. The Username will be treated as an UUID.<br>
         * <b>Recommended for:</b> Servers running in offline mode (cracked).
         */
        DISABLED,

        /**
         * Uses the integrated uuid fetcher from spigot/bungeecord to resolved UUIDs.<br>
         * <b>Recommended for:</b> None (should not be used as a default setting /
         * maybe useful to avoid exceeding API rate limits.)
         */
        INTERN,

        /**
         * Tries to resolve the UUID using the {@link #INTERN} fetcher and uses the
         * {@link #RESTFUL} fetcher as a fallback.<br>
         * <b>Recommended for:</b> Spigot &amp; Bungeecord Servers running in online mode.
         */
        MIXED,

        /**
         * Resolves the UUID using the REST-Services configured in the config.yml.
         * <b>Recommended for:</b> Servers in offline mode which still try to keep track of name changes.
         */
        RESTFUL
    }
}
