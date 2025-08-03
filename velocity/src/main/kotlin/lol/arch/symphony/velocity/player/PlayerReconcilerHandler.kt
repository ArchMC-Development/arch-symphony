package lol.arch.symphony.velocity.player

import lol.arch.symphony.velocity.VelocitySymphonyPlugin
import lol.arch.symphony.velocity.player.requests.PlayerReconcileRequest
import mc.arch.commons.communications.rpc.RPCContext
import mc.arch.commons.communications.rpc.RPCHandler

/**
 * @author Subham
 * @since 8/3/25
 */
class PlayerReconcilerHandler(
    private val plugin: VelocitySymphonyPlugin
) : RPCHandler<PlayerReconcileRequest, Unit>
{
    override fun handle(
        request: PlayerReconcileRequest,
        context: RPCContext<Unit>
    )
    {
        if (plugin.config.id != request.instance)
        {
            return
        }

        if (plugin.server.getPlayer(request.player) != null)
        {
            return
        }

        plugin.playerTracker.delete(request.player)
    }
}
