package technicfan.lanlock;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;

import java.util.concurrent.CompletableFuture;

public class PlayerSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        if (Minecraft.getInstance().getSingleplayerServer() != null) {
            for (String playerName : Minecraft.getInstance().getSingleplayerServer().getPlayerNames()) {
                builder.suggest(playerName);
            }
        } else if (Minecraft.getInstance().level != null) {
            for (AbstractClientPlayer player : Minecraft.getInstance().level.players()) {
                builder.suggest(player.getName().getString());
            }
        }

        return builder.buildFuture();
    }
}
