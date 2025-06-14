package lol.arch.symphony.velocity.player

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.connection.PostLoginEvent
import com.velocitypowered.api.event.player.ServerPostConnectEvent
import gg.scala.aware.message.AwareMessage
import gg.scala.aware.thread.AwareThreadContext
import gg.scala.commons.ScalaCommons
import lol.arch.symphony.api.model.TrackedPlayer
import lol.arch.symphony.velocity.VelocitySymphonyPlugin
import lol.arch.symphony.velocity.acquirePlayerLock
import lol.arch.symphony.velocity.into
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

    @Subscribe
    fun LoginEvent.on()
    {
        runCatching {
            player.uniqueId
                .acquirePlayerLock {
                    val existing = find(player.uniqueId)
                        ?: return@acquirePlayerLock

                    if (
                        existing.lastAttemptedReconcile != null &&
                        System.currentTimeMillis() - existing.lastAttemptedReconcile!! > Duration
                            .ofSeconds(1L)
                            .toMillis()
                    )
                    {
                        update(player.uniqueId) {
                            lastAttemptedReconcile = System.currentTimeMillis()
                        }

                        plugin.playerReconciler.reconcile(
                            lol.arch.symphony.velocity.player.requests.PlayerReconcileRequest(
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

    @Subscribe(order = PostOrder.FIRST)
    fun PostLoginEvent.on()
    {
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

                    AwareMessage
                        .of(
                            "login",
                            ScalaCommons.bundle().globals().aware(),
                            "player" to player.uniqueId
                        )
                        .publish(
                            AwareThreadContext.ASYNC,
                            "symphony:networkEvents"
                        )
                }
            }
            .join()
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

        if (previousServer != null)
        {
            AwareMessage
                .of(
                    "switch",
                    ScalaCommons.bundle().globals().aware(),
                    "player" to player.uniqueId,
                    "from" to previousServer?.serverInfo?.name,
                    "to" to instanceID
                )
                .publish(
                    AwareThreadContext.ASYNC,
                    "symphony:networkEvents"
                )
        }
    }

    @Subscribe(order = PostOrder.LAST)
    fun DisconnectEvent.on()
    {
        if (
            loginStatus == DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN ||
            loginStatus == DisconnectEvent.LoginStatus.PRE_SERVER_JOIN
        )
        {
            player.uniqueId.acquirePlayerLock {
                delete(player.uniqueId)
            }

            AwareMessage
                .of(
                    "logout",
                    ScalaCommons.bundle().globals().aware(),
                    "player" to player.uniqueId
                )
                .publish(
                    AwareThreadContext.ASYNC,
                    "symphony:networkEvents"
                )
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
