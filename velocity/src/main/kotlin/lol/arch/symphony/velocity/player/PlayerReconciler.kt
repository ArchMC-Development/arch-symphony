package lol.arch.symphony.velocity.player

import gg.scala.aware.AwareBuilder
import gg.scala.aware.codec.codecs.interpretation.AwareMessageCodec
import gg.scala.aware.message.AwareMessage
import gg.scala.aware.thread.AwareThreadContext
import lol.arch.symphony.api.model.TrackedPlayer
import lol.arch.symphony.velocity.VelocitySymphonyPlugin
import lol.arch.symphony.velocity.acquirePlayerLock
import lol.arch.symphony.velocity.player.requests.PlayerReconcileRequest
import java.time.Duration
import java.util.logging.Logger

/**
 * @author GrowlyX
 * @since 2/15/2025
 */
class PlayerReconciler : Runnable
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

        plugin.server.scheduler
            .buildTask(plugin, this)
            .repeat(Duration.ofSeconds(1L))
            .schedule()

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

    override fun run()
    {
        plugin.server.allPlayers.forEach { player ->
            val trackedPlayer = plugin.playerTracker.find(player.uniqueId)
            if (trackedPlayer != null)
            {
                return@forEach
            }

            player.uniqueId.acquirePlayerLock {
                val online = plugin.server.getPlayer(player.uniqueId).isPresent
                val untracked = plugin.playerTracker.find(player.uniqueId) == null

                if (online && untracked)
                {
                    plugin.playerTracker.save(TrackedPlayer(
                        uniqueId = player.uniqueId,
                        instance = plugin.config.id
                    ))

                    plugin.logger.info("Reconciled untracked player on local proxy ${player.uniqueId} (${player.username})")
                }
            }
        }
    }
}
