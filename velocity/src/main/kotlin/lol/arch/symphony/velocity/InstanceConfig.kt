package lol.arch.symphony.velocity

import kotlinx.serialization.Serializable

/**
 * @author GrowlyX
 * @since 2/15/2025
 */
@Suppress("PROVIDED_RUNTIME_TOO_LOW") // transitive
@Serializable
data class InstanceConfig(
    var id: String = "proxy-1",
)
