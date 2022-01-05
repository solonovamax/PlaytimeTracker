package us.spaceclouds42.playtime_tracker

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.server.MinecraftServer
import org.apache.logging.log4j.LogManager
import us.spaceclouds42.playtime_tracker.advancement.PlaytimeCriterion
import us.spaceclouds42.playtime_tracker.command.PlaytimeCommand

object Common : ModInitializer {
    lateinit var PLAYTIME: PlaytimeCriterion
    lateinit var SERVER: MinecraftServer
    private val logger = LogManager.getLogger()
    
    override fun onInitialize() {
        logger.info("[Playtime Tracker] Tracking playtime!")
        
        CommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.root.addChild(PlaytimeCommand().register())
        }
        
        ServerLifecycleEvents.SERVER_STARTED.register { SERVER = it }
        
        PLAYTIME = Criteria.register(PlaytimeCriterion())
    }
}

