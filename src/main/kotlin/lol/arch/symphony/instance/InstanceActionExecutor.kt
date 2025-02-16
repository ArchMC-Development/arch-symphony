package lol.arch.symphony.instance

import gg.scala.aware.AwareBuilder
import gg.scala.aware.codec.codecs.interpretation.AwareMessageCodec
import gg.scala.aware.message.AwareMessage
import gg.scala.aware.thread.AwareThreadContext
import lol.arch.symphony.VelocitySymphonyPlugin
import lol.arch.symphony.instance.actor.SymphonyCommandSource
import lol.arch.symphony.instance.requests.RunCommandRequest
import java.util.logging.Logger

/**
 * @author GrowlyX
 * @since 2/15/2025
 */
class InstanceActionExecutor
{
    private lateinit var plugin: VelocitySymphonyPlugin

    private val aware by lazy {
        AwareBuilder
            .of<AwareMessage>("symphony:commands")
            .codec(AwareMessageCodec)
            .logger(Logger.getGlobal())
            .build()
    }

    fun runCommand(request: RunCommandRequest) = AwareMessage
        .of(
            "runCommand",
            aware,
            "request" to request
        )
        .publish(
            AwareThreadContext.SYNC
        )

    fun startActionTracking(plugin: VelocitySymphonyPlugin)
    {
        this.plugin = plugin

        aware.listen("runCommand") {
            val request = retrieve<RunCommandRequest>("request")
            if (!(request.instance == "all" || request.instance == plugin.config.id))
            {
                return@listen
            }

            plugin.server.commandManager.executeAsync(
                SymphonyCommandSource,
                request.command
            )
        }
        aware.connect().toCompletableFuture().join()
    }
}
