package technicfan.lanwhitelist;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

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

        AtomicReference<Boolean> enabled = new AtomicReference<>(LANWhitelist.enabled());
        AtomicReference<Boolean> useUuid = new AtomicReference<>(LANWhitelist.getUseUuid());
        AtomicReference<List<String>> whitelist = new AtomicReference<>(LANWhitelist.getNames());

        general.addEntry(entryBuilder.startBooleanToggle(
                Text.of("Enabled"), enabled.get())
                .setTooltip(Text.of("Enable/Disable the whitelist"))
                .setDefaultValue(true)
                .setSaveConsumer(enabled::set)
                .build());

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

        builder.setSavingRunnable(() -> LANWhitelist.saveConfig(enabled.get(), useUuid.get(), whitelist.get()));

        return builder.build();
    }
}
