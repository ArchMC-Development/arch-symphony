package lol.arch.symphony.command

import com.velocitypowered.api.proxy.Player
import gg.scala.cache.uuid.ScalaStoreUuidCache
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.*
import gg.scala.commons.acf.annotation.Optional
import lol.arch.symphony.VelocitySymphonyPlugin
import lol.arch.symphony.instance.requests.RunCommandRequest
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.util.*

/**
 * @author GrowlyX
 * @since 2/16/2025
 */
class RunCommandCommand(private val plugin: VelocitySymphonyPlugin) : BaseCommand()
{
    @CommandCompletion("@instances")
    @CommandAlias("sendto|runremotecommand|rrc|runremote")
    @CommandPermission("symphony.command.sendto")
    fun onSendTo(
        player: Player,
        @Single instanceId: String,
        command: String
    )
    {
        plugin.instanceActionExecutor.runCommand(RunCommandRequest(
            instance = instanceId,
            command = command
        ))

        player.sendMessage(Component
            .text("Sent command \"$command\" to the proxy instance $instanceId.")
            .color(NamedTextColor.GREEN))
    }

    @CommandAlias("sendtoall|runremoteall|rra|runremoteall")
    @CommandPermission("symphony.command.sendtoall")
    fun onSendToAll(
        player: Player,
        command: String
    )
    {
        plugin.instanceActionExecutor.runCommand(RunCommandRequest(
            instance = "all",
            command = command
        ))

        player.sendMessage(Component
            .text("Sent command \"$command\" to all proxy instances.")
            .color(NamedTextColor.GREEN))
    }
}
