package technicfan.lanlock.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import technicfan.lanlock.LANLock;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static me.shedaniel.clothconfig2.api.ConfigBuilder.create;

public class LANLockConfigScreen {
    public static Screen getScreen(Screen parent) {
        ConfigBuilder builder = create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("lanlock.config.title"));

        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("lanlock.config.general"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        AtomicReference<Boolean> enabled = new AtomicReference<>(LANLock.enabled());
        AtomicReference<Boolean> useUuid = new AtomicReference<>(LANLock.getUseUuid());
        AtomicReference<Boolean> sendNotifications = new AtomicReference<>(LANLock.getSendNotification());
        AtomicReference<List<String>> whitelist = new AtomicReference<>(LANLock.getNames());

        general.addEntry(entryBuilder.startBooleanToggle(
                Text.translatable("lanlock.config.enabled"), enabled.get())
                .setTooltip(Text.translatable("lanlock.config.enabled.description"))
                .setDefaultValue(true)
                .setSaveConsumer(enabled::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(
                Text.translatable("lanlock.config.useUuid"), useUuid.get())
                .setTooltip(Text.translatable("lanlock.config.useUuid.description"))
                .setDefaultValue(true)
                .setSaveConsumer(useUuid::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(
                Text.translatable("lanlock.config.sendNotification"), sendNotifications.get())
                .setTooltip(Text.translatable("lanlock.config.sendNotification.description"))
                .setDefaultValue(true)
                .setSaveConsumer(sendNotifications::set)
                .build());

        general.addEntry(entryBuilder.startStrList
                (Text.translatable("lanlock.config.whitelist"), whitelist.get())
                .setTooltip(Text.translatable("lanlock.config.whitelist.description"))
                .setExpanded(true)
                .setSaveConsumer(whitelist::set)
                .build());

        builder.setSavingRunnable(() -> CompletableFuture.runAsync(() -> LANLock.saveConfig(enabled.get(), useUuid.get(), sendNotifications.get(), whitelist.get())));

        return builder.build();
    }
}
