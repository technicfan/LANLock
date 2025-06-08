package technicfan.lanlock;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;

public class LANLockClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                ClientCommandManager.literal("lanlock")
                    .then(ClientCommandManager.literal("list").executes(LANLockClient::list))
                    .then(ClientCommandManager.literal("reload").executes(LANLockClient::reload))
                    .then(ClientCommandManager.literal("add")
                        .then(ClientCommandManager.argument("player", StringArgumentType.string())
                            .suggests(new PlayerSuggestionProvider())
                            .executes(LANLockClient::add)))
                    .then(ClientCommandManager.literal("remove")
                        .then(ClientCommandManager.argument("player", StringArgumentType.string())
                            .suggests(new WhitelistPlayerSuggestionProvider())
                            .executes(LANLockClient::remove)))
                    .then(ClientCommandManager.literal("enabled")
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                            .executes(LANLockClient::toggle))
                        .executes(LANLockClient::enabled))
                    .then(ClientCommandManager.literal("useUuid")
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                            .executes(LANLockClient::toggleUseUuid))
                        .executes(LANLockClient::useUuid))
                    .then(ClientCommandManager.literal("notify")
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                            .executes(LANLockClient::toggleNotify))
                        .executes(LANLockClient::notify))
            );
        });
    }

    private static int list(CommandContext<FabricClientCommandSource> commandContext) {
        if (LANLock.getNames().isEmpty()) {
            commandContext.getSource().sendFeedback(Text.translatable("lanlock.command.whitelist.empty"));
        } else {
            commandContext.getSource().sendFeedback(Text.translatable("lanlock.command.whitelist"));
        }
        for (String s : LANLock.getNames()) {
            commandContext.getSource().sendFeedback(Text.literal("  " + s));
        }
        return 1;
    }

    private static int reload(CommandContext<FabricClientCommandSource> commandContext) {
        CompletableFuture.runAsync(() -> {
            LANLock.loadConfig();
            commandContext.getSource().sendFeedback(Text.translatable("lanlock.command.reload"));
        });
        return 1;
    }

    private static int add(CommandContext<FabricClientCommandSource> commandContext) {
        String name = StringArgumentType.getString(commandContext, "player");
        CompletableFuture.runAsync(() -> {
            boolean result = LANLock.add(name);
            if (result) {
                commandContext.getSource().sendFeedback(Text.translatable("lanlock.command.add", name));
            } else {
                commandContext.getSource().sendError(Text.translatable("lanlock.command.add.error", name));
            }
        });
        return 1;
    }

    private static int remove(CommandContext<FabricClientCommandSource> commandContext) {
        String name = StringArgumentType.getString(commandContext, "player");
        boolean result = LANLock.remove(name);
        if (result) {
            commandContext.getSource().sendFeedback(Text.translatable("lanlock.command.remove", name));
        } else {
            commandContext.getSource().sendError(Text.translatable("lanlock.command.remove.error", name));
        }
        return 1;
    }

    private static int toggle(CommandContext<FabricClientCommandSource> commandContext) {
        boolean enabled = BoolArgumentType.getBool(commandContext, "enabled");
        LANLock.setEnabled(enabled);
        if (enabled) {
            commandContext.getSource().sendFeedback(Text.translatable("lanlock.command.enable"));
        } else {
            commandContext.getSource().sendFeedback(Text.translatable("lanlock.command.disable"));
        }
        return 1;
    }

    private static int enabled(CommandContext<FabricClientCommandSource> commandContext) {
        if (LANLock.enabled()) {
            commandContext.getSource().sendFeedback(Text.translatable("lanlock.command.disabled", "Whitelist"));
        } else {
            commandContext.getSource().sendFeedback(Text.literal("Whitelist is disabled"));
        }
        return 1;
    }

    private static int toggleUseUuid(CommandContext<FabricClientCommandSource> commandContext) {
        boolean enabled = BoolArgumentType.getBool(commandContext, "enabled");
        LANLock.setUseUuid(enabled);
        if (enabled) {
            commandContext.getSource().sendFeedback(Text.translatable("lanlock.command.uuid.enable"));
        } else {
            commandContext.getSource().sendFeedback(Text.translatable("lanlock.command.uuid.disable"));
        }
        return 1;
    }

    private static int useUuid(CommandContext<FabricClientCommandSource> commandContext) {
        if (LANLock.getUseUuid()) {
            commandContext.getSource().sendFeedback(Text.translatable("lanlock.command.enabled", "useUuid"));
        } else {
            commandContext.getSource().sendFeedback(Text.translatable("lanlock.command.disabled", "useUuid"));
        }
        return 1;
    }

    private static int toggleNotify(CommandContext<FabricClientCommandSource> commandContext) {
        boolean enabled = BoolArgumentType.getBool(commandContext, "enabled");
        LANLock.setSendNotification(enabled);
        if (enabled) {
            commandContext.getSource().sendFeedback(Text.translatable("lanlock.command.notify.enable"));
        } else {
            commandContext.getSource().sendFeedback(Text.translatable("lanlock.command.notify.disable"));
        }
        return 1;
    }

    private static int notify(CommandContext<FabricClientCommandSource> commandContext) {
        if (LANLock.getSendNotification()) {
            commandContext.getSource().sendFeedback(Text.translatable("lanlock.command.enabled", "notify"));
        } else {
            commandContext.getSource().sendFeedback(Text.translatable("lanlock.command.disabled", "notify"));
        }
        return 1;
    }
}