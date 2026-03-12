package technicfan.lanlock.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
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

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginNetworkHandlerMixin {
    @Final
    @Shadow
    MinecraftServer server;

    @Shadow
    public abstract void disconnect(Component text);

    @Inject(
        method = "handleHello",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;usesAuthentication()Z"
        ),
        cancellable = true
    )
    private void checkPlayer(ServerboundHelloPacket packet, CallbackInfo ci) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (LANLock.enabled() && packet != null) {
            String id = LANLock.getUseUuid() ? packet.profileId().toString() : packet.name();
            if (!LANLock.checkWhitelist(id)) {
                disconnect(Component.translatable("multiplayer.disconnect.not_whitelisted"));
                if (LANLock.getSendNotification() && server.getSingleplayerProfile() != null) {
                    Method getId;
                    try {
                        getId = GameProfile.class.getMethod("id");
                    } catch (Exception e) {
                        getId = GameProfile.class.getMethod("getId");
                    }
                    ServerPlayer host = server.getPlayerList().getPlayer((UUID) getId.invoke(server.getSingleplayerProfile()));
                    if (host != null) {
                        boolean offline = packet.profileId().equals(
                            //? if <=1.20.1
                            /*java.util.Optional.of(*/
                            UUID.nameUUIDFromBytes(("OfflinePlayer:" + packet.name()).getBytes(StandardCharsets.UTF_8))
                            //? if <=1.20.1
                            /*)*/
                        );
                        if (!LANLock.getUseUuid() || !offline || !LANLock.checkWhitelist(packet.name())) {
                            MutableComponent message = Component.translatable("lanlock.notification", packet.name());
                            if (offline) message.append(Component.literal(" ")).append(Component.translatable("lanlock.notification.offline"));
                            // send notification
                            host.sendSystemMessage(message, false);

                            if (offline && LANLock.getUseUuid()) {
                                // send hint for notification
                                host.sendSystemMessage(Component.translatable("lanlock.notification.add.offline")
                                    .withStyle(ChatFormatting.BOLD), false);
                            } else {
                                //? if <=1.21.4 {
                                ClickEvent event = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lanlock add " + packet.name());
                                //?} else
                                /*ClickEvent event = new ClickEvent.RunCommand("/lanlock add " + packet.name());*/
                                // send action for notification
                                host.sendSystemMessage(Component.translatable("lanlock.notification.add")
                                    .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
                                    .withStyle(style -> style
                                        .withClickEvent(event)
                                    ), false
                                );
                            }
                        } else {
                            if (Objects.requireNonNull(LANLock.getWhitelistCounterpart(packet.name())).isEmpty())
                                host.sendSystemMessage(Component.translatable("lanlock.notification.offline.disabled", packet.name()), false);
                        }
                    }
                }
                ci.cancel();
            }
        }
    }
}
