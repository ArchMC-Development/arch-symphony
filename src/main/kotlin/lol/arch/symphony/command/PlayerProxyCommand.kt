package lol.arch.symphony.command

import com.velocitypowered.api.proxy.Player
import gg.scala.cache.uuid.ScalaStoreUuidCache
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandCompletion
import gg.scala.commons.acf.annotation.CommandPermission
import lol.arch.symphony.VelocitySymphonyPlugin
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.util.*

/**
 * @author GrowlyX
 * @since 2/16/2025
 */
class PlayerProxyCommand(private val plugin: VelocitySymphonyPlugin) : BaseCommand()
{
    @CommandCompletion("@players")
    @CommandAlias("pproxy|playerproxy")
    @CommandPermission("symphony.command.playerproxy")
    fun onPlayerProxy(player: Player, target: UUID)
    {
        val trackedPlayer = plugin.playerTracker.find(target)
            ?: throw ConditionFailedException("That player is not logged onto the network!")

        player.sendMessage(Component
            .text("${ScalaStoreUuidCache.username(trackedPlayer.uniqueId)} is connected to ${trackedPlayer.instance}.")
            .color(NamedTextColor.GREEN))
    }
}
