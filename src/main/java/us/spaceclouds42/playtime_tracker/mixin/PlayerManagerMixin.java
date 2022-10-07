package us.spaceclouds42.playtime_tracker.mixin;


import carpet.patches.EntityPlayerMPFake;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import us.spaceclouds42.playtime_tracker.duck.AFKPlayer;


@Mixin(PlayerManager.class)
abstract class PlayerManagerMixin {
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    private void updateLastActionTime(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        if (player instanceof EntityPlayerMPFake) // Skip carpet players
            return;
        
        ((AFKPlayer) player).setLastActionTime(Util.getMeasuringTimeMs());
        ((AFKPlayer) player).setLastTickTime(Util.getMeasuringTimeMs());
    }
}
