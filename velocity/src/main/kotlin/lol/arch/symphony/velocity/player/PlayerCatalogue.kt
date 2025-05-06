package lol.arch.symphony.velocity.player

import gg.scala.commons.ScalaCommons
import lol.arch.symphony.api.model.TrackedPlayer
import lol.arch.symphony.velocity.VelocitySymphonyPlugin
import lol.arch.symphony.velocity.acquirePlayerLock
import lol.arch.symphony.velocity.into
import java.time.Duration
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * @author GrowlyX
 * @since 2/15/2025
 */
class PlayerCatalogue : Runnable
{
    private val lock = ReentrantReadWriteLock()
    private var cache = setOf<TrackedPlayer>()

    private lateinit var plugin: VelocitySymphonyPlugin

    fun startTracking(plugin: VelocitySymphonyPlugin)
    {
        this.plugin = plugin

        plugin.server.scheduler
            .buildTask(plugin, this)
            .repeat(Duration.ofMillis(500L))
            .schedule()
    }

    fun deadPlayers() = ScalaCommons.bundle()
        .globals().redis().sync()
        .hgetall("symphony:players")
        .values
        .mapNotNull { it.into<TrackedPlayer>() }
        .filter { tracked ->
            System.currentTimeMillis() - tracked.lastHeartbeat >= Duration
                .ofSeconds(10L)
                .toMillis()
        }

    fun players() = lock.read { cache }
    fun playerCount() = lock.read { cache.size }

    fun onProxyAll(proxyID: String) = ScalaCommons.bundle()
        .globals().redis().sync()
        .hgetall("symphony:players")
        .values
        .mapNotNull { it.into<TrackedPlayer>() }
        .filter { player -> player.instance == proxyID }

    fun onProxy(proxyID: String) = cache.filter { it.instance == proxyID }
    fun onServer(server: String) = cache.filter { it.server == server }

    override fun run()
    {
        lock.write {
            val allPlayers = ScalaCommons.bundle()
                .globals().redis().sync()
                .hgetall("symphony:players")
                .values
                .mapNotNull { it.into<TrackedPlayer>() }
                .filterNot { tracked ->
                    System.currentTimeMillis() - tracked.lastHeartbeat > Duration
                        .ofSeconds(10L)
                        .toMillis()
                }

            cache = allPlayers.toSet()
        }

        onProxyAll(plugin.config.id)
            .filter {
                System.currentTimeMillis() - it.lastHeartbeat > Duration
                    .ofSeconds(5L)
                    .toMillis()
            }
            .forEach {
                it.uniqueId.acquirePlayerLock {
                    plugin.playerTracker.update(it.uniqueId) {
                        lastHeartbeat = System.currentTimeMillis()
                    }
                }
            }
    }
}
