package us.spaceclouds42.playtime_tracker.mixin;


import carpet.patches.EntityPlayerMPFake;
import net.kyori.adventure.identity.Identity;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import us.spaceclouds42.playtime_tracker.advancement.AfkCriterion;
import us.spaceclouds42.playtime_tracker.advancement.PlaytimeCriterion;
import us.spaceclouds42.playtime_tracker.duck.AFKPlayer;

import java.time.Duration;


@Mixin(ServerPlayNetworkHandler.class)
abstract class ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;
    
    @Unique
    private final long afkTime = Duration.ofMinutes(5).toMillis();
    
    @Unique
    private int throttle = 0;
    
    @Inject(method = "tick", at = @At("TAIL"))
    private void trackTime(CallbackInfo ci) {
        if (player instanceof EntityPlayerMPFake) // Skip carpet players
            return;
        
        throttle++;
        
        if (throttle % 100 == 0) {
            PlaytimeCriterion.trigger(player);
            AfkCriterion.trigger(player);
        }
        
        @SuppressWarnings("CastToIncompatibleInterface")
        AFKPlayer afkPlayer = (AFKPlayer) player;
        long nowTickTime = Util.getMeasuringTimeMs();
        long lastTickTime = afkPlayer.getLastTickTime();
        
        if (!afkPlayer.isAfk() && lastTickTime != 0L) {
            long lastActionTime = afkPlayer.getLastActionTime();
            
            if (lastActionTime > 0L && nowTickTime - lastActionTime > afkTime) {
                afkPlayer.setAfk(true);
                
                afkPlayer.setPlaytime(afkPlayer.getPlaytime() - afkTime); // removes last 5 afk minutes of playtime
                broadcastMessage(Text.literal("Player ")
                                         .append(player.getDisplayName())
                                         .append(" is now afk.")
                                         .formatted(Formatting.GRAY));
                player.server.getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player));
            } else {
                afkPlayer.setPlaytime(afkPlayer.getPlaytime() + (nowTickTime - lastTickTime));
            }
        }
        
        afkPlayer.setLastTickTime(nowTickTime);
    }
    
    @Inject(method = "onPlayerMove", at = @At("HEAD"))
    private void updateLastActionTime(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if (packet.changesLook()) {
            updateLastActionTime();
        }
    }
    
    @Inject(method = "onHandSwing", at = @At("HEAD"))
    private void onHandSwing(HandSwingC2SPacket packet, CallbackInfo ci) {
        updateLastActionTime();
    }
    
    @Inject(method = "onChatMessage", at = @At("HEAD"))
    private void onChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
        updateLastActionTime();
    }
    
    @Inject(method = "onUpdateSelectedSlot", at = @At("HEAD"))
    private void onPlayerInteractEntity(UpdateSelectedSlotC2SPacket packet, CallbackInfo ci) {
        updateLastActionTime();
    }
    
    @Inject(method = "onPlayerInteractItem", at = @At("HEAD"))
    private void onPlayerInteractItem(PlayerInteractItemC2SPacket packet, CallbackInfo ci) {
        updateLastActionTime();
    }
    
    private void updateLastActionTime() {
        @SuppressWarnings("CastToIncompatibleInterface")
        AFKPlayer afkPlayer = (AFKPlayer) player;
        
        afkPlayer.setLastActionTime(Util.getMeasuringTimeMs());
        if (afkPlayer.isAfk()) {
            afkPlayer.setAfk(false);
            
            broadcastMessage(Text.literal("Player ")
                                     .append(player.getDisplayName())
                                     .append(" is no longer afk.")
                                     .formatted(Formatting.GRAY));
            player.server.getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player));
        }
    }
    
    private void broadcastMessage(Text text) {
        player.server.sendMessage(Identity.nil(), text);
        
        for (ServerPlayerEntity player : player.server.getPlayerManager().getPlayerList()) {
            player.sendMessage(Identity.nil(), text);
        }
    }
}
