package lol.arch.symphony.velocity.command

import com.velocitypowered.api.proxy.Player
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import lol.arch.symphony.velocity.VelocitySymphonyPlugin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

/**
 * @author GrowlyX
 * @since 2/16/2025
 */
class DeadServerIDListCommand(private val plugin: VelocitySymphonyPlugin) : BaseCommand()
{
    @CommandAlias("deadinstances|deadserverids")
    fun onDeadInstances(player: Player)
    {
        val instances = plugin.instanceTracker.deadInstances()
        player.sendMessage(Component
            .text("Listing all dead proxy instances:")
            .color(NamedTextColor.RED))

        if (instances.isEmpty())
        {
            throw ConditionFailedException("There are no dead proxy instances.")
        }

        instances.forEach { instance ->
            player.sendMessage(Component.text {
                it.append(Component
                    .text("- $instance")
                    .color(NamedTextColor.WHITE))
                    .decorate(TextDecoration.STRIKETHROUGH)
            })
        }
    }
}
