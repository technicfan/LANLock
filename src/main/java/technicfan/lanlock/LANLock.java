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
	private static LANLockConfig CONFIG = new LANLockConfig();

	@Override
	public void onInitialize() {
		CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json").toFile();
		loadConfig();
	}

	private static Map<String, String> getPlayerFromWhitelist(String id) {
		String keyQuery = id.contains("-") ? "uuid" : "name";
		for (Map<String, String> player : CONFIG.whitelist()){
			if (player.get(keyQuery).equalsIgnoreCase(id)) {
				return player;
			}
		}
		return null;
	}

	public static String getWhitelistCounterpart(String id) {
		String keyResult = id.contains("-") ? "name" : "uuid";
		Map<String, String> player = getPlayerFromWhitelist(id);
		if (player == null) return null;
		return player.get(keyResult);
	}

	public static List<String> getNames() {
		ArrayList<String> names = new ArrayList<>(Collections.emptyList());
		for (Map<String, String> user : CONFIG.whitelist()) {
			if (!CONFIG.useUuid() || !user.get("uuid").isEmpty()) names.add(user.get("name"));
		}
		return names;
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

	private static Map<String, String> getPlayer(String name) {
		if (checkWhitelist(name)) {
			return getPlayerFromWhitelist(name);
		} else {
			try (HttpClient client = HttpClient.newHttpClient()) {
				HttpRequest request = HttpRequest.newBuilder()
						.uri(URI.create("https://api.minecraftservices.com/minecraft/profile/lookup/name/" + name))
						.build();

				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

				if (response.statusCode() == 200) {
					JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

					return Map.of(
						"uuid", json.get("id").getAsString().replaceAll(
						"(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
						"$1-$2-$3-$4-$5"),
						"name", json.get("name").getAsString()
					);
				}
			} catch (IOException | InterruptedException ignored) {}
			return Map.of(
				"uuid", "",
				"name", name
			);
		}
	}

	public static boolean checkWhitelist(String id) {
		String keyQuery = id.contains("-") ? "uuid" : "name";
		for (Map<String, String> player : CONFIG.whitelist()){
			if (player.get(keyQuery).equalsIgnoreCase(id)) {
				return true;
			}
		}
		return false;
	}

	private static Map<String, String> makePlayer(String name) {
		if (!name.isEmpty()) {
			Map<String, String> player = getPlayer(name);
			if (!CONFIG.useUuid() || !player.get("uuid").isEmpty()) {
				return player;
			}
		}
		return null;
	}

	public static void loadConfig() {
		if (CONFIG_FILE.exists()) {
			try {
				try (FileReader reader = new FileReader(CONFIG_FILE)) {
					LOGGER.info("Loaded LANLock config");
					CONFIG = new Gson().fromJson(reader, LANLockConfig.class);
				}
			} catch (IOException e) {
				LOGGER.error(Arrays.toString(e.getStackTrace()));
			}
		}
	}

	public static void saveConfig(boolean enabled, boolean useUuid, boolean sendNotifications, List<String> whitelist) {
		ArrayList<String> removeIds = new ArrayList<>(Collections.emptyList());
		ArrayList<Map<String, String>> newWhitelist = new ArrayList<>();

		for (String s : whitelist.stream().sorted().distinct().toList()) {
			Map<String, String> player = makePlayer(s);
			if (player != null && !player.get("uuid").isEmpty() &&
					(checkWhitelist(player.get("uuid")) && !checkWhitelist(s))
			) removeIds.add(player.get("uuid"));
			if (player != null) newWhitelist.add(player);
		}
		if (getUseUuid() || useUuid) {
			for (Map<String, String> player : CONFIG.whitelist()) {
				if (player.get("uuid").isEmpty() && !newWhitelist.contains(player)) {
					newWhitelist.add(player);
				}
			}
		}
		for (String id : removeIds) {
			newWhitelist.removeIf(player ->
					player.get("name")
							.equalsIgnoreCase(getWhitelistCounterpart(id)) &&
							player.get("uuid")
									.equals(id)
			);
		}

		CONFIG.setEnabled(enabled);
		CONFIG.setUseUuid(useUuid);
		CONFIG.setSendNotification(sendNotifications);
		CONFIG.setWhitelist(newWhitelist);
		saveToFile();
	}

	private static void saveToFile() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
			writer.write(gson.toJson(CONFIG));
		} catch (IOException e) {
			LOGGER.error(Arrays.toString(e.getStackTrace()));
		}
	}

	// commands
	public static String add(String name) {
		Map<String, String> player = makePlayer(name);
		if (player == null) return null;
		if (!CONFIG.whitelist().contains(player)) {
			CONFIG.addToWhitelist(player);
			saveToFile();
			return player.get("name");
		}
		return "";
	}

	public static boolean remove(String name) {
		if (checkWhitelist(name)) {
			CONFIG.removeFromWhitelist(Map.of(
					"uuid", Objects.requireNonNull(getWhitelistCounterpart(name)),
					"name", name
			));
			saveToFile();
			return true;
		}
		return false;
	}

	public static void setEnabled(boolean value) {
		if (CONFIG.enabled() != value) {
			CONFIG.setEnabled(value);
			saveToFile();
		}
	}

	public static void setUseUuid(boolean value) {
		if (CONFIG.useUuid() != value) {
			CONFIG.setUseUuid(value);
			saveToFile();
		}
	}

	public static void setSendNotification(boolean value) {
		if (CONFIG.sendNotification() != value) {
			CONFIG.setSendNotification(value);
			saveToFile();
		}
	}
}
