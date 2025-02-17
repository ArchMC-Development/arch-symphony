package lol.arch.symphony.api

import gg.scala.aware.AwareBuilder
import gg.scala.aware.codec.codecs.interpretation.AwareMessageCodec
import gg.scala.aware.message.AwareMessage
import java.util.logging.Logger

/**
 * @author GrowlyX
 * @since 2/16/2025
 */
object Symphony
{
    fun createEventSubscriber() = SymphonyClientBuilder(
        AwareBuilder
            .of<AwareMessage>("symphony:networkEvents")
            .logger(Logger.getGlobal())
            .codec(AwareMessageCodec)
            .build()
    )

    init
    {
        createEventSubscriber()
            .onLogin {
                println("player $it logged in")
            }

    }
}
