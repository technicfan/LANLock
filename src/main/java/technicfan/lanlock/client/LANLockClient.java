package technicfan.lanlock.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.ClientModInitializer;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LANLockClient implements ClientModInitializer {
	public static final String MOD_ID = "lanlock";
	private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static File CONFIG_FILE;
	private static Config CONFIG = new Config();

	@Override
	public void onInitializeClient() {
		CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json").toFile();
		if (CONFIG_FILE.exists()) CONFIG = loadConfig();
	}

	private static String getWhitelistCounterpart(String id) {
		String keyQuery, keyResult;
		if (id.contains("-")) {
			keyQuery = "uuid";
			keyResult = "name";
		} else {
			keyQuery = "name";
			keyResult = "uuid";
		}
		for (Player player : CONFIG.whitelist()){
			if (player.get(keyQuery).equals(id)) {
				return player.get(keyResult);
			}
		}
		return null;
	}

	private Config loadConfig() {
		try {
			try (FileReader reader = new FileReader(CONFIG_FILE)) {
				LOGGER.info("Loaded LANLock config");
				return new Gson().fromJson(reader, Config.class);
			}
		} catch (IOException e) {
			LOGGER.error(Arrays.toString(e.getStackTrace()));
		}
		return new Config();
	}

	private static String getUuid(String name) {
		if (checkWhitelist(name)) {
			return getWhitelistCounterpart(name);
		} else {
			try (HttpClient client = HttpClient.newHttpClient()) {
				HttpRequest request = HttpRequest.newBuilder()
						.uri(URI.create("https://api.mojang.com/users/profiles/minecraft/" + name))
						.build();

				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

				if (response.statusCode() == 200) {
					JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

					return json.get("id").getAsString().replaceAll(
							"(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
							"$1-$2-$3-$4-$5"
					);
				}
				return "";
			} catch (IOException | InterruptedException e) {
				return "";
			}
		}
	}

	public static List<String> getNames() {
		ArrayList<String> names = new ArrayList<>(Collections.emptyList());
		for (Player user : CONFIG.whitelist()) {
			if (CONFIG.useUuid() || !user.get("uuid").isEmpty()) names.add(user.get("name"));
		}
		return names;
	}

	public static boolean getUseUuid() {
		return CONFIG.useUuid();
	}

	public static boolean enabled() {
		return CONFIG.enabled();
	}


	public static boolean checkWhitelist(String id) {
		String keyQuery = id.contains("-") ? "uuid" : "name";
		for (Player player : CONFIG.whitelist()){
			if (player.get(keyQuery).equals(id)) {
				return true;
			}
		}
		return false;
	}

	public static void saveConfig(boolean enabled, boolean useUuid, List<String> whitelist) {
		ArrayList<String> removeIds = new ArrayList<>(Collections.emptyList());
		ArrayList<Player> newWhitelist = new ArrayList<>();

		for (String s : whitelist.stream().sorted().distinct().toList()) {
			String id = getUuid(s);
			if (!useUuid || !id.isEmpty()) {
				if (!id.isEmpty() &&
						(checkWhitelist(id) && !checkWhitelist(s))
				) removeIds.add(id);
				newWhitelist.add(new Player(id, s));
			}
		}
		for (String id : removeIds) {
			newWhitelist.removeIf(player ->
					player.get("name")
							.equals(getWhitelistCounterpart(id)) &&
							player.get("uuid")
									.equals(id)
			);
		}

		CONFIG.setEnabled(enabled);
		CONFIG.setUseUuid(useUuid);
		CONFIG.setWhitelist(newWhitelist);
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
			writer.write(gson.toJson(CONFIG));
		} catch (IOException e) {
			LOGGER.error(Arrays.toString(e.getStackTrace()));
		}
	}
}
