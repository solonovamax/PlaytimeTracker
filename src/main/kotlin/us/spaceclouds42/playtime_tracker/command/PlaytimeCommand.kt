package us.spaceclouds42.playtime_tracker.command

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.Flag
import cloud.commandframework.annotations.specifier.Greedy
import cloud.commandframework.annotations.suggestions.Suggestions
import cloud.commandframework.context.CommandContext
import cloud.commandframework.fabric.data.MultiplePlayerSelector
import cloud.commandframework.minecraft.extras.MinecraftHelp
import me.basiqueevangelist.nevseti.OfflineAdvancementCache.INSTANCE
import me.basiqueevangelist.nevseti.OfflineAdvancementUtils
import me.basiqueevangelist.nevseti.OfflineDataCache
import me.basiqueevangelist.nevseti.OfflineNameCache
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Util
import us.spaceclouds42.playtime_tracker.Common
import us.spaceclouds42.playtime_tracker.ServerCommand
import us.spaceclouds42.playtime_tracker.duck.AFKPlayer
import us.spaceclouds42.playtime_tracker.extension.prettyPrint
import us.spaceclouds42.playtime_tracker.mixin.access.IAccessPlayerManager
import us.spaceclouds42.playtime_tracker.util.revokeAdvancements
import java.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.toDuration

