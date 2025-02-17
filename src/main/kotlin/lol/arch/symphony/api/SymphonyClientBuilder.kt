package lol.arch.symphony.api

import gg.scala.aware.Aware
import gg.scala.aware.message.AwareMessage
import java.util.UUID

/**
 * @author GrowlyX
 * @since 2/16/2025
 */
class SymphonyClientBuilder(private val aware: Aware<AwareMessage>)
{
    fun onLogin(lambda: (UUID) -> Unit) = apply {
        aware.listen("login") {
            val uniqueId = retrieve<UUID>("player")
            lambda(uniqueId)
        }
    }

    fun onLogout(lambda: (UUID) -> Unit) = apply {
        aware.listen("logout") {
            val uniqueId = retrieve<UUID>("player")
            lambda(uniqueId)
        }
    }

    fun onSwitch(lambda: (UUID, String, String) -> Unit) = apply {
        aware.listen("switch") {
            val uniqueId = retrieve<UUID>("player")
            val from = retrieve<String>("from")
            val to = retrieve<String>("to")
            lambda(uniqueId, from, to)
        }
    }

    fun subscribe() = aware.connect().toCompletableFuture()
}
