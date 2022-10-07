package us.spaceclouds42.playtime_tracker.util

import net.minecraft.advancement.Advancement
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

fun ServerPlayerEntity.grantAdvancement(id: Identifier) {
    val tracker = advancementTracker
    val advancement = server.advancementLoader[id]
    val progress = tracker.getProgress(advancement)
    
    progress.unobtainedCriteria.forEach { criterion ->
        tracker.grantCriterion(advancement, criterion)
    }
}


fun ServerPlayerEntity.revokeAdvancement(id: Identifier) {
    val tracker = advancementTracker
    val advancement = server.advancementLoader[id]
    val progress = tracker.getProgress(advancement)
    
    progress.obtainedCriteria.forEach { criterion ->
        tracker.revokeCriterion(advancement, criterion)
    }
}

fun ServerPlayerEntity.revokeAdvancements(vararg advancements: Advancement) {
    revokeAdvancements(advancements.toList())
}


fun ServerPlayerEntity.revokeAdvancements(advancements: List<Advancement>) {
    for (advancement in advancements) {
        val tracker = advancementTracker
        val progress = tracker.getProgress(advancement)
        
        progress.obtainedCriteria.forEach { criterion ->
            tracker.revokeCriterion(advancement, criterion)
        }
    }
}


fun ServerPlayerEntity.hasAdvancement(id: Identifier): Boolean {
    val tracker = advancementTracker
    val advancement = server.advancementLoader[id]
    val progress = tracker.getProgress(advancement)
    
    return progress.isDone
}
