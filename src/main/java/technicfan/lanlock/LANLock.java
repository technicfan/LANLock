package technicfan.lanlock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class LANLock implements ModInitializer {
	public static final String MOD_ID = "lanlock";
	private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static File CONFIG_FILE;
	private static Config CONFIG = new Config();

	@Override
	public void onInitialize() {
		CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json").toFile();
		loadConfig();
	}

	private static PlayerEntry getPlayerFromWhitelist(String id) {
		return CONFIG.whitelistPlayer(id);
	}

	public static String getWhitelistCounterpart(String id) {
		String keyResult = id.contains("-") ? "name" : "uuid";
		PlayerEntry player = CONFIG.whitelistPlayer(id);
		if (player == null) return null;
		return player.get(keyResult);
	}

	public static List<String> getNames() {
		return CONFIG.whitelistNames(true);
	}

	public static boolean getUseUuid() {
		return CONFIG.useUuid();
	}

	public static boolean enabled() {
		return CONFIG.enabled();
	}

	public static boolean getSendNotification() {
		return CONFIG.sendNotification();
	}

	private static PlayerEntry getPlayer(String name, boolean allowOffline) {
		if (!name.isEmpty()) {
            PlayerEntry player;
            if (checkWhitelist(name)) {
                player = getPlayerFromWhitelist(name);
            } else {
                try {
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("https://api.minecraftservices.com/minecraft/profile/lookup/name/" + name))
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

                        player = new PlayerEntry(
                            json.get("id").getAsString().replaceAll(
                            "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                            "$1-$2-$3-$4-$5"),
                            json.get("name").getAsString()
                        );
                    } else {
                        throw new InterruptedException();
                    }
                } catch (IOException | InterruptedException e) {
                    player = new PlayerEntry("", name);
                }
            }
			if (allowOffline || !player.uuid.isEmpty()) {
				return player;
			}
        }
        return null;
	}

	public static boolean checkWhitelist(String id) {
		return CONFIG.whitelistContains(id);
	}

	protected static void loadConfig() {
		if (CONFIG_FILE.exists()) {
			try {
				try (FileReader reader = new FileReader(CONFIG_FILE)) {
					LOGGER.info("Loaded LANLock config");
					CONFIG = new Gson().fromJson(reader, Config.class);
				}
			} catch (IOException e) {
				LOGGER.error(Arrays.toString(e.getStackTrace()));
			}
		}
	}

	public static void updateConfig(boolean enabled, boolean useUuid, boolean sendNotification, List<String> whitelist) {
        ArrayList<PlayerEntry> newWhitelist = new ArrayList<>(CONFIG.whitelist());
        ArrayList<String> names = new ArrayList<>(CONFIG.whitelistNames(false));
        List<String> offlineNames = CONFIG.whitelistOfflineNames();

        for (String s : whitelist.stream().map(String::toLowerCase).distinct().toList()) {
            if (!names.contains(s)) {
                PlayerEntry player = getPlayer(s, !useUuid);
                if (player != null && !player.uuid.isEmpty()) {
                    newWhitelist.remove(player);
                }
                if (player != null) {
                    newWhitelist.add(player);
                    names.remove(s);
                }
            } else {
                names.remove(s);
            }
        }
        newWhitelist.removeIf(p -> {
            return (useUuid || !offlineNames.contains(p.name)) && names.contains(p.name);
        });

        CONFIG = new Config(enabled, useUuid, sendNotification, newWhitelist.stream().sorted().toList());
		saveConfig();
	}

	private static void saveConfig() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
			writer.write(gson.toJson(CONFIG));
		} catch (IOException e) {
			LOGGER.error(Arrays.toString(e.getStackTrace()));
		}
	}

	// commands
	public static String add(String name) {
		PlayerEntry player = getPlayer(name, !CONFIG.useUuid());
		if (player == null) return null;
        Config temp = CONFIG;
        CONFIG = CONFIG.addToWhitelist(player);
        if (temp != CONFIG) {
			saveConfig();
			return player.name;
        }
		return "";
	}

	public static boolean remove(String name) {
		if (checkWhitelist(name)) {
			CONFIG = CONFIG.removeFromWhitelist(new PlayerEntry("", name));
			saveConfig();
			return true;
		}
		return false;
	}

	public static void setEnabled(boolean value) {
		if (CONFIG.enabled() != value) {
			CONFIG = CONFIG.setEnabled(value);
			saveConfig();
		}
	}

	public static void setUseUuid(boolean value) {
		if (CONFIG.useUuid() != value) {
			CONFIG = CONFIG.setUseUuid(value);
			saveConfig();
		}
	}

	public static void setSendNotification(boolean value) {
		if (CONFIG.sendNotification() != value) {
			CONFIG = CONFIG.setSendNotification(value);
			saveConfig();
		}
	}
}
