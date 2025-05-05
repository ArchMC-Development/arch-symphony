package lol.arch.symphony.velocity.instance

import gg.scala.commons.ScalaCommons
import lol.arch.symphony.velocity.InstanceConfig
import lol.arch.symphony.velocity.VelocitySymphonyPlugin
import java.time.Duration
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * @author GrowlyX
 * @since 2/15/2025
 */
class LiveInstanceTracker : Runnable
{
    private lateinit var instanceConfig: InstanceConfig
    private lateinit var plugin: VelocitySymphonyPlugin

    private val lock = ReentrantReadWriteLock()
    private var cache = setOf<String>()

    fun startTracking(plugin: VelocitySymphonyPlugin, instanceConfig: InstanceConfig)
    {
        this.plugin = plugin
        this.instanceConfig = instanceConfig

        plugin.server.scheduler
            .buildTask(plugin, this)
            .repeat(Duration.ofMillis(500L))
            .schedule()
    }

    fun liveInstances() = lock.read { cache }

    fun globalPlayerCount() = cache
        .sumOf { serverID ->
            ScalaCommons
                .bundle().globals().redis()
                .sync().hget(
                    "symphony:instances",
                    serverID
                )
                ?.toIntOrNull()
                ?: 0
        }

    fun playerCount(instance: String) = ScalaCommons.bundle()
        .globals().redis().sync()
        .hget(
            "symphony:instances",
            instance
        )
        ?.toIntOrNull()
        ?: 0

    override fun run()
    {
        lock.write {
            with(
                ScalaCommons.bundle()
                    .globals().redis().sync()
            ) {
                hset(
                    "symphony:instances",
                    instanceConfig.id,
                    plugin.server.playerCount.toString()
                )

                hset(
                    "symphony:heartbeats",
                    instanceConfig.id,
                    System.currentTimeMillis().toString()
                )
            }

            cache = ScalaCommons.bundle()
                .globals().redis().sync()
                .hgetall("symphony:heartbeats")
                .filterNot {
                    System.currentTimeMillis() - it.value.toLong() > Duration
                        .ofSeconds(5L)
                        .toMillis()
                }
                .keys
                .toSet()
        }
    }
}
