package technicfan.lanlock.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;

import technicfan.lanlock.LANLock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    private void checkPlayer(LoginHelloC2SPacket packet, CallbackInfo ci) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (LANLock.enabled() && packet != null) {
            String id = LANLock.getUseUuid() ? packet.profileId().toString() : packet.name();
            if (!LANLock.checkWhitelist(id)) {
                disconnect(Text.translatable("multiplayer.disconnect.not_whitelisted"));
                if (LANLock.getSendNotification() && server.getHostProfile() != null) {
                    Method getId;
                    try {
                        getId = GameProfile.class.getMethod("id");
                    } catch (Exception e) {
                        getId = GameProfile.class.getMethod("getId");
                    }
                    ServerPlayerEntity host = server.getPlayerManager().getPlayer((UUID) getId.invoke(server.getHostProfile()));
                    if (host != null) {
                        boolean offline = packet.profileId().equals(
                            UUID.nameUUIDFromBytes(("OfflinePlayer:" + packet.name()).getBytes(StandardCharsets.UTF_8))
                        );
                        if (!LANLock.getUseUuid() || !offline || !LANLock.checkWhitelist(packet.name())) {
                            MutableText message = Text.translatable("lanlock.notification", packet.name());
                            if (offline) message.append(Text.literal(" ")).append(Text.translatable("lanlock.notification.offline"));
                            // send notification
                            host.sendMessage(message, false);

                            if (offline && LANLock.getUseUuid()) {
                                // send hint for notification
                                host.sendMessage(Text.translatable("lanlock.notification.add.offline")
                                    .formatted(Formatting.BOLD), false);
                            } else {
                                ClickEvent event;
                                try {
                                    MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
                                    // check if net.minecraft.text.ClickEvent$RunCommand exists (1.21.5+)
                                    Class<?> clazz = Class.forName(resolver.mapClassName("intermediary", "net.minecraft.class_2558$class_10609"));
                                    event = (ClickEvent) clazz.getDeclaredConstructor(String.class).newInstance("/lanlock add " + packet.name());
                                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                                    event = ClickEvent.class.getConstructor(ClickEvent.Action.class, String.class).newInstance(ClickEvent.Action.RUN_COMMAND, "/lanlock add " + packet.name());
                                }
                                ClickEvent finalEvent = event;
                                // send action for notification
                                host.sendMessage(Text.translatable("lanlock.notification.add")
                                    .formatted(Formatting.GREEN, Formatting.BOLD)
                                    .styled(style -> style
                                        .withClickEvent(finalEvent)
                                    ), false
                                );
                            }
                        } else {
                            if (Objects.requireNonNull(LANLock.getWhitelistCounterpart(packet.name())).isEmpty())
                                host.sendMessage(Text.translatable("lanlock.notification.offline.disabled", packet.name()), false);
                        }
                    }
                }
                ci.cancel();
            }
        }
    }
}