class PlaytimeCommand(
    val help: MinecraftHelp<ServerCommand>
                     ) {
    
    @CommandMethod("afk")
    @CommandDescription("Mark yourself as afk")
    fun setAfkCommand(command: ServerCommand) {
        val player = command.source.player
        
        (player as AFKPlayer).isAfk = true
        
        player.server.playerManager.sendToAll(PlayerListS2CPacket(UPDATE_DISPLAY_NAME, player))
        
        val text = text {
            content("Player ")
            append(
                    player.displayName,
                    text {
                        content(" is now afk.")
                    },
                  )
            color(NamedTextColor.GRAY)
        }
        
        player.server.sendMessage(text)
        
        for (serverPlayer in player.server.playerManager.playerList) {
            serverPlayer.sendMessage(Identity.nil(), text)
        }
    }
    
    @CommandMethod("playtime set <targets> <time>")
    @CommandPermission("playtime.modify")
    fun setPlaytime(
        command: ServerCommand,
        @Argument(value = "targets")
        targets: MultiplePlayerSelector,
        @Argument(value = "time")
        time: Duration
                   ) {
        val manager = command.source.server.playerManager
        
        targets.get().forEach { target ->
            val player = target as AFKPlayer
            
            player.playtime = time.toMillis()
            
            (manager as IAccessPlayerManager).invokeSavePlayerData(player as ServerPlayerEntity)
            
            val text = text {
                color(NamedTextColor.WHITE)
                content("Set ")
                append(
                        target.name.copy().apply {
                            color(NamedTextColor.GREEN)
                        },
                        text { content(" to ") },
                        text {
                            color(NamedTextColor.WHITE)
                            content(time.prettyPrint())
                        },
                        text { content(" of playtime.") }
                      )
            }
            command.sendMessage(text)
        }
    }
    
    @CommandMethod("playtime get <targets>")
    fun getPlaytime(
        command: ServerCommand,
        @Argument("targets")
        targets: MultiplePlayerSelector,
                   ) {
        targets.get().forEach { target ->
            val player = target as AFKPlayer
            
            Common.LOGGER.info("player ${target.entityName}: ${player.playtime}")
            
            val text = text {
                content("")
                append(
                        target.name.copy().apply {
                            color(NamedTextColor.GREEN)
                        },
                        text {
                            content(" has ")
                        },
                        text {
                            color(NamedTextColor.WHITE)
                            content(player.playtime.toDuration(MILLISECONDS).prettyPrint())
                        },
                        text {
                            content(" of playtime")
                        }
                      )
            }
            command.sendMessage(text)
        }
    }
    
    @CommandMethod("playtime add <targets> <time>")
    @CommandPermission("playtime.modify")
    fun addCommand(
        command: ServerCommand,
        @Argument(value = "targets")
        targets: MultiplePlayerSelector,
        @Argument(value = "time")
        time: Duration,
                   ) {
        val manager = command.source.server.playerManager
        
        targets.get().forEach { target ->
            val player = target as AFKPlayer
            
            player.playtime += time.toMillis()
            
            (manager as IAccessPlayerManager).invokeSavePlayerData(player as ServerPlayerEntity)
            
            val text = text {
                color(NamedTextColor.WHITE)
                content("Updated ")
                append(
                        target.name.copy().apply {
                            color(NamedTextColor.GREEN)
                        },
                        text { content(" to ") },
                        text {
                            color(NamedTextColor.WHITE)
                            content(player.playtime.milliseconds.prettyPrint())
                        },
                        text { content(" of playtime.") }
                      )
            }
            command.sendMessage(text)
        }
    }
    
    // @CommandMethod("playtime get [player]")
    // fun getCommand(
    //     command: ServerCommand,
    //     @Argument(value = "player", suggestions = "onlinePlayers")
    //     player: String? = null
    //               ) {
    //     val playerPlaytime = if (player != null) {
    //         val times = mutableMapOf<String, Long>()
    //         for ((uuid, tag) in OfflineDataCache.INSTANCE.players) {
    //             if (uuid == null)
    //                 continue
    //
    //             try {
    //                 val name = OfflineNameCache.INSTANCE.getNameFromUUID(uuid)
    //                 val time = if (tag.contains("Playtime")) {
    //                     tag.getLong("Playtime")
    //                 } else {
    //                     null
    //                 }
    //
    //                 if (time != null)
    //                     times[name] = time
    //             } catch (_: RuntimeException) {
    //             } // sometimes OfflineNameCache.INSTANCE.getNameFromUUID does a bruh moment
    //         }
    //
    //         // Use the latest data for any online players
    //         command.source.server.playerManager.playerList.forEach { player ->
    //             times[player.entityName] = (player as AFKPlayer).playtime
    //         }
    //
    //         times[player] to player
    //     } else {
    //         (command.source.player as AFKPlayer).playtime to command.source.player.entityName
    //     }
    //
    //     val text = if (playerPlaytime.first == null) {
    //         text {
    //             content("Player ${playerPlaytime.second} Not Found")
    //         }
    //     } else {
    //         text {
    //             color(NamedTextColor.GOLD)
    //             append(
    //                     text {
    //                         content("====")
    //                     },
    //                     text {
    //                         color(NamedTextColor.YELLOW)
    //                         content("<")
    //                     },
    //                     text {
    //                         content("Your Playtime")
    //                     },
    //                     text {
    //                         color(NamedTextColor.YELLOW)
    //                         content(">")
    //                     },
    //                     text {
    //                         content("====")
    //                     }
    //                   )
    //
    //             append {
    //                 text {
    //                     color(NamedTextColor.WHITE)
    //                     content("${playerPlaytime.second}: ${playerPlaytime.first!!.toDuration(MILLISECONDS).prettyPrint()}")
    //                 }
    //             }
    //         }
    //     }
    //
    //     command.sendMessage(text)
    // }
    
    @Suggestions("onlinePlayers")
    fun onlinePlayerSuggestions(sender: CommandContext<ServerCommand>, input: String): List<String> {
        return sender.sender.source.server.playerNames.asList()
    }
    
    @CommandMethod("playtime top")
    fun topCommand(
        command: ServerCommand,
                  ) {
        val times = mutableMapOf<String, Long>()
        for ((uuid, tag) in OfflineDataCache.INSTANCE.players) {
            if (uuid == null)
                continue
            
            try {
                val name = OfflineNameCache.INSTANCE.getNameFromUUID(uuid)
                val time = if (tag.contains("Playtime")) {
                    tag.getLong("Playtime")
                } else {
                    null
                }
                
                if (time != null)
                    times[name] = time
            } catch (_: RuntimeException) {
            } // sometimes OfflineNameCache.INSTANCE.getNameFromUUID does a bruh moment
        }
        
        // Use the latest data for any online players
        command.source.server.playerManager.playerList.forEach { player ->
            times[player.entityName] = (player as AFKPlayer).playtime
        }
        
        val text = text {
            color(NamedTextColor.GOLD)
            append(
                    text {
                        content("====")
                    },
                    text {
                        color(NamedTextColor.YELLOW)
                        content("<")
                    },
                    text {
                        content("Leaderboard")
                    },
                    text {
                        color(NamedTextColor.YELLOW)
                        content(">")
                    },
                    text {
                        content("====")
                    }
                  )
            
            times.asSequence().filter { it.value != 0L }.sortedByDescending { (_, value) -> value }.take(10)
                    .forEachIndexed { index, entry ->
                        val n = index + 1
                        if (n % 2 == 1)
                            append {
                                text {
                                    color(NamedTextColor.WHITE)
                                    content("\n$n. ${entry.key}: ${entry.value.toDuration(MILLISECONDS).prettyPrint()}")
                                }
                            }
                        else
                            append {
                                text {
                                    color(NamedTextColor.GRAY)
                                    content("\n$n. ${entry.key}: ${entry.value.toDuration(MILLISECONDS).prettyPrint()}")
                                }
                            }
                        // source.sendFeedback(LiteralText("§2$n. §3${entry.first}: ${entry.second!!.prettyPrint()}"), false)
                    }
        }
        
        command.sendMessage(text)
    }
    
    @CommandMethod("playtime revoke")
    @CommandPermission("playtime.modify")
    fun revokeCommand(
        command: ServerCommand,
        @Flag("confirm", aliases = ["c"])
        confirm: Boolean = false,
                     ) {
        val server = command.source.server
        
        if (confirm) {
            OfflineDataCache.INSTANCE.players.forEach { (uuid, _) ->
                val map = OfflineAdvancementUtils.copyAdvancementMap(INSTANCE[uuid])
                
                for ((_, playtimeAdvancement) in map.filter { it.key.namespace == "playtime_tracker" }) {
                    for (criterion in playtimeAdvancement.obtainedCriteria) {
                        playtimeAdvancement.reset(criterion)
                    }
                }
                
                INSTANCE.save(uuid, map)
            }
            
            server.playerManager.playerList.forEach { player ->
                player as AFKPlayer
                
                val playtimeAdvancements = server.advancementLoader.advancements.filter { it.id.namespace == "playtime_tracker" }
                
                player.revokeAdvancements(playtimeAdvancements)
            }
            
            command.sendMessage {
                text {
                    color(NamedTextColor.GREEN)
                    content("Revoked all advancements")
                }
            }
        } else {
            command.sendMessage {
                text {
                    color(NamedTextColor.WHITE)
                    content("Are you sure you want to revoke all advancements? This action is not reversible! If you are certain you wish to proceed, run: ")
                    append(text {
                        content("/playtime revoke --confirm")
                        color(NamedTextColor.GRAY)
                    })
                }
            }
        }
    }
    
    @CommandMethod("playtime reset")
    @CommandPermission("playtime.modify")
    fun resetCommand(
        command: ServerCommand,
        @Flag("confirm", aliases = ["c"])
        confirm: Boolean = false,
        @Flag("revoke", aliases = ["r"])
        revoke: Boolean = false,
                    ) {
        val server = command.source.server
        
        if (confirm) {
            OfflineDataCache.INSTANCE.players.forEach { (uuid, immutableTag) ->
                val tag = immutableTag.copy()
                if (tag.contains("Playtime")) {
                    tag.putLong("Playtime", 0L)
                    // OfflineDataCache.INSTANCE.save(uuid, tag)
                }
                
                if (revoke) {
                    val map = OfflineAdvancementUtils.copyAdvancementMap(INSTANCE[uuid])
                    
                    for ((_, playtimeAdvancement) in map.filter { it.key.namespace == "playtime_tracker" }) {
                        for (criterion in playtimeAdvancement.obtainedCriteria) {
                            playtimeAdvancement.reset(criterion)
                        }
                    }
                    
                    INSTANCE.save(uuid, map)
                }
            }
            
            server.playerManager.playerList.forEach { player ->
                player as AFKPlayer
                
                player.playtime = 0L
                
                val playtimeAdvancements = server.advancementLoader.advancements.filter { it.id.namespace == "playtime_tracker" }
                
                if (revoke) {
                    player.revokeAdvancements(playtimeAdvancements)
                    
                }
            }
            
            command.sendMessage {
                text {
                    color(NamedTextColor.GREEN)
                    content("Reset all playtimes")
                }
            }
        } else {
            command.sendMessage {
                text {
                    color(NamedTextColor.WHITE)
                    content("Are you sure you want to reset all playtimes? This action is not reversible! If you are certain you wish to proceed, run: ")
                    append(text {
                        content("/playtime reset --confirm")
                        color(NamedTextColor.GRAY)
                    })
                }
            }
        }
    }
    
    @CommandMethod("playtime help [query]")
    @CommandDescription("Help menu")
    fun helpCommand(
        command: ServerCommand,
        @Greedy
        @Argument("query")
        query: String? = null,
                   ) {
        help.queryCommands(query ?: "", command)
    }
}
