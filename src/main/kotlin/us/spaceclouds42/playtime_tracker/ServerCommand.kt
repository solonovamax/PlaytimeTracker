package us.spaceclouds42.playtime_tracker

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.kyori.adventure.platform.fabric.FabricServerAudiences
import net.minecraft.server.command.ServerCommandSource

data class ServerCommand(val source: ServerCommandSource) : ForwardingAudience.Single {
    val audience: Audience
        get() = FabricServerAudiences.of(this.source.server).audience(this.source)
    
    override fun audience(): Audience = audience
}
