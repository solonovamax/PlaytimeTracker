package us.spaceclouds42.playtime_tracker

import net.minecraft.server.command.ServerCommandSource

object SourceMapper {
    fun map(source: ServerCommandSource) = ServerCommand(source)
}
