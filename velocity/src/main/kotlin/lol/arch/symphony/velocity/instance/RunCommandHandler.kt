package lol.arch.symphony.velocity.instance

import lol.arch.symphony.velocity.VelocitySymphonyPlugin
import lol.arch.symphony.velocity.instance.actor.SymphonyCommandSource
import lol.arch.symphony.velocity.instance.requests.RunCommandRequest
import mc.arch.commons.communications.rpc.RPCContext
import mc.arch.commons.communications.rpc.RPCHandler

/**
 * @author Subham
 * @since 8/3/25
 */
class RunCommandHandler(
    private val plugin: VelocitySymphonyPlugin
) : RPCHandler<RunCommandRequest, Unit>
{
    override fun handle(
        request: RunCommandRequest,
        context: RPCContext<Unit>
    )
    {
        if (!(request.instance == "all" || request.instance == plugin.config.id))
        {
            return
        }

        plugin.server.commandManager
            .executeImmediatelyAsync(
                SymphonyCommandSource,
                request.command
            )
            .thenRunAsync {
                plugin.logger.info { "Ran remote command ${request.command}" }
            }
            .join()
    }
}
