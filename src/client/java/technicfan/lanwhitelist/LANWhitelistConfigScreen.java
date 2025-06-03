package technicfan.lanwhitelist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

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
import java.util.concurrent.atomic.AtomicReference;

import static me.shedaniel.clothconfig2.api.ConfigBuilder.create;

public class LANWhitelistConfigScreen {
    public static Screen getScreen(Screen parent) {
        ConfigBuilder builder = create()
                .setParentScreen(parent)
                .setTitle(Text.of("LANWhitelist Config"));

        ConfigCategory general = builder.getOrCreateCategory(Text.of("LANWhitelist Settings"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        AtomicReference<List<String>> whitelist = new AtomicReference<>(LANWhitelist.getNames());
        AtomicReference<Boolean> useUuid = new AtomicReference<>(LANWhitelist.getUseUuid());

        general.addEntry(entryBuilder.startBooleanToggle(
                Text.of("Use UUID for whitelist check (recommended)"), useUuid.get())
                .setTooltip(Text.of("Disable only for testing as it will use the username which less secure"))
                .setDefaultValue(true)
                .setSaveConsumer(useUuid::set)
                .build());

        general.addEntry(entryBuilder.startStrList
                (Text.of("Whitelisted Players"), whitelist.get())
                .setTooltip(Text.of("Edit whitelist for LAN worlds"))
                .setExpanded(true)
                .setSaveConsumer(whitelist::set)
                .build());

        builder.setSavingRunnable(() -> save(whitelist.get(), useUuid.get()));

        return builder.build();
    }

    private static String getUuid(String name) {
        if (LANWhitelist.checkWhitelist(name)) {
            return LANWhitelist.getWhitelistCounterpart(name);
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

    private static void save(List<String> whitelist, Boolean useUuid) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            ArrayList<String> removeIds = new ArrayList<>(Collections.emptyList());
            Whitelist newList = new Whitelist();

            for (String s : whitelist) {
                String id = getUuid(s);
                if ((!useUuid || !id.isEmpty()) && !newList.contains(s)) {
                    if (!id.isEmpty() &&
                        (LANWhitelist.checkWhitelist(id) && !LANWhitelist.checkWhitelist(s))
                    ) removeIds.add(id);
                    newList.add(new Player(s, id));
                }
            }
            for (String id : removeIds) {
                newList.removeIf(player ->
                        player.get("name")
                            .equals(LANWhitelist.getWhitelistCounterpart(id)) &&
                        player.get("uuid")
                            .equals(id)
                );
            }

            Config config = new Config(useUuid, newList);

            try (FileWriter writer = new FileWriter(LANWhitelist.CONFIG_FILE)) {
                writer.write(gson.toJson(config));
            }

            LANWhitelist.setConfig(config.useUuid(), config.whitelist());
        } catch (IOException e) {
            LANWhitelist.LOGGER.error(Arrays.toString(e.getStackTrace()));
        }
    }
}
