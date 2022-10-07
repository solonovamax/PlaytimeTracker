package us.spaceclouds42.playtime_tracker.advancement

import com.google.gson.JsonObject
import net.minecraft.advancement.criterion.AbstractCriterion
import net.minecraft.advancement.criterion.AbstractCriterionConditions
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer
import net.minecraft.predicate.entity.EntityPredicate.Extended
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import us.spaceclouds42.playtime_tracker.advancement.PlaytimeCriterion.Conditions
import us.spaceclouds42.playtime_tracker.duck.AFKPlayer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object PlaytimeCriterion : AbstractCriterion<Conditions>() {
    
    val ID = Identifier("playtime_tracker:playtime")
    
    val END_OF_TIME_ID = Identifier("playtime_tracker:end_of_time")
    
    val ANCIENT_ONE_ID = Identifier("playtime_tracker:ancient_one")
    
    val TIME_MARCHES_ID = Identifier("playtime_tracker:time_marches")
    
    val DEDICATED_ID = Identifier("playtime_tracker:dedicated")
    
    val A_NEW_ADVENTURE_ID = Identifier("playtime_tracker:a_new_adventure")
    
    val ANTIQUE_ID = Identifier("playtime_tracker:antique")
    
    val VETERAN_ID = Identifier("playtime_tracker:veteran")
    
    @JvmStatic
    fun trigger(player: ServerPlayerEntity) {
        trigger(player) {
            it.matches(player)
        }
    }
    
    override fun getId(): Identifier = ID
    
    override fun conditionsFromJson(
        json: JsonObject,
        playerPredicate: Extended,
        predicateDeserializer: AdvancementEntityPredicateDeserializer
                                   ): Conditions {
        val time = json["time"].asInt
        val unit = DurationUnit.valueOf(json["unit"].asString.uppercase())
        
        return Conditions(playerPredicate, time.toDuration(unit), unit)
    }
    
    class Conditions(playerPredicate: Extended, private val time: Duration, private val unit: DurationUnit) :
            AbstractCriterionConditions(ID, playerPredicate) {
        fun matches(player: ServerPlayerEntity): Boolean {
            player as AFKPlayer
            
            
            return !player.isAfk && player.playtime.milliseconds > time
        }
        
        override fun toJson(predicateSerializer: AdvancementEntityPredicateSerializer): JsonObject {
            val json = super.toJson(predicateSerializer)
            json.addProperty("time", time.toInt(unit))
            json.addProperty("unit", unit.name)
            
            return json
        }
    }
}