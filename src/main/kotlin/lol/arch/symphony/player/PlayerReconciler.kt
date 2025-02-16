package lol.arch.symphony.player

import gg.scala.aware.AwareBuilder
import gg.scala.aware.codec.codecs.interpretation.AwareMessageCodec
import gg.scala.aware.message.AwareMessage
import gg.scala.aware.thread.AwareThreadContext
import lol.arch.symphony.VelocitySymphonyPlugin
import lol.arch.symphony.player.requests.PlayerReconcileRequest
import java.util.logging.Logger

/**
 * @author GrowlyX
 * @since 2/15/2025
 */
class PlayerReconciler
{
    private lateinit var plugin: VelocitySymphonyPlugin

    private val aware by lazy {
        AwareBuilder
            .of<AwareMessage>("symphony:reconcile")
            .codec(AwareMessageCodec)
            .logger(Logger.getGlobal())
            .build()
    }

    fun reconcile(request: PlayerReconcileRequest) = AwareMessage
        .of(
            "reconcile",
            aware,
            "request" to request
        )
        .publish(
            AwareThreadContext.SYNC
        )

    fun startReconciliation(plugin: VelocitySymphonyPlugin)
    {
        this.plugin = plugin

        aware.listen("reconcile") {
            val request = retrieve<PlayerReconcileRequest>("request")
            if (plugin.config.id != request.instance)
            {
                return@listen
            }

            if (plugin.server.getPlayer(request.player) != null)
            {
                return@listen
            }

            plugin.playerTracker.delete(request.player)
        }
        aware.connect().toCompletableFuture().join()
    }
}
