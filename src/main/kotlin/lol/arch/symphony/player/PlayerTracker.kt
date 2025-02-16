package lol.arch.symphony.player

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import gg.scala.commons.ScalaCommons
import lol.arch.symphony.VelocitySymphonyPlugin
import lol.arch.symphony.acquirePlayerLock
import lol.arch.symphony.into
import lol.arch.symphony.instance.requests.RunCommandRequest
import net.evilblock.cubed.serializers.Serializers
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.time.Duration
import java.util.*
import kotlin.jvm.optionals.getOrNull

/**
 * @author GrowlyX
 * @since 2/15/2025
 */
class PlayerTracker
{
    lateinit var plugin: VelocitySymphonyPlugin

    @Subscribe(order = PostOrder.LAST)
    fun LoginEvent.on()
    {
        runCatching {
            player.uniqueId
                .acquirePlayerLock {
                    val existing = find(player.uniqueId)
                    if (existing == null)
                    {
                        val trackedPlayer = TrackedPlayer(
                            uniqueId = player.uniqueId,
                            instance = plugin.config.id
                        )

                        save(trackedPlayer)
                        return@acquirePlayerLock
                    }

                    println("Player is on the network ${existing.instance}")

                    if (
                        existing.lastAttemptedReconcile != null &&
                        System.currentTimeMillis() - existing.lastAttemptedReconcile!! > Duration
                            .ofSeconds(1L)
                            .toMillis()
                    )
                    {
                        println("Trying to reconcile")
                        update(player.uniqueId) {
                            lastAttemptedReconcile = System.currentTimeMillis()
                        }

                        plugin.playerReconciler.reconcile(
                            RunCommandRequest(
                            player.uniqueId, existing.instance
                        )
                        )
                    }

                    result = ResultedEvent.ComponentResult.denied(Component.text {
                        it.append(
                            Component
                                .text("You are already logged onto the network!")
                                .color(NamedTextColor.RED)
                        )
                        it.append(Component.newline())
                        it.append(
                            Component
                                .text("If you believe this is a mistake, please try logging in again.")
                                .color(NamedTextColor.GRAY)
                        )
                        it.append(Component.newline())
                        it.append(
                            Component
                                .text("Proxy: ${plugin.config.id}")
                                .color(NamedTextColor.WHITE)
                        )
                    })
                }
                .join()
        }.onFailure {
            result = ResultedEvent.ComponentResult.denied(Component.text {
                it.append(
                    Component
                        .text("We were unable to log you in.")
                        .color(NamedTextColor.RED)
                )
                it.append(Component.newline())
                it.append(
                    Component
                        .text("Please try again in a moment, or contact an administrator.")
                        .color(NamedTextColor.GRAY)
                )
                it.append(Component.newline())
                it.append(
                    Component
                        .text("Proxy: ${plugin.config.id}")
                        .color(NamedTextColor.WHITE)
                )
            })
        }
    }

    @Subscribe(order = PostOrder.LAST)
    fun ServerPostConnectEvent.on()
    {
        val instanceID = player.currentServer
            .getOrNull()
            ?.server?.serverInfo?.name
            ?: return

        player.uniqueId.acquirePlayerLock {
            update(player.uniqueId) {
                lastServerSwitchTime = if (previousServer != null) System.currentTimeMillis() else null
                lastServer = previousServer?.serverInfo?.name
                server = instanceID
            }
        }
    }

    @Subscribe(order = PostOrder.LAST)
    fun DisconnectEvent.on()
    {
        player.uniqueId.acquirePlayerLock {
            delete(player.uniqueId)
        }
    }

    fun delete(player: UUID) = ScalaCommons.bundle()
        .globals().redis().sync()
        .hdel("symphony:players", player.toString())

    fun save(player: TrackedPlayer) = ScalaCommons.bundle()
        .globals().redis().sync()
        .hset(
            "symphony:players",
            player.uniqueId.toString(),
            Serializers.gson.toJson(player)
        )

    fun update(player: UUID, use: TrackedPlayer.() -> Unit) = find(player)
        ?.apply(use)
        ?.apply {
            save(this)
        }

    fun find(player: UUID) = ScalaCommons.bundle()
        .globals().redis().sync()
        .hget("symphony:players", player.toString())
        ?.into<TrackedPlayer>()
}
