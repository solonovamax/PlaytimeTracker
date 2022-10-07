package us.spaceclouds42.playtime_tracker.mixin;


import carpet.patches.EntityPlayerMPFake;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import us.spaceclouds42.playtime_tracker.Common;
import us.spaceclouds42.playtime_tracker.duck.AFKPlayer;


@Mixin(PlayerListS2CPacket.class)
abstract class PlayerListS2CPacketMixin {
    @Mixin(PlayerListS2CPacket.Entry.class)
    private abstract static class EntryMixin {
        @Shadow
        @Final
        private GameProfile profile;
        
        @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
        private void modifyDisplayName(CallbackInfoReturnable<Text> cir) {
            ServerPlayerEntity player = Common.SERVER.getPlayerManager().getPlayer(this.profile.getId());
            
            if (player instanceof EntityPlayerMPFake) // Skip carpet players
                return;
            
            if (player != null) {
                @SuppressWarnings("CastToIncompatibleInterface")
                AFKPlayer afkPlayer = (AFKPlayer) player;
                
                if (afkPlayer.isAfk()) {
                    Text.literal("").append("");
                    Common.LOGGER.info("Player {} is afk!", player.getEntityName());
                    cir.setReturnValue(Text.literal("[AFK] ").append(player.getDisplayName()).formatted(Formatting.GRAY));
                }
            }
        }
    }
}
