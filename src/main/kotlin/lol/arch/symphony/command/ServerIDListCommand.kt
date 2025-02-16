package lol.arch.symphony.command

import com.velocitypowered.api.proxy.Player
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import lol.arch.symphony.VelocitySymphonyPlugin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

/**
 * @author GrowlyX
 * @since 2/16/2025
 */
class ServerIDListCommand(private val plugin: VelocitySymphonyPlugin) : BaseCommand()
{
    @CommandPermission("symphony.command.playerproxy")
    @CommandAlias("serverids|instanceids|instances|listinstances|sids")
    fun onInstanceList(player: Player)
    {
        val instances = plugin.instanceTracker.liveInstances()
        player.sendMessage(Component
            .text("Listing all proxy instances:")
            .color(NamedTextColor.GREEN))

        instances.forEach { instance ->
            player.sendMessage(Component
                .text("- $instance")
                .color(NamedTextColor.WHITE))
        }
    }
}
