package lol.arch.symphony.velocity.instance

import lol.arch.symphony.velocity.VelocitySymphonyPlugin
import lol.arch.symphony.velocity.instance.requests.RunCommandRequest
import lol.arch.symphony.velocity.rpc.SymphonyRPC

/**
 * @author GrowlyX
 * @since 2/15/2025
 */
class InstanceActionExecutor
{
    private lateinit var plugin: VelocitySymphonyPlugin

    fun runCommand(request: RunCommandRequest) = SymphonyRPC.runCommandRPC
        .callSync(request)

    fun startActionTracking(plugin: VelocitySymphonyPlugin)
    {
        this.plugin = plugin
        SymphonyRPC.runCommandRPC.addHandler(RunCommandHandler(plugin))
    }
}
