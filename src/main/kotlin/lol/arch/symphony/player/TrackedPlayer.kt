package lol.arch.symphony.player

import java.util.UUID

/**
 * @author GrowlyX
 * @since 2/15/2025
 */
data class TrackedPlayer(
    val uniqueId: UUID,
    val instance: String,
    var server: String? = null,
    val loginTime: Long = System.currentTimeMillis(),
    var lastServer: String? = null,
    var lastServerSwitchTime: Long? = null,
    var lastAttemptedReconcile: Long? = null,
    var lastHeartbeat: Long = System.currentTimeMillis()
)
