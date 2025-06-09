package technicfan.lanlock.mixin;

import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import technicfan.lanlock.LANLock;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class ServerLoginNetworkHandlerMixin {
    @Final
    @Shadow
    MinecraftServer server;

    @Shadow
    public abstract void disconnect(Text text);

    @Inject(
        method = "onHello",
        at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/server/MinecraftServer;isOnlineMode()Z"
        ),
        cancellable = true
    )
    private void checkPlayer(LoginHelloC2SPacket packet, CallbackInfo ci) {
        if (LANLock.enabled() && packet != null) {
            String id = LANLock.getUseUuid() ? packet.profileId().toString() : packet.name();
            if (!LANLock.checkWhitelist(id)) {
                disconnect(Text.translatable("multiplayer.disconnect.not_whitelisted"));
                if (LANLock.getSendNotification() && server.getHostProfile() != null) {
                    ServerPlayerEntity host = server.getPlayerManager().getPlayer(server.getHostProfile().getId());
                    if (host != null) {
                        boolean offline = packet.profileId().equals(
                            UUID.nameUUIDFromBytes(("OfflinePlayer:" + packet.name()).getBytes(StandardCharsets.UTF_8))
                        );
                        if (!LANLock.getUseUuid() || !offline || !LANLock.checkWhitelist(packet.name())) {
                            MutableText message = Text.translatable("lanlock.notification", packet.name());
                            if (offline) message.append(Text.literal(" ")).append(Text.translatable("lanlock.notification.offline"));
                            MutableText action;
                            if (offline && LANLock.getUseUuid()) {
                                action = Text.translatable("lanlock.notification.add.offline");
                            } else {
                                action = Text.translatable("lanlock.notification.add")
                                    .setStyle(Style.EMPTY
                                        .withColor(65280).withBold(true)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lanlock add " + packet.name())));
                            }
                            host.sendMessage(message, false);
                            host.sendMessage(action, false);
                        } else {
                            if (Objects.requireNonNull(LANLock.getWhitelistCounterpart(packet.name())).isEmpty())
                                host.sendMessage(Text.translatable("lanlock.notification.offline.disabled", packet.name()));
                        }
                    }
                }
                ci.cancel();
            }
        }
    }
}
