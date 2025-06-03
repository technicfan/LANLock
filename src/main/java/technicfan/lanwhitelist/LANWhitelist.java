package technicfan.lanwhitelist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LANWhitelist implements ModInitializer {
	public static final String MOD_ID = "lanwhitelist";
	private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static File CONFIG_FILE;
    private static Config CONFIG = new Config();

	@Override
	public void onInitialize() {
        CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json").toFile();
        if (CONFIG_FILE.exists()) CONFIG = loadConfig();
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

    public static String getWhitelistCounterpart(String id) {
        return CONFIG.whitelist().getCounterPart(id);
    }

    public static boolean checkWhitelist(String id) {
        return CONFIG.whitelist().contains(id);
    }

    public static void saveConfig(boolean enabled, boolean useName, Whitelist whitelist) {
        CONFIG.setEnabled(enabled);
        CONFIG.setUseUuid(useName);
        CONFIG.setWhitelist(whitelist);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(LANWhitelist.CONFIG_FILE)) {
            writer.write(gson.toJson(CONFIG));
        } catch (IOException e) {
            LANWhitelist.LOGGER.error(Arrays.toString(e.getStackTrace()));
        }
    }

    private Config loadConfig() {
        try {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                LOGGER.info("Loaded config");
                return new Gson().fromJson(reader, Config.class);
            }
        } catch (IOException e) {
            LANWhitelist.LOGGER.error(Arrays.toString(e.getStackTrace()));
        }
        return new Config();
    }

}
