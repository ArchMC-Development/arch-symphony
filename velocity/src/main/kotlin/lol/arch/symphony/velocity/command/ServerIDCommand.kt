package lol.arch.symphony.velocity.command

import com.velocitypowered.api.proxy.Player
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import lol.arch.symphony.velocity.VelocitySymphonyPlugin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

/**
 * @author GrowlyX
 * @since 2/16/2025
 */
class ServerIDCommand(private val plugin: VelocitySymphonyPlugin) : BaseCommand()
{
    @CommandAlias("instance|serverid")
    fun onInstance(player: Player)
    {
        val trackedPlayer = plugin.playerTracker.find(player.uniqueId)
            ?: throw ConditionFailedException("You are not registered on the player tracker!")

        player.sendMessage(
            Component
                .text("You are logged on ${trackedPlayer.instance}.")
                .color(NamedTextColor.GREEN)
        )

        player.sendMessage(
            Component
                .text("You are on server ${trackedPlayer.server}.")
                .color(NamedTextColor.GRAY)
        )
    }
}
