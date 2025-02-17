package lol.arch.symphony.velocity.command

import com.velocitypowered.api.proxy.Player
import gg.scala.cache.uuid.ScalaStoreUuidCache
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.Optional
import lol.arch.symphony.velocity.VelocitySymphonyPlugin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

/**
 * @author GrowlyX
 * @since 2/16/2025
 */
class GlobalListCommand(private val plugin: VelocitySymphonyPlugin) : BaseCommand()
{
    @CommandAlias("glist")
    fun onTrackedPlayer(player: Player, @Optional all: String?)
    {
        val players = plugin.playerCatalogue.playerCount()
        player.sendMessage(Component.text {
            it.append(Component
                .text("$players ")
                .color(NamedTextColor.AQUA))

            it.append(Component
                .text("player${if (players == 1) " is" else "s are"} currently connected to the network.")
                .color(NamedTextColor.GRAY))
        })

        if (!player.hasPermission("symphony.command.glist.all"))
        {
            return
        }

        if (all != null)
        {
            val grouped = plugin.playerCatalogue.players()
                .filter { tracked -> tracked.server != null }
                .groupBy { tracked -> tracked.server!! }

            grouped.forEach { pair ->
                player.sendMessage(Component.text {
                    it.append(Component
                        .text("[${pair.key}]: ")
                        .color(NamedTextColor.DARK_AQUA))

                    val usernames = pair.value
                        .mapNotNull { tracked ->
                            ScalaStoreUuidCache.username(tracked.uniqueId)
                        }
                        .joinToString(", ")

                    it.append(Component
                        .text(usernames)
                        .color(NamedTextColor.WHITE))
                })
            }
            return
        }

        player.sendMessage(Component.text {
            it.append(Component
                .text("To view all players on servers, use /glist all.")
                .color(NamedTextColor.WHITE))
        })
    }
}
