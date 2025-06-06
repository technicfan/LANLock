package technicfan.lanlock.mixin;

import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import technicfan.lanlock.LANLock;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class ServerLoginNetworkHandlerMixin {
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
                ci.cancel();
            }
        }
    }
}
