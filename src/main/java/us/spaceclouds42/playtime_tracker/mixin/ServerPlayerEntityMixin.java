package us.spaceclouds42.playtime_tracker.mixin;


import carpet.patches.EntityPlayerMPFake;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtLong;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import us.spaceclouds42.playtime_tracker.duck.AFKPlayer;


@Mixin(ServerPlayerEntity.class)
abstract class ServerPlayerEntityMixin implements AFKPlayer {
    @Unique
    private boolean isAfk = false;
    
    @Unique
    private long playtime = 0L;
    
    @Unique
    private long lastActionTime = 0L;
    
    @Unique
    private long lastTickTime = 0L;
    
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void saveData(NbtCompound tag, CallbackInfo ci) {
        if (((ServerPlayerEntity) (Object) this) instanceof EntityPlayerMPFake) // Skip carpet players
            return;
        
        NbtLong playtimeTag = NbtLong.of(this.playtime);
        tag.put("Playtime", playtimeTag);
    }
    
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readData(NbtCompound tag, CallbackInfo ci) {
        if (((ServerPlayerEntity) (Object) this) instanceof EntityPlayerMPFake) // Skip carpet players
            return;
        
        if (tag.contains("Playtime")) {
            this.playtime = tag.getLong("Playtime");
        } else {
            this.playtime = 0L;
        }
    }
    
    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void copyData(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        @SuppressWarnings("CastToIncompatibleInterface")
        AFKPlayer oldAfkPlayer = (AFKPlayer) oldPlayer;
        
        this.isAfk = oldAfkPlayer.isAfk();
        this.playtime = oldAfkPlayer.getPlaytime();
        this.lastTickTime = oldAfkPlayer.getLastTickTime();
    }
    
    @Override
    public boolean isAfk() {
        return this.isAfk;
    }
    
    @Override
    public void setAfk(boolean isAfk) {
        this.isAfk = isAfk;
    }
    
    @Override
    public long getPlaytime() {
        return this.playtime;
    }
    
    @Override
    public void setPlaytime(long playtime) {
        this.playtime = playtime;
    }
    
    @Override
    public long getLastActionTime() {
        return this.lastActionTime;
    }
    
    @Override
    public void setLastActionTime(long playtime) {
        this.lastActionTime = playtime;
    }
    
    @Override
    public long getLastTickTime() {
        return lastTickTime;
    }
    
    @Override
    public void setLastTickTime(long time) {
        lastTickTime = time;
    }
}
