package lol.arch.symphony.velocity.player

import lol.arch.symphony.api.model.TrackedPlayer
import lol.arch.symphony.velocity.VelocitySymphonyPlugin
import lol.arch.symphony.velocity.acquirePlayerLock
import lol.arch.symphony.velocity.player.requests.PlayerReconcileRequest
import lol.arch.symphony.velocity.rpc.SymphonyRPC
import java.time.Duration

/**
 * @author GrowlyX
 * @since 2/15/2025
 */
class PlayerReconciler : Runnable
{
    private lateinit var plugin: VelocitySymphonyPlugin

    fun reconcile(request: PlayerReconcileRequest) = SymphonyRPC
        .reconcilePlayerRPC.callSync(request)

    fun startReconciliation(plugin: VelocitySymphonyPlugin)
    {
        this.plugin = plugin

        plugin.server.scheduler
            .buildTask(plugin, this)
            .repeat(Duration.ofSeconds(1L))
            .schedule()

        SymphonyRPC.reconcilePlayerRPC.addHandler(PlayerReconcilerHandler(plugin))
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
