package us.spaceclouds42.playtime_tracker.advancement

import com.google.gson.JsonObject
import net.minecraft.advancement.criterion.AbstractCriterion
import net.minecraft.advancement.criterion.AbstractCriterionConditions
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer
import net.minecraft.predicate.entity.EntityPredicate.Extended
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import us.spaceclouds42.playtime_tracker.advancement.AfkCriterion.Conditions
import us.spaceclouds42.playtime_tracker.duck.AFKPlayer

object AfkCriterion : AbstractCriterion<Conditions>() {
    
    val ID = Identifier("playtime_tracker:afk")
    
    @JvmStatic
    fun trigger(player: ServerPlayerEntity) {
        AfkCriterion.trigger(player) {
            it.matches(player)
        }
    }
    
    override fun getId(): Identifier = ID
    
    override fun conditionsFromJson(
        json: JsonObject,
        playerPredicate: Extended,
        predicateDeserializer: AdvancementEntityPredicateDeserializer
                                   ): Conditions = Conditions(playerPredicate)
    
    class Conditions(playerPredicate: Extended) : AbstractCriterionConditions(ID, playerPredicate) {
        fun matches(player: ServerPlayerEntity): Boolean {
            player as AFKPlayer
            
            return player.isAfk
        }
    }
}
