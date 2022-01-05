package us.spaceclouds42.playtime_tracker.advancement;


import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import us.spaceclouds42.playtime_tracker.duck.AFKPlayer;
import us.spaceclouds42.playtime_tracker.util.AdvancementHelper;

import java.time.Duration;


public class PlaytimeCriterion extends AbstractCriterion<PlaytimeCriterion.Conditions> {
    private static final Identifier ID = new Identifier("playtime_tracker:playtime");
    
    private static final long DEDICATED_TIME_REQUIREMENT = Duration.ofHours(10).toMillis();
    
    private static final long TIME_MARCHES_TIME_REQUIREMENT = Duration.ofHours(25).toMillis();
    
    private static final long ANCIENT_ONE_TIME_REQUIREMENT = Duration.ofHours(100).toMillis();
    
    private static final long END_OF_TIME_REQUIREMENT = Duration.ofHours(1000).toMillis();
    
    public static void trigger(ServerPlayerEntity player) {
        AFKPlayer afkPlayer = (AFKPlayer) player;
        if (afkPlayer.getPlaytime() >= END_OF_TIME_REQUIREMENT) {
            AdvancementHelper.INSTANCE.grant(player, "playtime_tracker:end_of_time");
        }
        
        if (afkPlayer.getPlaytime() >= ANCIENT_ONE_TIME_REQUIREMENT) {
            AdvancementHelper.INSTANCE.grant(player, "playtime_tracker:ancient_one");
        }
        
        if (afkPlayer.getPlaytime() >= TIME_MARCHES_TIME_REQUIREMENT) {
            AdvancementHelper.INSTANCE.grant(player, "playtime_tracker:time_marches");
        }
        
        if (afkPlayer.getPlaytime() >= DEDICATED_TIME_REQUIREMENT) {
            AdvancementHelper.INSTANCE.grant(player, "playtime_tracker:dedicated");
        }
    }
    
    @Override
    public Identifier getId() {
        return ID;
    }
    
    @Override
    protected Conditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate,
                                            AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return null;
    }
    
    
    public static class Conditions extends AbstractCriterionConditions {
        public Conditions(EntityPredicate.Extended playerPredicate) {
            super(PlaytimeCriterion.ID, playerPredicate);
        }
    }
}
