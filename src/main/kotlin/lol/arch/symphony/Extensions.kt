package lol.arch.symphony

import gg.scala.commons.consensus.Locks
import net.evilblock.cubed.serializers.Serializers
import java.util.*

/**
 * @author GrowlyX
 * @since 2/15/2025
 */
inline fun <reified T> String.into() = Serializers.gson.fromJson(this, T::class.java)

fun <T> UUID.acquirePlayerLock(use: () -> T) = Locks.withGlobalLock(
    "symphony-trackedPlayer", this.toString(), use
)
