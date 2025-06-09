package technicfan.lanlock;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;

import java.util.concurrent.CompletableFuture;

public class PlayerSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        if (MinecraftClient.getInstance().getServer() != null) {
            for (String playerName : MinecraftClient.getInstance().getServer().getPlayerNames()) {
                builder.suggest(playerName);
            }
        } else if (MinecraftClient.getInstance().world != null) {
            for (AbstractClientPlayerEntity player : MinecraftClient.getInstance().world.getPlayers()) {
                builder.suggest(player.getName().getString());
            }
        }

        return builder.buildFuture();
    }
}
