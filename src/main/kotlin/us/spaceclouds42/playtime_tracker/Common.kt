package us.spaceclouds42.playtime_tracker

import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator
import cloud.commandframework.fabric.FabricServerCommandManager
import cloud.commandframework.kotlin.coroutines.annotations.installCoroutineSupport
import cloud.commandframework.meta.SimpleCommandMeta
import cloud.commandframework.minecraft.extras.AudienceProvider.nativeAudience
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler
import cloud.commandframework.minecraft.extras.MinecraftHelp
import io.leangen.geantyref.TypeToken
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.server.MinecraftServer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import us.spaceclouds42.playtime_tracker.advancement.AfkCriterion
import us.spaceclouds42.playtime_tracker.advancement.PlaytimeCriterion
import us.spaceclouds42.playtime_tracker.command.DurationParser
import us.spaceclouds42.playtime_tracker.command.PlaytimeCommand
import java.time.Duration


object Common : ModInitializer {
    lateinit var SERVER: MinecraftServer
    
    @JvmField
    val LOGGER: Logger = LogManager.getLogger()
    
    override fun onInitialize() {
        LOGGER.info("[Playtime Tracker] Tracking playtime!")
        
        ServerLifecycleEvents.SERVER_STARTED.register { SERVER = it }
        
        Criteria.register(PlaytimeCriterion)
        Criteria.register(AfkCriterion)
        
        val manager = FabricServerCommandManager(
                AsynchronousCommandExecutionCoordinator.newBuilder<ServerCommand>()
                        .withAsynchronousParsing()
                        .build(),
                SourceMapper::map, ServerCommand::source)
        
        manager.parserRegistry.registerParserSupplier(TypeToken.get(Duration::class.java)) { DurationParser() }
        
        // manager.brigadierManager().setNativeNumberSuggestions(false)
        
        MinecraftExceptionHandler<ServerCommand>()
                .withDefaultHandlers()
                .withInvalidSenderHandler()
                .withInvalidSyntaxHandler()
                .withNoPermissionHandler()
                .withCommandExecutionHandler()
                .apply(manager, nativeAudience<ServerCommand>())
        
        val help = MinecraftHelp.createNative("playtime help", manager)
        
        val annotationParser = AnnotationParser(manager, ServerCommand::class.java) { SimpleCommandMeta.empty() }.apply {
            installCoroutineSupport()
        }
        annotationParser.parse(PlaytimeCommand(help))
    }
}

