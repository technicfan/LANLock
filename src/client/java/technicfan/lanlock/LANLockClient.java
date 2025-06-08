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
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
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
                .then(ClientCommandManager.literal("on")
                    .executes(LANLockClient::on))
                .then(ClientCommandManager.literal("off")
                    .executes(LANLockClient::off))
                .then(ClientCommandManager.literal("useUuid")
                    .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                        .executes(LANLockClient::toggleUseUuid))
                    .executes(LANLockClient::useUuid))
                .then(ClientCommandManager.literal("notify")
                    .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                        .executes(LANLockClient::toggleNotify))
                    .executes(LANLockClient::notify))
        ));
    }

    private static int list(CommandContext<FabricClientCommandSource> commandContext) {
        if (LANLock.getNames().isEmpty()) {
            commandContext.getSource().sendFeedback(Text.translatable("commands.whitelist.none"));
            return 1;
        } else {
            commandContext.getSource().sendFeedback(
                Text.translatable("commands.whitelist.list",
                LANLock.getNames().size(),
                String.join(", ", LANLock.getNames()))
            );
            return LANLock.getNames().size();
        }
    }

    private static int reload(CommandContext<FabricClientCommandSource> commandContext) {
        CompletableFuture.runAsync(() -> {
            LANLock.loadConfig();
            commandContext.getSource().sendFeedback(Text.translatable("commands.whitelist.reloaded"));
        });
        return 1;
    }

    private static int add(CommandContext<FabricClientCommandSource> commandContext) {
        String name = StringArgumentType.getString(commandContext, "player");
        CompletableFuture.runAsync(() -> {
            Boolean result = LANLock.add(name);
            if (result == null) {
                commandContext.getSource().sendError(Text.translatable("argument.player.unknown"));
            } else if (result) {
                commandContext.getSource().sendFeedback(Text.translatable("commands.whitelist.add.success", name));
            } else {
                commandContext.getSource().sendError(Text.translatable("commands.whitelist.add.failed"));
            }
        });
        return 1;
    }

    private static int remove(CommandContext<FabricClientCommandSource> commandContext) {
        String name = StringArgumentType.getString(commandContext, "player");
        boolean result = LANLock.remove(name);
        if (result) {
            commandContext.getSource().sendFeedback(Text.translatable("commands.whitelist.remove.success", name));
        } else {
            commandContext.getSource().sendError(Text.translatable("commands.whitelist.remove.failed"));
        }
        return 1;
    }

    private static int on(CommandContext<FabricClientCommandSource> commandContext) {
        if (LANLock.enabled()) {
            commandContext.getSource().sendFeedback(Text.translatable("commands.whitelist.alreadyOn"));
            return 0;
        } else {
            commandContext.getSource().sendFeedback(Text.translatable("commands.whitelist.enabled"));
            LANLock.setEnabled(true);
            return 1;
        }
    }

    private static int off(CommandContext<FabricClientCommandSource> commandContext) {
        if (!LANLock.enabled()) {
            commandContext.getSource().sendFeedback(Text.translatable("commands.whitelist.alreadyOff"));
            return 0;
        } else {
            commandContext.getSource().sendFeedback(Text.translatable("commands.whitelist.disabled"));
            LANLock.setEnabled(false);
            return 1;
        }
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