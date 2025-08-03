package lol.arch.symphony.velocity.system

import com.velocitypowered.api.scheduler.ScheduledTask
import gg.scala.cache.uuid.ScalaStoreUuidCache
import gg.scala.commons.ScalaCommons
import lol.arch.symphony.velocity.VelocitySymphonyPlugin
import okio.withLock
import java.time.Duration
import java.util.concurrent.locks.ReentrantLock

/**
 * @author Subham
 * @since 5/5/25
 */
class SystemSentinel : Runnable
{
    private lateinit var plugin: VelocitySymphonyPlugin

    companion object
    {
        fun startSentinelWatcher(
            plugin: VelocitySymphonyPlugin
        )
        {
            var reconcilerInstance: SystemSentinel? = null
            plugin.server.scheduler
                .buildTask(plugin, Runnable {
                    val chosenInstance = plugin.instanceTracker
                        .liveInstances()
                        .minOf { it }

                    if (chosenInstance == plugin.config.id)
                    {
                        if (reconcilerInstance != null)
                        {
                            return@Runnable
                        }

                        plugin.logger.info { "[sentinel] We are now the system reconciler ($chosenInstance), starting background tasks." }
                        reconcilerInstance = SystemSentinel()
                        reconcilerInstance!!.start(plugin)
                    } else
                    {
                        if (reconcilerInstance != null)
                        {
                            plugin.logger.info { "[sentinel] Was previously the system reconciler, but the responsibility has been moven to $chosenInstance. Stopping background tasks." }
                            reconcilerInstance!!.stop()
                            reconcilerInstance = null
                        }
                    }
                })
                .repeat(Duration.ofMillis(500L))
                .schedule()
        }
    }

    fun stop()
    {
        task.cancel()
    }

    private lateinit var task: ScheduledTask

    fun start(plugin: VelocitySymphonyPlugin)
    {
        this.plugin = plugin
        this.task = plugin.server.scheduler
            .buildTask(plugin, this)
            .repeat(Duration.ofMillis(500L))
            .schedule()
    }

    override fun run()
    {
        runCatching {
            plugin.instanceTracker.deadInstances().onEach { instance ->
                with(ScalaCommons.bundle().globals().redis().sync()) {
                    hdel("symphony:instances", instance)
                    hdel("symphony:heartbeats", instance)
                }

                plugin.logger.info { "[sentinel] Disposing $instance as the instance has been unresponsive for >5s." }
            }

            println(plugin.playerCatalogue.deadPlayers())
            plugin.playerCatalogue.deadPlayers().onEach { player ->
                plugin.playerTracker.delete(player.uniqueId)
                plugin.logger.info { "[sentinel] Disposing player $player (${
                    ScalaStoreUuidCache.username(player.uniqueId)
                }) as they have been unresponsive for >5s." }
            }
        }.onFailure { throwable ->
            throwable.printStackTrace()
        }
    }
}
